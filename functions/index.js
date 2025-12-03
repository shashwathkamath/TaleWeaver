const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');

admin.initializeApp();

const SHIPROCKET_BASE_URL = 'https://apiv2.shiprocket.in/v1/external';
const TOKEN_VALIDITY_HOURS = 240; // 10 days

// ============================================
// Helper: Get or Refresh Shiprocket Auth Token
// ============================================
async function getShiprocketToken() {
  const db = admin.firestore();
  const tokenRef = db.collection('config').doc('shiprocket');

  try {
    const tokenDoc = await tokenRef.get();
    const now = Date.now();

    // Check if token exists and is still valid
    if (tokenDoc.exists) {
      const data = tokenDoc.data();
      const expiresAt = data.expiresAt;

      // If token expires in more than 1 hour, use it
      if (expiresAt && (expiresAt - now) > (60 * 60 * 1000)) {
        return data.token;
      }
    }

    // Token doesn't exist or is expiring soon, get new one
    console.log('Fetching new Shiprocket token...');

    // Get credentials from Firebase config
    const config = functions.config();
    const email = config.shiprocket?.email;
    const password = config.shiprocket?.password;

    if (!email || !password) {
      throw new Error('Shiprocket credentials not configured. Run: firebase functions:config:set shiprocket.email="your-email" shiprocket.password="your-password"');
    }

    const response = await axios.post(`${SHIPROCKET_BASE_URL}/auth/login`, {
      email: email,
      password: password
    });

    const token = response.data.token;
    const expiresAt = now + (TOKEN_VALIDITY_HOURS * 60 * 60 * 1000);

    // Save token to Firestore
    await tokenRef.set({
      token: token,
      expiresAt: expiresAt,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log('New Shiprocket token obtained and saved');
    return token;

  } catch (error) {
    console.error('Error getting Shiprocket token:', error.response?.data || error.message);
    throw new Error('Failed to authenticate with Shiprocket');
  }
}

// ============================================
// Cloud Function: Create Shiprocket Order
// ============================================
exports.createShiprocketOrder = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { orderId } = data;

  if (!orderId) {
    throw new functions.https.HttpsError('invalid-argument', 'Order ID is required');
  }

  try {
    const db = admin.firestore();

    // Get order details from Firestore
    const orderDoc = await db.collection('orders').doc(orderId).get();

    if (!orderDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Order not found');
    }

    const order = orderDoc.data();

    // Verify order is paid and ready for shipping
    if (order.status !== 'PAID') {
      throw new functions.https.HttpsError('failed-precondition', 'Order must be paid before creating shipment');
    }

    // Get buyer and seller profiles
    const buyerDoc = await db.collection('users').doc(order.buyerId).get();
    const sellerDoc = await db.collection('users').doc(order.sellerId).get();

    if (!buyerDoc.exists || !sellerDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Buyer or seller profile not found');
    }

    const buyer = buyerDoc.data();
    const seller = sellerDoc.data();

    // Validate addresses
    if (!buyer.shippingAddress || !seller.shippingAddress) {
      throw new functions.https.HttpsError('failed-precondition', 'Buyer and seller addresses are required');
    }

    const buyerAddress = buyer.shippingAddress;
    const sellerAddress = seller.shippingAddress;

    // Get Shiprocket token
    const token = await getShiprocketToken();

    // Prepare Shiprocket order payload
    const currentDate = new Date().toISOString().slice(0, 16).replace('T', ' ');

    const shiprocketPayload = {
      order_id: orderId,
      order_date: currentDate,
      pickup_location: seller.username || 'Seller Location', // Seller needs to configure this in Shiprocket
      billing_customer_name: buyer.name || buyer.username,
      billing_last_name: '', // Can split name if needed
      billing_address: buyerAddress.addressLine1,
      billing_address_2: buyerAddress.addressLine2 || '',
      billing_city: buyerAddress.city,
      billing_pincode: buyerAddress.pincode,
      billing_state: buyerAddress.state,
      billing_country: buyerAddress.country,
      billing_email: buyer.email || context.auth.token.email,
      billing_phone: buyerAddress.phone,
      shipping_is_billing: true,
      order_items: [{
        name: order.bookTitle,
        sku: order.listingId,
        units: 1,
        selling_price: order.bookPrice,
        discount: 0,
        tax: 0,
        hsn: 49019900 // HSN code for printed books in India
      }],
      payment_method: 'Prepaid',
      sub_total: order.bookPrice,
      length: 25, // Default book dimensions in cm
      breadth: 18,
      height: 3,
      weight: 0.5 // Default weight in kg
    };

    // Create order in Shiprocket
    const response = await axios.post(
      `${SHIPROCKET_BASE_URL}/orders/create/adhoc`,
      shiprocketPayload,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    const shiprocketData = response.data;

    // Update order in Firestore with Shiprocket details
    await orderDoc.ref.update({
      shiprocketOrderId: shiprocketData.order_id,
      shiprocketShipmentId: shiprocketData.shipment_id,
      status: 'LABEL_CREATED',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log(`Shiprocket order created: ${orderId}`);

    return {
      success: true,
      orderId: shiprocketData.order_id,
      shipmentId: shiprocketData.shipment_id,
      message: 'Order created successfully in Shiprocket'
    };

  } catch (error) {
    console.error('Error creating Shiprocket order:', error.response?.data || error.message);
    throw new functions.https.HttpsError('internal', error.message || 'Failed to create Shiprocket order');
  }
});

// ============================================
// Cloud Function: Generate AWB and Shipping Label
// ============================================
exports.generateShippingLabel = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { orderId, courierId } = data;

  if (!orderId) {
    throw new functions.https.HttpsError('invalid-argument', 'Order ID is required');
  }

  try {
    const db = admin.firestore();

    // Get order details
    const orderDoc = await db.collection('orders').doc(orderId).get();

    if (!orderDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Order not found');
    }

    const order = orderDoc.data();

    // Verify user is the seller
    if (order.sellerId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Only seller can generate shipping label');
    }

    if (!order.shiprocketShipmentId) {
      throw new functions.https.HttpsError('failed-precondition', 'Shiprocket order must be created first');
    }

    // Get Shiprocket token
    const token = await getShiprocketToken();

    // If courier ID not provided, get recommended courier
    let selectedCourierId = courierId;

    if (!selectedCourierId) {
      // Get courier serviceability
      const courierResponse = await axios.get(
        `${SHIPROCKET_BASE_URL}/courier/serviceability`,
        {
          params: {
            pickup_postcode: order.sellerAddress?.pincode,
            delivery_postcode: order.buyerAddress?.pincode,
            weight: 0.5,
            cod: 0
          },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      const couriers = courierResponse.data.data?.available_courier_companies || [];

      if (couriers.length === 0) {
        throw new functions.https.HttpsError('failed-precondition', 'No courier service available for this route');
      }

      // Select cheapest or fastest courier
      const bestCourier = couriers.sort((a, b) => a.rate - b.rate)[0];
      selectedCourierId = bestCourier.courier_company_id;
    }

    // Generate AWB
    const awbResponse = await axios.post(
      `${SHIPROCKET_BASE_URL}/courier/assign/awb`,
      {
        shipment_id: order.shiprocketShipmentId,
        courier_id: selectedCourierId
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    const awbData = awbResponse.data.response?.data;

    if (!awbData?.awb_code) {
      throw new Error('Failed to generate AWB code');
    }

    // Generate manifest/label
    await axios.post(
      `${SHIPROCKET_BASE_URL}/manifests/generate`,
      {
        shipment_id: [order.shiprocketShipmentId]
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    // Get label URL
    const labelResponse = await axios.post(
      `${SHIPROCKET_BASE_URL}/courier/generate/label`,
      {
        shipment_id: [order.shiprocketShipmentId]
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );

    const labelUrl = labelResponse.data.label_url;

    // Update order with AWB and label details
    await orderDoc.ref.update({
      trackingNumber: awbData.awb_code,
      courierName: awbData.courier_name,
      shippingLabelUrl: labelUrl,
      status: 'LABEL_CREATED',
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    console.log(`Shipping label generated for order: ${orderId}`);

    return {
      success: true,
      awbCode: awbData.awb_code,
      courierName: awbData.courier_name,
      labelUrl: labelUrl,
      message: 'Shipping label generated successfully'
    };

  } catch (error) {
    console.error('Error generating shipping label:', error.response?.data || error.message);
    throw new functions.https.HttpsError('internal', error.message || 'Failed to generate shipping label');
  }
});

// ============================================
// Cloud Function: Track Shipment
// ============================================
exports.trackShipment = functions.https.onCall(async (data, context) => {
  // Verify authentication
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }

  const { orderId } = data;

  if (!orderId) {
    throw new functions.https.HttpsError('invalid-argument', 'Order ID is required');
  }

  try {
    const db = admin.firestore();

    // Get order details
    const orderDoc = await db.collection('orders').doc(orderId).get();

    if (!orderDoc.exists) {
      throw new functions.https.HttpsError('not-found', 'Order not found');
    }

    const order = orderDoc.data();

    // Verify user is buyer or seller
    if (order.buyerId !== context.auth.uid && order.sellerId !== context.auth.uid) {
      throw new functions.https.HttpsError('permission-denied', 'Access denied');
    }

    if (!order.shiprocketShipmentId) {
      throw new functions.https.HttpsError('failed-precondition', 'Shipment not created yet');
    }

    // Get Shiprocket token
    const token = await getShiprocketToken();

    // Track shipment
    const response = await axios.get(
      `${SHIPROCKET_BASE_URL}/courier/track/shipment/${order.shiprocketShipmentId}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    const trackingData = response.data.tracking_data;

    return {
      success: true,
      trackingData: trackingData,
      awbCode: order.trackingNumber,
      courierName: order.courierName
    };

  } catch (error) {
    console.error('Error tracking shipment:', error.response?.data || error.message);
    throw new functions.https.HttpsError('internal', error.message || 'Failed to track shipment');
  }
});

// ============================================
// Scheduled Function: Update Shipment Status
// ============================================
exports.updateShipmentStatuses = functions.pubsub.schedule('every 2 hours').onRun(async (context) => {
  console.log('Running scheduled shipment status update...');

  try {
    const db = admin.firestore();

    // Get all orders that are shipped but not delivered
    const ordersSnapshot = await db.collection('orders')
      .where('status', 'in', ['LABEL_CREATED', 'SHIPPED'])
      .where('shiprocketShipmentId', '!=', null)
      .get();

    if (ordersSnapshot.empty) {
      console.log('No active shipments to update');
      return null;
    }

    const token = await getShiprocketToken();

    const updatePromises = ordersSnapshot.docs.map(async (doc) => {
      const order = doc.data();

      try {
        const response = await axios.get(
          `${SHIPROCKET_BASE_URL}/courier/track/shipment/${order.shiprocketShipmentId}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`
            }
          }
        );

        const trackingData = response.data.tracking_data;
        const currentStatus = trackingData?.shipment_status_id;

        // Update order status based on Shiprocket status
        let newStatus = order.status;

        if (currentStatus === 6) { // Delivered
          newStatus = 'DELIVERED';
        } else if (currentStatus === 7 || currentStatus === 8) { // Cancelled/RTO
          newStatus = 'CANCELLED';
        } else if (currentStatus >= 4) { // In transit
          newStatus = 'SHIPPED';
        }

        if (newStatus !== order.status) {
          await doc.ref.update({
            status: newStatus,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
          console.log(`Updated order ${doc.id} status to ${newStatus}`);
        }

      } catch (error) {
        console.error(`Error updating order ${doc.id}:`, error.message);
      }
    });

    await Promise.all(updatePromises);
    console.log('Shipment status update completed');

    return null;

  } catch (error) {
    console.error('Error in scheduled update:', error);
    return null;
  }
});

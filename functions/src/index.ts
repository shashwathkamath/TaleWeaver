import * as admin from "firebase-admin";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import * as nodemailer from "nodemailer";
import { defineString } from "firebase-functions/params";

admin.initializeApp();

const mailUser = defineString("MAIL_USER");
const mailPass = defineString("MAIL_PASS");

function getTransporter() {
  return nodemailer.createTransport({
    service: "gmail",
    auth: {
      user: mailUser.value(),
      pass: mailPass.value(),
    },
  });
}

function generateOtp(): string {
  return String(Math.floor(100000 + Math.random() * 900000));
}

function isValidEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// Sends a 6-digit OTP to the given email. Stores the code in Firestore
// under otpCodes/{email} with a 5-minute expiry.
export const sendOtp = onCall(async (request) => {
  const email: string = request.data?.email ?? "";
  if (!isValidEmail(email)) {
    throw new HttpsError("invalid-argument", "A valid email is required.");
  }

  const otp = generateOtp();
  const expiresAt = admin.firestore.Timestamp.fromMillis(Date.now() + 5 * 60 * 1000);

  await admin.firestore().collection("otpCodes").doc(email).set({
    code: otp,
    expiresAt,
    attempts: 0,
    createdAt: admin.firestore.FieldValue.serverTimestamp(),
  });

  const mailOptions = {
    from: `"TaleWeaver" <${mailUser.value()}>`,
    to: email,
    subject: "Your TaleWeaver sign-in code",
    text: `Your TaleWeaver sign-in code is: ${otp}\n\nThis code expires in 5 minutes.\n\nIf you did not request this, you can safely ignore this email.`,
    html: `
      <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px 24px; background: #fff;">
        <h2 style="color: #6750A4; margin-bottom: 8px;">Your sign-in code</h2>
        <p style="color: #444; font-size: 16px; margin-bottom: 32px;">Use the code below to sign in to TaleWeaver. It expires in 5 minutes.</p>
        <div style="background: #F3EFF5; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 32px;">
          <span style="font-size: 48px; font-weight: bold; letter-spacing: 12px; color: #6750A4;">${otp}</span>
        </div>
        <p style="color: #888; font-size: 13px;">If you did not request this code, you can safely ignore this email.</p>
      </div>
    `,
  };

  await getTransporter().sendMail(mailOptions);
  return { success: true };
});

// Verifies the OTP for the given email. On success, creates a Firebase Auth
// user if one doesn't exist yet and returns a custom sign-in token.
export const verifyOtp = onCall(async (request) => {
  const email: string = request.data?.email ?? "";
  const code: string = request.data?.code ?? "";

  if (!isValidEmail(email) || code.length !== 6) {
    throw new HttpsError("invalid-argument", "Email and 6-digit code are required.");
  }

  const otpRef = admin.firestore().collection("otpCodes").doc(email);
  const otpDoc = await otpRef.get();

  if (!otpDoc.exists) {
    throw new HttpsError("not-found", "No code was sent to this email. Please request a new one.");
  }

  const { code: storedCode, expiresAt, attempts } = otpDoc.data() as {
    code: string;
    expiresAt: admin.firestore.Timestamp;
    attempts: number;
  };

  if (attempts >= 3) {
    await otpRef.delete();
    throw new HttpsError("resource-exhausted", "Too many incorrect attempts. Please request a new code.");
  }

  if (expiresAt.toMillis() < Date.now()) {
    await otpRef.delete();
    throw new HttpsError("deadline-exceeded", "Code expired. Please request a new one.");
  }

  if (storedCode !== code) {
    await otpRef.update({ attempts: admin.firestore.FieldValue.increment(1) });
    const remaining = 3 - (attempts + 1);
    throw new HttpsError("invalid-argument", `Incorrect code. ${remaining} attempt${remaining === 1 ? "" : "s"} remaining.`);
  }

  // Code is valid — clean it up
  await otpRef.delete();

  // Get or create the Firebase Auth user
  let uid: string;
  try {
    const existing = await admin.auth().getUserByEmail(email);
    uid = existing.uid;
  } catch {
    const created = await admin.auth().createUser({ email });
    uid = created.uid;
  }

  const customToken = await admin.auth().createCustomToken(uid);
  return { token: customToken };
});

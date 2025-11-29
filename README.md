TaleWeaver is an Android application designed as a peer-to-peer marketplace for buying and selling used books. Built with a modern, reactive architecture using Jetpack Compose and Firebase, the app allows users to list books for sale, browse a feed of available listings, and engage in a community-driven marketplace.

## Tech Stack
- Jetpack Compose
- Firebase
- Firestore (DB)
- Google Places API (Address Autocomplete)

## Setup Instructions

### Google Maps API Key
The app uses Google Places API for address autocomplete functionality. To set it up:

1. Get a Google Maps API key from [Google Cloud Console](https://console.cloud.google.com/)
   - Enable the **Places API** for your project

2. Add the API key to your `local.properties` file:
   ```properties
   MAPS_API_KEY=YOUR_API_KEY_HERE
   ```

3. The app will automatically read this key during build time

**Note:** Never commit your API key to version control. The `local.properties` file is gitignored by default.

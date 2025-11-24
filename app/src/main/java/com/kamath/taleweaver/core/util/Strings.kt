package com.kamath.taleweaver.core.util

import android.annotation.SuppressLint

/**
 * Centralized UI strings for TaleWeaver app.
 * All user-facing text should be defined here for easy maintenance and future localization.
 */
object Strings {

    // ============== Screen Titles ==============
    object Titles {
        const val FEED = "Discover"
        const val SEARCH = "Explore"
        const val SELL = "Pass it on"
        const val ACCOUNT = "Reader's corner"
        const val LOGIN = "Login"
        const val SIGN_UP = "Sign Up"
        const val LISTING_DETAILS = "Listing Details"
    }

    // ============== Button Labels ==============
    object Buttons {
        const val LOGIN = "Login"
        const val SIGN_UP = "Sign Up"
        const val REGISTER = "Register"
        const val SAVE = "Save"
        const val LOGOUT = "Logout"
        const val CREATE_LISTING = "Create Listing"
        const val CONTACT_SELLER = "CONTACT SELLER"
        const val SCAN_ISBN = "Scan ISBN Barcode"
        const val FETCH_BOOK_DETAILS = "Fetch Book Details"
        const val ADD = "Add"
        const val RETRY = "Retry"
        const val GRANT_PERMISSION = "Grant Permission"
        const val VIEW_ALL = "View All"
        const val EDIT = "Edit"
        const val DELETE = "Delete"
        const val CANCEL = "Cancel"
        const val VIEW_DETAILS = "View Details"
    }

    // ============== Labels & Headings ==============
    object Labels {
        // Section Titles
        const val BOOK_DETAILS = "Book Details"
        const val LISTING_DETAILS = "Listing Details"
        const val PROFILE_INFORMATION = "Profile Information"
        const val MY_LISTINGS = "My Listings"
        const val PHOTOS = "Photos *"
        const val DESCRIPTION = "Description"
        const val GENRES = "Genres"

        // Form Labels
        const val EMAIL = "Enter email"
        const val PASSWORD = "Enter password"
        const val USERNAME = "Enter username"
        const val TITLE_REQUIRED = "Title *"
        const val AUTHOR_REQUIRED = "Author *"
        const val PRICE_REQUIRED = "Price *"
        const val CONDITION_REQUIRED = "Condition *"
        const val DISPLAY_NAME = "Display Name"
        const val BIO = "Bio"
        const val ADDRESS = "Address"
        const val ENTER_ISBN = "Enter ISBN"
        const val OFFER_SHIPPING = "Offer Shipping"

        // Other Labels
        const val STEP_ISBN = "Step 1: Scan or Enter ISBN"
        const val OR = "OR"
        const val CONDITION_PREFIX = "Condition: "
        const val SOLD_BY_PREFIX = "Sold by: "
        const val BY_PREFIX = "by "
        const val RATING = "Rating"
        const val YOU = "You"
        const val API_BADGE = "API"
        const val UNKNOWN_SELLER = "Unknown Seller"
        const val NO_TITLE = "No Title"
        const val RATING_LABEL = "Rating"
        const val MORE = "more"
    }

    // ============== Placeholders ==============
    object Placeholders {
        const val ISBN_EXAMPLE = "e.g., 9780141036144"
        const val PRICE = "0.00"
        const val BIO = "Tell us something about yourself..."
        const val ADDRESS = "Enter your city or area..."
        const val SEARCH_NEARBY = "Explore books nearby..."
    }

    // ============== Content Descriptions (Accessibility) ==============
    object ContentDescriptions {
        const val BACK = "Back"
        const val SEARCH = "Search"
        const val CLEAR = "Clear"
        const val LOGOUT = "Logout"
        const val ADD_PHOTO = "Add photo"
        const val REMOVE = "Remove"
        const val PROFILE_PICTURE = "Profile Picture"
        const val NAME_ICON = "Name"
        const val BIO_ICON = "Bio"
        const val ADDRESS_ICON = "Address"
        const val RATING = "Rating"
        const val RATING_ICON = "Rating"
        const val COVER_FROM_API = "Cover from API"
        const val SELECTED_IMAGE = "Selected image"
        fun coverImage(title: String) = "Cover of $title"
        fun coverFor(title: String) = "Cover for $title"
    }

    // ============== Success Messages ==============
    object Success {
        const val LOGIN = "Login Successful"
        const val SIGN_UP = "Sign up successful"
        const val LISTING_CREATED = "Listing created successfully!"
        const val BOOK_DETAILS_LOADED = "Book details loaded!"
        const val LISTING_DELETED = "Listing deleted"
        const val PROFILE_SAVED = "Profile saved"
    }

    // ============== Error Messages ==============
    object Errors {
        const val UNKNOWN = "An unknown error occurred"
        const val FAILED = "Failed"
        const val PROFILE_LOAD_FAILED = "Could not load profile."
        const val LOGOUT_FAILED = "Logout failed"
        const val SIGN_UP_FAILED = "Sign up failed"

        // Validation Errors
        const val EMAIL_PASSWORD_EMPTY = "Email and password cannot be empty"
        const val PASSWORD_TOO_SHORT = "Password must be at least 6 characters long"
        const val ISBN_REQUIRED = "Enter ISBN first"
        const val LOCATION_FETCH_FAILED = "Could not retrieve device location"
        const val MIGRATION_FAILED = "Migration failed"
        const val TITLE_REQUIRED = "Title is required"
        const val AUTHOR_REQUIRED = "Author is required"
        const val PRICE_REQUIRED = "Valid price is required"
        const val CONDITION_REQUIRED = "Select condition"
        const val IMAGES_REQUIRED = "Add at least one image"
        const val MINIMUM_IMAGES_REQUIRED = "Please add at least 3 photos to show the book's condition"
        const val DELETE_FAILED = "Failed to delete listing"
    }

    // ============== Empty States ==============
    object EmptyStates {
        const val NO_LISTINGS = "No listings found nearby.\nCheck back later for new books!"
        const val NO_NEARBY_LISTINGS = "No listings found nearby."
        const val NO_USER_LISTINGS = "You haven't posted any books yet"
    }

    // ============== Permission Messages ==============
    object Permissions {
        const val LOCATION_REQUIRED = "Location Required"
        const val LOCATION_HELP = "Please set your location in the Account screen before creating a listing."
        const val LOCATION_RATIONALE = "We need your location to find nearby book sellers."
        const val CAMERA_REQUIRED = "Camera permission required"
        const val CAMERA_RATIONALE = "Please grant camera access to scan ISBN barcodes"
        const val SCAN_INSTRUCTIONS = "Point camera at the barcode on the back of the book"
    }

    // ============== Format Strings ==============
    object Formats {
        @SuppressLint("DefaultLocale")
        fun price(amount: Double) = String.format("$%.2f", amount)
        @SuppressLint("DefaultLocale")
        fun milesAway(miles: Double) = String.format("%.1f miles away", miles)
        fun sellerUsername(username: String) = "@$username"
        fun byAuthor(author: String) = "by $author"
        fun errorMessage(error: String) = "Error: $error"
    }

    // ============== Dialogs ==============
    object Dialogs {
        const val DELETE_LISTING_TITLE = "Delete Listing"
        fun deleteListingMessage(title: String) = "Are you sure you want to delete \"$title\"? This action cannot be undone."
    }

    // ============== Photo Capture ==============
    object PhotoCapture {
        const val FRONT_LABEL = "Front"
        const val BACK_LABEL = "Back"
        const val SIDE_LABEL = "Side"
        const val INSTRUCTIONS = "Take 3 photos to show the book's condition"
        const val TAP_TO_START = "Tap to Take Photos"
        const val WILL_CAPTURE_THREE = "You'll capture front cover, back cover, and side/spine"
        const val RETAKE_PHOTOS = "Retake Photos"
        const val API_COVER_NOTE = "Cover from book database (shown in listing)"
        fun promptFor(step: String) = "Take photo of $step"
    }
}

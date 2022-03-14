package com.aukdeclient.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

/**
 * A custom object to declare all the constant values in a single file. The constant values declared here is can be used in whole application.
 */
object Constants {

    // Firebase Constants
    // This is used for the collection name for USERS.
    const val LOCATION_REQUEST_CODE = 1
    const val SETTINGS_REQUEST_CODE = 2
    const val MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey"
    const val USERS: String = "users"
    const val USER_DATA : String = "DATA"
    const val USER_CARD_DATA : String = "CARD_DATA"
    const val KEY : String = "key"
    const val KEY_CARD : String = "key_card"
    const val CLIENT: String = "client"
    const val TOKEN: String = "token"
    const val CART: String = "cart"
    const val STORE: String = "store"
    const val PACKAGE: String = "package"
    const val SUSPEND : String = "Suspend"
    const val TRIAL : String = "Trial"
    const val BASIC : String = "Básico"
    const val PREMIUM : String = "Premium"
    const val DATE_ACTIVE_PACKAGE: String = "date_active_package"
    const val NAME_STORE: String = "name_store"
    const val HAS_DELIVERY: String = "has_delivery"
    const val PRODUCTS: String = "products"
    const val CART_ITEMS: String = "cart_items"
    const val ADDRESSES: String = "addresses"
    const val ORDERS: String = "orders"
    const val SOLD_PRODUCTS: String = "sold_products"
    const val DELIVER_ORDER: String = "Entregar pedido"

    const val MYSHOPPAL_PREFERENCES: String = "MyShopPalPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"
    const val USER_DATA_OBJECT = "user_data_object"
    const val KEY_USER_DATA_OBJECT = "key_user_data_object"

    // Intent extra constants.
    const val EXTRA_USER_DETAILS: String = "extra_user_details"
    const val EXTRA_DRIVER_ID: String = "extra_driver_id"
    const val EXTRA_USER_TYPE_PRODUCT : String = "extra_user_type_product"
    const val EXTRA_USER_PHOTO : String = "extra_user_photo"
    const val EXTRA_PRODUCT_ID: String = "extra_product_id"
    const val EXTRA_PRODUCT_OWNER_ID: String = "extra_product_owner_id"
    const val EXTRA_ADDRESS_DETAILS: String = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS: String = "extra_select_address"
    const val EXTRA_SELECTED_ADDRESS: String = "extra_selected_address"
    const val EXTRA_MY_ORDER_DETAILS: String = "extra_my_order_details"
    const val EXTRA_SOLD_PRODUCT_DETAILS: String = "extra_sold_product_details"
    const val FAVORITE_ITEMS: String= "favorite_items"

    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2

    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val ADD_ADDRESS_REQUEST_CODE: Int = 121
    const val GOOGLE_SIGN_IN = 100

    // Constant variables for Gender
    const val MALE: String = "Masculino"
    const val FEMALE: String = "Femenino"

    const val DEFAULT_CART_QUANTITY: String = "1"

    // Firebase database field names
    const val MOBILE: String = "mobile"
    const val GENDER: String = "gender"
    const val IMAGE: String = "image"
    const val COMPLETE_PROFILE: String = "profileCompleted"
    const val COMPLETED: String = "Completado"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val USER_ID: String = "user_id"
    const val PROVIDER_ID: String = "provider_id"
    const val PRODUCT_ID: String = "product_id"
    const val PRICE: String = "price"
    const val DNI: String = "dni"
    const val CODE_CLIENT: String = "code_client"

    const val USER_PROFILE_IMAGE: String = "User_Profile_Image"
    const val PRODUCT_IMAGE: String = "Product_Image"

    const val CART_QUANTITY: String = "cart_quantity"

    const val STOCK_QUANTITY: String = "stock_quantity"
    const val SKU : String = "sku"

    const val HOME: String = "Casa"
    const val OFFICE: String = "Oficina"
    const val OTHER: String = "Otro"

    const val LOGOUT : String = "Cerrando sesión"

    const val REQUEST_PROVIDER: String = "SolicitudSocio"

    const val PROCESSING : String = "Procesando"
    const val PENDING : String = "Pendiente"
    const val IN_ROUTE : String = "En ruta"
    const val FINISH_ORDER : String = "Entregado"
    const val CANCELLED : String = "Cancelado"

    const val TITTLE_NOTIFICATION : String = "SOLICITUD DE SERVICIO"
    const val BODY_NOTIFICATION : String = "Revise su lista de pedidos en su panel"
    const val SEARCH_DRIVER : String = "Buscando Aukdeliver cercano..."
    const val DRIVER_FOUND : String = "Aukdeliver encontrado!"
    const val NO_CLOSEST_DRIVER : String = "No hay aukdeliver cercano"
    const val TAKE_ORDER_DRIVER : String = "El Aukdeliver tomó tu pedido !"
    const val SUCCESS_ORDER : String = "SU PEDIDO SE REALIZÓ CORRECTAMENTE."
    const val FAILED_ORDER : String = "No aceptó el pedido, intentelo de nuevo !"
    const val PLEASE_WAIT : String = "espere un momento..."
    const val FINISHING_ORDER : String = "Finalizando Compra"
    const val PROCESSING_ORDER : String = "Procesar pedido"
    const val DELIVERED : String = "Recibido"
    const val USERS_REALTIME : String = "Usuarios"
    const val DRIVER_OUR : String = "Aukdeliver"
    const val SENDING_STORE : String = "Producto enviado por el mismo negocio!"
    const val SENDING : String = "Enviando"
    const val SEND_PRODDUCT : String = "Enviar producto"
    const val SENDING_PRODDUCT : String = "Enviando producto"
    const val COMPLETED_ORDER: String = "Completar pedido"
    const val FREE_SHIPPING = "Envío gratuito"
    const val INCLUDE_SHIPPING = "Incluye envío"
    const val NO_INCLUDE_SHIPPING = "No Incluye envío"
    const val FAVORITE_ITEMS: String= "favorite_items"
    const val COMPLETED_ALL_DATA : String = "Complete todos los datos!"

    const val PUBLIC_API_KEY = "pk_live_c84HQbHmA05kP1hr"
    const val PRIVATE_API_KEY = "sk_live_pRtJrGvZC01P7zEg"
    const val SUCCESS_TOKEN = "Token creado !"
    const val SUCCESS_CHARGE = "CARGO CREADO"
    const val ERROR = "Error"
    const val ERROR_CARD = "Error de tarjeta ó saldo insuficiente"
    const val ERROR_SERVER = "Error de servidor, intentelo más tarde..."
    const val ADD_AMOUNT = "Agregue el monto"
    const val MINOR_AMOUNT = "El monto debe ser mayor a 3 soles"
    const val TYPE_MONEY = "PEN"
    const val VALIDATING_INFO_CARD = "Validando informacion de la tarjeta"
    const val RUN_CHARGE = "Realizando cargo"
    const val CARD_NUMBER = "card_number"
    const val CVV = "cvv"
    const val EXPIRATION_MONTH = "expiration_month"
    const val EXPIRATION_YEAR = "expiration_year"
    const val EMAIL = "email"
    const val ID = "id"
    const val URL_BASE_SECURE = "https://secure.culqi.com/v2/"
    const val URL_BASE = "https://api.culqi.com/v2/"
    const val URL_TOKENS = "tokens/"
    const val URL_CHARGES = "charges/"
    const val HEADERS_CONTENT_TYPE = "Content-Type"
    const val CONTENT_TYPE = "application/json"
    const val HEADERS_AUTHORIZATION = "Authorization"
    const val AUTHORIZATION = "Bearer "
    const val INITIAL_TIMEOUTS_MS = 30000
    const val AMOUNT = "amount"
    const val CURRENCY_CODE = "currency_code"
    const val SOURCE_ID = "source_id"
    const val VISA = "Visa"
    const val MASTERCARD = "MasterCard"
    const val DINERS_CLUB = "Diners Club"
    const val AMEX = "Amex"
    const val PAY = "PAGADO!"
    const val CACHE_PRODUCT = "cacheProduct"
    const val CACHE_SLIDER = "preference_slider"
    const val CATEGORIES = "categories"
    const val ALERT = "alert"
    const val CACHE_ALERT = 0

    /**
     * A function for user profile image selection from phone storage.
     */
    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /**
     * A function to get the image file extension of the selected image.
     *
     * @param activity Activity reference.
     * @param uri Image file uri.
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}
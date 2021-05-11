package com.aukdeshop.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeshop.Maps.ClientBookingProvider
import com.aukdeshop.Maps.GeofireDriverProvider
import com.aukdeshop.Maps.GeofireStoreProvider
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.*
import com.aukdeshop.notifications.server.FCMBody
import com.aukdeshop.notifications.server.FCMResponse
import com.aukdeshop.notifications.server.NotificationProvider
import com.aukdeshop.ui.adapters.CartItemsListAdapter
import com.aukdeshop.utils.Constants
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_checkout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * A CheckOut activity screen.
 */

class CheckoutActivity : BaseActivity() {

    // A global variable for the selected address details.
    private var mAddressDetails: Address? = null

    // A global variable for the product list.
    private lateinit var mProductsList: ArrayList<Product>

    // A global variable for the cart list.
    private lateinit var mCartItemsList: ArrayList<Cart>

    // A global variable for the SubTotal Amount.
    private var mSubTotal: Double = 0.0

    // A global variable for the Total Amount.
    private var mTotalAmount: Double = 0.0

    // A global variable for Order details.
    private lateinit var mOrderDetails: Order

    private var typeMoney : String = ""

    private var mPhoto : String = ""
    lateinit var sharedPhoto : SharedPreferences

    private var mGeofireDriverDriver: GeofireDriverProvider = GeofireDriverProvider("active_drivers")
    private var mGeofireStore : GeofireStoreProvider = GeofireStoreProvider("cart",FirestoreClass().getCurrentUserID())

    private lateinit var mCurrentLatLng: LatLng
    private lateinit var mDriverFoundLatLng: LatLng
    private lateinit var mStoreFoundLatLng: LatLng
    private var mRadius = 0.1
    private var mRadiusDriver = 0.1
    private var mDriverFound = false
    private var mIdDriverFound = ""
    private var mStoreFound = false
    private var mIdStoreFound = ""
    private var mClientBookingProvider : ClientBookingProvider = ClientBookingProvider()
    private var mListener: ValueEventListener? = null
    private var mNotificationProvider = NotificationProvider()

    var photo_default = "https://firebasestorage.googleapis.com/v0/b" +
            "/gestor-de-pedidos-aukdefood.appspot.com/o" +
            "/fotoDefault.jpg?alt=media&token=f74486bf-432e-4af6-b114-baa523e1f801"

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_checkout)
        typeMoney = resources.getString(R.string.type_money)
        sharedPhoto = getSharedPreferences(Constants.EXTRA_USER_PHOTO, MODE_PRIVATE)
        mPhoto = sharedPhoto.getString(Constants.EXTRA_USER_PHOTO, "").toString()

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mAddressDetails =
                intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)!!
        }

        if (mAddressDetails != null) {
            tv_checkout_address_type.text = mAddressDetails?.type
            tv_checkout_full_name.text = mAddressDetails?.name
            tv_checkout_address.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            tv_checkout_additional_note.text = mAddressDetails?.additionalNote
            mCurrentLatLng = LatLng(mAddressDetails!!.latitude, mAddressDetails!!.longitude)


            if (mAddressDetails?.otherDetails!!.isNotEmpty()) {
                tv_checkout_other_details.text = mAddressDetails?.otherDetails
            }
            tv_checkout_mobile_number.text = mAddressDetails?.mobileNumber
        }

        btn_place_order.setOnClickListener {
            getClosestDriverAndStore()
        }

        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_checkout_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_checkout_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }

    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        mProductsList = productsList

        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CheckoutActivity)
    }

    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    @SuppressLint("SetTextI18n")
    fun successCartItemsList(cartList: ArrayList<Cart>) {

        // Hide progress dialog.
        hideProgressDialog()

        for (product in mProductsList) {
            for (cart in cartList) {
                if (product.product_id == cart.product_id) {
                    cart.stock_quantity = product.stock_quantity
                }
            }
        }

        mCartItemsList = cartList

        rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        rv_cart_list_items.setHasFixedSize(true)

        val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, mCartItemsList, false)
        rv_cart_list_items.adapter = cartListAdapter

        for (item in mCartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }

        tv_checkout_sub_total.text = typeMoney+"$mSubTotal"
        // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
        tv_checkout_shipping_charge.text = typeMoney+"10.0"

        if (mSubTotal > 0) {
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + 10.0
            tv_checkout_total_amount.text = typeMoney+"$mTotalAmount"
        } else {
            ll_checkout_place_order.visibility = View.GONE
        }

    }


    private fun getClosestDriverAndStore() {

        showProgressDialog(resources.getString(R.string.please_wait))

        mGeofireStore.getActiveStore(mCurrentLatLng, mRadius)
            .addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    if (!mStoreFound) {
                        mStoreFound = true
                        mIdStoreFound = key
                        mStoreFoundLatLng = LatLng(location.latitude, location.longitude)
                        //Toast.makeText(this@CheckoutActivity,"STORE FOUND!",Toast.LENGTH_LONG).show()
                        getClosestDriver()
                        //Log.d("store", "ID: $mIdStoreFound")
                    }
                }
                override fun onKeyExited(key: String) {
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                }

                override fun onGeoQueryReady() {
                    // INGRESA CUANDO TERMINA LA BUSQUEDA DEL LOCAL EN UN RADIO DE 0.1 KM
                    if (!mStoreFound) {
                        mRadius += 0.1f
                        // NO ENCONTRO NINGUN CONDUCTOR
                        if (mRadius > 5) {
                            hideProgressDialog()
                            finish()
                            Toast.makeText(this@CheckoutActivity, "el local esta muy lejos", Toast.LENGTH_SHORT).show()
                            return
                        } else {
                            getClosestDriverAndStore()
                        }
                    }

                }
                override fun onGeoQueryError(error: DatabaseError) {}
            })
    }

    private fun getClosestDriver(){
        mGeofireDriverDriver.getActiveDrivers(mStoreFoundLatLng,mRadiusDriver)
                .addGeoQueryEventListener(object : GeoQueryEventListener {
                    override fun onKeyEntered(keyDriver: String, locationDriver: GeoLocation) {
                        if (!mDriverFound) {
                            mDriverFound = true
                            mIdDriverFound = keyDriver
                            mDriverFoundLatLng = LatLng(locationDriver.latitude, locationDriver.longitude)
                            //Toast.makeText(this@CheckoutActivity,"DRIVER FOUND!",Toast.LENGTH_LONG).show()
                            sendNotificationDriver()
                            //Log.d("driver", "ID: $mIdDriverFound")
                        }
                    }
                    override fun onKeyExited(keyDriver: String) {
                    }
                    override fun onKeyMoved(keyDriver: String, locationDriver: GeoLocation) {
                    }
                    override fun onGeoQueryReady() {
                        // INGRESA CUANDO TERMINA LA BUSQUEDA DEL LOCAL EN UN RADIO DE 0.1 KM
                        if (!mDriverFound) {
                            mRadiusDriver += 0.1f
                            // NO ENCONTRO NINGUN CONDUCTOR
                            if (mRadiusDriver > 5) {
                                hideProgressDialog()
                                finish()
                                Toast.makeText(this@CheckoutActivity, "No hay conductor cercano", Toast.LENGTH_SHORT).show()
                                return
                            } else {
                                getClosestDriver()
                            }
                        }
                    }
                    override fun onGeoQueryError(error: DatabaseError) {
                    }
                })
    }

    private fun sendNotificationDriver() {

        FirestoreClass().getTokenDriver(mIdDriverFound).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val token = snapshot.child(Constants.TOKEN).value.toString()
                    val map: MutableMap<String, String> = HashMap()
                    map["title"] = Constants.TITTLE_NOTIFICATION
                    map["body"] = Constants.BODY_NOTIFICATION
                    map["path"] = photo_default
                    map["numPedido"] = "#12345" // para testear
                    map["nombre"] = FirestoreClass().getCurrentUserID()
                    val fcmBody = FCMBody(token, "high", map)
                    mNotificationProvider.sendNotification(fcmBody).enqueue(object : Callback<FCMResponse>{
                        override fun onResponse(call: Call<FCMResponse>, response: Response<FCMResponse>) {
                            if (response.body() != null) {
                                if (response.body()!!.success === 1) {
                                    val solicitud = ClientBooking(
                                            "",
                                            FirestoreClass().getCurrentUserID(),
                                            mIdDriverFound,
                                            "create"
                                    )
                                    mClientBookingProvider.create(solicitud).addOnSuccessListener {
                                        checkStatusClientBooking()
                                    }
                                }
                            }
                        }
                        override fun onFailure(call: Call<FCMResponse>, t: Throwable) {
                        }
                    })

                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun sendNotificationStore(){
        if (mPhoto == ""){
            mPhoto = photo_default
        }
        for (submit in mCartItemsList) {
            FirestoreClass().createNotificationOrder(submit.provider_id, photo_default)
        }
    }

    private fun checkStatusClientBooking() {
        mListener = mClientBookingProvider.getStatus(FirestoreClass().getCurrentUserID()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status: String = snapshot.value.toString()
                    if (status == "accept") {
                        placeAnOrder()
                        Toast.makeText(this@CheckoutActivity, "Aceptó el pedido", Toast.LENGTH_SHORT).show()
                    } else if (status == "cancel") {
                        hideProgressDialog()
                        Toast.makeText(this@CheckoutActivity, "NO aceptó el pedido", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * A function to prepare the Order details to place an order.
     */
    private fun placeAnOrder() {
        // Show the progress dialog.
        //showProgressDialog(resources.getString(R.string.please_wait))

        mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartItemsList,
                mAddressDetails!!,
                "#${System.currentTimeMillis()}",
                mCartItemsList[0].image,
                mSubTotal.toString(),
                "10.0", // The Shipping Charge is fixed as $10 for now in our case.
                mTotalAmount.toString(),
                System.currentTimeMillis()
        )
        sendNotificationStore()
        FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
    }

    /**
     * A function to notify the success result of the order placed.
     */
    fun orderPlacedSuccess() {
        FirestoreClass().updateAllDetails(this@CheckoutActivity, mCartItemsList, mOrderDetails)
    }

    /**
     * A function to notify the success result after updating all the required details.
     */
    fun allDetailsUpdatedSuccessfully() {
        // Hide the progress dialog.
        hideProgressDialog()
        Toast.makeText(
                this@CheckoutActivity,
                "Su pedido se realizó correctamente.",
                Toast.LENGTH_SHORT
        )
            .show()
        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
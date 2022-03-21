package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeclient.Culqi.Functions.Charge
import com.aukdeclient.Culqi.Functions.TokenCulqi
import com.aukdeclient.Culqi.Utils.Validation
import com.aukdeclient.Maps.ClientBookingProvider
import com.aukdeclient.Maps.GeofireDriverProvider
import com.aukdeclient.Maps.GeofireStoreProvider
import com.aukdeclient.Services.Visanet
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.*
import com.aukdeclient.notifications.server.FCMBody
import com.aukdeclient.notifications.server.FCMResponse
import com.aukdeclient.notifications.server.NotificationProvider
import com.aukdeclient.ui.adapters.CartItemsListAdapter
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.MSPEditText
import com.aukdeclient.utils.MSPTextView
import com.aukdeclient.utils.TinyDB
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.image.mainactivity.Culqui.Callbacks.ChargeCallback
import com.image.mainactivity.Culqui.Callbacks.TokenCallback
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.activity_checkout.*
import lib.visanet.com.pe.visanetlib.VisaNet
import lib.visanet.com.pe.visanetlib.data.custom.Channel
import lib.visanet.com.pe.visanetlib.presentation.custom.VisaNetViewAuthorizationCustom
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import com.aukdeshop.R
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
    private var mSubTotal: Double = 0.00

    // A global variable for the Total Amount.
    private var mTotalAmount: Double = 0.00
    var radioGroupPayment: RadioGroup? = null

    private var shipping : Double? = null

    // A global variable for Order details.
    private lateinit var mOrderDetails: Order

    private var typeMoney : String = ""

    private var mPhoto : String = ""
    lateinit var sharedPhoto : SharedPreferences

    private var mGeofireDriverDriver: GeofireDriverProvider = GeofireDriverProvider("active_drivers")
    private var mGeofireStore : GeofireStoreProvider = GeofireStoreProvider(
        "cart",
        FirestoreClass().getCurrentUserID()
    )

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
    private val mDriversNotAccept = ArrayList<String>()
    private var mTimeLimit = 0
    private val mHandler = Handler()
    private var mIsFinishSearch = false
    private var mIsLookingFor = false
    var ctx = 0
    var typePayment = ""
    var amountToPay = ""
    var dialogAmountToPay : Dialog? = null
    var dialogPayWithCard : Dialog? = null
    var totalPay : MSPTextView? = null
    var df = DecimalFormat("#.00")

    var photo_default = "https://firebasestorage.googleapis.com/v0/b" +
            "/gestor-de-pedidos-aukdefood.appspot.com/o" +
            "/fotoDefault.jpg?alt=media&token=f74486bf-432e-4af6-b114-baa523e1f801"

    var hasDelivery = false

    var mRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mTimeLimit < 25) {
                mTimeLimit++
                mHandler.postDelayed(this, 1000)
            } else {
                if (mIdDriverFound != null) {
                    if (mIdDriverFound != "") {
                        mClientBookingProvider.updateStatus(
                            FirestoreClass().getCurrentUserID(),
                            "cancel"
                        )
                        restartRequest()
                    }
                }
                mHandler.removeCallbacks(this)
            }
        }
    }

    var validation: Validation? = null
    var tokenID : String? = null
    var orderDateTime : Long? = null
    private lateinit var checkTermsAndConditions : CheckBox
    var JsonResponseToPay : String? = null

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("SetTextI18n", "CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_checkout)

        dialogAmountToPay = Dialog(this)
        dialogAmountToPay!!.setContentView(R.layout.dialog_amount_to_pay)
        dialogAmountToPay!!.window?.setBackgroundDrawable(ColorDrawable(0))

        dialogPayWithCard = Dialog(this)
        dialogPayWithCard!!.setContentView(R.layout.dialog_pay_with_card)
        dialogPayWithCard!!.window?.setBackgroundDrawable(ColorDrawable(0))

        /**
         * Dialog amount to pay
         */

        val inputAmount = dialogAmountToPay!!.findViewById(R.id.dialog_et_amount_to_pay) as MSPEditText
        totalPay = dialogAmountToPay!!.findViewById(R.id.dialog_total_to_pay) as MSPTextView

        val pay = dialogAmountToPay!!.findViewById(R.id.dialog_btn_pay) as Button
        pay.setOnClickListener {
            if (inputAmount.text.toString() != ""){
                amountToPay = inputAmount.text.toString()
                if (amountToPay.toDouble() >= tv_checkout_total_amount.text.toString()
                                .replace("S/", "").toDouble()){
                    initPayment()
                }
                else{
                    Toast.makeText(
                        this@CheckoutActivity,
                        "EL MONTO A PAGAR DEBE SE MAYOR Ó IGUAL A LA CANTIDAD TOTAL!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            else{
                Toast.makeText(
                    this@CheckoutActivity,
                    "INGRESE EL MONTO A PAGAR!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        val closeDialogAmountToPay = dialogAmountToPay!!.findViewById(R.id.closeDialog) as ImageView
        closeDialogAmountToPay.setOnClickListener {
            dialogAmountToPay!!.dismiss()
        }

        val closeDialogPayWithCard = dialogPayWithCard!!.findViewById(R.id.closeDialog) as ImageView
        closeDialogPayWithCard.setOnClickListener {
            dialogPayWithCard!!.dismiss()
        }

        /**
         *  Dialog pay with card
         */

        validation = Validation()

        val Cvv = dialogPayWithCard!!.findViewById(R.id.txt_cvv) as MSPEditText
        Cvv.isEnabled = false
        val kindCard  = dialogPayWithCard!!.findViewById(R.id.kind_card) as TextView
        val typeCard  = dialogPayWithCard!!.findViewById(R.id.type_card) as ImageView
        val months = resources.getStringArray(R.array.Months)
        val years  = resources.getStringArray(R.array.Years)
        var month = ""
        var year  = ""

        val card : MSPEditText = dialogPayWithCard!!.findViewById(R.id.txt_cardnumber) as MSPEditText
        var cardData = Card()
        val containerCard : TextInputLayout = dialogPayWithCard!!.findViewById(R.id.til_cardnumber) as TextInputLayout
        val spinnerMonth : Spinner = dialogPayWithCard!!.findViewById(R.id.spinner_month) as Spinner
        val spinnerYear : Spinner = dialogPayWithCard!!.findViewById(R.id.spinner_year) as Spinner
        val btnPayWithCard : Button = dialogPayWithCard!!.findViewById(R.id.btn_pay) as Button
        val dialogEmail : MSPEditText = dialogPayWithCard!!.findViewById(R.id.txt_email) as MSPEditText
        val switchCard : SwitchMaterial = dialogPayWithCard!!.findViewById(R.id.switch_save_card) as SwitchMaterial

        val sharedPreferencesCard = getSharedPreferences(
            Constants.USER_CARD_DATA,
            Context.MODE_PRIVATE
        )
        val editorCard = sharedPreferencesCard.edit()
        val gson = Gson()

        switchCard.setOnClickListener{
            if (card.text.toString().isNotEmpty() && Cvv.text.toString().isNotEmpty()
                && dialogEmail.text.toString().isNotEmpty()){
                if (switchCard.isChecked){
                    val sharedCard = Card(
                        card.text.toString(),
                        Cvv.text.toString(),
                        month,
                        year.toInt(),
                        dialogEmail.text.toString()
                    )
                    val json = gson.toJson(sharedCard)
                    editorCard.putString(Constants.KEY_CARD, json)
                    editorCard.apply()
                    Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
                } else{
                    if (sharedPreferencesCard.contains(Constants.KEY_CARD)){
                        getSharedPreferences(Constants.USER_CARD_DATA, Context.MODE_PRIVATE).edit().clear().apply();
                        Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else{
                Toast.makeText(this, Constants.COMPLETED_ALL_DATA, Toast.LENGTH_SHORT).show()
            }
        }



        card.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    Cvv.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable) {
                val textCard: String = card.text.toString()
                if (s.isEmpty()) {
                    containerCard.boxStrokeColor =
                        resources.getColor(R.color.design_default_color_error)
                }
                if (validation!!.luhn(textCard)) {
                    containerCard.boxStrokeColor = resources.getColor(R.color.colorPrimaryDark)

                } else {
                    containerCard.boxStrokeColor =
                        resources.getColor(R.color.design_default_color_error)
                }
                val cvv = validation!!.bin(textCard, kindCard, typeCard)
                if (cvv > 0) {
                    Cvv.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(cvv))
                    Cvv.isEnabled = true
                } else {
                    Cvv.isEnabled = false
                    Cvv.setText("")
                }
            }
        })

        val adapterSpinnerMonth = ArrayAdapter(
            this,
            R.layout.custom_spinner, months
        )
        spinnerMonth.adapter = adapterSpinnerMonth

        spinnerMonth.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {
                month = months[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }


        val adapterSpinnerYear = ArrayAdapter(
            this,
            R.layout.custom_spinner, years
        )
        spinnerYear.adapter = adapterSpinnerYear

        spinnerYear.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long,
            ) {
                year = years[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }


        if (sharedPreferencesCard.contains(Constants.KEY_CARD)){

            val type = object : TypeToken<Card>() {}.type
            val jsonReceived = sharedPreferencesCard.getString(Constants.KEY_CARD, null)
            cardData = gson.fromJson(jsonReceived, type)
            card.setText(cardData.card_number)
            for (i in 0 until spinnerMonth.adapter.count) {
                if (spinnerMonth.adapter.getItem(i).toString().contains(cardData.expiration_month)) {
                    spinnerMonth.setSelection(i)
                    month = cardData.expiration_month
                }
            }
            for (i in 0 until spinnerYear.adapter.count) {
                if (spinnerYear.adapter.getItem(i).toString().contains(cardData.expiration_year.toString())) {
                    spinnerYear.setSelection(i)
                    year = cardData.expiration_year.toString()
                }
            }
            Cvv.setText(cardData.cvv)
            dialogEmail.setText(cardData.email)
            switchCard.isChecked = true
        }


        btnPayWithCard.setOnClickListener {

            if (Cvv.text!!.isNotEmpty() && card.text!!.isNotEmpty() && dialogEmail.text!!.isNotEmpty()){
                showProgressDialog(resources.getString(R.string.validate_card))


                cardData = Card(
                    card.text.toString(),
                    Cvv.text.toString(),
                    month,
                    year.toInt(),
                    dialogEmail.text.toString()
                )

                if (sharedPreferencesCard.contains(Constants.KEY_CARD)){
                    val json = gson.toJson(cardData)
                    editorCard.putString(Constants.KEY_CARD, json)
                    editorCard.apply()
                }

                val token = TokenCulqi(Constants.PUBLIC_API_KEY)
                token.createToken(applicationContext, cardData, object : TokenCallback {
                    override fun onSuccess(token: JSONObject?) {
                        try {
                            tokenID = token?.get(Constants.ID).toString()
                        } catch (ex: Exception) {
                            Toast.makeText(
                                this@CheckoutActivity,
                                Constants.ERROR,
                                Toast.LENGTH_SHORT
                            ).show()
                            hideProgressDialog()
                        }
                        hideProgressDialog()
                        showProgressDialog(resources.getString(R.string.success_card))

                        val dataCharge = DataCharge(
                            df.format(mTotalAmount).replace(".", "").toInt(),
                            Constants.TYPE_MONEY,
                            dialogEmail.text.toString(),
                            tokenID!!
                        )

                        val charge = Charge(Constants.PRIVATE_API_KEY)
                        charge.createCharge(
                            applicationContext,
                            dataCharge,
                            object : ChargeCallback {
                                override fun onSuccess(charge: JSONObject?) {
                                    try {
                                        // retrieving charge ID
                                    } catch (ex: Exception) {
                                        Toast.makeText(
                                            this@CheckoutActivity,
                                            Constants.ERROR_CARD,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        hideProgressDialog()
                                        dialogPayWithCard!!.dismiss()
                                        //initPayment()
                                    }
                                    Toasty.success(
                                        this@CheckoutActivity,
                                        Constants.PAY,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    hideProgressDialog()
                                    dialogPayWithCard!!.dismiss()
                                    initPayment()
                                }

                                override fun onError(error: Exception?) {
                                    Toast.makeText(
                                        this@CheckoutActivity,
                                        Constants.ERROR_CARD,
                                        Toast.LENGTH_LONG
                                    ).show()
                                    hideProgressDialog()
                                    dialogPayWithCard!!.dismiss()
                                    //initPayment()
                                }
                            })

                    }

                    override fun onError(error: Exception?) {
                        dialogPayWithCard!!.dismiss()
                        Toast.makeText(
                            this@CheckoutActivity,
                            Constants.ERROR_SERVER,
                            Toast.LENGTH_LONG
                        ).show()
                        hideProgressDialog()
                    }
                })
            }else{
                Toast.makeText(
                    this@CheckoutActivity,
                    Constants.COMPLETED_ALL_DATA,
                    Toast.LENGTH_LONG
                ).show()
            }


        }


        //progressDialog = ProgressDialog(this, R.style.ThemeOverlayCustom)
        typeMoney = resources.getString(R.string.type_money)
        shipping = 0.00
        sharedPhoto = getSharedPreferences(Constants.EXTRA_USER_PHOTO, MODE_PRIVATE)
        mPhoto = sharedPhoto.getString(Constants.EXTRA_USER_PHOTO, "").toString()
        radioGroupPayment = findViewById(R.id.radio_group_payment)
        checkTermsAndConditions = findViewById(R.id.cb_terms_and_condition)
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

        radioGroupPayment?.setOnCheckedChangeListener { _, radioID ->

            typePayment = when (radioID) {
                R.id.rdb_no_card -> {
                    resources.getString(R.string.lbl_cash_on_delivery)
                }
                else -> {
                    resources.getString(R.string.lbl_credit_card)
                }
            }
        }

        tv_terms_condition.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERMS_AND_CONDITIONS))
            startActivity(browserIntent)
        }

        btn_place_order.setOnClickListener {
            if (typePayment != ""){
                if (checkTermsAndConditions.isChecked){
                    when (typePayment) {
                        resources.getString(R.string.lbl_credit_card) -> {
                            enableComponents()
                            orderDateTime = System.currentTimeMillis()/1000
                            Visanet().getTokenSecurityProvider(this)
                        }
                        else -> {
                            orderDateTime = System.currentTimeMillis()/1000
                            dialogAmountToPay!!.show()
                        }
                    }
                }else{
                    Toast.makeText(this@CheckoutActivity, "DEBE ACEPTAR LOS TÉRMINOS Y CONDICIONES...", Toast.LENGTH_LONG).show()
                }
            } else{
                Toast.makeText(this@CheckoutActivity, "ELIJA UN MÉTODO DE PAGO!", Toast.LENGTH_SHORT).show()
            }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VisaNet.VISANET_AUTHORIZATION) {
            if (data != null) {
                if (resultCode == RESULT_OK) {
                    disableComponents()
                    val JSONString = data.extras!!.getString("keySuccess")
                    JsonResponseToPay = JSONString!!
                    Log.d("TAGJSON", JSONString)
                    initPayment()

                } else {
                    disableComponents()
                    var JSONString = data.extras!!.getString("keyError")
                    JSONString = JSONString ?: ""
                    Log.d("TAGJSON", JSONString)
                    val gsonConverter = Gson()
                    val responseSuccess = gsonConverter.fromJson(JSONString,com.aukdeclient.models.Response::class.java)

                    val intent = Intent(this, DenyOrderActivity::class.java)
                    intent.putExtra("card",responseSuccess.data.CARD)
                    intent.putExtra("brand",responseSuccess.data.BRAND)
                    intent.putExtra("motive",responseSuccess.data.ACTION_DESCRIPTION)
                    intent.putExtra("codeerror",responseSuccess.data.ACTION_CODE)
                    intent.putExtra("idOrder",orderDateTime!!)
                    startActivity(intent)
                }
            } else {
                Toast.makeText(this,"PEDIDO CANCELADO...",Toast.LENGTH_LONG).show()
                disableComponents()
            }
        }
    }
    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }

    fun receiveToken(token : String , pinHash : String){

        val TAG = "NIUBIZ"
        val db = TinyDB(this).getObject(Constants.KEY_USER_DATA_OBJECT, User::class.java)


        val data: MutableMap<String, Any> = HashMap()
        data[VisaNet.VISANET_SECURITY_TOKEN] = token
        data[VisaNet.VISANET_CHANNEL] = Channel.MOBILE
        data[VisaNet.VISANET_COUNTABLE] = true
        data[VisaNet.VISANET_MERCHANT] = Constants.MERCHANT_ID
        data[VisaNet.VISANET_PURCHASE_NUMBER] = orderDateTime.toString()
        data[VisaNet.VISANET_AMOUNT] = mTotalAmount
        data[VisaNet.VISANET_REGISTER_NAME] = db.firstName
        data[VisaNet.VISANET_REGISTER_LASTNAME] = db.lastName
        data[VisaNet.VISANET_REGISTER_EMAIL] = db.email

        val MDDdata = HashMap<String, String>()

        MDDdata["4"] = db.email
        MDDdata["21"] = "0"
        MDDdata["32"] = db.code_client
        MDDdata["75"] = "Registrado"

        data[VisaNet.VISANET_MDD] = MDDdata
        data[VisaNet.VISANET_ENDPOINT_URL] = "https://apiprod.vnforapps.com/"
        data[VisaNet.VISANET_CERTIFICATE_HOST] = "apiprod.vnforapps.com"
        data[VisaNet.VISANET_CERTIFICATE_PIN] =
            "sha256/$pinHash"


        val custom = VisaNetViewAuthorizationCustom()
        custom.logoImage = R.drawable.tutorials_eu_logo
        custom.buttonColorMerchant = R.color.primaryPink

        try {
            VisaNet.authorization(this, data, custom)

        } catch (e: java.lang.Exception) {
            Log.i(TAG, "onClick: " + e.message)
            disableComponents()
        }

    }

    private fun initPayment(){
        dialogAmountToPay!!.dismiss()
        if (hasDelivery){
            showProgressDialog(resources.getString(R.string.please_wait))
            getClosestStore()
            Toast.makeText(
                this@CheckoutActivity,
                "BUSCANDO CONDUCTOR CERCANO...",
                Toast.LENGTH_LONG
            ).show()
        }
        else{
            showProgressDialog(resources.getString(R.string.please_wait))
            placeAnOrder()
        }
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

        tv_checkout_sub_total.text = typeMoney+df.format(mSubTotal)
        // Here we have kept Shipping Charge is fixed as S/5 but in your case it may cary. Also, it depends on the location and total amount.
        setupShipping()

        if (mSubTotal > 0) {
            ll_checkout_place_order.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + shipping!!
            tv_checkout_total_amount.text = typeMoney+df.format(mTotalAmount)
            totalPay!!.text =  typeMoney+df.format(mTotalAmount)

        } else {
            ll_checkout_place_order.visibility = View.GONE
        }

        checkingDelivery()
    }


    private fun getClosestStore() {
        mGeofireStore.getActiveStore(mCurrentLatLng, mRadius)
                .addGeoQueryEventListener(object : GeoQueryEventListener {
                    override fun onKeyEntered(key: String, location: GeoLocation) {
                        if (!mStoreFound) {
                            //progressDialog.setMessage(Constants.SEARCH_DRIVER)
                            mStoreFound = true
                            mIdStoreFound = key
                            mStoreFoundLatLng = LatLng(location.latitude, location.longitude)
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
                                finish()
                                return
                            } else {
                                getClosestStore()
                            }
                        }

                    }

                    override fun onGeoQueryError(error: DatabaseError) {}
                })
    }

    private fun getClosestDriver() {
        mGeofireDriverDriver.getActiveDrivers(mStoreFoundLatLng, mRadiusDriver).addGeoQueryEventListener(
            object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    // INGRESA CUANDO ENCUENTRA UN CONDUCTOR EN UN RADIO DE BUSQUEDA
                    if (!mDriverFound && !isDriverCancel(key) && !mIsFinishSearch) {

                        // ESTA BUSCANDO UN CONDUCTOR QUE AUN NO RECHAZADO LA SOLICTUD
                        mIsLookingFor = true
                        mClientBookingProvider.getClientBookingByDriver(key)
                            .addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        var isDriverNotification = false
                                        if (!mIsFinishSearch) {
                                            for (d in snapshot.children) {
                                                if (d.exists()) {
                                                    if (d.hasChild("status")) {
                                                        val status =
                                                            d.child("status").value.toString()
                                                        if (status == "create" || status == "accept") {
                                                            isDriverNotification = true
                                                            break
                                                        }
                                                    } else {
                                                        Log.d("STATUS", "No existe el estado child")
                                                    }
                                                } else {
                                                    Log.d("STATUS", "No existe el Estado EXIST")
                                                }
                                            }
                                            if (!isDriverNotification) {
                                                mDriverFound = true
                                                mIdDriverFound = key
                                                mTimeLimit = 0
                                                mHandler.postDelayed(mRunnable, 1000)
                                                mDriverFoundLatLng = LatLng(
                                                    location.latitude,
                                                    location.longitude
                                                )
                                                Toast.makeText(
                                                    this@CheckoutActivity,
                                                    "CONDUCTOR ENCONTRADO!\nESPERANDO RESPUESTA...",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                sendNotificationDriver()
                                                Log.d("DRIVER", "ID: $mIdDriverFound")
                                            } else {
                                                mIsLookingFor = false
                                                getClosestDriver()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                    }
                }

                override fun onKeyExited(key: String) {}
                override fun onKeyMoved(key: String, location: GeoLocation) {}
                override fun onGeoQueryReady() {
                    // INGRESA CUANDO TERMINA LA BUSQUEDA DEL CONDUCTOR EN UN RADIO DE 0.1 KM
                    if (!mDriverFound && !mIsLookingFor) {
                        mRadiusDriver += 0.1f

                        // NO ENCONTRO NINGUN CONDUCTOR
                        if (mRadiusDriver > 5) {

                            // TERMINAR TOTALMENTE LA BUSQUEDA YA QUE NO SE ENCONTRO NINGUN CONDCUTOR
                            if (mListener != null) {
                                mClientBookingProvider.getStatus(FirestoreClass().getCurrentUserID())
                                    .removeEventListener(
                                        mListener!!
                                    )
                            }
                            //.setText("NO SE ENCONTRO UN CONDUCTOR")
                            mIsFinishSearch = true
                            mClientBookingProvider.delete(FirestoreClass().getCurrentUserID())
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@CheckoutActivity,
                                        "NO SE ENCONTRO UN CONDUCTOR",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@CheckoutActivity,
                                            CartListActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            // eliminar nodo ClientBooking de su ID
                            return
                        } else {
                            Log.d("REQUEST", "ENTRO GET CLOSETS")
                            getClosestDriver()
                        }
                    }
                }

                override fun onGeoQueryError(error: DatabaseError) {}
            })
    }

    private fun restartRequest() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable)
        }
        mTimeLimit = 0
        mIsLookingFor = false
        mDriversNotAccept.add(mIdDriverFound)
        mDriverFound = false
        mIdDriverFound = ""
        mRadius = 0.1
        mIsFinishSearch = false
        //.setText("BUSCANDO CONDUCTOR")
        getClosestDriver()
    }

    private fun sendNotificationDriver() {
        FirestoreClass().getTokenDriver(mIdDriverFound).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val token = snapshot.child(Constants.TOKEN).value.toString()
                    val map: MutableMap<String, String> = HashMap()
                    map["title"] = Constants.TITTLE_NOTIFICATION
                    map["body"] = Constants.BODY_NOTIFICATION
                    map["path"] = photo_default
                    map["idClient"] = FirestoreClass().getCurrentUserID()
                    map["numPedido"] = orderDateTime.toString() // para testear
                    map["nombre"] = FirestoreClass().getCurrentUserID()
                    val fcmBody = FCMBody(token, "high", map)
                    mNotificationProvider.sendNotification(fcmBody).enqueue(object :
                        Callback<FCMResponse> {
                        override fun onResponse(
                            call: Call<FCMResponse>,
                            response: Response<FCMResponse>,
                        ) {
                            if (response.body() != null) {
                                if (response.body()!!.success == 1) {
                                    val bookingStatus = ClientBooking(
                                        "",
                                        FirestoreClass().getCurrentUserID(),
                                        mIdDriverFound,
                                        "create"
                                    )
                                    mClientBookingProvider.create(bookingStatus)
                                        .addOnSuccessListener {
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

    /**
     * RETORNAR SI EL ID DEL CODNDUCTOR ENCONTRADO YA CANCELO EL VIAJE
     * @param idDriver
     * @return
     */
    private fun isDriverCancel(idDriver: String): Boolean {
        for (id in mDriversNotAccept) {
            if (id == idDriver) {
                return true
            }
        }
        return false
    }

    private fun checkStatusClientBooking() {
        mListener = mClientBookingProvider.getStatus(FirestoreClass().getCurrentUserID()).addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status: String = snapshot.value.toString()
                        if (status == "accept") {
                            mHandler.removeCallbacks(mRunnable)
                            //progressDialog.dismiss()
                            Toast.makeText(
                                this@CheckoutActivity,
                                "PEDIDO ACEPTADO!",
                                Toast.LENGTH_LONG
                            ).show()
                            placeAnOrder()
                        } else if (status == "cancel") {
                            if (mIsLookingFor) {
                                restartRequest()
                            }
                            Toast.makeText(
                                this@CheckoutActivity,
                                "EL CONDUCTOR NO ACEPTÓ EL PEDIDO!\nBUSCANDO SIGUIENTE...",
                                Toast.LENGTH_LONG
                            ).show()
                            //progressDialog.setMessage("EL CONDUCTOR NO ACEPTÓ EL PEDIDO!\nBUSCANDO SIGUIENTE...")
                            /*progressDialog.dismiss()
                           finish()*/
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

        ctx += 1

        if (ctx == 1){

            mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartItemsList,
                mAddressDetails!!,
                "#${orderDateTime!!}",
                mCartItemsList[0].image,
                mSubTotal.toString(),
                shipping.toString(),
                mTotalAmount.toString(),
                orderDateTime!!,
                "",
                0,
                typePayment,
                amountToPay,
                mIdDriverFound
            )
            sendNotificationStore()
            FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
        }


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

        // chekear aquIIIIIIIIIIIIIIIIIIIIIIII
        
        FirestoreClass().deleteCartRealtime(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
            ClientBookingProvider().delete(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
                hideProgressDialog()

                if (JsonResponseToPay != null){
                    val gsonConverter = Gson()
                    val responseSuccess = gsonConverter.fromJson(JsonResponseToPay,com.aukdeclient.models.Response::class.java)

                    val intent = Intent(this, SuccessOrderActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("amount",mTotalAmount)
                    intent.putExtra("card",responseSuccess.dataMap.CARD)
                    intent.putExtra("brand",responseSuccess.dataMap.BRAND)
                    intent.putExtra("idOrder",orderDateTime!!)
                    startActivity(intent)
                    finish()
                }else{
                    val intent = Intent(this, SuccessOrderActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("amount",mTotalAmount)
                    intent.putExtra("card","")
                    intent.putExtra("brand","")
                    intent.putExtra("idOrder",orderDateTime!!)
                    startActivity(intent)
                    finish()
                }



            }
        }
    }

    private fun checkingDelivery(){
        for (i in 0 until  mCartItemsList.size) {
            if (mCartItemsList[i].delivery == "no"){
                hasDelivery = true
                break
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupShipping(){

        shipping = 0.00

        var noDelivery = 0
        var hasDelivery = 0

        for(item in 0 until mCartItemsList.size){
            if (mCartItemsList[item].delivery == "no"){
                hasDelivery++
                shipping = if(hasDelivery == 1){
                    5.00
                } else{
                    shipping!! + 0.50
                }
            }
            else if (mCartItemsList[item].delivery == "si"){
                noDelivery++
                shipping = shipping!! + 0.00
            }
        }

        if (shipping == 0.00){
            tv_checkout_shipping_charge.text = Constants.FREE_SHIPPING
        }
        else {
            tv_checkout_shipping_charge.text = typeMoney+df.format(shipping)
        }

    }

    private fun enableComponents(){
        val progress : ProgressBar = findViewById(R.id.progress_niubiz)
        val payNiubiz : Button = findViewById(R.id.btn_place_order)

        progress.visibility = View.VISIBLE
        payNiubiz.isEnabled = false
        payNiubiz.setBackgroundColor(resources.getColor(R.color.black))

    }

    private fun disableComponents(){

        val progress : ProgressBar = findViewById(R.id.progress_niubiz)
        val payNiubiz : Button = findViewById(R.id.btn_place_order)

        progress.visibility = View.GONE
        payNiubiz.isEnabled = true
        payNiubiz.setBackgroundColor(resources.getColor(R.color.primaryPink))

    }

}
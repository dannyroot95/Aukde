package com.aukdeclient.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Address
import com.aukdeclient.models.Partner
import com.aukdeclient.utils.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.okhttp.MediaType
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import kotlinx.android.synthetic.main.activity_register.btn_register
import kotlinx.android.synthetic.main.activity_register.cb_terms_and_condition
import kotlinx.android.synthetic.main.activity_register.et_confirm_password
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_first_name
import kotlinx.android.synthetic.main.activity_register.et_last_name
import kotlinx.android.synthetic.main.activity_register.et_password
import kotlinx.android.synthetic.main.activity_register.tv_login
import kotlinx.android.synthetic.main.activity_register_provider.*


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class RegisterProvider : BaseActivity() , OnMapReadyCallback{

    //NOTA AGREGAR DIRECCIÓN , DEFERENCIA Y UBICACION EN TIEMPO REAL

    private var mTypeProduct : String = ""
    private lateinit var spinnerProduct : Spinner

    private var mAddressDetails: Address? = null
    private var geocoder: Geocoder? = null
    private var mMap: GoogleMap? = null
    private lateinit var mapView: MapView
    var latitudeX  = 0.0
    var longitudeX  = 0.0
    var mapViewBundle: Bundle? = null
    lateinit var mCurrentLatLng: LatLng
    private var mCameraListener: GoogleMap.OnCameraIdleListener? = null
    lateinit var mLocationRequest: LocationRequest
    lateinit var mFusedLocation: FusedLocationProviderClient
    private var mIsFirstTime = true
    private lateinit var linearMap : LinearLayout
    private lateinit var floatingMap : FloatingActionButton
    private lateinit var showMap : Button
    private lateinit var imageLocation : ImageView

    private lateinit var mButtonMale : CardView
    private lateinit var mButtonFemale : CardView
    private lateinit var mLinearMale : LinearLayout
    private lateinit var mLinearFemale : LinearLayout

    private var mGender : String = ""
    private var mDelivery : String = ""
    private var mTypeUser : String = "provider"

    var radioGroup: RadioGroup? = null
    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (applicationContext != null) {
                    mCurrentLatLng = LatLng(location.latitude, location.longitude)
                    /*if (mMarker != null) {
                        mMarker!!.remove()
                    }*/
                    // GET LOCATION USER IN REAL TIME
                    mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                    .target(LatLng(location.latitude, location.longitude))
                                    .zoom(16f)
                                    .build()
                    ))
                    /*mMarker = mMap!!.addMarker(MarkerOptions().position(
                            LatLng(location.latitude, location.longitude)
                    )
                            .title("Tú Poscición")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_location))
                    )
                    mMarker!!.showInfoWindow()*/

                    if (mIsFirstTime) {
                        mIsFirstTime = false
                    }
                }
            }
        }
    }
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_register_provider)
        supportActionBar?.hide()

        linearMap = findViewById(R.id.map_container)
        floatingMap = findViewById(R.id.floatingMap)
        showMap = findViewById(R.id.btn_show_map)
        imageLocation = findViewById(R.id.locationImg)
        closeMap()

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)
        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
            mAddressDetails =
                    intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)!!
        }

        if (savedInstanceState != null) {
            mapViewBundle =
                    savedInstanceState.getBundle(Constants.MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

        mButtonMale  = findViewById(R.id.btn_male)
        mButtonFemale  = findViewById(R.id.btn_female)
        mLinearMale = findViewById(R.id.linear_male)
        mLinearFemale = findViewById(R.id.linear_female)

        spinnerProduct = findViewById(R.id.spinner_type_product)
        radioGroup = findViewById(R.id.radio_group)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.

        defineGender()

        val adapterSpinner = ArrayAdapter.createFromResource(
                this,
                R.array.type_product,
                R.layout.support_simple_spinner_dropdown_item
        )
        spinnerProduct.adapter = adapterSpinner
        spinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                mTypeProduct = parent!!.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            mDelivery = if (checkedId == R.id.rdb_yes) {
                "si"
            } else{
                "no"
            }
        }

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        btn_register.setOnClickListener {
            registerUser()
        }

        // START
        tv_login.setOnClickListener{
            // Here when the user click on login text we can either call the login activity or call the onBackPressed function.
            // We will call the onBackPressed function.
            finish()
        }

        mCameraListener = GoogleMap.OnCameraIdleListener {
            try{
                var geocoder : Geocoder = Geocoder(this)
                mCurrentLatLng = mMap!!.cameraPosition.target
                latitudeX = mCurrentLatLng.latitude
                longitudeX = mCurrentLatLng.longitude
                Toast.makeText(this,"Ubicación actualizada!",Toast.LENGTH_SHORT).show()

            }
            catch (e: Exception){}
        }

        floatingMap.setOnClickListener {
            closeMap()
        }
        showMap.setOnClickListener {
            btn_show_map.hideKeyboard()
            openMap()
        }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {

            mGender == "" ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_gender), true)
                false
            }

            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_dni.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_dni), true)
                false
            }

            TextUtils.isEmpty(et_phone_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_phone_number), true)
                false
            }


            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_store.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_store), true)
                false
            }

            TextUtils.isEmpty(et_address.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }

            latitudeX == 0.0 || longitudeX == 0.0 ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_location), true)
                false
            }

            TextUtils.isEmpty(et_ruc.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_ruc), true)
                false
            }

            mDelivery == "" ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_delivery), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_confirm_password),
                        true
                )
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                    .trim { it <= ' ' } -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                        true
                )
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_agree_terms_and_condition),
                        true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to register the user with email and password using FirebaseAuth.
     */
    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val partner = Partner(
                    "",
                    et_first_name.text.toString().trim { it <= ' ' },
                    et_last_name.text.toString().trim { it <= ' ' },
                    et_dni.text.toString().trim { it <= ' ' },
                    et_phone_number.text.toString().toLong(),
                    et_ruc.text.toString().trim { it <= ' ' },
                    mGender,
                    et_email.text.toString().trim { it <= ' ' },
                    et_password.text.toString().trim { it <= ' ' },
                    mTypeProduct,
                    mDelivery,
                    et_store.text.toString().trim { it <= ' ' },
                    et_address.text.toString().trim { it <= ' ' },
                    mTypeUser, "", latitudeX, longitudeX,
                    0,"","","",0
            )

            // Pass the required values in the constructor.
            FirestoreClass().requestProvider(this, partner)
            sendSMS()
            // Create an instance and create a register a user with email and password.
        }
    }

    private fun sendSMS() {
        AsyncTask.execute { threadSMS() }
    }

    private fun threadSMS() {

        //991900557
        val phone = "989280394"
        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, "{\"messages\": [{\"source\": \"mashape\",\"from\": \"Test\",\"body\": \"Alguien quiere ser nuestro SOCIO!, revisa el panel\",\"to\": \"+51$phone\",\"schedule\": \"1452244637\",\"custom_string\": \"Alguien quiere ser nuestro SOCIO! , revisa el panel\" }]}")

        val request = Request.Builder()
            .url("https://clicksend.p.rapidapi.com/sms/send")
            .post(body)
            .addHeader("content-type", "application/json")
            .addHeader("authorization", "Basic Y29udGFjdG9AY29uZXhmaW4ub3JnOkZpbmFuemFzaW50ZWxpZ2VudGVzMTArKg==")
            .addHeader("x-rapidapi-key", "a79f8e5c23mshc421c059f1161bfp1c0b26jsn40bee7f91d53")
            .addHeader("x-rapidapi-host", "clicksend.p.rapidapi.com")
            .build()

         val response = client.newCall(request).execute()
    }

    private fun defineGender(){
        btn_male.setOnClickListener {
            linear_male.setBackgroundColor(Color.parseColor("#FFBAD1"))
            linear_female.setBackgroundColor(Color.TRANSPARENT)
            mGender = Constants.MALE
        }
        btn_female.setOnClickListener {
            linear_male.setBackgroundColor(Color.TRANSPARENT)
            linear_female.setBackgroundColor(Color.parseColor("#FFBAD1"))
            mGender = Constants.FEMALE
        }
    }
    fun providerRegistrationSuccess() {
        // Hide the progress dialog
        hideProgressDialog()
        Toast.makeText(
                this,
                resources.getString(R.string.provider_register_success),
                Toast.LENGTH_LONG
        ).show()
        finish()
    }

    @SuppressLint("RestrictedApi")
    private fun closeMap(){
        linearMap.visibility = View.INVISIBLE
        floatingMap.visibility = View.INVISIBLE
        imageLocation.visibility = View.INVISIBLE
    }

    @SuppressLint("RestrictedApi")
    private fun openMap(){
        linearMap.visibility = View.VISIBLE
        floatingMap.visibility = View.VISIBLE
        imageLocation.visibility = View.VISIBLE
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle =
                outState.getBundle(Constants.MAP_VIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(
                    Constants.MAP_VIEW_BUNDLE_KEY,
                    mapViewBundle
            )
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                        mMap!!.isMyLocationEnabled = false
                    } else {
                        showAlertDialogNOGPS()
                    }
                } else {
                    checkLocationPermissions()
                }
            } else {
                checkLocationPermissions()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            mMap!!.isMyLocationEnabled = false
        } else if (requestCode == Constants.SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNOGPS()
        }
    }

    private fun showAlertDialogNOGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(resources.getString(R.string.active_gps_for_continue))
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.active_gps)) { _, _ -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Constants.SETTINGS_REQUEST_CODE) }.create().show()
    }

    private fun gpsActived(): Boolean {
        var isActive = false
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true
        }
        return isActive
    }

    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle(resources.getString(R.string.permision_for_continue))
                        .setMessage(resources.getString(R.string.require_permision))
                        .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE) }
                        .create()
                        .show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
            }
        }
    }

    private fun startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                    mMap!!.isMyLocationEnabled = false
                } else {
                    showAlertDialogNOGPS()
                }
            } else {
                checkLocationPermissions()
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                mMap!!.isMyLocationEnabled = false
            } else {
                showAlertDialogNOGPS()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.setOnCameraIdleListener(mCameraListener)
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.smallestDisplacement = 5f
        startLocation()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onBackPressed() {
        if (linearMap.isVisible){
            closeMap()
        }
        else{
            finish()
        }

    }

}
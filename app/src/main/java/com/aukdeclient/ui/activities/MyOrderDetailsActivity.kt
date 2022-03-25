package com.aukdeclient.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeclient.Maps.GeofireDriverProvider
import com.aukdeshop.R
import com.aukdeclient.models.Order
import com.aukdeclient.ui.adapters.CartItemsListAdapter
import com.aukdeclient.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.shuhart.stepview.StepView
import kotlinx.android.synthetic.main.activity_my_order_details.*
import kotlinx.android.synthetic.main.activity_sold_product_details.*
import java.text.SimpleDateFormat
import java.util.*


class MyOrderDetailsActivity : AppCompatActivity() , OnMapReadyCallback {

    private lateinit var stepView : StepView
    var myOrderDetails: Order = Order()
    var colorWhite = Color.parseColor("#FFFFFF")
    private var geocoder: Geocoder? = null
    private var mMap: GoogleMap? = null
    private lateinit var mapView: MapView
    var mapViewBundle: Bundle? = null
    private var mMarker: Marker? = null
    lateinit var mDriverLatLng: LatLng
    private lateinit var mListener : ValueEventListener
    private lateinit var mGeofireProvider: GeofireDriverProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_my_order_details)

        mGeofireProvider = GeofireDriverProvider("active_drivers")

        geocoder = Geocoder(this)

        if (savedInstanceState != null) {
            mapViewBundle =
                    savedInstanceState.getBundle(Constants.MAP_VIEW_BUNDLE_KEY)
        }

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)


        stepView = findViewById(R.id.step_view)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
            myOrderDetails =
                intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }

        setupUI(myOrderDetails)
        setupStepView()
        reactiveGetAllData(myOrderDetails)

    }

    private fun setupStepView(){
        stepView.state
            .selectedTextColor(ContextCompat.getColor(this, R.color.colorAccent))
            .animationType(StepView.ANIMATION_CIRCLE)
            .selectedCircleColor(ContextCompat.getColor(this, R.color.colorAccent))
            .selectedCircleRadius(resources.getDimensionPixelSize(R.dimen.rv_item_name_textSize))
            .selectedStepNumberColor(
                    ContextCompat.getColor(
                            this,
                            R.color.colorPrimary
                    )
            )
            .stepsNumber(4)
            .animationDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
            .stepLineWidth(resources.getDimensionPixelSize(R.dimen.grey_border_stroke_size))
            .stepNumberTextSize(resources.getDimensionPixelSize(R.dimen.rv_item_name_textSize))
            .commit()
            checkStatusStepView()
    }


    private fun checkStatusStepView(){
        when (myOrderDetails.status) {
            0 -> {
                tv_order_status.text = resources.getString(R.string.order_status_pending)
                tv_order_status.setTextColor(Color.parseColor("#FC0000"))
                val color = Color.parseColor("#FC0000")
                stepView.go(0, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()
                map_container.visibility = View.GONE
            }
            1 -> {
                tv_order_status.text = resources.getString(R.string.order_status_in_process)
                tv_order_status.setTextColor(Color.parseColor("#F1C40F"))
                val color = Color.parseColor("#F1C40F")
                stepView.go(1, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()
                map_container.visibility = View.GONE

            }
            2 -> {
                tv_order_status.text = resources.getString(R.string.order_status_in_route)
                tv_order_status.setTextColor(Color.parseColor("#154360"))
                val color = Color.parseColor("#154360")
                stepView.go(2, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()
                map_container.visibility = View.VISIBLE
            }
           4 -> {
                tv_order_status.text = resources.getString(R.string.order_sending)
                tv_order_status.setTextColor(Color.parseColor("#154360"))
                val color = Color.parseColor("#154360")
                stepView.go(2, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()
            }
            3 -> {
                tv_order_status.text = resources.getString(R.string.order_status_finish)
                tv_order_status.setTextColor(Color.parseColor("#5BBD00"))
                val color = Color.parseColor("#5BBD00")
                stepView.go(3, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()
                map_container.visibility = View.GONE
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_my_order_details_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        toolbar_my_order_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to setup UI.
     *
     * @param orderDetails Order details received through intent.
     */
    @SuppressLint("SetTextI18n")

    private fun setupUI(orderDetails: Order) {

        var ctxItems = 0

        tv_order_details_id.text = orderDetails.title

        // Date Format in which the date will be displayed in the UI.
        val dateFormat = "dd MMM yyyy HH:mm"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = orderDetails.order_datetime

        val orderDateTime = formatter.format(calendar.time)
        tv_order_details_date.text = orderDateTime

        rv_my_order_items_list.layoutManager = LinearLayoutManager(this@MyOrderDetailsActivity)
        rv_my_order_items_list.setHasFixedSize(true)

        val cartListAdapter =
            CartItemsListAdapter(this@MyOrderDetailsActivity, orderDetails.items, false)
        rv_my_order_items_list.adapter = cartListAdapter

        for (i in 0 until orderDetails.items.size){
            if (orderDetails.items[i].delivery == "no"){
                ctxItems++
            }
        }

        if (ctxItems > 0){
            if(orderDetails.driver_id == ""){
                tv_driver_asigned.visibility = View.VISIBLE
            }
        }

        tv_my_order_details_address_type.text = orderDetails.address.type
        tv_my_order_details_full_name.text = orderDetails.address.name
        tv_my_order_details_address.text =
            "${orderDetails.address.address}, ${orderDetails.address.zipCode}"
        tv_my_order_details_additional_note.text = orderDetails.address.additionalNote

        if (orderDetails.address.otherDetails.isNotEmpty()) {
            tv_my_order_details_other_details.visibility = View.VISIBLE
            tv_my_order_details_other_details.text = orderDetails.address.otherDetails
        } else {
            tv_my_order_details_other_details.visibility = View.GONE
        }
        tv_my_order_details_mobile_number.text = orderDetails.address.mobileNumber

        tv_order_details_sub_total.text = orderDetails.sub_total_amount
        if (orderDetails.shipping_charge == "0.0"){
            tv_order_details_shipping_charge.text = Constants.FREE_SHIPPING
        }
        else{
            tv_order_details_shipping_charge.text = orderDetails.shipping_charge
        }
        tv_order_details_total_amount.text = orderDetails.total_amount
        tv_order_details_mode_payment.text = orderDetails.type_payment

        getDriverLocation()
    }

    private fun reactiveGetAllData(orderId: Order){
        val mFirestore : FirebaseFirestore = FirebaseFirestore.getInstance()
        mFirestore.collection(Constants.ORDERS).document(orderId.id).addSnapshotListener { document, e ->
            if (document != null) {
                myOrderDetails = document.toObject(Order::class.java)!!
                setupUI(myOrderDetails)
                setupStepView()
            }
        }
    }

    private fun getDriverLocation(){

            mListener = mGeofireProvider
                    .getDriverLocation(myOrderDetails.driver_id)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val latitude: Double? = snapshot.child("0").value.toString().toDoubleOrNull()
                                val longitude: Double? = snapshot.child("1").value.toString().toDoubleOrNull()
                                mDriverLatLng = LatLng(latitude!!, longitude!!)
                                if (mMarker != null) {
                                    mMarker?.remove()
                                }
                                mMarker = mMap!!.addMarker(MarkerOptions()
                                        .position(LatLng(latitude, longitude))
                                        .title("Tu Pedido")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.motomap)))

                                mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.Builder()
                                                .target(mDriverLatLng)
                                                .zoom(16f)
                                                .build()
                                ))
                                mMarker!!.showInfoWindow()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
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

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.uiSettings.isZoomControlsEnabled = true
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        mMap!!.isMyLocationEnabled = false
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

}
package com.aukdeshop.ui.activities

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Order
import com.aukdeshop.models.SoldProduct
import com.aukdeshop.utils.Constants
import com.aukdeshop.utils.GlideLoader
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_order_details.*
import kotlinx.android.synthetic.main.activity_sold_product_details.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A detail screen for the sold product item.
 */
class SoldProductDetailsActivity : BaseActivity() {

    private var typeMoney : String = ""
    private var mPhoto : String = ""
    lateinit var sharedPhoto : SharedPreferences
    var path = "https://firebasestorage.googleapis.com/v0/b" +
            "/gestor-de-pedidos-aukdefood.appspot.com/o" +
            "/fotoDefault.jpg?alt=media&token=f74486bf-432e-4af6-b114-baa523e1f801"

    var position = 0
    var mFiresbase : FirebaseFirestore = FirebaseFirestore.getInstance()
    var updating : Boolean = false

    private var mHasDelivery : String = ""
    lateinit var sharedHasDelivery : SharedPreferences

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_sold_product_details)

        typeMoney = resources.getString(R.string.type_money)
        var productDetails: SoldProduct = SoldProduct()

        sharedPhoto = getSharedPreferences(Constants.EXTRA_USER_PHOTO, MODE_PRIVATE)
        mPhoto = sharedPhoto.getString(Constants.EXTRA_USER_PHOTO, "").toString()

        sharedHasDelivery = getSharedPreferences(Constants.HAS_DELIVERY, MODE_PRIVATE)
        mHasDelivery = sharedHasDelivery.getString(Constants.HAS_DELIVERY,"").toString()

        if (intent.hasExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)) {
            productDetails =
                intent.getParcelableExtra<SoldProduct>(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
        }

        // TODO Step 2: Call the function to setup action bar.
        // START
        setupActionBar()
        // END

        // TODO Step 4: Call the function to populate the data in UI.
        // START
        setupUI(productDetails)
        // END

        if (mHasDelivery == "no"){
            when (productDetails.status) {
                0 -> {
                    btnUpdateInProcess.setTextColor(Color.parseColor("#FFFFFF"))
                    btnUpdateInProcess.setBackgroundColor(Color.parseColor("#F1C40F"))
                    btnUpdateInProcess.text = Constants.PROCESSING_ORDER
                }
                1 -> {
                    btnUpdateInProcess.setTextColor(Color.parseColor("#FFFFFF"))
                    btnUpdateInProcess.setBackgroundColor(Color.parseColor("#5BBD00"))
                    btnUpdateInProcess.text = Constants.DELIVER_ORDER
                }
                2 -> {
                    btnUpdateInProcess.visibility = View.GONE
                }
                3 -> {
                    btnUpdateInProcess.visibility = View.GONE
                }
            }
        }

        else {
            when (productDetails.status) {
                0 -> {
                    btnUpdateInProcess.setTextColor(Color.parseColor("#FFFFFF"))
                    btnUpdateInProcess.setBackgroundColor(Color.parseColor("#F1C40F"))
                    btnUpdateInProcess.text = Constants.PROCESSING_ORDER
                }
                1 -> {
                    btnUpdateInProcess.setTextColor(Color.parseColor("#FFFFFF"))
                    btnUpdateInProcess.setBackgroundColor(Color.parseColor("#154360"))
                    btnUpdateInProcess.text = Constants.SEND_PRODDUCT
                }
                4 -> {
                    btnUpdateInProcess.setTextColor(Color.parseColor("#FFFFFF"))
                    btnUpdateInProcess.setBackgroundColor(Color.parseColor("#5BBD00"))
                    btnUpdateInProcess.text = Constants.COMPLETED_ORDER
                }
                3 -> {
                    btnUpdateInProcess.visibility = View.GONE
                }
            }

        }


        btnUpdateInProcess.setOnClickListener {

            showProgressDialog(resources.getString(R.string.updating))
            updating = true

            if(mHasDelivery == "si"){
                when (productDetails.status) {
                    0 -> {
                        FirestoreClass().updateStatusOrder(this, productDetails.order_id, 1, productDetails.id,position)
                        sendNotification(productDetails.user_id,productDetails.order_id,1,productDetails)
                        updating = false

                    }
                    1 -> {
                        FirestoreClass().updateStatusOrder(this, productDetails.order_id, 4, productDetails.id,position)
                        sendNotification(productDetails.user_id,productDetails.order_id,4,productDetails)
                        updating = false
                    }
                    4 -> {
                        FirestoreClass().updateStatusOrder(this, productDetails.order_id, 3, productDetails.id,position)
                        sendNotification(productDetails.user_id,productDetails.order_id,3,productDetails)
                        updating = false
                    }
                }
            }

            else{
                if(productDetails.status == 0){
                    FirestoreClass().updateStatusOrder(this, productDetails.order_id, 1, productDetails.id,position)
                    sendNotification(productDetails.user_id,productDetails.order_id,1,productDetails)
                    updating = false

                }
                else if (productDetails.status == 1){
                    FirestoreClass().updateStatusOrder(this, productDetails.order_id, 2, productDetails.id,position)
                    sendNotification(productDetails.user_id,productDetails.order_id,2,productDetails)
                    updating = false
                }
            }

           //sendNotification(productDetails.user_id,productDetails.order_date.toString())
        }

    }

    private fun sendNotification(id: String, numOrder: String , status : Int , item : SoldProduct) {

        when (status) {
            1 -> {
                FirestoreClass().createNotificationUpdateStatusProvider(this,id, numOrder, Constants.PROCESSING,
                        item.image,item.title)
            }
            2 -> {
                FirestoreClass().createNotificationUpdateStatusProvider(this,id, numOrder, Constants.IN_ROUTE,
                        item.image,item.title)
            }
            4 -> {
                FirestoreClass().createNotificationUpdateStatusProvider(this,id, numOrder, Constants.SENDING,
                        item.image,item.title)
            }

            3 -> {
                FirestoreClass().createNotificationUpdateStatusProvider(this,id, numOrder, Constants.COMPLETED,
                        item.image,item.title)
            }

        }

    }

    // TODO Step 1: Create a function to setup action bar.
    // START
    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_sold_product_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_sold_product_details_activity.setNavigationOnClickListener { onBackPressed() }
    }
    // END

    // TODO Step 3: Create a function to setupUI.
    // START
    /**
     * A function to setup UI.
     *
     * @param productDetails Order details received through intent.
     */
    @SuppressLint("SetTextI18n")
    private fun setupUI(productDetails: SoldProduct) {

        //Toast.makeText(this , "Usuario : "+productDetails.user_id + "Pedido : "+productDetails.order_id,Toast.LENGTH_LONG).show()
        //REALIZAR LA CONSULTA EN FUNCION AL NUMERO DE PEDIDO

        tv_sold_product_details_id.text = productDetails.order_id

        // Date Format in which the date will be displayed in the UI.
        val dateFormat = "dd MMM yyyy HH:mm"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = productDetails.order_date
        tv_sold_product_details_date.text = formatter.format(calendar.time)

        if (mHasDelivery == "no") {
            when (productDetails.status) {
                0 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_pending)
                    tv_sold_product_status.setTextColor(Color.parseColor("#FC0000"))
                }
                1 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_in_process)
                    tv_sold_product_status.setTextColor(Color.parseColor("#F1C40F"))
                }
                2 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_finish)
                    tv_sold_product_status.setTextColor(Color.parseColor("#5BBD00"))
                }

                3 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_finish)
                    tv_sold_product_status.setTextColor(Color.parseColor("#5BBD00"))
                }

            }
        }
        else {
            when (productDetails.status) {
                0 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_pending)
                    tv_sold_product_status.setTextColor(Color.parseColor("#FC0000"))
                }
                1 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_in_process)
                    tv_sold_product_status.setTextColor(Color.parseColor("#F1C40F"))
                }

                4 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_sending)
                    tv_sold_product_status.setTextColor(Color.parseColor("#154360"))
                }

                3 -> {
                    tv_sold_product_status.text = resources.getString(R.string.order_status_finish)
                    tv_sold_product_status.setTextColor(Color.parseColor("#5BBD00"))
                }

            }
        }



        GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
                productDetails.image,
                iv_product_item_image
        )
        tv_product_item_name.text = productDetails.title
        tv_product_item_price.text = typeMoney+productDetails.price
        tv_sold_product_quantity.text = productDetails.sold_quantity

        tv_sold_details_address_type.text = productDetails.address.type
        tv_sold_details_full_name.text = productDetails.address.name
        tv_sold_details_address.text =
            "${productDetails.address.address}, ${productDetails.address.zipCode}"
        tv_sold_details_additional_note.text = productDetails.address.additionalNote

        if (productDetails.address.otherDetails.isNotEmpty()) {
            tv_sold_details_other_details.visibility = View.VISIBLE
            tv_sold_details_other_details.text = productDetails.address.otherDetails
        } else {
            tv_sold_details_other_details.visibility = View.GONE
        }
        tv_sold_details_mobile_number.text = productDetails.address.mobileNumber

        tv_sold_product_sub_total.text = productDetails.sub_total_amount
        tv_sold_product_shipping_charge.text = productDetails.shipping_charge
        tv_sold_product_total_amount.text = productDetails.total_amount

        mFiresbase.collection(Constants.ORDERS)
                .whereEqualTo("title", productDetails.order_id)
                .addSnapshotListener{ document, e ->
                    if (document != null) {
                        for (i in document.documents) {
                            val orderItem = i.toObject(Order::class.java)!!
                            orderItem.id = i.id
                            val product = orderItem.items

                            for (j in 0 until product.size) {
                                if (product[j].title == productDetails.title){
                                    position = j
                                }
                            }

                        }
                    }
                    if (e != null) {
                        return@addSnapshotListener
                    }
        }
    }

    override fun onBackPressed() {
        if (updating){
        }
        else
        {
            super.onBackPressed()
        }

    }

    // END
}

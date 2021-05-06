package com.aukdeshop.ui.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeshop.R
import com.aukdeshop.models.Order
import com.aukdeshop.ui.adapters.CartItemsListAdapter
import com.aukdeshop.utils.Constants
import com.shuhart.stepview.StepView
import kotlinx.android.synthetic.main.activity_my_order_details.*
import kotlinx.android.synthetic.main.activity_sold_product_details.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * My Order Details Screen.
 */
class MyOrderDetailsActivity : AppCompatActivity() {

    private lateinit var stepView : StepView
    private val currentStep = 0
    var myOrderDetails: Order = Order()
    var colorWhite = Color.parseColor("#FFFFFF")

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_my_order_details)

        stepView = findViewById(R.id.step_view)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
            myOrderDetails =
                intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }

        setupUI(myOrderDetails)

        setupStepView()
    }

    /**
     * A function for actionBar Setup.
     */

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
            ) // You should specify only stepsNumber or steps array of strings.
            // In case you specify both steps array is chosen.
            .steps(object : ArrayList<String?>() {
                init {
                    add(resources.getString(R.string.order_status_pending))
                    add(resources.getString(R.string.order_status_in_process))
                    add(resources.getString(R.string.order_status_in_route))
                    add(resources.getString(R.string.order_status_finish))
                }
            }) // You should specify only steps number or steps array of strings.
            // In case you specify both steps array is chosen.

            .stepsNumber(4)
            .animationDuration(resources.getInteger(android.R.integer.config_shortAnimTime))
            .stepLineWidth(resources.getDimensionPixelSize(R.dimen.grey_border_stroke_size))
            .textSize(resources.getDimensionPixelSize(R.dimen.cart_item_paddingTopBottom))
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
            }
            1 -> {
                tv_order_status.text = resources.getString(R.string.order_status_in_process)
                tv_order_status.setTextColor(Color.parseColor("#F1C40F"))
                val color = Color.parseColor("#F1C40F")
                stepView.go(1, true)
                stepView.state.selectedCircleColor(color).commit()
                stepView.state.selectedTextColor(color).commit()
                stepView.state.selectedStepNumberColor(colorWhite).commit()

            }
            2 -> {
                tv_order_status.text = resources.getString(R.string.order_status_in_route)
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

        // Get the difference between the order date time and current date time in hours.
        // If the difference in hours is 1 or less then the order status will be PENDING.
        // If the difference in hours is 2 or greater then 1 then the order status will be PROCESSING.
        // And, if the difference in hours is 3 or greater then the order status will be DELIVERED.

        val diffInMilliSeconds: Long = System.currentTimeMillis() - orderDetails.order_datetime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds)
        Log.e("Diferencia en Horas", "$diffInHours")

        when {
            diffInHours < 1 -> {
                tv_order_status.text = resources.getString(R.string.order_status_pending)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorAccent
                    )
                )
            }
            diffInHours < 2 -> {
                tv_order_status.text = resources.getString(R.string.order_status_in_process)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorOrderStatusInProcess
                    )
                )
            }
            else -> {
                tv_order_status.text = resources.getString(R.string.order_status_finish)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorOrderStatusFinish
                    )
                )
            }
        }

        rv_my_order_items_list.layoutManager = LinearLayoutManager(this@MyOrderDetailsActivity)
        rv_my_order_items_list.setHasFixedSize(true)

        val cartListAdapter =
            CartItemsListAdapter(this@MyOrderDetailsActivity, orderDetails.items, false)
        rv_my_order_items_list.adapter = cartListAdapter

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
        tv_order_details_shipping_charge.text = orderDetails.shipping_charge
        tv_order_details_total_amount.text = orderDetails.total_amount
    }
}
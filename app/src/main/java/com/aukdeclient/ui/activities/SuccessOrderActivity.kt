package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_success_order.*
import android.os.Bundle
import android.view.View
import com.aukdeclient.models.User
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.TinyDB
import com.aukdeshop.R
import kotlinx.android.synthetic.main.dialog_pay_with_card.*
import java.text.SimpleDateFormat
import java.util.*

class SuccessOrderActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_order)

        supportActionBar!!.hide()


        val dateFormat = "dd MMM yyyy HH:mm aa"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()

        val idOrder : Long = intent.getLongExtra("idOrder",0L)
        val amount : Double = intent.getDoubleExtra("amount",0.0)
        val card : String = intent.getStringExtra("card")!!

        val actualDate = System.currentTimeMillis()

        calendar.timeInMillis = actualDate

        val date = formatter.format(calendar.time)
        val db = TinyDB(this).getObject(Constants.KEY_USER_DATA_OBJECT, User::class.java)

        txt_success_date.text = date.toString()
        txt_success_id_order.text = "#$idOrder"
        txt_success_total_amount.text = "S/$amount"
        txt_success_name.text = db.firstName+" "+db.lastName
        txt_success_email.text = db.email
        if(card != ""){
            txt_success_credit_card.text = card
        }else{
            linear_card.visibility = View.GONE
        }




        btn_success_order_continue_shopping.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btn_success_order_show_orders.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra("success", "order")
            startActivity(intent)
            finish()
        }


    }
}
package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.aukdeclient.models.User
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.TinyDB
import com.aukdeshop.R
import kotlinx.android.synthetic.main.activity_denegate_order.*
import kotlinx.android.synthetic.main.activity_success_order.*
import kotlinx.android.synthetic.main.activity_success_order.btn_success_order_continue_shopping
import kotlinx.android.synthetic.main.activity_success_order.btn_success_order_show_orders
import kotlinx.android.synthetic.main.activity_success_order.txt_success_credit_card
import kotlinx.android.synthetic.main.activity_success_order.txt_success_date
import kotlinx.android.synthetic.main.activity_success_order.txt_success_id_order
import kotlinx.android.synthetic.main.activity_success_order.txt_success_name
import kotlinx.android.synthetic.main.activity_success_order.txt_success_total_amount
import kotlinx.android.synthetic.main.activity_success_order.txt_success_type_card
import java.text.SimpleDateFormat
import java.util.*

class DenyOrderActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_denegate_order)
        supportActionBar!!.hide()


        val dateFormat = "dd MMM yyyy HH:mm aa"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calendar: Calendar = Calendar.getInstance()

        val idOrder: Long = intent.getLongExtra("idOrder", 0L)
        val card: String = intent.getStringExtra("card")!!
        val brand: String = intent.getStringExtra("brand")!!
        val motive: String = intent.getStringExtra("motive")!!

        val actualDate = System.currentTimeMillis()

        calendar.timeInMillis = actualDate

        val date = formatter.format(calendar.time)
        val db = TinyDB(this).getObject(Constants.KEY_USER_DATA_OBJECT, User::class.java)

        txt_success_date.text = date.toString()
        txt_success_id_order.text = "#$idOrder"
        txt_success_name.text = db.firstName + " " + db.lastName
        txt_success_credit_card.text = card
        txt_success_type_card.text = brand
        txt_success_motive.text = motive

        if(motive == ""){
            txt_success_motive.text = "Error de servidor"
        }

        if(brand == ""){
            txt_success_motive.text = "Ninguno"
        }


        btn_deny_reintent.setOnClickListener {
            finish()
        }

        btn_deny_order_continue_shopping.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }

    override fun onBackPressed() {
    }
}
package com.aukdeclient.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aukdeclient.R
import kotlinx.android.synthetic.main.activity_success_order.*

class SuccessOrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_order)

        supportActionBar!!.hide()

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

    override fun onBackPressed() {
    }

}
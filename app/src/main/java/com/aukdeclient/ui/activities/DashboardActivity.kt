package com.aukdeclient.ui.activities


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.ui.fragments.OrdersFragment
import com.aukdeclient.utils.Constants
import com.github.clans.fab.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_dashboard.*

/**
 *  Dashboard Screen of the app.
 */
class DashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        // Update the background color of the action bar as per our design requirement.
        supportActionBar!!.setBackgroundDrawable(
            ContextCompat.getDrawable(
                this@DashboardActivity,
                R.drawable.app_toolbar_background
            )
        )

        window.statusBarColor = ContextCompat.getColor(this, R.color.primaryPink)

        val cart : FloatingActionButton = findViewById(R.id.float_button_cart)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_orders
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
        cart()
        cart.setOnClickListener {
            startActivity(Intent(this, CartListActivity::class.java))
        }

        val success = intent.getStringExtra("success")
        if (success != null){
             supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment , OrdersFragment())
                .addToBackStack(null).commit()
            supportActionBar!!.title = "Mis compras"
        }

        val sharedSuccess: SharedPreferences = getSharedPreferences(
            Constants.ALERT,
            Context.MODE_PRIVATE)
        var request = sharedSuccess.getInt(Constants.ALERT,0)
        val editor: SharedPreferences.Editor = sharedSuccess.edit()

        val dialogAlert = Dialog(this)
        dialogAlert.setContentView(R.layout.dialog_alerts)
        dialogAlert.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogAlert.window!!.attributes.windowAnimations = R.style.DialogScale
        val closeDialogAlert = dialogAlert.findViewById(R.id.dialog_btn_ok) as Button

        if (request <= 3){
            dialogAlert.show()
            closeDialogAlert.setOnClickListener {
                request += 1
                editor.putInt(Constants.ALERT,request)
                editor.apply()
                dialogAlert.dismiss()
            }
        }


        /*if (difference_in_days == 29L && mPackage == Constants.TRIAL){
            logout()
        }*/
    }

    private fun cart(){
        FirestoreClass().countProductsInCart(this@DashboardActivity)
    }

    override fun onBackPressed() {
        doubleBackToExit()
    }

    override fun onResume() {
        super.onResume()
        cart()
    }


}
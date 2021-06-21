package com.aukdeshop.ui.activities


import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.github.clans.fab.FloatingActionButton
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

        val cart : FloatingActionButton = findViewById(R.id.float_button_cart)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_products,
                R.id.navigation_dashboard,
                R.id.navigation_orders,
                R.id.navigation_sold_products
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)
        cart()
        cart.setOnClickListener {
            startActivity(Intent(this, CartListActivity::class.java))
        }

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
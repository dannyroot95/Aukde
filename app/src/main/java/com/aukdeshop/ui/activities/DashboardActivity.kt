package com.aukdeshop.ui.activities


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.utils.Constants
import com.github.clans.fab.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_dashboard.*

/**
 *  Dashboard Screen of the app.
 */
class DashboardActivity : BaseActivity() {

    lateinit var sharedPackage : SharedPreferences
    lateinit var sharedDateActivePackage : SharedPreferences
    private var mPackage : String = ""
    private var mDateActivePackage : Long = 0
    var currentTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sharedPackage = getSharedPreferences(Constants.PACKAGE, MODE_PRIVATE)
        sharedDateActivePackage = getSharedPreferences(Constants.DATE_ACTIVE_PACKAGE, MODE_PRIVATE)

        mPackage = sharedPackage.getString(Constants.PACKAGE, "").toString()
        mDateActivePackage = sharedDateActivePackage.getLong(Constants.DATE_ACTIVE_PACKAGE, 0L)

        var difference_in_days = (currentTime - mDateActivePackage) / (1000  * 60 * 60 * 24)

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


        /*if (difference_in_days == 29L && mPackage == Constants.TRIAL){
            logout()
        }*/
        supportActionBar!!.title = "Aukde"
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
        supportActionBar!!.title = "Aukde"
    }

    private fun logout(){
        showProgressDialog(Constants.LOGOUT)
        FirestoreClass().deleteToken(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
            FirestoreClass().deleteTokenRealtime(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
                hideProgressDialog()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                Toast.makeText(this,"CUENTA SUSPENDIDA!",Toast.LENGTH_LONG).show()
            }.addOnFailureListener{
                hideProgressDialog()
                //error
            }
        }.addOnFailureListener{
            hideProgressDialog()
            //error
        }
    }

}
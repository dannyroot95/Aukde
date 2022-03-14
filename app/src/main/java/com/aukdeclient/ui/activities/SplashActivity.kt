package com.aukdeclient.ui.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Partner
import com.aukdeclient.models.User
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.GlideLoader
import com.aukdeclient.utils.TinyDB
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.File

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {

    private var mProfileComplete : Int = 0
    lateinit var sharedProfile: SharedPreferences
    var delayTime = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        sharedProfile = getSharedPreferences(Constants.CLIENT, MODE_PRIVATE)
        mProfileComplete = sharedProfile.getInt(Constants.CLIENT,0)
        // This is used to hide the status bar and make the splash screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        FirestoreClass().getCategories(this)
        // Adding the handler to after the a task after some delay.
        // It is deprecated in the API level 30.
        Handler().postDelayed(
            {

                // If the user is logged in once and did not logged out manually from the app.
                // So, next time when the user is coming into the app user will be redirected to MainScreen.
                // If user is not logged in or logout manually then user will  be redirected to the Login screen as usual.

                // Get the current logged in user id
                val currentUserID = FirestoreClass().getCurrentUserID()

                if (currentUserID.isNotEmpty()) {

                    if (mProfileComplete == 0){
                        startActivity(Intent(this@SplashActivity, UserProfileActivity::class.java))
                    }
                    else {
                        // Launch dashboard screen.
                        startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
                    }
                }
                else {
                    // Launch the Login Activity
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))

                }
                finish() // Call this when your activity is done and should be closed.
            },
            delayTime
        ) // Here we pass the delay time in milliSeconds after which the splash activity will disappear.
    }

}
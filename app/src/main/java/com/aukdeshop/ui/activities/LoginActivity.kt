package com.aukdeshop.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.aukdeshop.Maps.GeofireDriverProvider
import com.google.firebase.auth.FirebaseAuth
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Partner
import com.aukdeshop.utils.Constants
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Login Screen of the application.
 */
@Suppress("DEPRECATION")
class LoginActivity : BaseActivity(), View.OnClickListener {

    var mFirestore = FirebaseFirestore.getInstance()
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_login)

        // This is used to hide the status bar and make the login screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Click event assigned to Forgot Password text.
        tv_forgot_password.setOnClickListener(this)
        // Click event assigned to Login button.
        btn_login.setOnClickListener(this)
        // Click event assigned to Register text.
        tv_register.setOnClickListener(this)
    }

    /**
     * In Login screen the clickable components are Login Button, ForgotPassword text and Register Text.
     */
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.tv_forgot_password -> {

                    // Launch the forgot password screen when the user clicks on the forgot password text.
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login -> {

                    logInRegisteredUser()
                }

                R.id.tv_register -> {
                    // Launch the register screen when the user clicks on the text.
                    val intent = Intent(this@LoginActivity, RegisterProvider::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * A function to validate the login entries of a user.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to Log-In. The user will be able to log in using the registered email and password with Firebase Authentication.
     */
    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Get the text from editText and trim the space
            val email = et_email.text.toString().trim { it <= ' ' }
            val password = et_password.text.toString().trim { it <= ' ' }

            // Log-In using FirebaseAuth
            mFirestore.collection(Constants.USERS).whereEqualTo("email",email).get().addOnSuccessListener { document ->
                if (document !=null){
                    for (Query : QueryDocumentSnapshot in document){
                        if (Query.exists()){
                            val typeUser = Query.data["type_user"].toString()
                            if (typeUser == "provider"){
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val firebaseUser: FirebaseUser = task.result!!.user!!
                                            FirestoreClass().getUserDetails(this@LoginActivity)
                                            FirestoreClass().createToken(firebaseUser.uid)

                                        } else {
                                            // Hide the progress dialog
                                            hideProgressDialog()
                                            showErrorSnackBar("No se pudo iniciar sesión, revise sus datos", true)
                                        }
                                    }
                            }
                            else {
                                hideProgressDialog()
                                showErrorSnackBar("Usuario no permitido", true)
                            }

                        }

                    }
                }
            }
        }
    }

    /**
     * A function to notify user that logged in success and get the user details from the FireStore database after authentication.
     */
    fun userLoggedInSuccess(user: Partner) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val location = LatLng(user.latitude, user.longitude)
            val mGeofireProvider  = GeofireDriverProvider("store")
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            mGeofireProvider.saveLocation(FirestoreClass().getCurrentUserID(),location)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Dashboard Screen after log in.
            val location = LatLng(user.latitude, user.longitude)
            val mGeofireProvider  = GeofireDriverProvider("store")
            mGeofireProvider.saveLocation(FirestoreClass().getCurrentUserID(),location)
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
    }

}
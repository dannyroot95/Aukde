package com.aukdeshop.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Partner
import com.aukdeshop.utils.Constants
import com.aukdeshop.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_settings.*

/**
 * Setting screen of the app.
 */
class SettingsActivity : BaseActivity(), View.OnClickListener {

    // A variable for user details which will be initialized later on.
    private lateinit var mUserDetails: Partner
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_settings)
        window.statusBarColor = ContextCompat.getColor(this, R.color.semiWhite)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        btn_logout.setOnClickListener(this@SettingsActivity)
        ll_address.setOnClickListener(this@SettingsActivity)
        tv_edit.setOnClickListener(this@SettingsActivity)
        btn_wishlist.setOnClickListener(this@SettingsActivity)
        tv_settings_wishlist.text = "Lista de deseos"
        tv_edit_profile.text = "Editar perfil"
        tv_my_address.text = "Mis direcciones"
        tv_my_orders.text = "Mis pedidos"
        tv_my_sales.text = "Mis ventas"
        tv_settings_share_app.text = "Compartir"
        tv_settings_support.text = "Atención al cliente"
        tv_settings_logout.text = "Salir"

    }

    override fun onResume() {
        super.onResume()

        getUserDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.btn_wishlist ->{
                    val intent = Intent(this@SettingsActivity, WishListListActivity::class.java)
                    startActivity(intent)
                }

                R.id.tv_edit -> {
                    val intent = Intent(this@SettingsActivity, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }

                R.id.ll_address -> {
                    val intent = Intent(this@SettingsActivity, AddressListActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_logout -> {
                    showProgressDialog(Constants.LOGOUT)
                    FirestoreClass().deleteToken(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
                        FirestoreClass().deleteTokenRealtime(FirestoreClass().getCurrentUserID()).addOnSuccessListener {
                            hideProgressDialog()
                            FirebaseAuth.getInstance().signOut()
                            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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
        }
    }

    /**
     * A function to get the user details from firestore.
     */
    private fun getUserDetails() {

        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class to get the user details from firestore which is already created.
        FirestoreClass().getUserDetails(this@SettingsActivity)
    }

    /**
     * A function to receive the user details and populate it in the UI.
     */
    @SuppressLint("SetTextI18n")
    fun userDetailsSuccess(user: Partner) {

        mUserDetails = user

        // Hide the progress dialog
        hideProgressDialog()

        // Load the image using the Glide Loader class.
        GlideLoader(this@SettingsActivity).loadUserPicture(user.image, iv_user_photo)

        tv_name.text = "${user.firstName} ${user.lastName}"
        if (user.gender == Constants.MALE){
            img_male.visibility = View.VISIBLE
        }
        else{
            img_female.visibility = View.VISIBLE
        }
        tv_email.text = user.email
        tv_mobile_number.text = "${user.mobile}"
        tv_name_store.text = user.name_store
        tv_category_store.text = user.type_product
        tv_code_client.text = "#"+user.code_client
        tv_type_package.text = user.type_package

        when (user.type_package) {
            Constants.TRIAL -> {
                tv_type_package.setTextColor(Color.parseColor("#21618C"))
            }
            Constants.BASIC -> {
                tv_type_package.setTextColor(Color.parseColor("#A569BD"))
            }
            else -> {
                tv_type_package.setTextColor(Color.parseColor("#F39C12"))
            }
        }

        FirestoreClass().getSalesAndOrders(this,user.id)

    }
}
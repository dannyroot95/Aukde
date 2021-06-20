package com.aukdeshop.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Partner
import com.aukdeshop.models.User
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
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_settings)

        tv_edit.setOnClickListener(this@SettingsActivity)
        btn_logout.setOnClickListener(this@SettingsActivity)
        ll_address.setOnClickListener(this@SettingsActivity)
        tv_settings_wishlist.text = "Lista de deseos"
        tv_edit.text = "Editar perfil"
        tv_my_address.text = "Mis direcciones"
        tv_order.text = "122"
        tv_my_orders.text = "Mis pedidos"
        tv_sales.text = "S/1200"
        tv_my_sales.text = "Mis ventas"

    }

    override fun onResume() {
        super.onResume()

        getUserDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

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
    fun userDetailsSuccess(user: Partner) {

        mUserDetails = user

        // Hide the progress dialog
        hideProgressDialog()

        // Load the image using the Glide Loader class.
        GlideLoader(this@SettingsActivity).loadUserPicture(user.image, iv_user_photo)

        tv_name.text = "${user.firstName} ${user.lastName}"
        tv_gender.text = user.gender
        tv_email.text = user.email
        tv_mobile_number.text = "${user.mobile}"
        tv_name_store.text = user.name_store
        tv_category_store.text = user.type_product
    }
}
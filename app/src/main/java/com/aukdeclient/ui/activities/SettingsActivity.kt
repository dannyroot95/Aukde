package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.User
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.GlideLoader
import com.aukdeclient.utils.TinyDB
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.URL
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import com.aukdeclient.databinding.DialogAlertsBinding
import com.aukdeclient.databinding.DialogSlowInternetBinding
import com.aukdeclient.databinding.DialogSupportBinding


/**
 * Setting screen of the app.
 */
class SettingsActivity : BaseActivity(), View.OnClickListener {

    // A variable for user details which will be initialized later on.
    private lateinit var mUserDetails: User
    private lateinit var bindingSupport : DialogSupportBinding
    private lateinit var binding : DialogAlertsBinding
    private lateinit var dialogSupport: Dialog
    private lateinit var dialog : Dialog
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

        bindingSupport = DialogSupportBinding.inflate(layoutInflater)
        dialogSupport = Dialog(this)
        dialogSupport.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogSupport.setContentView(bindingSupport.root)

        binding  = DialogAlertsBinding.inflate(layoutInflater)
        dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setContentView(binding .root)

        btn_logout.setOnClickListener(this@SettingsActivity)
        ll_address.setOnClickListener(this@SettingsActivity)
        tv_edit.setOnClickListener(this@SettingsActivity)
        btn_wishlist.setOnClickListener(this@SettingsActivity)
        tv_settings_wishlist.text = "Lista de deseos"
        tv_edit_profile.text = "Editar perfil"
        tv_my_address.text = "Mis direcciones"
        tv_my_orders.text = "Mis compras"
        tv_settings_share_app.text = "Compartir"
        tv_settings_support.text = "Atención al cliente"
        tv_settings_logout.text = "Salir"

        ln_share.setOnClickListener {
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "text/plain"
            val url = "https://play.google.com/store/apps/details?id=com.aukdeshop"
            share.putExtra(android.content.Intent.EXTRA_TEXT,url)
            startActivity(Intent.createChooser(share,"Compartir vía"))
        }
        ln_support.setOnClickListener{
            showDialogSupport()
        }
        ln_alerts.setOnClickListener {
            dialog.show()
            dialog.setCancelable(false)
            binding.dialogBtnOk.setOnClickListener {
                dialog.dismiss()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = getSharedPreferences(Constants.USER_DATA_OBJECT, Context.MODE_PRIVATE)
        val data = sharedPreferences.getString(Constants.KEY_USER_DATA_OBJECT,"")

        if (data != ""){
            val db = TinyDB(this).getObject(Constants.KEY_USER_DATA_OBJECT,User::class.java)
            userDetailsSuccessFromCache(db)
        }else{
            getUserDetails()
        }

    }



    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.btn_wishlist -> {
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
                    FirestoreClass().deleteToken(FirestoreClass().getCurrentUserID())
                    FirestoreClass().deleteTokenRealtime(FirestoreClass().getCurrentUserID())
                    val settings: SharedPreferences = getSharedPreferences(Constants.ALERT, Context.MODE_PRIVATE)
                    settings.edit().clear().apply()
                    FirebaseAuth.getInstance().signOut()
                    hideProgressDialog()
                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

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
    fun userDetailsSuccess(user: User) {

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
        tv_code_client.text = "#"+user.code_client

        FirestoreClass().getPurchases(this)

    }

    private fun userDetailsSuccessFromCache(user: User) {

        mUserDetails = user

        // Hide the progress dialog
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
        tv_code_client.text = "#"+user.code_client

        FirestoreClass().getPurchases(this)

    }

    private fun showDialogSupport(){
        dialogSupport.show()
        bindingSupport.closeDialog.setOnClickListener {
            dialogSupport.dismiss()
        }
    }

}
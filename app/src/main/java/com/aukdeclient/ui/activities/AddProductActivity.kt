package com.aukdeclient.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Product
import com.aukdeclient.notifications.server.NotificationProvider
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.IOException
import java.util.*

/**
 * Add Product screen of the app.
 */
@Suppress("DEPRECATED_IDENTITY_EQUALS")
class AddProductActivity : BaseActivity(), View.OnClickListener {

    // A global variable for URI of a selected image from phone storage.
    private var mSelectedImageFileUri: Uri? = null
    // A global variable for uploaded product image URL.
    private var mProductImageURL: String = ""
    private var mTypeProduct : String = ""
    private var mSku : String = ""
    private var mNameStore : String = ""
    private var mHasDelivery : String = ""

    private lateinit var sharedTypeProduct : SharedPreferences
    lateinit var sharedSku : SharedPreferences
    lateinit var sharedNameStore : SharedPreferences
    lateinit var sharedHasDelivery : SharedPreferences


    var path = "https://firebasestorage.googleapis.com/v0/b" +
            "/gestor-de-pedidos-aukdefood.appspot.com/o" +
            "/fotoDefault.jpg?alt=media&token=f74486bf-432e-4af6-b114-baa523e1f801"


    lateinit var notificationProvider: NotificationProvider


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        sharedTypeProduct = getSharedPreferences(Constants.EXTRA_USER_TYPE_PRODUCT, MODE_PRIVATE)
        sharedSku = getSharedPreferences(Constants.SKU, MODE_PRIVATE)
        sharedNameStore = getSharedPreferences(Constants.NAME_STORE, MODE_PRIVATE)
        sharedHasDelivery = getSharedPreferences(Constants.HAS_DELIVERY, MODE_PRIVATE)


        mTypeProduct = sharedTypeProduct.getString(Constants.EXTRA_USER_TYPE_PRODUCT, "").toString()
        mNameStore = sharedNameStore.getString(Constants.NAME_STORE,"").toString()
        mSku = sharedSku.getString(Constants.SKU, "").toString()
        mHasDelivery = sharedHasDelivery.getString(Constants.HAS_DELIVERY,"").toString()

        notificationProvider = NotificationProvider()
       //
        setupActionBar()
        // Assign the click event to iv_add_update_product image.
        iv_add_update_product.setOnClickListener(this)

        // Assign the click event to submit button.
        btn_submit.setOnClickListener(this)

        et_product_sku.prefix = mSku
        et_product_sku.setHintTextColor(Color.parseColor("#FF86CA"))

    }

    override fun onClick(v: View?) {

        if (v != null) {
            when (v.id) {

                // The permission code is similar to the user profile image selection.
                R.id.iv_add_update_product -> {
                    if (ContextCompat.checkSelfPermission(
                                    this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            == PackageManager.PERMISSION_GRANTED
                    ) {
                        Constants.showImageChooser(this@AddProductActivity)
                    } else {
                        /*Requests permissions to be granted to this application. These permissions
                         must be requested in your manifest, they should not be granted to your app,
                         and they should have protection level*/
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }

                R.id.btn_submit -> {
                    if (validateProductDetails()) {

                        uploadProductImage()
                    }
                }
            }
        }
    }

    /**
     * This function will identify the result of runtime permission after the user allows or deny permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@AddProductActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                        this,
                        resources.getString(R.string.read_storage_permission_denied),
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {

            // Replace the add icon with edit icon once the image is selected.
            iv_add_update_product.setImageDrawable(
                    ContextCompat.getDrawable(
                            this@AddProductActivity,
                            R.drawable.ic_vector_edit
                    )
            )

            // The uri of selection image from phone storage.
            mSelectedImageFileUri = data.data!!

            try {
                // Load the product image in the ImageView.
                GlideLoader(this@AddProductActivity).loadProductPicture(
                        mSelectedImageFileUri!!,
                        iv_product_image
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to validate the product details.
     */
    private fun validateProductDetails(): Boolean {
        return when {

            mSelectedImageFileUri == null -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }

            TextUtils.isEmpty(et_product_title.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }

            TextUtils.isEmpty(et_product_sku.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_sku), true)
                false
            }

            TextUtils.isEmpty(et_product_price.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(et_product_description.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_product_description),
                        true
                )
                false
            }

            TextUtils.isEmpty(et_product_quantity.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_product_quantity),
                        true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to upload the selected product image to firebase cloud storage.
     */
    private fun uploadProductImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().uploadImageToCloudStorage(
                this@AddProductActivity,
                mSelectedImageFileUri,
                Constants.PRODUCT_IMAGE
        )
    }

    /**
     * A function to get the successful result of product image upload.
     */
    fun imageUploadSuccess(imageURL: String) {

        // Initialize the global image url variable.
        mProductImageURL = imageURL

        uploadProductDetails()
    }

    private fun uploadProductDetails() {
        val id_user = FirestoreClass().getCurrentUserID()
        val sku_code = mSku+et_product_sku.text.toString()
        // Get the logged in username from the SharedPreferences that we have stored at a time of login.
        val username =
            this.getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
                .getString(Constants.LOGGED_IN_USERNAME, "")!!

        // Here we get the text from editText and trim the space
        val product = Product(
                id_user,
                id_user,
                username,
                et_product_title.text.toString().trim { it <= ' ' },
                et_product_price.text.toString().trim { it <= ' ' },
                et_product_description.text.toString().trim { it <= ' ' },
                et_product_quantity.text.toString().trim { it <= ' ' },
                mProductImageURL,
                mTypeProduct,
                "",
                sku_code,
                mNameStore,
                mHasDelivery
        )

        FirestoreClass().searchSKUAndUpload(this@AddProductActivity, product , sku_code )
    }

    /**
     * A function to return the successful result of Product upload.
     */
    fun productUploadSuccess() {

        // Hide the progress dialog
        hideProgressDialog()

        Toast.makeText(
                this@AddProductActivity,
                resources.getString(R.string.product_uploaded_success_message),
                Toast.LENGTH_SHORT
        ).show()

        finish()
    }
}
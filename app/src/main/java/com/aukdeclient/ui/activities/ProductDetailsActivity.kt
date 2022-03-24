package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.aukdeshop.R
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Cart
import com.aukdeclient.models.Product
import com.aukdeclient.models.WishList
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.GlideLoader
import com.aukdeclient.utils.TinyDB
import com.aukdeclient.utils.VerifyActualDay
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.item_cart_layout.view.*
import kotlinx.android.synthetic.main.item_list_layout.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Product Details Screen.
 */
class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mProductDetails: Product

    // A global variable for product id.
    private var mProductId: String = ""

    private var typeMoney : String = ""

    private var mProviderId: String = ""

    private var mImageResize: String = ""

    private var existCategory : Boolean = false

    var successToShop = Constants.ACCEPT_TO_SHOP

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.semiWhite)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        // This is used to align the xml view to this class

        typeMoney = resources.getString(R.string.type_money)
        setContentView(R.layout.activity_product_details)

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            mProductId =
                intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }

        var productOwnerId: String = ""

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            productOwnerId =
                intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }


        if (FirestoreClass().getCurrentUserID() == productOwnerId) {
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
            btn_delete_favorite.visibility= View.GONE
        } else {
            btn_add_to_cart.visibility = View.GONE
            btn_add_favorite.visibility= View.VISIBLE
        }

        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)
        btn_add_favorite.setOnClickListener(this)
        btn_delete_favorite.setOnClickListener(this)

        val db : ArrayList<Product> = TinyDB(this).getListProduct(Constants.CACHE_PRODUCT,Product::class.java)
        if (db.isEmpty()){
            getProductDetails()
        }
        else{
            getProductDetailsFromCache()
        }

        FirestoreClass().verifyCategoryInCart(mProductDetails.type_product,mProductDetails.name_store,this)
    }

    override fun onClick(v: View?) {

        if (v != null) {
            when (v.id) {

                R.id.btn_add_to_cart -> {
                    if (successToShop != Constants.BLOCK_TO_SHOP){
                        if (existCategory){
                        addToCart()
                    }else{
                        val dialog = Dialog(this)
                        dialog.setContentView(R.layout.dialog_no_add_product)
                        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
                        dialog.window!!.attributes.windowAnimations = R.style.DialogScale
                        dialog.setCancelable(false)
                        dialog.show()

                        val btnOk = dialog.findViewById(R.id.closeDialog) as Button
                        btnOk.setOnClickListener {
                            dialog.dismiss()
                        }
                     }
                    }else{
                        Toast.makeText(this,"El negocio ya cerró!",Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.btn_go_to_cart -> {
                    startActivity(Intent(this@ProductDetailsActivity, CartListActivity::class.java))
                }
                R.id.btn_add_favorite->{
                    addToFavorite()
                }
                R.id.btn_delete_favorite->{
                    deleteToFavorite()
                }
            }
        }
    }

    fun getCategoryFromCart(value : Boolean){
        existCategory = value
    }

    private fun deleteToFavorite(){
        //val model = list.get(6)
        showProgressDialog("Espere...")
        FirestoreClass().removeItemFromWishList(this@ProductDetailsActivity,mProductDetails.title)
    }

    private fun addToFavorite(){
        val addToFavorites= WishList(
            FirestoreClass().getCurrentUserID(),
            mProductDetails.provider_id,
            mProductDetails.user_name,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.description,
            mProductDetails.stock_quantity,
            mProductDetails.image,
            mProductDetails.type_product,
            mProductId,
            mProductDetails.sku,
            mProductDetails.name_store,
            mProductDetails.delivery,"")
        FirestoreClass().addWishListItems(this@ProductDetailsActivity,addToFavorites)
    }

    /**
     * A function to prepare the cart item to add it to the cart in cloud firestore.
     */


    private fun addToCart() {

        val addToCart = Cart(
                FirestoreClass().getCurrentUserID(),
                mProviderId,
                mProductId,
                mProductDetails.title,
                mProductDetails.price,
                mProductDetails.image,
                Constants.DEFAULT_CART_QUANTITY,
                "",
                "",
                0,
                mProductDetails.name_store,
                mProductDetails.delivery,
                mProductDetails.type_product
        )
        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mProductDetails.delivery == "no"){
            FirestoreClass().addCartItemsForLocation(mProviderId)
            FirestoreClass().addCartItems(this@ProductDetailsActivity, addToCart)
        }
        else{
            FirestoreClass().addCartItems(this@ProductDetailsActivity, addToCart)
        }

    }

    /**
     * A function for actionBar Setup.
     */


    /**
     * A function to call the firestore class function that will get the product details from cloud firestore based on the product id.
     */
    private fun getProductDetails() {
        getWishListDetails()
            // Show the product dialog
            showProgressDialog(resources.getString(R.string.please_wait))
            // Call the function of FirestoreClass to get the product details.
            FirestoreClass().getProductDetails(this@ProductDetailsActivity, mProductId)

    }


    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun getProductDetailsFromCache() {
        getWishListDetailsFromCache()
        val db : ArrayList<Product> = TinyDB(this).getListProduct(Constants.CACHE_PRODUCT,Product::class.java)
        for (i in  db){
            if (i.product_id == mProductId){
                mProductDetails = i
                val path: String = this.getExternalFilesDir(null).toString()+"/"+mProductDetails.product_id+".jpg"
                val file = File(path)
                val imageUri: Uri = Uri.fromFile(file)
                if(!file.exists()){
                    GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                            mProductDetails.image,
                            iv_product_detail_image
                    )
                }else{
                    GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                            imageUri,
                            iv_product_detail_image
                    )
                }

                if (mProductDetails.hours.isNotEmpty()){
                    val sdf = SimpleDateFormat("EEEE")
                    val sdf2 = SimpleDateFormat("HH:mm", Locale.UK)
                    val currentDate = sdf2.format(Date())
                    val actual : Date = sdf2.parse(currentDate)!!
                    val d = Date()
                    val dayOfTheWeek: String = sdf.format(d)
                    tv_linear_hours.visibility = View.VISIBLE
                    txt_hours_details.text = VerifyActualDay().retreiveHours(dayOfTheWeek,mProductDetails.hours)

                    val hr = VerifyActualDay().retreiveHours(dayOfTheWeek,mProductDetails.hours).split(" - ")
                    val d1 : Date = sdf2.parse(hr[0])!!
                    val d2 : Date = sdf2.parse(hr[1])!!

                    successToShop = if(actual > d1 && actual < d2){
                        Constants.ACCEPT_TO_SHOP
                    }else{
                        Constants.BLOCK_TO_SHOP
                    }


                }

                tv_product_details_title.text = mProductDetails.title
                tv_product_details_sku.text = "Sku : "+mProductDetails.sku
                tv_product_details_price.text = typeMoney+mProductDetails.price
                tv_product_details_description.text = mProductDetails.description
                tv_product_details_stock_quantity.text = mProductDetails.stock_quantity
                if (mProductDetails.delivery == "si"){
                    tv_product_details_delivery.text = Constants.INCLUDE_SHIPPING
                    tv_product_details_delivery.setTextColor(Color.parseColor("#5BBD00"))
                }
                else {
                    tv_product_details_delivery.text =  Constants.NO_INCLUDE_SHIPPING
                    tv_product_details_delivery.setTextColor(Color.parseColor("#A60000"))
                }

                mProviderId = mProductDetails.provider_id
                mImageResize = mProductDetails.image
                setupDialog(mImageResize,mProductDetails.product_id)
                FirestoreClass().getStatusPackageProvider(this,mProductDetails.provider_id)

                if(mProductDetails.stock_quantity.toInt() == 0){

                    btn_add_to_cart.visibility = View.GONE

                    tv_product_details_stock_quantity.text =
                            resources.getString(R.string.lbl_out_of_stock)

                    tv_product_details_stock_quantity.setTextColor(
                            ContextCompat.getColor(
                                    this@ProductDetailsActivity,
                                    R.color.colorSnackBarError
                            )
                    )
                }else{
                    // There is no need to check the cart list if the product owner himself is seeing the product details.
                    if (FirestoreClass().getCurrentUserID() == mProductDetails.user_id) {
                        // Hide Progress dialog.
                    } else {
                        FirestoreClass().checkIfItemExistInCartFromCache(this@ProductDetailsActivity, mProductId)
                    }
                }
            }
        }
    }

    fun productExistsInFavorite(){
        hideProgressDialog()
        btn_add_favorite.visibility= View.GONE
        btn_delete_favorite.visibility= View.VISIBLE
    }

    fun productExistsInFavoriteFromCache(){
        btn_add_favorite.visibility= View.GONE
        btn_delete_favorite.visibility= View.VISIBLE
    }

    private fun getWishListDetails(){
        FirestoreClass().getWishListDetails(this@ProductDetailsActivity, mProductId)
    }

    private fun getWishListDetailsFromCache(){
        FirestoreClass().getWishListDetailsFromCache(this@ProductDetailsActivity, mProductId)
    }


    fun itemRemovedFavoriteSuccess() {
        hideProgressDialog()
        btn_add_favorite.visibility = View.VISIBLE
        btn_delete_favorite.visibility = View.GONE
        Toast.makeText(this@ProductDetailsActivity, "Favoritos removido con exito", Toast.LENGTH_SHORT).show()
    }

    /**
     * A function to notify the success result of the product details based on the product id.
     *
     * @param favorite A model class with product details.
     */
    @SuppressLint("SetTextI18n")
    fun favoriteDetailsSuccess(product: WishList){
        if (FirestoreClass().getCurrentUserID() == product.user_id) {
            // Hide Progress dialog.
            hideProgressDialog()
        } else {
            FirestoreClass().checkIfItemExistInWishList(this@ProductDetailsActivity, mProductId)
        }

    }

    fun favoriteDetailsSuccessFromCache(product: WishList){
        FirestoreClass().checkIfItemExistInWishListFromCache(this@ProductDetailsActivity, mProductId)
    }

    /**
     * A function to notify the success result of the product details based on the product id.
     *
     * @param product A model class with product details.
     */
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    fun productDetailsSuccess(product: Product) {

        mProductDetails = product
        // Populate the product details in the UI.

        GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                product.image,
                iv_product_detail_image
        )

        if (product.hours.isNotEmpty()){
            val sdf = SimpleDateFormat("EEEE")
            val sdf2 = SimpleDateFormat("HH:mm", Locale.UK)
            val currentDate = sdf2.format(Date())
            val actual : Date = sdf2.parse(currentDate)!!
            val d = Date()
            val dayOfTheWeek: String = sdf.format(d)
            tv_linear_hours.visibility = View.VISIBLE
            txt_hours_details.text = VerifyActualDay().retreiveHours(dayOfTheWeek,product.hours)

            val hr = VerifyActualDay().retreiveHours(dayOfTheWeek,product.hours).split(" - ")
            val d1 : Date = sdf2.parse(hr[0])!!
            val d2 : Date = sdf2.parse(hr[1])!!

            successToShop = if(actual > d1 && actual < d2){
                Constants.ACCEPT_TO_SHOP
            }else{
                Constants.BLOCK_TO_SHOP
            }
        }

        tv_product_details_title.text = product.title
        tv_product_details_sku.text = "Sku : "+product.sku
        tv_product_details_price.text = typeMoney+product.price
        tv_product_details_description.text = product.description
        tv_product_details_stock_quantity.text = product.stock_quantity
        if (product.delivery == "si"){
            tv_product_details_delivery.text = Constants.INCLUDE_SHIPPING
            tv_product_details_delivery.setTextColor(Color.parseColor("#5BBD00"))
        }
        else {
            tv_product_details_delivery.text =  Constants.NO_INCLUDE_SHIPPING
            tv_product_details_delivery.setTextColor(Color.parseColor("#A60000"))
        }

        // setup dialog

        mProviderId = product.provider_id
        mImageResize = product.image

        setupDialog(mImageResize,mProductDetails.product_id)

        FirestoreClass().getStatusPackageProvider(this,product.provider_id)

        if(product.stock_quantity.toInt() == 0){

            // Hide Progress dialog.
            hideProgressDialog()

            // Hide the AddToCart button if the item is already in the cart.
            btn_add_to_cart.visibility = View.GONE

            tv_product_details_stock_quantity.text =
                resources.getString(R.string.lbl_out_of_stock)

            tv_product_details_stock_quantity.setTextColor(
                    ContextCompat.getColor(
                            this@ProductDetailsActivity,
                            R.color.colorSnackBarError
                    )
            )
        }else{

            // There is no need to check the cart list if the product owner himself is seeing the product details.
            if (FirestoreClass().getCurrentUserID() == product.user_id) {
                // Hide Progress dialog.
                hideProgressDialog()
            } else {
                FirestoreClass().checkIfItemExistInCart(this@ProductDetailsActivity, mProductId)
            }
        }
    }

    /**
     * A function to notify the success result of item exists in the cart.
     */
    fun productExistsInCart() {
        // Hide the progress dialog.
        hideProgressDialog()
        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productNotExistsInCart() {

        // Hide the progress dialog.
        hideProgressDialog()
        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.VISIBLE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_go_to_cart.visibility = View.GONE
    }

    fun productExistsInCartFromCache() {
        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productNotExistsInCartFromCache() {
        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.VISIBLE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_go_to_cart.visibility = View.GONE
    }

    /**
     * A function to notify the success result of item added to the to cart.
     */
    fun addToCartSuccess() {
        // Hide the progress dialog.
        hideProgressDialog()

        Toast.makeText(
                this@ProductDetailsActivity,
                resources.getString(R.string.success_message_item_added_to_cart),
                Toast.LENGTH_SHORT
        ).show()

        // Hide the AddToCart button if the item is already in the cart.
        btn_add_to_cart.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_go_to_cart.visibility = View.VISIBLE
    }

    private fun setupDialog(url : String,id : String){

        val path: String = getExternalFilesDir(null).toString()+"/"+id+".jpg"
        val file = File(path)
        val imageUri: Uri = Uri.fromFile(file)
        val mDialogImage = Dialog(this)

        mDialogImage.setContentView(R.layout.image_dialog)
        mDialogImage.window?.setBackgroundDrawable(ColorDrawable(0))
        mDialogImage.window!!.attributes.windowAnimations = R.style.DialogScale
        val resizeImage = mDialogImage.findViewById(R.id.dialog_image) as PhotoView
        val closeDialog = mDialogImage.findViewById(R.id.close_image) as ImageView

        if (file.exists()){
            GlideLoader(this).loadProductPicture(
                imageUri,
                resizeImage
            )
        }
        else{
            GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                url,
                resizeImage
            )
        }

        iv_product_detail_image.setOnClickListener {
            mDialogImage.show()
            closeDialog.setOnClickListener {
                mDialogImage.dismiss()
            }
        }
    }

    fun addToFavoriteSuccess(){
        // Hide the progress dialog.
        //hideProgressDialog()
        Toast.makeText(this@ProductDetailsActivity,"Producto Agregado como favorito!.", Toast.LENGTH_SHORT).show()
        // Hide the AddToCart button if the item is already in the cart.
        btn_add_favorite.visibility = View.GONE
        // Show the GoToCart button if the item is already in the cart. User can update the quantity from the cart list screen if he wants.
        btn_delete_favorite.visibility = View.VISIBLE
    }

    override fun onResume() {
        FirestoreClass().checkIfItemExistInCartFromCache(this,mProductId)
        super.onResume()
    }

}
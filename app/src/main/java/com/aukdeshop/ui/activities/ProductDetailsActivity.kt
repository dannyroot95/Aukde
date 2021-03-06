package com.aukdeshop.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Cart
import com.aukdeshop.models.Product
import com.aukdeshop.models.WishList
import com.aukdeshop.utils.Constants
import com.aukdeshop.utils.GlideLoader
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.item_cart_layout.view.*
import java.util.*
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

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
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
            btn_add_to_cart.visibility = View.VISIBLE
            btn_add_favorite.visibility= View.VISIBLE
        }



        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)
        btn_add_favorite.setOnClickListener(this)
        btn_delete_favorite.setOnClickListener(this)

        getProductDetails()
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.btn_add_to_cart -> {
                    addToCart()
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

    private fun deleteToFavorite(){
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
                mProductDetails.delivery
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


    fun productExistsInFavorite(){
        hideProgressDialog()
        btn_add_favorite.visibility= View.GONE
        btn_delete_favorite.visibility= View.VISIBLE
    }

    private fun getWishListDetails(){
        FirestoreClass().getWishListDetails(this@ProductDetailsActivity, mProductId)
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

    /**
     * A function to notify the success result of the product details based on the product id.
     *
     * @param product A model class with product details.
     */
    @SuppressLint("SetTextI18n")
    fun productDetailsSuccess(product: Product) {
        mProductDetails = product

        // Populate the product details in the UI.

        GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                product.image,
                iv_product_detail_image
        )

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

        setupDialog(mImageResize)

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


    private fun setupDialog(url : String){
        val mDialogImage = Dialog(this)

        mDialogImage.setContentView(R.layout.image_dialog)
        mDialogImage.window?.setBackgroundDrawable(ColorDrawable(0))
        mDialogImage.window!!.attributes.windowAnimations = R.style.DialogScale
        val resizeImage = mDialogImage.findViewById(R.id.dialog_image) as ImageView

        GlideLoader(this@ProductDetailsActivity).loadProductPicture(
                url,
                resizeImage
        )

        iv_product_detail_image.setOnClickListener {
            mDialogImage.show()
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

}
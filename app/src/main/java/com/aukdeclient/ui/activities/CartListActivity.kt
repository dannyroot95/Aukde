package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Cart
import com.aukdeclient.models.Product
import com.aukdeclient.ui.adapters.CartItemsListAdapter
import com.aukdeclient.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.text.DecimalFormat

/**
 * Cart list activity of the application.
 */
class CartListActivity : BaseActivity() {

    // A global variable for the product list.
    private lateinit var mProductsList: ArrayList<Product>

    // A global variable for the cart list items.
    private lateinit var mCartListItems: ArrayList<Cart>
    private var typeMoney : String = ""
    private var shipping : Double? = null
    var df = DecimalFormat("#.00")

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        typeMoney = resources.getString(R.string.type_money)
        shipping = 0.00
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_cart_list)

        setupActionBar()

        btn_checkout.setOnClickListener {
            val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CartListActivity)
    }

    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        mProductsList = productsList

        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CartListActivity)
    }

    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    @SuppressLint("SetTextI18n")
    fun successCartItemsList(cartList: ArrayList<Cart>) {

        // Hide progress dialog.
        hideProgressDialog()

        for (product in mProductsList) {
            for (cart in cartList) {
                if (product.product_id == cart.product_id) {

                    cart.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cart.cart_quantity = product.stock_quantity
                    }
                }
            }
        }

        mCartListItems = cartList

        if (mCartListItems.size > 0) {

            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, mCartListItems, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal = 0.00

            for (item in mCartListItems) {

                val availableQuantity = item.stock_quantity.toInt()

                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()

                    subTotal += (price * quantity)
                }
                //////////////////////////////
                if (item.delivery == "si"){
                    tv_shipping_charge.text = typeMoney+shipping!!.toString()
                }
                else{
                    tv_shipping_charge.text = typeMoney+(shipping!!+5.00).toString()
                }

            }

            tv_sub_total.text = typeMoney+df.format(subTotal)
            // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.

            //tv_shipping_charge.text = typeMoney+"10.0"
            setupShipping()


            if (subTotal > 0) {
                ll_checkout.visibility = View.VISIBLE

                val total = subTotal + shipping!!
                tv_total_amount.text = typeMoney+df.format(total)
            } else {
                ll_checkout.visibility = View.GONE
            }

        } else {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    /**
     * A function to notify the user about the item removed from the cart list.
     */
    fun itemRemovedSuccess() {

        hideProgressDialog()

        Toast.makeText(
                this@CartListActivity,
                resources.getString(R.string.msg_item_removed_successfully),
                Toast.LENGTH_SHORT
        ).show()

        getCartItemsList()
    }

    /**
     * A function to notify the user about the item quantity updated in the cart list.
     */
    fun itemUpdateSuccess() {

        hideProgressDialog()

        getCartItemsList()
    }

    @SuppressLint("SetTextI18n")
    private fun setupShipping(){

        var noDelivery = 0
        var hasDelivery = 0
        shipping = 0.00

        for(item in 0 until mCartListItems.size){
            if (mCartListItems[item].delivery == "no"){
                hasDelivery++
                if(hasDelivery == 1){
                    shipping = 5.00
                }
                else{
                    shipping = shipping!! + 0.50
                }
            }
            else if (mCartListItems[item].delivery == "si"){
                noDelivery++
                shipping = shipping!! + 0.00
            }
        }

        if(shipping == 0.00){
            tv_shipping_charge.text = Constants.FREE_SHIPPING
        }
        else{
            tv_shipping_charge.text = typeMoney+df.format(shipping)
        }

    }

}
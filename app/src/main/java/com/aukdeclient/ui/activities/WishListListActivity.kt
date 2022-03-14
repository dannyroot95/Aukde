package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.WishList
import com.aukdeclient.ui.adapters.WishListItemsAdapter
import kotlinx.android.synthetic.main.activity_wish_list_list.*

class WishListListActivity : BaseActivity() {
    // A global variable for the product list.
    private lateinit var mProductsList: ArrayList<WishList>
    private lateinit var mFavoriteListItems: ArrayList<WishList>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list_list)
    }
    override fun onResume() {
        super.onResume()
        getProductListFavorite()
    }
    private fun getProductListFavorite(){
        showProgressDialog("Espere...")
        FirestoreClass().getAllWishList(this@WishListListActivity)
    }
    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successFavoriteListFromFireStore(productsList: ArrayList<WishList>){
        mProductsList = productsList
        getFavoriteItemsList()

    }
    private fun getFavoriteItemsList(){
        FirestoreClass().getWishList(this@WishListListActivity)
    }
    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param favoriteList
     */
    @SuppressLint("SetTextI18n")
    fun successFavoriteItemsList(favoriteList: ArrayList<WishList>){
        // Hide progress dialog.
        hideProgressDialog()
        mFavoriteListItems = favoriteList
        if(mFavoriteListItems.size > 0){
            rv_favorite_items_list.visibility = View.VISIBLE
            tv_no_favorite_item_found.visibility= View.GONE

            rv_favorite_items_list.layoutManager = LinearLayoutManager(this@WishListListActivity)
            rv_favorite_items_list.setHasFixedSize(true)

            val favoriteListAdapter= WishListItemsAdapter(this@WishListListActivity, mFavoriteListItems, true)
            rv_favorite_items_list.adapter=favoriteListAdapter



        }
        else{
            rv_favorite_items_list.visibility = View.GONE
            tv_no_favorite_item_found.visibility= View.VISIBLE
        }
    }

    fun itemRemovedSuccessFavorite(){
        Toast.makeText(this@WishListListActivity,"Producto eliminado!", Toast.LENGTH_SHORT).show()
        getFavoriteItemsList()
    }
    /**
     * A function to notify the user about the item quantity updated in the favorite list.
     */

}

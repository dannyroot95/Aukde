package com.aukdeclient.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.WishList
import com.aukdeclient.ui.activities.ProductDetailsActivity
import com.aukdeclient.ui.activities.WishListListActivity
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.GlideLoader
import kotlinx.android.synthetic.main.item_wishlist_layout.view.*

class WishListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<WishList>,
    private val updateFavoriteItems: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_wishlist_layout,
                parent,
                false
            )
        )
    }
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if(holder is MyViewHolder){
            GlideLoader(context).loadProductPicture(model.image, holder.itemView.image_Product_Favorite)
            holder.itemView.tv_favorite_item_title.text=model.title
            holder.itemView.tv_favorite_item_price.text = context.resources.getString(R.string.type_money)+model.price
            if(updateFavoriteItems){
                holder.itemView.delete_favorite_item.visibility = View.VISIBLE
            }
            else{
                holder.itemView.delete_favorite_item.visibility = View.GONE
            }
            holder.itemView.delete_favorite_item.setOnClickListener {
                when(context){
                    is WishListListActivity ->{
                        context.showProgressDialog("Espere...")
                    }
                }
                FirestoreClass().removeItemFromWishListAdapter(context, model.key)
            }

            holder.itemView.cardview_wishlist.setOnClickListener {
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, model.product_id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, model.provider_id)
                context.startActivity(intent)
            }

        }
    }


    override fun getItemCount(): Int {
        return list.size
    }


    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
package com.aukdeshop.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Cart
import com.aukdeshop.ui.activities.CartListActivity
import com.aukdeshop.ui.activities.MyOrderDetailsActivity
import com.aukdeshop.utils.Constants
import com.aukdeshop.utils.GlideLoader
import kotlinx.android.synthetic.main.item_cart_layout.view.*

/**
 * A adapter class for dashboard items list.
 */
open class CartItemsListAdapter(
    private val context: Context,
    private var list: ArrayList<Cart>,
    private val updateCartItems: Boolean,


) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_cart_layout,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {

            GlideLoader(context).loadProductPicture(model.image, holder.itemView.iv_cart_item_image)
            holder.itemView.tv_cart_item_title.text = model.title
            holder.itemView.tv_cart_item_price.text = context.resources.getString(R.string.type_money)+model.price
            holder.itemView.tv_cart_quantity.text = model.cart_quantity

            if (model.cart_quantity == "0") {
                holder.itemView.ib_remove_cart_item.visibility = View.GONE
                holder.itemView.ib_add_cart_item.visibility = View.GONE

                if (updateCartItems) {
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                } else {
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }

                holder.itemView.tv_cart_quantity.text =
                    context.resources.getString(R.string.lbl_out_of_stock)

                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSnackBarError
                    )
                )
            } else {

                if (updateCartItems) {
                    holder.itemView.ib_remove_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_add_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                } else {

                    holder.itemView.ib_remove_cart_item.visibility = View.GONE
                    holder.itemView.ib_add_cart_item.visibility = View.GONE
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }

                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )
            }



            holder.itemView.ib_remove_cart_item.setOnClickListener {

                if (model.cart_quantity == "1") {
                    FirestoreClass().removeItemFromCart(context, model.id)
                } else {

                    val cartQuantity: Int = model.cart_quantity.toInt()

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                    // Show the progress dialog.

                    if (context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                }
            }

            holder.itemView.ib_add_cart_item.setOnClickListener {

                val cartQuantity: Int = model.cart_quantity.toInt()

                if (cartQuantity < model.stock_quantity.toInt()) {

                    val itemHashMap = HashMap<String, Any>()

                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    // Show the progress dialog.
                    if (context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                } else {
                    if (context is CartListActivity) {
                        context.showErrorSnackBar(
                            context.resources.getString(
                                R.string.msg_for_available_stock,
                                model.stock_quantity
                            ),
                            true
                        )
                    }
                }
            }

            holder.itemView.ib_delete_cart_item.setOnClickListener {

                when (context) {
                    is CartListActivity -> {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }
                }

                FirestoreClass().removeItemFromCart(context, model.id)
            }

            if (context is MyOrderDetailsActivity){

                when (model.status) {

                    -1 -> {
                        holder.itemView.tv_cart_status.visibility = View.VISIBLE
                        holder.itemView.tv_cart_status.text = Constants.CANCELLED
                        holder.itemView.tv_cart_status.setTextColor(Color.parseColor("#fc0000"))
                    }

                    0 -> {
                        holder.itemView.tv_cart_status.visibility = View.VISIBLE
                        holder.itemView.tv_cart_status.text = Constants.PENDING
                        holder.itemView.tv_cart_status.setTextColor(Color.parseColor("#fc0000"))
                    }
                    1 -> {
                        holder.itemView.tv_cart_status.visibility = View.VISIBLE
                        holder.itemView.tv_cart_status.text = Constants.PROCESSING
                        holder.itemView.tv_cart_status.setTextColor(Color.parseColor("#F1C40F"))
                    }

                    2 -> {
                        holder.itemView.tv_cart_status.visibility = View.VISIBLE
                        holder.itemView.tv_cart_status.text = Constants.IN_ROUTE
                        holder.itemView.tv_cart_status.setTextColor(Color.parseColor("#154360"))
                    }

                    else -> {
                        holder.itemView.tv_cart_status.visibility = View.VISIBLE
                        holder.itemView.tv_cart_status.text = Constants.COMPLETED
                        holder.itemView.tv_cart_status.setTextColor(Color.parseColor("#5bbd00"))
                    }
                }
            }

        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
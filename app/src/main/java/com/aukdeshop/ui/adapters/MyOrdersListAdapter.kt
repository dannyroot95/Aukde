package com.aukdeshop.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aukdeshop.R
import com.aukdeshop.models.Order
import com.aukdeshop.ui.activities.MyOrderDetailsActivity
import com.aukdeshop.utils.Constants
import com.aukdeshop.utils.GlideLoader
import kotlinx.android.synthetic.main.item_list_layout.view.*

open class MyOrdersListAdapter(
    private val context: Context,
    private var list: ArrayList<Order>
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
                R.layout.item_list_layout,
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

            GlideLoader(context).loadProductPicture(
                model.image,
                holder.itemView.iv_item_image
            )

            holder.itemView.tv_item_name.text = model.title
            holder.itemView.tv_item_price.text = context.resources.getString(R.string.type_money)+model.total_amount

            holder.itemView.ib_delete_product.visibility = View.GONE

            when (model.status) {
                -1 -> {
                    holder.itemView.tv_item_status.text = Constants.CANCELLED
                    holder.itemView.tv_item_status.setTextColor(Color.parseColor("#FC0000"))
                }
                0 -> {
                    holder.itemView.tv_item_status.text = Constants.PENDING
                    holder.itemView.tv_item_status.setTextColor(Color.parseColor("#FC0000"))
                }
                1 -> {
                    holder.itemView.tv_item_status.text = Constants.PROCESSING
                    holder.itemView.tv_item_status.setTextColor(Color.parseColor("#F1C40F"))
                }
                2 -> {
                    holder.itemView.tv_item_status.text = Constants.IN_ROUTE
                    holder.itemView.tv_item_status.setTextColor(Color.parseColor("#154360"))
                }
                else -> {
                    holder.itemView.tv_item_status.text = Constants.FINISH_ORDER
                    holder.itemView.tv_item_status.setTextColor(Color.parseColor("#5BBD00"))
                }
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, MyOrderDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_MY_ORDER_DETAILS, model)
                context.startActivity(intent)
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
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
// END
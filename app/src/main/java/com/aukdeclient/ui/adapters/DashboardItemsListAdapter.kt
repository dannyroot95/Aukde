package com.aukdeclient.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aukdeshop.R
import com.aukdeclient.models.Product
import com.aukdeclient.utils.GlideLoader
import kotlinx.android.synthetic.main.item_dashboard_layout.view.*
import java.io.File

/**
 * A adapter class for dashboard items list.
 */
open class DashboardItemsListAdapter(
        private val context: Context,
        private var list: ArrayList<Product>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // A global variable for OnClickListener interface.
    private var onClickListener: OnClickListener? = null
    /**
     * Inflates the item views which is designed in xml layout file
     *
     * create a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_dashboard_layout,
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

            val path: String = context.getExternalFilesDir(null).toString()+"/"+model.product_id+".jpg"
            val file = File(path)
            val imageUri: Uri = Uri.fromFile(file)

            if (file.exists()){
                GlideLoader(context).loadProductPicture(
                        imageUri,
                        holder.itemView.iv_dashboard_item_image
                )
            }
            else{
                GlideLoader(context).loadProductPicture(
                        model.image,
                        holder.itemView.iv_dashboard_item_image
                )
            }

            holder.itemView.tv_dashboard_item_title.text = model.title
            holder.itemView.tv_dashboard_item_store.text = model.name_store
            holder.itemView.tv_dashboard_item_category.text = model.type_product
            holder.itemView.tv_dashboard_item_price.text = context.resources.getString(R.string.type_money)+model.price

            holder.itemView.card_item.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
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
     * A function for OnClickListener where the Interface is the expected parameter and assigned to the global variable.
     *
     * @param onClickListener
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {

        fun onClick(position: Int, product: Product)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
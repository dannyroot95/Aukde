package com.aukdeclient.ui.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aukdeshop.R
import com.aukdeclient.models.Categories
import com.aukdeclient.utils.GlideLoader
import kotlinx.android.synthetic.main.item_category_layout.view.*
import kotlinx.android.synthetic.main.item_category_layout.view.card_item
import java.io.File


class CategoryAdapter(private val context: Context,
                      private var list: ArrayList<Categories>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: CategoryAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_category_layout,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        val path: String = context.getExternalFilesDir(null).toString()+"/categories/"+model.id+".jpg"
        val file = File(path)
        val imageUri: Uri = Uri.fromFile(file)

        if (holder is MyViewHolder) {

            if (file.exists()){
                GlideLoader(context).loadProductPicture(imageUri, holder.itemView.iv_category_item_image)
            }else{
                GlideLoader(context).loadProductPicture(model.foto, holder.itemView.iv_category_item_image)
            }
            
            holder.itemView.tv_item_title_category.text = model.type_product
            holder.itemView.card_item.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int, category: Categories)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)}
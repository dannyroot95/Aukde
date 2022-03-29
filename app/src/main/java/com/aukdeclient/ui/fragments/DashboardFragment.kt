package com.aukdeclient.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aukdeshop.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Categories
import com.aukdeclient.models.Product
import com.aukdeclient.models.Slider
import com.aukdeclient.ui.activities.WishListListActivity
import com.aukdeclient.ui.activities.ProductDetailsActivity
import com.aukdeclient.ui.activities.SettingsActivity
import com.aukdeclient.ui.adapters.CategoryAdapter
import com.aukdeclient.ui.adapters.DashboardItemsListAdapter
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.TinyDB
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.menu_dialog.*
import org.imaginativeworld.whynotimagecarousel.CarouselItem
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : BaseFragment() {

    private lateinit var itemList: ArrayList<Product>
    val list = mutableListOf<CarouselItem>()
    var sliderLists: ArrayList<Slider>? = null
    lateinit var carousel : ImageCarousel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)
        sliderLists = ArrayList()
        itemList = ArrayList()
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view  = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val buttonCategory = view.findViewById(R.id.float_button_menu) as FloatingActionButton
        carousel = view.findViewById(R.id.carousel)
        val search = view.findViewById(R.id.search_product) as SearchView
        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.menu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.attributes.windowAnimations = R.style.DialogScale
        val closeDialog = dialog.findViewById(R.id.closeDialog) as ImageView

        val recycler = dialog.findViewById(R.id.rv_category) as RecyclerView
        getAllCategories(recycler,dialog)


        sliderLists = ArrayList()

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String): Boolean {
                searching(text)
                return true
            }
        })

        buttonCategory.setOnClickListener {
            dialog.show()
            closeDialog.setOnClickListener {
                dialog.dismiss()
            }


        }

        list.clear()
        sliderLists!!.clear()
        usingFirebaseDatabase()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }

            R.id.action_wishlist -> {
                startActivity(Intent(activity, WishListListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        list.clear()
        sliderLists!!.clear()
        getDashboardItemsList()
        usingFirebaseDatabase()
        FirestoreClass().getCategories(requireContext())
    }

    override fun onStart() {
        super.onStart()
        list.clear()
        sliderLists!!.clear()
    }

    /**
     * A function to get the dashboard items list from cloud firestore.
     */
    private fun getDashboardItemsList() {
        val ctx = requireContext().applicationContext
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this@DashboardFragment,ctx)
    }

    /*
    private fun getDashboardTypeProductItemsList(type: String) {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardTypeItemsList(this@DashboardFragment, type)
    }*/

    /**
     * A function to get the success result of the dashboard items from cloud firestore.
     *
     * @param dashboardItemsList
     */
    fun successDashboardItemsList(dashboardItemsList: ArrayList<Product>) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (dashboardItemsList.size > 0) {

            rv_dashboard_items.visibility = View.VISIBLE
            tv_no_dashboard_items_found.visibility = View.GONE

            rv_dashboard_items.layoutManager = GridLayoutManager(activity, 2)
            rv_dashboard_items.setHasFixedSize(true)

            val adapter = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
            rv_dashboard_items.adapter = adapter
            itemList = dashboardItemsList

            adapter.setOnClickListener(object :
                DashboardItemsListAdapter.OnClickListener {
                override fun onClick(position: Int, product: Product) {
                    val intent = Intent(context, ProductDetailsActivity::class.java)
                    intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.product_id)
                    intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
                    startActivity(intent)
                }
            })
            // END
        } else {
            rv_dashboard_items.visibility = View.GONE
            tv_no_dashboard_items_found.visibility = View.VISIBLE
        }

    }

    fun searching(text: String){
        val list : ArrayList<Product> = ArrayList<Product>()

        for (newProduct in itemList){
            if (newProduct.title.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                list.add(newProduct)
            }
            else{}
        }
        val adapterX = DashboardItemsListAdapter(requireContext(), list)
        rv_dashboard_items.adapter = adapterX

        adapterX.setOnClickListener(object :
            DashboardItemsListAdapter.OnClickListener {
            override fun onClick(position: Int, product: Product) {
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.product_id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
                startActivity(intent)
            }
        })

    }

    private fun usingFirebaseDatabase() {

        val tinyDB = TinyDB(context)

        if (tinyDB.getListSlider(Constants.CACHE_SLIDER,Slider::class.java).isEmpty()){
            FirebaseDatabase.getInstance().reference.child("Slider")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                list.clear()
                                sliderLists!!.clear()
                                for (snapshot in dataSnapshot.children) {
                                    val model = snapshot.getValue(Slider::class.java)!!
                                    val path: String = context!!.getExternalFilesDir(null).toString()+"/slider/"+model.id+".jpg"
                                    val file = File(path)
                                    sliderLists?.add(model)
                                    if (!file.exists()){
                                        FirestoreClass().getBitmapSliderFromURL(model.url,context!!,model.id)
                                    }
                                }
                                usingFirebaseImages(sliderLists!!)
                            } else {
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(
                                    context, "No hay publicidad", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }else{
            val slider : ArrayList<Slider> = tinyDB.getListSlider(Constants.CACHE_SLIDER,Slider::class.java)
            usingFirebaseImages(slider)
            FirebaseDatabase.getInstance().reference.child("Slider")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                list.clear()
                                sliderLists!!.clear()
                                for (snapshot in dataSnapshot.children) {
                                    val model = snapshot.getValue(Slider::class.java)!!
                                    val path: String? = context!!.getExternalFilesDir(null).toString()+"/slider/"+model.id+".jpg"
                                    val file = File(path!!)
                                    sliderLists?.add(model)
                                    if (!file.exists()){
                                        FirestoreClass().getBitmapSliderFromURL(model.url,context!!,model.id)
                                    }
                                }
                                usingFirebaseImages(sliderLists!!)
                            } else {
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Toast.makeText(
                                    context, "No hay publicidad", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
        }

    }


    private fun usingFirebaseImages(sliderLists: List<Slider>) {

        val path: String = requireContext().getExternalFilesDir(null).toString()
        val completePath = "$path/slider/"
        for (i in sliderLists.indices) {
            //val downloadImageUrl = sliderLists[i].url
            val textDatabase=sliderLists[i].id
            val path: String = requireContext().getExternalFilesDir(null).toString()+"/"+sliderLists[i].id+".jpg"
            val file = File(path)
            var s = "$completePath$textDatabase.jpg"
            if (file.exists()){
                list.add(CarouselItem(s, textDatabase))
            }else{
                s = sliderLists[i].url
                list.add(CarouselItem(s, textDatabase))
            }

            carousel.addData(list)
        }
    }

    private fun getAllCategories(recycler : RecyclerView, dialog : Dialog){
        val tinyDB : TinyDB = TinyDB(context)
        val list : ArrayList<Categories> = tinyDB.getListCategories(Constants.CATEGORIES,Categories::class.java)

        if (list.size > 0){
            recycler.layoutManager = GridLayoutManager(activity, 2)
            recycler.setHasFixedSize(true)
            val adapter = CategoryAdapter(requireActivity(), list)
            recycler.adapter = adapter

            adapter.setOnClickListener(object : CategoryAdapter.OnClickListener{
                override fun onClick(position: Int, category: Categories) {
                    filterByCategory(category.type_product,dialog)
                }
            })
        }
    }

     fun filterByCategory(category : String, dialog : Dialog){
        val db = TinyDB(context)
        val array = db.getListProduct(Constants.CACHE_PRODUCT,Product::class.java)
         // create a new list from products filters
        val list : ArrayList<Product> = ArrayList<Product>()

        for (i in array){
            if (i.type_product == category){
                list.add(i)
            }
        }

        val adapter = DashboardItemsListAdapter(requireContext(), list)
        rv_dashboard_items.adapter = adapter
        adapter.setOnClickListener(object :
             DashboardItemsListAdapter.OnClickListener {
             override fun onClick(position: Int, product: Product) {
                 val intent = Intent(context, ProductDetailsActivity::class.java)
                 intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.product_id)
                 intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
                 startActivity(intent)
             }
         })
        dialog.dismiss()
    }

}


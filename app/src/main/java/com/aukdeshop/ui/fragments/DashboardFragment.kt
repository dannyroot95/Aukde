package com.aukdeshop.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Product
import com.aukdeshop.models.Slider
import com.aukdeshop.ui.activities.ProductDetailsActivity
import com.aukdeshop.ui.activities.SettingsActivity
import com.aukdeshop.ui.activities.WishListListActivity
import com.aukdeshop.ui.adapters.DashboardItemsListAdapter
import com.aukdeshop.utils.Constants
import com.github.clans.fab.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.imaginativeworld.whynotimagecarousel.CarouselItem
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : BaseFragment() {

    lateinit var itemList: ArrayList<Product>
    val list = mutableListOf<CarouselItem>()
    var sliderLists: ArrayList<Slider>? = null
    lateinit var carousel : ImageCarousel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)
        sliderLists = ArrayList()

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view  = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val buttonCategory = view.findViewById(R.id.float_button_menu) as FloatingActionButton
        carousel = view.findViewById(R.id.carousel)
        val listTypeProduct = resources.getStringArray(R.array.type_product)
        val search = view.findViewById(R.id.search_product) as SearchView
        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.menu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.window!!.attributes.windowAnimations = R.style.DialogScale

        val btnRecently = dialog.findViewById(R.id.dialog_recently) as ImageView
        val btnFood = dialog.findViewById(R.id.dialog_food) as ImageView
        val btnCuisine = dialog.findViewById(R.id.dialog_home_and_cuisine) as ImageView
        val btnElectro = dialog.findViewById(R.id.dialog_electric) as ImageView
        val btnSuperMarket = dialog.findViewById(R.id.dialog_supermarket) as ImageView

        val closeDialog = dialog.findViewById(R.id.closeDialog) as ImageView

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

        btnRecently.setOnClickListener{
            getDashboardItemsList()
            dialog.dismiss()
        }

        btnFood.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[0])
            dialog.dismiss()
        }
        btnCuisine.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[1])
            dialog.dismiss()
        }

        btnSuperMarket.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[2])
            dialog.dismiss()
        }

        btnElectro.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[3])
            dialog.dismiss()
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
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this@DashboardFragment)
    }

    private fun getDashboardTypeProductItemsList(type: String) {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardTypeItemsList(this@DashboardFragment, type)
    }

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

            /*
            sc_dashboard.viewTreeObserver.addOnScrollChangedListener(OnScrollChangedListener {
                if (sc_dashboard != null) {
                    if (sc_dashboard.height == 1222 && sc_dashboard.scrollY == 0) {
                        carousel.visibility = View.VISIBLE
                        Toast.makeText(context,sc_dashboard.height.toString() +" - "+ sc_dashboard.scrollY.toString(),Toast.LENGTH_SHORT).show()
                    } else {
                        carousel.visibility = View.GONE
                    }
                }
            })*/

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

        FirebaseDatabase.getInstance().reference.child("Slider")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        list.clear()
                        sliderLists!!.clear()
                        for (snapshot in dataSnapshot.children) {
                            val model = snapshot.getValue(Slider::class.java)!!
                            sliderLists?.add(model)
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

    private fun usingFirebaseImages(sliderLists: List<Slider>) {
        for (i in sliderLists.indices) {
            val downloadImageUrl = sliderLists[i].url
            val textDatabase=sliderLists[i].titulo
            list.add(CarouselItem(downloadImageUrl, textDatabase))
            carousel.addData(list)
        }
    }

}


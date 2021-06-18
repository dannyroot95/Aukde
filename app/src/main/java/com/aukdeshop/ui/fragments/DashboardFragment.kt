package com.aukdeshop.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Product
import com.aukdeshop.ui.activities.CartListActivity
import com.aukdeshop.ui.activities.ProductDetailsActivity
import com.aukdeshop.ui.activities.SettingsActivity
import com.aukdeshop.ui.adapters.DashboardItemsListAdapter
import com.aukdeshop.utils.Constants
import com.github.clans.fab.FloatingActionButton
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import java.util.*
import kotlin.collections.ArrayList


class DashboardFragment : BaseFragment() {

    lateinit var itemList: ArrayList<Product>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // If we want to use the option menu in fragment we need to add it.
        setHasOptionsMenu(true)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //(activity as AppCompatActivity).supportActionBar?.hide()
        val view  = inflater.inflate(R.layout.fragment_dashboard, container, false)
        val btnRecently = view.findViewById(R.id.btnRecently) as CardView
        val btnFood = view.findViewById(R.id.btnFood) as CardView
        val btnCuisine = view.findViewById(R.id.btnCuisine) as CardView
        val btnElectro = view.findViewById(R.id.btnElectro) as CardView
        val btnSuperMarket = view.findViewById(R.id.btnSuperMarket) as CardView
        val buttonCategory = view.findViewById(R.id.float_button_menu) as FloatingActionButton
        val listTypeProduct = resources.getStringArray(R.array.type_product)
        val search = view.findViewById(R.id.search_product) as SearchView
        val dialog = Dialog(view.context)
        dialog.setContentView(R.layout.menu_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        val closeDialog = dialog.findViewById(R.id.closeDialog) as ImageView

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
        }

        btnFood.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[0])
        }
        btnCuisine.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[1])
        }

        btnSuperMarket.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[2])
        }

        btnElectro.setOnClickListener{
            getDashboardTypeProductItemsList(listTypeProduct[3])
        }

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

            R.id.action_cart -> {
                startActivity(Intent(activity, CartListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
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

}
package com.aukdeshop.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
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
import com.aukdeshop.utils.MSPTextView
import kotlinx.android.synthetic.main.fragment_dashboard.*


class DashboardFragment : BaseFragment() {

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
        val textFilter = view.findViewById(R.id.textFilter) as MSPTextView
        val btnRecently = view.findViewById(R.id.btnRecently) as CardView
        val btnFood = view.findViewById(R.id.btnFood) as CardView
        val btnCuisine = view.findViewById(R.id.btnCuisine) as CardView
        val btnElectro = view.findViewById(R.id.btnElectro) as CardView
        val btnSuperMarket = view.findViewById(R.id.btnSuperMarket) as CardView

        val listTypeProduct = resources.getStringArray(R.array.type_product)

        btnRecently.setOnClickListener{
            textFilter.text = "Mas recientes"
            getDashboardItemsList()
        }

        btnFood.setOnClickListener{
            textFilter.text = "Comidas y Bebidas"
            getDashboardTypeProductItemsList(listTypeProduct[0])
        }
        btnCuisine.setOnClickListener{
            textFilter.text = "Hogar y Cocina"
            getDashboardTypeProductItemsList(listTypeProduct[1])
        }
        btnElectro.setOnClickListener{
            textFilter.text = "Electrodomésticos"
            getDashboardTypeProductItemsList(listTypeProduct[2])
        }
        btnSuperMarket.setOnClickListener{
            textFilter.text = "SuperMercado"
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
        textFilter.text = "Mas recientes"
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


}
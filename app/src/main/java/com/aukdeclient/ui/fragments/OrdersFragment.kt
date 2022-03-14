package com.aukdeclient.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.aukdeclient.R
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.Order
import com.aukdeclient.ui.adapters.MyOrdersListAdapter
import kotlinx.android.synthetic.main.fragment_orders.*

/**
 * Order listing fragment.
 */
class OrdersFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onResume() {
        super.onResume()

        getMyOrdersList()
    }

    /**
     * A function to get the list of my orders.
     */
    private fun getMyOrdersList() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getMyOrdersList(this@OrdersFragment)
    }

    /**
     * A function to get the success result of the my order list from cloud firestore.
     *
     * @param ordersList List of my orders.
     */
    fun populateOrdersListInUI(ordersList: ArrayList<Order>) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (ordersList.size > 0) {
            if (rv_my_order_items != null){
                rv_my_order_items.visibility = View.VISIBLE
                tv_no_orders_found.visibility = View.GONE

                rv_my_order_items.layoutManager = LinearLayoutManager(activity)
                rv_my_order_items.setHasFixedSize(true)

                val myOrdersAdapter = MyOrdersListAdapter(requireActivity(), ordersList)
                rv_my_order_items.adapter = myOrdersAdapter
            }
        }
        else {
            if (rv_my_order_items != null){
                rv_my_order_items.visibility = View.GONE
                tv_no_orders_found.visibility = View.VISIBLE
            }
        }
    }
}
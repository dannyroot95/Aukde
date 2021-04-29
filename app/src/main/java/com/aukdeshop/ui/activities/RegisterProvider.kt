package com.aukdeshop.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.cardview.widget.CardView
import com.aukdeshop.R
import com.aukdeshop.firestore.FirestoreClass
import com.aukdeshop.models.Partner
import com.aukdeshop.utils.Constants
import kotlinx.android.synthetic.main.activity_register.btn_register
import kotlinx.android.synthetic.main.activity_register.cb_terms_and_condition
import kotlinx.android.synthetic.main.activity_register.et_confirm_password
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_first_name
import kotlinx.android.synthetic.main.activity_register.et_last_name
import kotlinx.android.synthetic.main.activity_register.et_password
import kotlinx.android.synthetic.main.activity_register.tv_login
import kotlinx.android.synthetic.main.activity_register_provider.*


class RegisterProvider : BaseActivity() {

    //NOTA AGREGAR DIRECCIÓN , DEFERENCIA Y UBICACION EN TIEMPO REAL

    private var mTypeProduct : String = ""
    private lateinit var spinnerProduct : Spinner

    private lateinit var mButtonMale : CardView
    private lateinit var mButtonFemale : CardView
    private lateinit var mLinearMale : LinearLayout
    private lateinit var mLinearFemale : LinearLayout

    private var mGender : String = ""
    private var mDelivery : String = ""
    private var mTypeUser : String = "provider"

    var radioGroup: RadioGroup? = null
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_register_provider)
        supportActionBar?.hide()

        mButtonMale  = findViewById(R.id.btn_male)
        mButtonFemale  = findViewById(R.id.btn_female)
        mLinearMale = findViewById(R.id.linear_male)
        mLinearFemale = findViewById(R.id.linear_female)

        spinnerProduct = findViewById(R.id.spinner_type_product)
        radioGroup = findViewById(R.id.radio_group)

        // This is used to hide the status bar and make the splash screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.

        defineGender()

        val adapterSpinner = ArrayAdapter.createFromResource(
                this,
                R.array.type_product,
                R.layout.support_simple_spinner_dropdown_item
        )
        spinnerProduct.adapter = adapterSpinner
        spinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                mTypeProduct = parent!!.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        radioGroup?.setOnCheckedChangeListener { _, checkedId ->
            mDelivery = if (checkedId == R.id.rdb_yes) {
                "Si"
            } else{
                "No"
            }
        }

        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        btn_register.setOnClickListener {
            registerUser()
        }

        // START
        tv_login.setOnClickListener{
            // Here when the user click on login text we can either call the login activity or call the onBackPressed function.
            // We will call the onBackPressed function.
            onBackPressed()
        }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {

            mGender == "" ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_gender), true)
                false
            }

            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(et_dni.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_dni), true)
                false
            }

            TextUtils.isEmpty(et_phone_number.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_phone_number), true)
                false
            }


            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(et_store.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_store), true)
                false
            }

            TextUtils.isEmpty(et_address.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }

            TextUtils.isEmpty(et_ruc.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_ruc), true)
                false
            }

            mDelivery == "" ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_delivery), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_enter_confirm_password),
                        true
                )
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                    .trim { it <= ' ' } -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                        true
                )
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(
                        resources.getString(R.string.err_msg_agree_terms_and_condition),
                        true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to register the user with email and password using FirebaseAuth.
     */
    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val partner = Partner(
                    "",
                    et_first_name.text.toString().trim { it <= ' ' },
                    et_last_name.text.toString().trim { it <= ' ' },
                    et_dni.text.toString().trim { it <= ' ' },
                    et_phone_number.text.toString().toLong(),
                    et_ruc.text.toString().trim { it <= ' ' },
                    mGender,
                    et_email.text.toString().trim { it <= ' ' },
                    et_password.text.toString().trim { it <= ' ' },
                    mTypeProduct,
                    mDelivery,
                    et_store.text.toString().trim { it <= ' ' },
                    et_address.text.toString().trim { it <= ' ' },
                    mTypeUser
            )

            // Pass the required values in the constructor.
            FirestoreClass().requestProvider(this, partner)
            // Create an instance and create a register a user with email and password.
        }
    }

    private fun defineGender(){
        btn_male.setOnClickListener {
            linear_male.setBackgroundColor(Color.parseColor("#FFBAD1"))
            linear_female.setBackgroundColor(Color.TRANSPARENT)
            mGender = Constants.MALE
        }
        btn_female.setOnClickListener {
            linear_male.setBackgroundColor(Color.TRANSPARENT)
            linear_female.setBackgroundColor(Color.parseColor("#FFBAD1"))
            mGender = Constants.FEMALE
        }
    }

    fun providerRegistrationSuccess() {
        // Hide the progress dialog
        hideProgressDialog()
        Toast.makeText(
                this,
                resources.getString(R.string.provider_register_success),
                Toast.LENGTH_LONG
        ).show()
        finish()
    }

}
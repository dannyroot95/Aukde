package com.aukdeclient.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.aukdeclient.firestore.FirestoreClass
import com.aukdeclient.models.User
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.TinyDB
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.aukdeshop.R
import com.aukdeshop.databinding.DialogSlowInternetBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_login.*

/**
 * Login Screen of the application.
 */
@Suppress("DEPRECATION")
class LoginActivity : BaseActivity(), View.OnClickListener {

    private val callbackManager = CallbackManager.Factory.create()
    var mFirestore = FirebaseFirestore.getInstance()
    private lateinit var binding: DialogSlowInternetBinding
    private lateinit var dialog : Dialog
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    @SuppressLint("PackageManagerGetSignatures")
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_login)
        // This is used to hide the status bar and make the login screen as a full screen activity.
        // It is deprecated in the API level 30. I will update you with the alternate solution soon.
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = DialogSlowInternetBinding.inflate(layoutInflater)
        dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)

        // Click event assigned to Forgot Password text.
        tv_forgot_password.setOnClickListener(this)
        // Click event assigned to Login button.
        btn_login.setOnClickListener(this)
        // Click event assigned to Register text.
        tv_register.setOnClickListener(this)

        btn_login_facebook.setOnClickListener(this)
        btn_login_google.setOnClickListener(this)

        FirestoreClass().getDashboardItemsListActivity(this)
        FirestoreClass().getAllSliders(this)

    }

    /**
     * In Login screen the clickable components are Login Button, ForgotPassword text and Register Text.
     */
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {

                R.id.tv_forgot_password -> {
                    // Launch the forgot password screen when the user clicks on the forgot password text.
                    val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btn_login -> {
                    logInRegisteredUser()
                }

                R.id.btn_login_facebook -> {
                    loginWithFacebook()
                }

                R.id.btn_login_google -> {
                    loginWithGoogle()
                }

                R.id.tv_register -> {
                    // Launch the register screen when the user clicks on the text.
                    val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    /**
     * A function to validate the login entries of a user.
     */
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_email.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to Log-In. The user will be able to log in using the registered email and password with Firebase Authentication.
     */
    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            // Show the progress dialog.
            showProgressDialog("Espere\nConectando a red...")

            // Get the text from editText and trim the space
            val email = et_email.text.toString().trim { it <= ' ' }
            val password = et_password.text.toString().trim { it <= ' ' }

            // Log-In using FirebaseAuth

            mFirestore.collection(Constants.USERS).whereEqualTo("email",email).get().addOnSuccessListener { document ->
                if (document !=null){
                    for (Query : QueryDocumentSnapshot in document){
                        if (Query.exists()){
                            val typeUser = Query.data["type_user"].toString()
                            if (typeUser == "client"){
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                                FirestoreClass().getUserDetails(this@LoginActivity)
                                                FirestoreClass().createToken(firebaseUser.uid)

                                            } else {
                                                // Hide the progress dialog
                                                hideProgressDialog()
                                                showErrorSnackBar("No se pudo iniciar sesión, revise sus datos", true)
                                            }
                                        }.addOnFailureListener {
                                        hideProgressDialog()
                                        showErrorSnackBar("No se pudo iniciar sesión, inténtelo más tarde", true)
                                    }.addOnCanceledListener {
                                        hideProgressDialog()
                                        showErrorSnackBar("No se pudo iniciar sesión, inténtelo más tarde", true)
                                    }
                            }
                            else {
                                hideProgressDialog()
                                showErrorSnackBar("Usuario no permitido", true)
                            }

                        }

                    }
                }
            }


        }
    }

    private fun loginWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {

                        showProgressDialog("Espere\nConectando a red...")

                        val handler = Handler()
                        handler.postDelayed({
                            hideProgressDialog()
                            onResume()
                            dialog.show()
                            binding.closeDialog.setOnClickListener {
                                dialog.dismiss()
                            }
                          },
                            30000)


                        result?.let {
                            val token = it.accessToken
                            val credential = FacebookAuthProvider.getCredential(token.token)

                            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val firebaseUser: FirebaseUser = task.result!!.user!!
                                    val id = firebaseUser.uid
                                    var email : String?
                                    var firstName  : String?
                                    var lastName : String?
                                    val photo : String = task.result.user?.photoUrl.toString()

                                    val graphRequest = GraphRequest.newMeRequest(result.accessToken){obj , response ->
                                        try{
                                            if(obj.has("id")){
                                                firstName = obj.getString("first_name")
                                                lastName = obj.getString("last_name")
                                                email = obj.getString("email")

                                                val user = User(id, firstName!!, lastName!!, email!!, photo, 0L, "",
                                                        0, Constants.CLIENT, "", "")
                                                handler.removeCallbacksAndMessages(null)
                                                mFirestore.collection(Constants.USERS).document(id).get().addOnSuccessListener { document ->
                                                    if (document != null) {
                                                        if (document.exists()) {
                                                            FirestoreClass().getUserDetails(this@LoginActivity)
                                                            FirestoreClass().createToken(firebaseUser.uid)
                                                        } else {
                                                            FirestoreClass().createToken(firebaseUser.uid)
                                                            saveInCacheAndFirebase(user)
                                                        }
                                                    } else {
                                                        hideProgressDialog()
                                                        Log.d("errorm",response.error.toString())
                                                        showErrorSnackBar("No se pudo iniciar sesión", true)
                                                    }
                                                }.addOnCanceledListener {
                                                    hideProgressDialog()
                                                    Log.d("errorm",response.error.toString())
                                                    showErrorSnackBar("No se pudo iniciar sesión", true)
                                                }.addOnFailureListener {
                                                    hideProgressDialog()
                                                    Log.d("errorm",response.error.toString())
                                                    showErrorSnackBar("No se pudo iniciar sesión", true)
                                                }

                                            }
                                        }catch (e : Exception){
                                            hideProgressDialog()
                                            Log.d("errorm",e.message.toString())
                                            showErrorSnackBar("No se pudo iniciar sesión", true)
                                        }
                                    }
                                    val param = Bundle()
                                    param.putString("fields","first_name,last_name,email")
                                    graphRequest.parameters = param
                                    graphRequest.executeAsync()

                                } else {
                                    hideProgressDialog()
                                    Log.d("errorm",task.exception.toString())
                                    showErrorSnackBar("No se pudo iniciar sesión", true)
                                }
                            }

                        }
                    }

                    override fun onCancel() {
                        showErrorSnackBar("No se pudo iniciar sesión", true)
                    }

                    override fun onError(error: FacebookException?) {
                        showErrorSnackBar("No se pudo iniciar sesión", true)
                    }

                })
    }

    private fun loginWithGoogle(){
        val googleConf : GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        val googleClient = GoogleSignIn.getClient(this,googleConf)
        googleClient.signOut()
        startActivityForResult(googleClient.signInIntent,Constants.GOOGLE_SIGN_IN)
    }

    private fun saveInCacheAndFirebase(user: User){
        val db = TinyDB(this)
        val sharedProfile = getSharedPreferences(Constants.CLIENT, Context.MODE_PRIVATE)
        val editorProfile: SharedPreferences.Editor = sharedProfile.edit()
        editorProfile.putInt(Constants.CLIENT, user.profileCompleted)
        editorProfile.apply()


        val sharedPreferencesX = getSharedPreferences("DATA", MODE_PRIVATE)
        val editorX = sharedPreferencesX.edit()
        val gson = Gson()
        val json = gson.toJson(user)
        editorX.putString("key", json)
        editorX.apply()
        db.putObject(Constants.KEY_USER_DATA_OBJECT,user)

        FirestoreClass().registerUserWithFacebookOrGoogle(this@LoginActivity, user)
    }

    /**
     * A function to notify user that logged in success and get the user details from the FireStore database after authentication.
     */
    fun userLoggedInSuccess(user: User) {

        // Hide the progress dialog.
        hideProgressDialog()

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Dashboard Screen after log in.
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.GOOGLE_SIGN_IN){

            showProgressDialog("Espere\nConectando a red...")
            val handler = Handler()
            handler.postDelayed({
                hideProgressDialog()
                onResume()
                dialog.show()
                binding.closeDialog.setOnClickListener {
                    dialog.dismiss()

                }
             }, 30000)

            val task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account : GoogleSignInAccount? = task.getResult(ApiException::class.java)

                if (account != null){

                    val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken,null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{ task ->

                        if (task.isSuccessful){

                            val firebaseUser: FirebaseUser = task.result!!.user!!
                            val id = firebaseUser.uid
                            val email = account.email
                            val name = account.givenName
                            val lastName = account.familyName
                            val photo = account.photoUrl?.toString()

                            val user = User(id, name!!, lastName!!, email!!, photo!!, 0L, "",
                                    0, Constants.CLIENT, "", "")
                            handler.removeCallbacksAndMessages(null)
                            mFirestore.collection(Constants.USERS).document(id).get().addOnSuccessListener { document ->
                                if (document != null) {
                                    if (document.exists()) {
                                        FirestoreClass().getUserDetails(this@LoginActivity)
                                        FirestoreClass().createToken(firebaseUser.uid)
                                    } else {
                                        FirestoreClass().createToken(firebaseUser.uid)
                                        saveInCacheAndFirebase(user)
                                    }
                                } else {
                                    hideProgressDialog()
                                    showErrorSnackBar("No se pudo iniciar sesión", true)
                                }
                            }.addOnCanceledListener {
                                hideProgressDialog()
                                showErrorSnackBar("No se pudo iniciar sesión", true)
                            }.addOnFailureListener {
                                hideProgressDialog()
                                showErrorSnackBar("No se pudo iniciar sesión", true)
                            }
                        }
                        else{
                            hideProgressDialog()
                            showErrorSnackBar("No se pudo iniciar sesión", true)
                        }
                    }
                }else{
                    hideProgressDialog()
                    showErrorSnackBar("No se pudo iniciar sesión", true)
                }

            }catch (e : Exception){
                hideProgressDialog()
            }
        }

    }


}
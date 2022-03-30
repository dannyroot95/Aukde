package com.aukdeclient.firestore

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.aukdeclient.Services.Visanet
import com.aukdeshop.R
import com.aukdeclient.models.*
import com.aukdeclient.notifications.server.FCMBody
import com.aukdeclient.notifications.server.FCMResponse
import com.aukdeclient.notifications.server.NotificationProvider
import com.aukdeclient.ui.activities.*
import com.aukdeclient.ui.fragments.DashboardFragment
import com.aukdeclient.ui.fragments.OrdersFragment
import com.aukdeclient.ui.fragments.ProductsFragment
import com.aukdeclient.ui.fragments.SoldProductsFragment
import com.aukdeclient.utils.Constants
import com.aukdeclient.utils.TinyDB
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A custom class where we will add the operation performed for the FireStore database.
 */
@Suppress("NAME_SHADOWING")
class FirestoreClass {

    // Access a Cloud Firestore instance.
    private val mFireStore = FirebaseFirestore.getInstance()
    private var mDatabase : DatabaseReference = FirebaseDatabase.getInstance().reference
    var notificationProvider: NotificationProvider = NotificationProvider()

    /**
     * A function to make an entry of the registered user in the FireStore database.
     */

    fun requestProvider(activity: RegisterProvider, partnerInfo: Partner){

        mDatabase.child(Constants.REQUEST_PROVIDER).push().setValue(partnerInfo)
            .addOnSuccessListener {
                activity.providerRegistrationSuccess()
            }
            .addOnFailureListener{ e->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while registering the provider.",
                        e
                )
            }
    }

    fun registerUser(activity: RegisterActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while registering the user.",
                        e
                )
            }
    }

    fun registerUserWithFacebookOrGoogle(activity: LoginActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
                // Document ID for users fields. Here the document it is the User ID.
                .document(userInfo.id)
                // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    // Here call a function of base activity for transferring the result to it.
                    activity.userLoggedInSuccess(userInfo)
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while registering the user.",
                            e
                    )
                }.addOnCanceledListener {
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * A function to get the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser

        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    fun createToken(id: String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            // Get new FCM registration token
            if (task.isSuccessful) {
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                    val token = Token(instanceIdResult.token)
                    mFireStore.collection(Constants.TOKEN).document(id).set(token)
                    mDatabase.child(Constants.TOKEN).child(id).setValue(token)
                }
            }
        })
    }

    fun getTokenDriver(idUser: String): DatabaseReference {
        return mDatabase.child("Tokens").child(idUser)
    }

    fun deleteToken(id: String): Task<Void> {
        return mFireStore.collection(Constants.TOKEN).document(id).delete()
        //mDatabase.child(Constants.TOKEN).child(id).removeValue()
    }

    fun deleteTokenRealtime(id: String): Task<Void> {
        return mDatabase.child(Constants.TOKEN).child(id).removeValue()
    }

    fun deleteCartRealtime(id: String): Task<Void> {
        return mDatabase.child(Constants.CART).child(id).removeValue()
    }

    fun createNotificationOrder(tokenIDUser: String, mPhoto: String){
        mFireStore.collection(Constants.TOKEN)
                // The document id to get the Fields of user.
                .document(tokenIDUser)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        val token : String = document.getString(Constants.TOKEN).toString()
                        val map: MutableMap<String, String> = java.util.HashMap()
                        map["title"] = "Hay un pedido!"
                        map["body"] = "Revise su lista de pedidos en su panel"
                        map["path"] = mPhoto
                        val fcmBody = FCMBody(token, "high", map)
                        notificationProvider.sendNotification(fcmBody).enqueue(object :
                                Callback<FCMResponse?> {
                            override fun onResponse(
                                    call: Call<FCMResponse?>,
                                    response: Response<FCMResponse?>
                            ) {
                                if (response.body() != null) {
                                    if (response.body()!!.success == 1) {
                                        //Toast.makeText(this@AddProductActivity, "Notificación enviada", Toast.LENGTH_LONG).show();
                                    } else {
                                        //Toast.makeText(this, "NO se pudo ENVIAR la notificación!", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    //Toast.makeText(this, "NO se pudo ENVIAR la notificación!", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                                Log.d("Error", "Error encontrado" + t.message)
                            }
                        })
                    }
                }
                .addOnFailureListener {
                    // Hide the progress dialog if there is any error. And print the error in log.
                }

    }


    fun createNotificationUpdateStatusProvider(
            activity: SoldProductDetailsActivity, tokenIDUser: String, numOrder: String,
            status: String, mPhoto: String, title: String
    ){
        mFireStore.collection(Constants.TOKEN)
                // The document id to get the Fields of user.
                .document(tokenIDUser)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()){
                        val token : String = document.getString(Constants.TOKEN).toString()
                        val map: MutableMap<String, String> = java.util.HashMap()
                        map["title"] = "Pedido $numOrder"
                        map["body"] = "Producto : $title" +
                                "     \nEstado : $status"
                        map["path"] = mPhoto
                        val fcmBody = FCMBody(token, "high", map)
                        notificationProvider.sendNotification(fcmBody).enqueue(object :
                                Callback<FCMResponse?> {
                            override fun onResponse(
                                    call: Call<FCMResponse?>,
                                    response: Response<FCMResponse?>
                            ) {
                                if (response.body() != null) {
                                    if (response.body()!!.success == 1) {
                                        activity.hideProgressDialog()
                                        activity.finish()
                                        //Toast.makeText(this@AddProductActivity, "Notificación enviada", Toast.LENGTH_LONG).show();
                                    } else {
                                        activity.hideProgressDialog()
                                        Toast.makeText(
                                                activity,
                                                "NO se pudo ENVIAR la notificación!",
                                                Toast.LENGTH_LONG
                                        ).show()
                                    }
                                } else {
                                    activity.hideProgressDialog()
                                    Toast.makeText(
                                            activity,
                                            "NO se pudo ENVIAR la notificación!",
                                            Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<FCMResponse?>, t: Throwable) {
                                Toast.makeText(activity, "ERROR!", Toast.LENGTH_LONG).show()
                                Log.d("Error", "Error encontrado" + t.message)
                                activity.hideProgressDialog()
                            }
                        })
                    }
                    else{
                        Log.d("Error", "Error encontrado")
                        activity.hideProgressDialog()
                        activity.finish()
                        Toast.makeText(
                                activity,
                                "No se pudo notificar al usuario!",
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { _ ->
                    // Hide the progress dialog if there is any error. And print the error in log.
                }

    }

    /**
     * A function to get the logged user details from from FireStore Database.
     */
    fun getUserDetails(activity: Activity) {

        //CREAR UNA BASE DE DATOS Y ALMACENARLOS PARA RECUPERARLO CUANDO NO EXISTA INTERNET
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())
                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!
                val db = TinyDB(activity)

                val sharedPreferencesY = activity.getSharedPreferences(Constants.USER_DATA_OBJECT, Context.MODE_PRIVATE)
                val editorY = sharedPreferencesY.edit()
                val sharedPreferencesX = activity.getSharedPreferences(Constants.USER_DATA, Context.MODE_PRIVATE)
                val editorX = sharedPreferencesX.edit()
                val gson = Gson()
                val json = gson.toJson(user)
                editorX.putString(Constants.KEY, json)
                editorX.apply()
                editorY.putString(Constants.KEY_USER_DATA_OBJECT, "data_success")
                editorY.apply()
                db.putObject(Constants.KEY_USER_DATA_OBJECT,user)

                val sharedPreferences =
                    activity.getSharedPreferences(
                            Constants.MYSHOPPAL_PREFERENCES,
                            Context.MODE_PRIVATE
                    )
                val sharedPhoto =
                        activity.getSharedPreferences(
                                Constants.EXTRA_USER_PHOTO,
                                Context.MODE_PRIVATE
                        )

                val sharedProfile =
                        activity.getSharedPreferences(
                                Constants.CLIENT,
                                Context.MODE_PRIVATE
                        )


                // Create an instance of the editor which is help us to edit the SharedPreference.
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                val editorPhoto: SharedPreferences.Editor = sharedPhoto.edit()
                val editorProfile: SharedPreferences.Editor = sharedProfile.edit()

                editor.putString(
                        Constants.LOGGED_IN_USERNAME,
                        "${user.firstName} ${user.lastName}"
                )
                editor.apply()

                editorPhoto.putString(
                        Constants.EXTRA_USER_PHOTO,
                        user.image
                )
                editorPhoto.apply()

                editorProfile.putInt(
                        Constants.CLIENT,
                        user.profileCompleted
                )
                editorProfile.apply()

                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userLoggedInSuccess(user)
                    }

                    is SettingsActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
                    }
                    is SettingsActivity -> {
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
                    }
                }

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting user details.",
                        e
                )
            }.addOnCanceledListener {
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
                    }
                    is SettingsActivity -> {
                        activity.hideProgressDialog()
                        Toast.makeText(activity,"Error",Toast.LENGTH_SHORT).show()
                    }
                }

            }
    }


    fun countProductsInCart(activity: DashboardActivity){
        mFireStore.collection(Constants.CART_ITEMS).whereEqualTo(
                Constants.USER_ID,
                FirestoreClass().getCurrentUserID()
        )
                .get().addOnSuccessListener { document ->
                    if (document != null){
                        var counter = 0
                        for (Query : QueryDocumentSnapshot in document){
                            val id : String = Query.data[Constants.USER_ID].toString()
                            if (id == FirestoreClass().getCurrentUserID()){
                                counter++
                            }
                        }
                        activity.countCart.text = counter.toString()
                    }
                    else{
                        activity.countCart.text = "0"
                    }
                }
    }


    @SuppressLint("SetTextI18n")
    fun getPurchases(activity: SettingsActivity){
        mFireStore.collection(Constants.ORDERS).whereEqualTo(Constants.USER_ID, getCurrentUserID()).get()
                .addOnSuccessListener { document ->
                    var ctx = 0
                    if (document != null){
                        for (Query : QueryDocumentSnapshot in document){
                            val found : String = Query.data[Constants.USER_ID].toString()
                            if (found == getCurrentUserID()){
                                ctx++
                            }
                        }
                        activity.progress_purchases.visibility = View.GONE
                        activity.tv_purchases.text = ctx.toString()
                    }
                }
    }


    /**
     * A function to update the user profile data into the database.
     *
     * @param activity The activity is used for identifying the Base activity to which the result is passed.
     * @param userHashMap HashMap of fields which are to be updated.
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        // Collection Name
        mFireStore.collection(Constants.USERS)
            // Document ID against which the data to be updated. Here the document id is the current logged in user id.
            .document(getCurrentUserID())
            // A HashMap of fields which are to be updated.
            .update(userHashMap)
            .addOnSuccessListener {

                // Notify the success result.
                when (activity) {
                    is UserProfileActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userProfileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                when (activity) {
                    is UserProfileActivity -> {
                        // Hide the progress dialog if there is any error. And print the error in log.
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating the user details.",
                        e
                )
            }
    }

    // A function to upload the image to the cloud storage.
    fun uploadImageToCloudStorage(activity: Activity, imageFileURI: Uri?, imageType: String) {

        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                imageType + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(
                        activity,
                        imageFileURI
                )
        )

        //adding the file to reference
        sRef.putFile(imageFileURI!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.e("Downloadable Image URL", uri.toString())

                        // Here call a function of base activity for transferring the result to it.
                        when (activity) {
                            is UserProfileActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }

                            is AddProductActivity -> {
                                activity.imageUploadSuccess(uri.toString())
                            }
                        }
                    }
            }
            .addOnFailureListener { exception ->

                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }

                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                        activity.javaClass.simpleName,
                        exception.message,
                        exception
                )
            }
    }

    /**
     * A function to make an entry of the user's product in the cloud firestore database.
     */

    fun searchSKUAndUpload(activity: AddProductActivity, productInfo: Product, sku: String){

        mFireStore.collection(Constants.PRODUCTS).whereEqualTo(Constants.SKU, sku).get().addOnCompleteListener { documents ->

            if (documents.isSuccessful){
                if (!documents.result.isEmpty){
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar(
                            activity.resources.getString(R.string.err_msg_enter_product_sku_exist),
                            true
                    )
                }
                else {
                    uploadProductDetails(activity, productInfo)
                }
            }
            else{
                activity.hideProgressDialog()
                Toast.makeText(activity, "ERROR!", Toast.LENGTH_SHORT).show()
            }

        }.addOnCanceledListener {
            activity.hideProgressDialog()
            Toast.makeText(activity, "ERROR!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener{
            activity.hideProgressDialog()
            Toast.makeText(activity, "ERROR!", Toast.LENGTH_SHORT).show()
        }

    }

    private fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {

        mFireStore.collection(Constants.PRODUCTS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.productUploadSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while uploading the product details.",
                        e
                )
            }
    }

    /**
     * A function to get the products list from cloud firestore.
     *
     * @param fragment The fragment is passed as parameter as the function is called from fragment and need to the success result.
     */
    fun getProductsList(fragment: Fragment) {


        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productsList.add(product)
                }

                when (fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.hideProgressDialog()
                    }
                }

                Log.e("Get Product List", "Error while getting product list.", e)
            }
    }

    /**
     * A function to get all the product list from the cloud firestore.
     *
     * @param activity The activity is passed as parameter to the function because it is called from activity and need to the success result.
     */
    fun getAllProductsList(activity: Activity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<Product> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id
                    productsList.add(product)

                }

                when (activity) {
                    is CartListActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                    is CheckoutActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e("Get Product List", "Error while getting all product list.", e)
            }
    }

    /**
     * A function to get the dashboard items list. The list will be an overall items list, not based on the user's id.
     */
    fun getDashboardItemsList(fragment: DashboardFragment, context: Context) {
        // The collection name for PRODUCTS
        val tinyDB = TinyDB(context)
        if (tinyDB.getListProduct(Constants.CACHE_PRODUCT, Product::class.java).isNotEmpty()){
            val productObjects = tinyDB.getListProduct(Constants.CACHE_PRODUCT, Product::class.java)
            val products: ArrayList<Product> = ArrayList<Product>()

            for (objs in productObjects) {
                products.add(objs as Product)
            }

            fragment.successDashboardItemsList(products)
            mFireStore.collection(Constants.PRODUCTS)
                    .get() // Will get the documents snapshots.
                    .addOnSuccessListener { document ->
                        val productsList: ArrayList<Product> = ArrayList()
                        for ((ctx, i) in document.documents.withIndex()) {
                            val product = i.toObject(Product::class.java)!!
                            val photo: File = File(context.getExternalFilesDir(null), "${product.product_id}.jpg")
                            product.product_id = i.id
                            productsList.add(product)
                            if (!photo.exists()){
                                getBitmapFromURL(product.image,context,product.product_id)
                            }
                            /*if (product.product_id == products[ctx].product_id){
                                if (product.image != products[ctx].image){
                                    getBitmapFromURL(product.image,context,product.product_id)
                                }
                            }*/
                        }
                        tinyDB.putListProduct(Constants.CACHE_PRODUCT, productsList)
                    }

        }
        else{
            mFireStore.collection(Constants.PRODUCTS)
                    .get() // Will get the documents snapshots.
                    .addOnSuccessListener { document ->
                        val productsList: ArrayList<Product> = ArrayList()
                        // Here we get the list of boards in the form of documents.
                        Log.e(fragment.javaClass.simpleName, document.documents.toString())
                        // Here we have created a new instance for Products ArrayList.
                        // A for loop as per the list of documents to convert them into Products ArrayList.
                        for (i in document.documents) {

                            val product = i.toObject(Product::class.java)!!
                            product.product_id = i.id
                            productsList.add(product)
                            val photo: File = File(context.getExternalFilesDir(null), "${product.product_id}.jpg")
                            if (!photo.exists()){
                                getBitmapFromURL(product.image,context,product.product_id)
                            }
                        }

                        tinyDB.putListProduct(Constants.CACHE_PRODUCT, productsList)

                        // Pass the success result to the base fragment.
                        fragment.successDashboardItemsList(productsList)
                    }
                    .addOnFailureListener { e ->
                        // Hide the progress dialog if there is any error which getting the dashboard items list.
                        fragment.hideProgressDialog()
                        Log.e(fragment.javaClass.simpleName, "Error while getting dashboard items list.", e)
                    }
        }


    }

    private fun getBitmapFromURL(url: String, context: Context, name: String) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            val mFolder  = File(context.getExternalFilesDir(null), "$name.jpg")
            val storageRef: StorageReference = Firebase.storage.getReferenceFromUrl(url)
            storageRef.getFile(mFolder).addOnSuccessListener {
            }
        }
    }

    fun getBitmapSliderFromURL(src: String?, context: Context, name : String) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                val tinyDB = TinyDB(context)
                val url = URL(src)
                val bitMap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                //image = Bitmap.createScaledBitmap(bitMap, 500, 500, true)
                tinyDB.putImage(context.filesDir.parent, "$name.jpg", bitMap)
            } catch (e: IOException) {
                // Log exception
            }
        }
    }

    private fun getBitmapCategoriesFromURL(src: String?, context: Context, name : String) {
        CoroutineScope(Job() + Dispatchers.IO).launch {
            try {
                val tinyDB = TinyDB(context)
                val url = URL(src)
                val bitMap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                //image = Bitmap.createScaledBitmap(bitMap, 500, 500, true)
                tinyDB.putImageCategories(context.filesDir.parent, "$name.jpg", bitMap)
            } catch (e: IOException) {
                // Log exception
            }
        }
    }

    fun getDashboardItemsListActivity(activity: LoginActivity) {
        // The collection name for PRODUCTS
        var ctx = 0
        val tinyDB = TinyDB(activity)
        val products: ArrayList<Product> = ArrayList<Product>()
        if (tinyDB.getListProduct(Constants.CACHE_PRODUCT, Product::class.java).isNotEmpty()) {
            val productObjects = tinyDB.getListProduct(Constants.CACHE_PRODUCT, Product::class.java)
            for (objs in productObjects) {
                products.add(objs as Product)
            }
        }
            mFireStore.collection(Constants.PRODUCTS)
                    .get() // Will get the documents snapshots.
                    .addOnSuccessListener { document ->
                        val productsList: ArrayList<Product> = ArrayList()
                        for (i in document.documents) {
                            val product = i.toObject(Product::class.java)!!
                            product.product_id = i.id
                            productsList.add(product)
                            val photo: File = File(activity.getExternalFilesDir(null), "${product.product_id}.jpg")
                            if (!photo.exists()){
                                getBitmapFromURL(product.image, activity, product.product_id)
                            }
                            if(products.isNotEmpty()){
                                if (product.product_id == products[ctx].product_id){
                                    if (product.image != products[ctx].image){
                                        getBitmapFromURL(product.image,activity,product.product_id)
                                    }
                                }
                            }
                           ctx++
                        }
                        tinyDB.putListProduct(Constants.CACHE_PRODUCT, productsList)
                    }
    }

    /*
    fun getDashboardTypeItemsList(fragment: DashboardFragment, type: String) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS).whereEqualTo("type_product", type)
                .get() // Will get the documents snapshots.
                .addOnSuccessListener { document ->

                    // Here we get the list of boards in the form of documents.
                    Log.e(fragment.javaClass.simpleName, document.documents.toString())

                    // Here we have created a new instance for Products ArrayList.
                    val productsList: ArrayList<Product> = ArrayList()

                    // A for loop as per the list of documents to convert them into Products ArrayList.
                    for (i in document.documents) {

                        val product = i.toObject(Product::class.java)!!
                        product.product_id = i.id
                        productsList.add(product)
                    }

                    // Pass the success result to the base fragment.
                    fragment.successDashboardItemsList(productsList)
                }
                .addOnFailureListener { e ->
                    // Hide the progress dialog if there is any error which getting the dashboard items list.
                    fragment.hideProgressDialog()
                    Log.e(
                            fragment.javaClass.simpleName,
                            "Error while getting dashboard items list.",
                            e
                    )
                }
    }*/

    /**
     * A function to delete the product from the cloud firestore.
     */
    fun deleteProduct(fragment: ProductsFragment, productId: String) {

        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {

                // Notify the success result to the base class.
                fragment.productDeleteSuccess()
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is an error.
                fragment.hideProgressDialog()

                Log.e(
                        fragment.requireActivity().javaClass.simpleName,
                        "Error while deleting the product.",
                        e
                )
            }
    }

    /**
     * A function to get the product details based on the product id.
     */
    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {

        // The collection name for PRODUCTS
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get() // Will get the document snapshots.
            .addOnSuccessListener { document ->

                // Here we get the product details in the form of document.
                Log.e(activity.javaClass.simpleName, document.toString())

                // Convert the snapshot to the object of Product data model class.
                val product = document.toObject(Product::class.java)!!

                activity.productDetailsSuccess(product)
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is an error.
                activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while getting the product details.", e)
            }
    }

    fun getStatusPackageProvider(activity: ProductDetailsActivity, id: String){

        mFireStore.collection(Constants.USERS).document(id)
                .get().addOnSuccessListener { document ->

                    if (document != null) {

                        val typePackage : String = document.data!!["type_package"].toString()
                        if (typePackage == Constants.SUSPEND){
                            activity.btn_add_to_cart.visibility = View.GONE
                            activity.tv_product_no_add.visibility = View.VISIBLE
                        }
                    }

                }

    }

    /**
     * A function to add the item to the cart in the cloud firestore.
     *
     * @param activity
     * @param addToCart
     */
    fun addCartItems(activity: ProductDetailsActivity, addToCart: Cart) {

        mFireStore.collection(Constants.CART_ITEMS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addToCartSuccess()
            }
            .addOnFailureListener { e ->

                activity.hideProgressDialog()

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while creating the document for cart item.",
                        e
                )
            }
    }

    fun verifyCategoryInCart(category : String , store : String ,activity: ProductDetailsActivity) {
        mFireStore.collection(Constants.CART_ITEMS).whereEqualTo(Constants.USER_ID,getCurrentUserID()).get().addOnSuccessListener { snapshot ->
            if (snapshot.size() > 0){
               for(i in snapshot.documents){
                   val product = i.toObject(Cart::class.java)!!
                   if(category == product.category && store == product.name_store){
                       activity.getCategoryFromCart(true)
                   }
               }
            }else{
                activity.getCategoryFromCart(true)
            }
        }
    }

    fun addCartItemsForLocation(providerID: String){
        mDatabase.child(Constants.STORE).child(providerID).addListenerForSingleValueEvent(object :
                ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val getData: Store = snapshot.getValue(Store::class.java)!!
                    val latLong: ArrayList<Double> = ArrayList()
                    latLong.add(getData.l[0])
                    latLong.add(getData.l[1])
                    val location = Store(getData.g, latLong)
                    mDatabase.child(Constants.CART).child(FirestoreClass().getCurrentUserID())
                            .child(
                                    providerID
                            ).setValue(location)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    /**
     * A function to check whether the item already exist in the cart or not.
     */
    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {

        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->

                Log.e(activity.javaClass.simpleName, document.documents.toString())

                // If the document size is greater than 1 it means the product is already added to the cart.
                if (document.documents.size > 0) {
                    activity.productExistsInCart()
                } else {
                    activity.productNotExistsInCart()
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error.
                activity.hideProgressDialog()

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while checking the existing cart list.",
                        e
                )
            }
    }

    fun checkIfItemExistInCartFromCache(activity: ProductDetailsActivity, productId: String) {

        mFireStore.collection(Constants.CART_ITEMS)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                .whereEqualTo(Constants.PRODUCT_ID, productId)
                .get()
                .addOnSuccessListener { document ->

                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    // If the document size is greater than 1 it means the product is already added to the cart.
                    if (document.documents.size > 0) {
                        activity.productExistsInCartFromCache()
                    }else {
                        activity.productNotExistsInCartFromCache()
                    }
                }
                .addOnFailureListener { e ->
                }
    }

    /**
     * A function to get the cart items list from the cloud firestore.
     *
     * @param activity
     */
    fun getCartList(activity: Activity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->

                // Here we get the list of cart items in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                // Here we have created a new instance for Cart Items ArrayList.
                val list: ArrayList<Cart> = ArrayList()

                // A for loop as per the list of documents to convert them into Cart Items ArrayList.
                for (i in document.documents) {

                    val cartItem = i.toObject(Cart::class.java)!!
                    cartItem.id = i.id

                    list.add(cartItem)
                }

                when (activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(list)
                    }
                    is CheckoutActivity -> {
                        activity.successCartItemsList(list)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is an error based on the activity instance.
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                    }

                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(activity.javaClass.simpleName, "Error while getting the cart list items.", e)
            }
    }

    /**
     * A function to remove the cart item from the cloud firestore.
     *
     * @param activity activity class.
     * @param cart_id cart id of the item.
     */
    fun removeItemFromCart(context: Context, cart_id: String) {

        // Cart items collection name
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .delete()
            .addOnSuccessListener {

                // Notify the success result of the removed cart item from the list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.itemRemovedSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e(
                        context.javaClass.simpleName,
                        "Error while removing the item from the cart list.",
                        e
                )
            }
    }

    fun removeItemFromRealTimeCart(providerID: String) {
        mDatabase.child(Constants.CART).child(getCurrentUserID()).child(providerID).removeValue()
    }
    /**
     * A function to update the cart item in the cloud firestore.
     *
     * @param activity activity class.
     * @param id cart id of the item.
     * @param itemHashMap to be updated values.
     */
    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>) {

        // Cart items collection name
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id) // cart id
            .update(itemHashMap) // A HashMap of fields which are to be updated.
            .addOnSuccessListener {

                // Notify the success result of the updated cart items list to the base class.
                when (context) {
                    is CartListActivity -> {
                        context.itemUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->

                // Hide the progress dialog if there is any error.
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                    }
                }

                Log.e(
                        context.javaClass.simpleName,
                        "Error while updating the cart item.",
                        e
                )
            }
    }

    /**
     * A function to add address to the cloud firestore.
     *
     * @param activity
     * @param addressInfo
     */
    fun addAddress(activity: AddEditAddressActivity, addressInfo: Address) {

        // Collection name address.
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while adding the address.",
                        e
                )
            }
    }

    /**
     * A function to get the list of address from the cloud firestore.
     *
     * @param activity
     */
    fun getAddressesList(activity: AddressListActivity) {
        // The collection name for PRODUCTS
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of boards in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for address ArrayList.
                val addressList: ArrayList<Address> = ArrayList()

                // A for loop as per the list of documents to convert them into Boards ArrayList.
                for (i in document.documents) {

                    val address = i.toObject(Address::class.java)!!
                    address.id = i.id
                    addressList.add(address)
                }

                activity.successAddressListFromFirestore(addressList)
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.

                activity.hideProgressDialog()

                Log.e(activity.javaClass.simpleName, "Error while getting the address list.", e)
            }
    }

    /**
     * A function to update the existing address to the cloud firestore.
     *
     * @param activity Base class
     * @param addressInfo Which fields are to be updated.
     * @param addressId existing address id
     */
    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {

        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.addUpdateAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating the Address.",
                        e
                )
            }
    }

    /**
     * A function to delete the existing address from the cloud firestore.
     *
     * @param activity Base class
     * @param addressId existing address id
     */
    fun deleteAddress(activity: AddressListActivity, addressId: String) {

        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.deleteAddressSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while deleting the address.",
                        e
                )
            }
    }


    /**
     * A function to place an order of the user in the cloud firestore.
     *
     * @param activity base class
     * @param order Order Info
     */
    fun placeOrder(activity: CheckoutActivity, order: Order) {
        mFireStore.collection(Constants.ORDERS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(order, SetOptions.merge())
            .addOnSuccessListener {
                activity.orderPlacedSuccess()
                // Here call a function of base activity for transferring the result to it.
            }.addOnFailureListener { e ->
                // Hide the progress dialog if there is any error.
                activity.hideProgressDialog()
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while placing an order.",
                        e
                )
            }
    }

    /**
     * A function to update all the required details in the cloud firestore after placing the order successfully.
     *
     * @param activity Base class.
     * @param cartList List of cart items.
     */
    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<Cart>, order: Order) {

        val writeBatch = mFireStore.batch()

        // Prepare the sold product details
        for (cart in cartList) {

            val soldProduct = SoldProduct(
                    FirestoreClass().getCurrentUserID(),
                    cart.provider_id,
                    cart.title,
                    cart.price,
                    cart.cart_quantity,
                    cart.image,
                    order.title,
                    order.order_datetime,
                    order.sub_total_amount,
                    order.shipping_charge,
                    order.total_amount,
                    order.address,
                    order.driver_id
            )

            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS)
                .document()
            writeBatch.set(documentReference, soldProduct)
        }

        // Here we will update the product stock in the products collection based to cart quantity.
        for (cart in cartList) {

            val productHashMap = HashMap<String, Any>()

            productHashMap[Constants.STOCK_QUANTITY] =
                (cart.stock_quantity.toInt() - cart.cart_quantity.toInt()).toString()

            val documentReference = mFireStore.collection(Constants.PRODUCTS)
                .document(cart.product_id)

            writeBatch.update(documentReference, productHashMap)
        }

        // Delete the list of cart items
        for (cart in cartList) {

            val documentReference = mFireStore.collection(Constants.CART_ITEMS)
                .document(cart.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit().addOnSuccessListener {

            activity.allDetailsUpdatedSuccessfully()

        }.addOnFailureListener { e ->
            // Here call a function of base activity for transferring the result to it.
            activity.hideProgressDialog()

            Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating all the details after order placed.",
                    e
            )
        }
    }

    /**
     * A function to get the list of orders from cloud firestore.
     */

    fun getMyOrdersList(fragment: OrdersFragment) {
        mFireStore.collection(Constants.ORDERS)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID()).addSnapshotListener { document, e ->
                    if (document != null) {
                        Log.e(fragment.javaClass.simpleName, document.documents.toString())
                        val list: ArrayList<Order> = ArrayList()
                        list.clear()
                        for (i in document.documents) {
                            val orderItem = i.toObject(Order::class.java)!!
                            orderItem.id = i.id
                            list.add(orderItem)
                        }
                        list.reverse()
                        fragment.populateOrdersListInUI(list)
                    }

                    if (e != null) {
                        fragment.hideProgressDialog()
                        Log.e(
                                fragment.javaClass.simpleName,
                                "Error while getting the orders list.",
                                e
                        )
                        return@addSnapshotListener
                    }


                }

    }


    /**
     * A function to get the list of sold products from the cloud firestore.
     *
     *  @param fragment Base class
     */
    fun getSoldProductsList(fragment: SoldProductsFragment) {
        // The collection name for SOLD PRODUCTS
        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.PROVIDER_ID, getCurrentUserID())
            .get() // Will get the documents snapshots.
            .addOnSuccessListener { document ->
                // Here we get the list of sold products in the form of documents.
                Log.e(fragment.javaClass.simpleName, document.documents.toString())

                // Here we have created a new instance for Sold Products ArrayList.
                val list: ArrayList<SoldProduct> = ArrayList()

                // A for loop as per the list of documents to convert them into Sold Products ArrayList.
                for (i in document.documents) {

                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id = i.id
                    list.add(soldProduct)
                }
                list.reverse()
                fragment.successSoldProductsList(list)
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error.
                fragment.hideProgressDialog()

                Log.e(
                        fragment.javaClass.simpleName,
                        "Error while getting the list of sold products.",
                        e
                )
            }
    }

    fun updateStatusOrder(
            activity: SoldProductDetailsActivity, orderId: String, status: Int,
            id: String, position: Int, hasDelivery: String
    ){
        //activity.showProgressDialog()
        mFireStore.collection(Constants.ORDERS)
                .whereEqualTo("title", orderId)
                .get().addOnSuccessListener { document ->
                    for (i in document.documents) {

                        val key = i.id
                        val order = i.toObject(Order::class.java)!!

                        val map: MutableMap<String, Any> = java.util.HashMap()
                        map["cart_quantity"] = order.items[position].cart_quantity
                        map["delivery"] = order.items[position].delivery
                        map["id"] = order.items[position].id
                        map["image"] = order.items[position].image
                        map["name_store"] = order.items[position].name_store
                        map["price"] = order.items[position].price
                        map["product_id"] = order.items[position].product_id
                        map["provider_id"] = order.items[position].provider_id
                        map["status"] = order.items[position].status
                        map["stock_quantity"] = order.items[position].stock_quantity
                        map["title"] = order.items[position].title
                        map["user_id"] = order.items[position].user_id

                        val map2: MutableMap<String, Any> = java.util.HashMap()
                        map2["cart_quantity"] = order.items[position].cart_quantity
                        map2["delivery"] = order.items[position].delivery
                        map2["id"] = order.items[position].id
                        map2["image"] = order.items[position].image
                        map2["name_store"] = order.items[position].name_store
                        map2["price"] = order.items[position].price
                        map2["product_id"] = order.items[position].product_id
                        map2["provider_id"] = order.items[position].provider_id
                        map2["status"] = status
                        map2["stock_quantity"] = order.items[position].stock_quantity
                        map2["title"] = order.items[position].title
                        map2["user_id"] = order.items[position].user_id

                        mFireStore.collection(Constants.ORDERS).document(key).update(
                                "items", FieldValue.arrayRemove(
                                map
                        ),
                                "items", FieldValue.arrayUnion(map2)
                        )

                        val map3: MutableMap<String, Any> = java.util.HashMap()
                        map3["status"] = status
                        mFireStore.collection(Constants.SOLD_PRODUCTS).document(id).update(map3)

                        mFireStore.collection(Constants.ORDERS).document(key).get().addOnSuccessListener { document  ->
                            if (document.exists()){
                                val order = document.toObject(Order::class.java)!!
                                val verifyStatus = document.data?.get("status").toString().toInt()
                                //verficar aqui el algoritmo
                                if (hasDelivery == "si" && status == 1 || status == 3 || status == 4){
                                    var ctx = 0
                                    for (i in 0 until order.items.size) {
                                        if (order.items[i].delivery == "si"){
                                            ctx++
                                            if (ctx == order.items.size){
                                                mFireStore.collection(Constants.ORDERS).document(key).update(
                                                        map3
                                                )
                                            }
                                        }
                                    }
                                }
                                else {
                                    if (status > verifyStatus || verifyStatus == 4) {
                                        mFireStore.collection(Constants.ORDERS).document(key).update(
                                                map3
                                        )
                                    }
                                }

                            }
                        }.addOnFailureListener{
                            activity.hideProgressDialog()
                            Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                        }

                    }

                }.addOnFailureListener{ e ->
                    activity.hideProgressDialog()
                    Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show()
                    Log.e(
                            activity.javaClass.simpleName,
                            "Error while getting the list of sold products.",
                            e
                    )
                }

    }

    fun getAllWishList(activity: WishListListActivity){
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document->
                // Here we get the list of boards in the form of documents.
                Log.e("Products List", document.documents.toString())

                // Here we have created a new instance for Products ArrayList.
                val productsList: ArrayList<WishList> = ArrayList()

                // A for loop as per the list of documents to convert them into Products ArrayList.
                for (i in document.documents) {

                    val product = i.toObject(WishList::class.java)
                    product!!.key = i.id
                    productsList.add(product)
                    when(activity){
                        is WishListListActivity -> {
                            activity.successFavoriteListFromFireStore(productsList)
                        }
                    }
                }


            }
            .addOnFailureListener { e->
                // Hide the progress dialog if there is any error based on the base class instance.
                when (activity) {
                    is WishListListActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e("Get Product List", "Error while getting all product list.", e)
            }
    }

    fun getWishList(activity: WishListListActivity) {
        mFireStore.collection(Constants.FAVORITE_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                // Here we get the list of cart items in the form of documents.
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                // Here we have created a new instance for Favorite Items ArrayList.
                val list: ArrayList<WishList> = ArrayList()
                for(i in document.documents){
                    val favoriteItem = i.toObject(WishList::class.java)!!
                    favoriteItem.key = i.id
                    list.add(favoriteItem)
                }
                when (activity) {
                    is WishListListActivity -> {
                        activity.successFavoriteItemsList(list)
                    }
                }

            }
            .addOnFailureListener { e->
                // Hide the progress dialog if there is an error based on the activity instance.
                when (activity) {
                    is WishListListActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting the favorite list items.",
                        e
                )
            }

    }

    fun removeItemFromWishListAdapter(context: Context, favorite_id: String){
        mFireStore.collection(Constants.FAVORITE_ITEMS)
            .document(favorite_id)
            .delete()
            .addOnSuccessListener {
                when(context){
                    is WishListListActivity -> {
                        context.itemRemovedSuccessFavorite()
                    }
                }
            }
            .addOnFailureListener { e->
                when(context){
                    is ProductDetailsActivity -> {
                        context.hideProgressDialog()
                    }
                }
                Log.e(
                        context.javaClass.simpleName,
                        "Error while removing the item from the favorite list.",
                        e
                )
            }
    }


    fun removeItemFromWishList(activity: Context, title_from_activity: String){
        mFireStore.collection(Constants.FAVORITE_ITEMS).whereEqualTo(
                Constants.USER_ID,
                FirestoreClass().getCurrentUserID()
        ).get()
            .addOnSuccessListener { document->
                if(document !=null){
                    for(Query:QueryDocumentSnapshot in document){
                        val title :String = Query.data["title"].toString()
                        val key:String = Query.id
                        if(title == title_from_activity){
                            mFireStore.collection(Constants.FAVORITE_ITEMS).document(key).delete()
                                .addOnSuccessListener {
                                    when(activity){
                                        is ProductDetailsActivity -> {
                                            activity.itemRemovedFavoriteSuccess()
                                        }
                                    }

                                }
                                .addOnFailureListener { e->
                                    when(activity){
                                        is ProductDetailsActivity -> {
                                            activity.hideProgressDialog()
                                        }
                                    }
                                    Log.e(
                                            activity.javaClass.simpleName,
                                            "Error while removing the item from the favorite list.",
                                            e
                                    )

                                }
                        }
                    }
                }
            }
            .addOnFailureListener {

            }
    }

    fun checkIfItemExistInWishList(activity: ProductDetailsActivity, productId: String){
        mFireStore.collection(Constants.FAVORITE_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                if(document.documents.size > 0){
                    activity.productExistsInFavorite()
                } else{
                    activity.hideProgressDialog()
                }
            }
            .addOnFailureListener { e->
                Log.e(
                        activity.javaClass.simpleName,
                        "Error while checking the existing cart favorite.",
                        e
                )
            }
    }

    fun checkIfItemExistInWishListFromCache(activity: ProductDetailsActivity, productId: String){
        mFireStore.collection(Constants.FAVORITE_ITEMS)
                .whereEqualTo(Constants.USER_ID, getCurrentUserID())
                .whereEqualTo(Constants.PRODUCT_ID, productId)
                .get()
                .addOnSuccessListener { document->
                    Log.e(activity.javaClass.simpleName, document.documents.toString())
                    if(document.documents.size > 0){
                        activity.productExistsInFavoriteFromCache()
                    }
                }
                .addOnFailureListener { e->
                }
    }

    fun getWishListDetails(activity: ProductDetailsActivity, productId: String){
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document->
                // Here we get the favorite details in the form of document.
                Log.e(activity.javaClass.simpleName, document.toString())
                val product= document.toObject(WishList::class.java)!!
                activity.favoriteDetailsSuccess(product)

            }
            .addOnFailureListener { e->
                Log.e(activity.javaClass.simpleName, "Error while getting the favorite details.", e)
            }
    }

    fun getWishListDetailsFromCache(activity: ProductDetailsActivity, productId: String){
        mFireStore.collection(Constants.PRODUCTS)
                .document(productId)
                .get()
                .addOnSuccessListener { document->
                    // Here we get the favorite details in the form of document.
                    Log.e(activity.javaClass.simpleName, document.toString())
                    val product= document.toObject(WishList::class.java)!!
                    activity.favoriteDetailsSuccessFromCache(product)

                }
                .addOnFailureListener { e->
                    Log.e(activity.javaClass.simpleName, "Error while getting the favorite details.", e)
                }
    }

    fun addWishListItems(activity: ProductDetailsActivity, addToFavorite: WishList){
        mFireStore.collection(Constants.FAVORITE_ITEMS)
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(addToFavorite, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToFavoriteSuccess()
            }
            .addOnFailureListener { e->
                //activity.hideProgressDialog()

                Log.e(
                        activity.javaClass.simpleName,
                        "Error while creating the document for cart item.",
                        e
                )

            }

    }

    fun getAllSliders(context : Context){
        val tinyDB = TinyDB(context)

        FirebaseDatabase.getInstance().reference.child("Slider")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val sliderList: ArrayList<Slider> = ArrayList()
                            for (snapshot in dataSnapshot.children) {
                                val model = snapshot.getValue(Slider::class.java)!!
                                val path: String = context.getExternalFilesDir(null).toString()+"/slider/"+model.id+".jpg"
                                val file = File(path)
                                sliderList.add(model)
                                if (!file.exists()){
                                    getBitmapSliderFromURL(model.url, context,model.id)
                                }
                            }
                            tinyDB.putListSlider(Constants.CACHE_SLIDER,sliderList)
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
    }

    fun getCategories(context : Context){

        val tinyDB = TinyDB(context)
        mFireStore.collection(Constants.CATEGORIES).get().addOnSuccessListener { document ->
            val list: ArrayList<Categories> = ArrayList()
            for (i in document.documents) {
                val category = i.toObject(Categories::class.java)!!
                list.add(category)
                getBitmapCategoriesFromURL(category.foto,context,category.id)
            }
            tinyDB.putListCategories(Constants.CATEGORIES,list)
        }

    }

    fun cancelOrderFromOnDelivery(id : String,activity: MyOrderDetailsActivity){
        mFireStore.collection(Constants.ORDERS).document(id).update("status",-1).addOnCompleteListener {
            if(it.isSuccessful){
                Toast.makeText(activity,"Pedido Cancelado!",Toast.LENGTH_SHORT).show()
                activity.hideProgressDialog()
                activity.finish()
            }else{
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            activity.hideProgressDialog()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            activity.hideProgressDialog()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelOrderFromCreditCard(id : String,activity: MyOrderDetailsActivity){
        mFireStore.collection(Constants.ORDERS).document(id).update("status",-1).addOnCompleteListener {
            if(it.isSuccessful){
                activity.hideProgressDialog()
                Toast.makeText(activity,"Pedido Cancelado!",Toast.LENGTH_SHORT).show()
                activity.finish()
            }else{
                activity.hideProgressDialog()
                Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            activity.hideProgressDialog()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            activity.hideProgressDialog()
            Toast.makeText(activity,"Error!",Toast.LENGTH_SHORT).show()
        }
    }

    /*
    fun availabilityQuery() {
        mFireStore.collection(Constants.PRODUCTS).get().addOnSuccessListener { doc ->
            for (i in doc.documents) {
                mFireStore.collection(Constants.PRODUCTS).document(i.id)
                    .update("availability", Constants.YES)
            }
        }
    }*/

    fun availabilityProduct(id:String , activity: ProductDetailsActivity) {
        mFireStore.collection(Constants.PRODUCTS).document(id).addSnapshotListener { doc , error ->
            if (doc!!.exists()){
                val product = doc.data!!["availability"].toString()
                activity.availability(product)
            }
        }
    }

}
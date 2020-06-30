package com.saimhassan.uberkotlin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit

class SplashScreen : AppCompatActivity() {

    companion object{
        private val LOG_IN_REQUEST_CODE = 7171;
    }

    private lateinit var providers:List<AuthUI.IdpConfig>
    private lateinit var firebaseAuth:FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private lateinit var database:FirebaseDatabase
    private lateinit var driverInfoRef:DatabaseReference

    override fun onStart() {
        super.onStart()
        delaySplashScreen();
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    private fun delaySplashScreen() {
        Completable.timer(3,TimeUnit.SECONDS,AndroidSchedulers.mainThread())
            .subscribe({
                firebaseAuth.addAuthStateListener(listener)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actiity_splash_screen)
        init()

    }

    private fun init() {
        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REF)
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->
            val user = myFirebaseAuth.currentUser
            if (user != null)
            {
                checkUserFromFirebase()
            }
            else
                showLoginLayout()
        }
    }

    private fun checkUserFromFirebase() {
        driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SplashScreen,error.message,Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        Toast.makeText(this@SplashScreen,"user already register",Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                       showRegisterLayout()
                    }
                }

            })
    }

    private fun showRegisterLayout() {
        val builder = AlertDialog.Builder(this,R.style.DialogTheme)
        val itemiew = LayoutInflater.from(this).inflate(R.layout.layout_register,null)

        val edt_first_name = itemiew.findViewById<View>(R.id.edt_first_name) as TextInputEditText
        val edt_last_name = itemiew.findViewById<View>(R.id.edt_last_name) as TextInputEditText
        val edt_phone_number = itemiew.findViewById<View>(R.id.edit_phone_number) as TextInputEditText
        val btn_register = itemiew.findViewById<View>(R.id.btn_continue) as Button

        //Set Data
        if (FirebaseAuth.getInstance().currentUser!!.phoneNumber != null && !TextUtils.isDigitsOnly(FirebaseAuth.getInstance().currentUser!!.phoneNumber))

            //View
            builder.setView(itemiew)
            val dialog = builder.create()
            dialog.show()
        //Event
        btn_register.setOnClickListener{
            if (TextUtils.isDigitsOnly(edt_first_name.text.toString()))
            {
                Toast.makeText(this@SplashScreen,"Please enter first name",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (TextUtils.isDigitsOnly(edt_last_name.text.toString()))
            {
                Toast.makeText(this@SplashScreen,"Please enter last name",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isDigitsOnly(edt_phone_number.text.toString()))
            {
                Toast.makeText(this@SplashScreen,"Please enter phone number",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else
            {

            }
        }


    }

    private fun showLoginLayout() {
        val authMethodPickerLayout = AuthMethodPickerLayout.Builder(R.layout.layout_sign_in)
            .setPhoneButtonId(R.id.btn_phone_sign_in)
            .setGoogleButtonId(R.id.btn_email_sign_in)
            .build();
       startActivityForResult(
           AuthUI.getInstance()
               .createSignInIntentBuilder()
               .setAuthMethodPickerLayout(authMethodPickerLayout)
               .setTheme(R.style.LogInTheme)
               .setAvailableProviders(providers)
               .setIsSmartLockEnabled(false)
               .build()
           ,LOG_IN_REQUEST_CODE
       )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOG_IN_REQUEST_CODE){
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK)
            {
                val user = FirebaseAuth.getInstance().currentUser
            }
            else
                Toast.makeText(this@SplashScreen,""+response!!.error!!.message,Toast.LENGTH_SHORT).show()
        }

    }
}
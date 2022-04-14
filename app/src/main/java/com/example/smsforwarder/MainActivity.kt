package com.example.smsforwarder

import android.Manifest
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi

class MainActivity : AppCompatActivity() {


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(
            arrayOf(
                Manifest.permission.SEND_SMS
            ), 0
        )
        val sharedPreferences =
            getSharedPreferences(getString(R.string.app_shared_preferences_key), MODE_PRIVATE)
        initPhoneNumberInput(sharedPreferences)
    }

    private fun initPhoneNumberInput(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val editText = findViewById<View>(R.id.targetPhoneNumberInput) as EditText
        editText.setText(
            sharedPreferences.getString(
                "lastMessage",
                getString(R.string.default_forward_number)
            )
        )
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                editor.putString(getString(R.string.target_phone_number_key), s.toString()).apply()
            }
        })
    }
}
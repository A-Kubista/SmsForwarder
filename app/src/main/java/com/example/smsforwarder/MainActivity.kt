package com.example.smsforwarder

import android.Manifest
import android.Manifest.permission.RECEIVE_SMS
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.permissions).setOnClickListener{
                    requestPermissions(  arrayOf(
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.INTERNET,
                        Manifest.permission.SEND_SMS
                    ), 1 )


        }

        val sharedPreferences =
            getSharedPreferences(getString(R.string.app_shared_preferences_key), MODE_PRIVATE)
        initPhoneNumberInput(sharedPreferences)

        val lastMsg = findViewById<View>(R.id.lastMsg) as TextView
        lastMsg.text = sharedPreferences.getString(
            "lastMessage",
            "no message in cache"
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "pr " + requestCode + "  result ture ", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initPhoneNumberInput(sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        val editText = findViewById<View>(R.id.targetPhoneNumberInput) as EditText
        editText.setText(
            sharedPreferences.getString(
                "number",
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
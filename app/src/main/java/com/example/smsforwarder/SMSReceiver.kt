package com.example.smsforwarder

import android.content.BroadcastReceiver
import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.Intent
import com.example.smsforwarder.SMSReceiver
import android.os.Bundle
import android.content.SharedPreferences
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.smsforwarder.R
import java.nio.charset.Charset

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != SMS_RECEIVED_ACTION) return
        val bundle = intent.extras
        val pduObjects = bundle!!["pdus"] as Array<Any>? ?: return
        val sharedPreferences =
            context.getSharedPreferences(context.getString(R.string.app_shared_preferences_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val targetNumber = sharedPreferences.getString(
            "number",
            context.getString(R.string.default_forward_number)
        )
        for (messageObj in pduObjects) {

            val currentMessage = SmsMessage.createFromPdu(messageObj as ByteArray, bundle["format"] as String?)
            editor.putString("lastMessage", "lst msg " + currentMessage.messageBody).apply()
            val senderNumber = currentMessage.displayOriginatingAddress
            val rawMessageContent = currentMessage.displayMessageBody

            /**
             * when receive a message from ordinary numbers
             * forward the message to target number in this format:
             * 'from `senderNumber` :`messageContent`'
             *
             * when receive a message from the target number
             * e.g. target phone replying the message
             * the format should be:
             * 'to `toNumber` :`messageContent`'
             * then forward the message to 'toNumber'
             */
            var forwardNumber: String? = null
            var forwardPrefix: String? = null
            var forwardContent: String? = null
            if (senderNumber.equals(targetNumber)) {
                postReceivedSms(rawMessageContent,context)
            } else {
                // it's a normal message, need to be forwarded
                forwardNumber = targetNumber
                forwardPrefix = "from $senderNumber:\n"
                forwardContent = rawMessageContent
            }
            if (forwardNumber != null && forwardContent != null) {
                if ((forwardPrefix + forwardContent).toByteArray().size > 120) {
                    // there is a length limit in SMS, if the message length exceeds it, separate the meta data and content
                    smsManager.sendTextMessage(forwardNumber, null, forwardPrefix, null, null)
                    smsManager.sendTextMessage(forwardNumber, null, forwardContent, null, null)
                } else {
                    // if it's not too long, combine meta data and content to save money
                    smsManager.sendTextMessage(forwardNumber, null, forwardPrefix + forwardContent, null, null)
                }
            }
        }
    }

    fun postReceivedSms(msg : String, context :  Context) {
        val queue = Volley.newRequestQueue(context)
        val url = "https://github.com/ecoenergysoultions/sms-forwarder/blob/master/forwarder/handler.py"

        val requestBody = "&msg=${msg}"
        val stringReq : StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()
                    Log.d("API", strResp)
                },
                Response.ErrorListener { error ->
                    Log.d("API", "error => $error")
                }
            ){
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        queue.add(stringReq)
    }

    companion object {
        const val SMS_RECEIVED_ACTION = Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        val smsManager = SmsManager.getDefault()
    }
}
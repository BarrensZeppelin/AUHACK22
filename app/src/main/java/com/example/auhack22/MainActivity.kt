package com.example.auhack22

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentFilter.MalformedMimeTypeException
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.*
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


private const val TAG = "MyActivity"

class MainActivity : AppCompatActivity() {
    val MIME_TEXT_PLAIN = "text/plain"
    private var mNfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val room = findViewById<RadioGroup>(R.id.radioGroup)
        room.setOnCheckedChangeListener { radioGroup, i ->
            Log.i(TAG, findViewById<RadioButton>(i).text.toString())
        }

        Log.i(TAG, "We're ready!")
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    override fun onResume() {
        super.onResume()
        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter!!)
    }

    override fun onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter!!)
        super.onPause()
    }

    /**
     * @param activity The corresponding [BaseActivity] requesting to stop the foreground dispatch.
     * @param adapter The [NfcAdapter] used for the foreground dispatch.
     */
    fun stopForegroundDispatch(activity: Activity?, adapter: NfcAdapter) {
        adapter.disableForegroundDispatch(activity)
    }

    /**
     * @param activity The corresponding [Activity] requesting the foreground dispatch.
     * @param adapter The [NfcAdapter] used for the foreground dispatch.
     */
    fun setupForegroundDispatch(activity: Activity, adapter: NfcAdapter) {
        val intent = Intent(activity, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, intent, 0)
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")    /* Handles all MIME based dispatches.
                                 You should specify only the ones that you need. */
            } catch (e: MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }

        val filters = arrayOf<IntentFilter>() // ndef
        val techs = arrayOf<Array<String>>() //arrayOf<String>(Ndef::class.java.name, NfcF::class.java.name, NfcV::class.java.name, NfcA::class.java.name, NfcB::class.java.name))

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techs)
    }

    val idMap = mapOf(114 to "Oskar", 110 to "Camilla", 106 to "Unknown 2")

    override fun onNewIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            Log.i(TAG, "Got message $intent")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array.
                Log.i(TAG, messages.toString())
            }
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            Log.i(TAG, "Got message $intent")
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)!!
            val id: Int = tag.id[1].toInt()
            //Log.i(TAG, tag.id.joinToString(separator = ", ") { it.toString() })
            val name: String? = idMap[id]
            if(name == null) {
                Log.w(TAG, "Unrecognised ID $id")
            } else {
                Log.i(TAG, "Hello $name!")
            }
        } else super.onNewIntent(intent)
    }

}
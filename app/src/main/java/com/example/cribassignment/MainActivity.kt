package com.example.cribassignment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*


class MainActivity : AppCompatActivity() {

    private val list = ArrayList<String>()
    private lateinit var mTvText: TextView
    lateinit var mEtNumber: EditText
    lateinit var mEtDays: EditText
    lateinit var mBtnFetchData: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTvText = findViewById<TextView>(R.id.tvText)
        mEtNumber = findViewById<EditText>(R.id.etNumber)
        mEtDays = findViewById<EditText>(R.id.etDays)
        mBtnFetchData = findViewById<Button>(R.id.btnFetchData)

        /**
         * checking Read SMS permission is granted or not
         */
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), 0)
        }

        /**
         * handle the button click
         */
        mBtnFetchData.setOnClickListener {

            list.clear()
            val ph = mEtNumber.text.toString()
            val d = mEtDays.text.toString()

            if (ph.isEmpty() || d.isEmpty()) {
                list.clear()
                mTvText.text = "Sorry,no messages found"
            }

            else if (ph.isNotEmpty() && d.isNotEmpty()) {
                val uri = Uri.parse("content://sms/inbox")
                val cursor = contentResolver.query(uri, null, null, null, null)
                while (cursor?.moveToNext() == true) {
                    val number = cursor.getString(2)
                    val message = cursor.getString(12)

                    val smsDate: String =
                        cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val dateFormat = Date(java.lang.Long.valueOf(smsDate))
                    val formatDate: String = SimpleDateFormat("dd-MM-yyyy").format(dateFormat)

                    val days = filterDays(formatDate)
                    Log.d("TAG", "days => $days")

                    if (number.contains(ph) && days <= d.toLong()) {
                        list.add("$number \n $message \n $formatDate")
                    }
                }
                if (list.size > 0) {
                    mTvText.text = "${list.size.toString()} messages found"

                } else {
                    list.clear()
                    mTvText.text = "Sorry,no messages found"
                }
            }
        }
    }


    /**
     * Calculate days from two DATES(dd-mm-yyyy | dd-mm-yyyy)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun filterDays(oldDate: String): Long {

        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val currentDate = LocalDate.now()
        val d = dtf.format(currentDate)

        val startDateValue = LocalDate.parse(oldDate, dtf)
        val endDateValue = LocalDate.parse(d, dtf)

        return ChronoUnit.DAYS.between(startDateValue, endDateValue) + 1

    }

}
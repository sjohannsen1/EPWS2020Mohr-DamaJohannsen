package com.example.jetsetfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class POC_DataReq: AppCompatActivity() {
    val gson= Gson()
    val produceType=object: TypeToken<Produce>(){}.type
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    lateinit var produceInput:TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {

        fun getCountryName(produce: Produce):String{
            var res=""
            produce.origin[currentMonth].land.forEach{land ->
                countryListe.forEach{
                    if(it.first==land)
                        res+="${it.third}, "
                }
            }
            return res
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poc__data_req)
        produceInput=findViewById(R.id.produce)
        val button:Button=findViewById(R.id.click)
        val datenblatt:TextView=findViewById(R.id.datenblatt)

        datenblatt.visibility=View.INVISIBLE

        button.setOnClickListener{
            if(produceInput.text.toString().isEmpty())
                produceInput.error=("Bitte gib eine Obst oder Gemüsesorte ein")
            else if(!produceListe.contains(produceInput.text.toString().toLowerCase())) {
                produceInput.error = "Dieses Obst oder Gemüse wird leider noch nicht unterstützt!"
                datenblatt.text="Unterstützes Obst und Gemüse: ${produceString} "
                datenblatt.visibility= View.VISIBLE

            }
            else {
                val jsonString = getJsonDataFromAsset(this,produceInput.text.toString().toLowerCase() +".json")
                val data:Produce = gson.fromJson(jsonString,produceType)
                datenblatt.text="Im ${currentMonth+1}. Monat im Jahr kann man ${data.name} aus ${getCountryName(data)} kaufen"
                datenblatt.visibility=View.VISIBLE


            }
        }



    }
}

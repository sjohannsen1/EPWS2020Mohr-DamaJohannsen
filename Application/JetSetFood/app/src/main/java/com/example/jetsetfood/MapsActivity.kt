package com.example.jetsetfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

class MapsActivity : AppCompatActivity(){
    //Handles für die View objekte werden onCreate gesetzt
    private lateinit var produceInput: TextInputEditText
    private lateinit var textView: TextView
    private lateinit var button:Button

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype)
        produceInput=findViewById(R.id.produce)
        textView=findViewById(R.id.datenblatt)
        textView.visibility= View.INVISIBLE
        button=findViewById(R.id.click)

        //Alle unterstützen Produkte aus der Datenbank extrahieren
        val produceList=runBlocking {
            async{ DatabaseUtil.getProduceList().map { cur ->
                ProduceUtil.convertUmlaut(cur.name,false)
            }
            }.await()
        }

        button.setOnClickListener{
            //Nachricht falls die Eingabe leer ist
            if(produceInput.text.toString().isEmpty())
                produceInput.error=("Bitte gib eine Obst oder Gemüsesorte ein")

            //Falls die Eingabe nicht in der Datenbank vorhanden ist
            else if(!produceList.map { it.toLowerCase() }.contains(produceInput.text.toString().toLowerCase())) {
                Log.d("ProduceList", "input: ${produceInput.text}")
                produceInput.error =
                    "Dieses Obst oder Gemüse wird leider noch nicht unterstützt!"

                //Gibt unterstütze Produkte aus
                textView.text= getString(R.string.produceListe, ProduceUtil.makeString(produceList, "\n", null))
                textView.visibility= View.VISIBLE
            }
            else {
                //wechselt zur zweiten Aktivität und hängt die Nutzereingabe als extra dran
                startActivity(Intent(this, PrototypeMap::class.java).putExtra("input", produceInput.text.toString()))
            }
        }



    }
}

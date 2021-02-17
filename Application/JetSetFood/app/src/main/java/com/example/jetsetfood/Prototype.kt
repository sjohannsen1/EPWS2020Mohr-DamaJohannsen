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

class Prototype : AppCompatActivity() {

    lateinit var produceInput:TextInputEditText
    lateinit var textView:TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype)
        produceInput=findViewById(R.id.produce)
        textView=findViewById(R.id.datenblatt)
        textView.visibility=View.INVISIBLE
        val button:Button=findViewById(R.id.click)
        val produceList:List<String> =runBlocking {
            async{ DatabaseUtil.getProduceList().map { cur ->
                ProduceUtil.convertUmlaut(cur.name,false)
            }
            }.await()
        }
        button.setOnClickListener{
                if(produceInput.text.toString().isEmpty())
                    produceInput.error=("Bitte gib eine Obst oder Gemüsesorte ein")
                //else if(!produceListe.contains(produceInput.text.toString().toLowerCase())) {
                else if(! produceList?.map { it.toLowerCase() }?.contains(produceInput.text.toString().toLowerCase())) {
                    Log.d("ProduceList", "input: ${produceInput.text}")
                    //produceliste=API get /produce
                    produceInput.error =
                        "Dieses Obst oder Gemüse wird leider noch nicht unterstützt!"
                    textView.text="Unterstützes Obst und Gemüse: \n${ProduceUtil.makeString(produceList, "\n", null)} "
                    //textView.text=produceList.toString()
                    textView.visibility= View.VISIBLE
                }
                else {
                    startActivity(Intent(this, PrototypeMap::class.java).putExtra("input", produceInput.text.toString()))
                }
        }



    }
}

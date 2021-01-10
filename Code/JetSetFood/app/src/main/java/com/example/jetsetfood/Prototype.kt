package com.example.jetsetfood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

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
        button.setOnClickListener{
                if(produceInput.text.toString().isEmpty())
                    produceInput.error=("Bitte gib eine Obst oder Gemüsesorte ein")
                else if(!produceListe.contains(produceInput.text.toString().toLowerCase())) {
                    produceInput.error =
                        "Dieses Obst oder Gemüse wird leider noch nicht unterstützt!"
                    textView.text="Unterstützes Obst und Gemüse: ${produceString} "
                    textView.visibility= View.VISIBLE
                }
                else {

                    startActivity(Intent(this, PrototypeMap::class.java).putExtra("input", produceInput.text.toString()))

                }
        }



    }
}

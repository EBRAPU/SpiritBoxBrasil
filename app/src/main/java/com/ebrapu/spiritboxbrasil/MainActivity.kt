package com.ebrapu.spiritboxbrasil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Alterar o plano de fundo
        val imageView = findViewById<ImageView>(R.id.backgroundImage)
        imageView.setImageResource(R.drawable.itc_transcomunicacao)

        val startButton = findViewById<Button>(R.id.startButton)
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        val descriptionTextView = findViewById<TextView>(R.id.descriptionTextView)

        // Não definir o texto programaticamente para que o texto do XML seja usado
        // titleTextView.text = "Spirit Box Brasil - Contatos Espirituais"
        // descriptionTextView.text = """
        //     Que os bons espíritos estejam com você! Esse aplicativo foi desenvolvido com o objetivo de permitir as pessoas terem contatos espirituais reais com o mundo espiritual. Não recomendo usar esse aplicativo para brincar. Ao tentar fazer seu primeiro contato, experimente chamar pela estação "Amor Divino". Esse é o nome de uma das estações de comunicação que existe no plano astral. Lá tem vários espíritos especializados na comunicação através de aparelhos eletrônicos e eles sabem usar esse aplicativo para se comunicar. Para começar sua primeira sessão de comunicação espiritual, clique no botão abaixo e entre na sala.
        // """.trimIndent()

        startButton.text = "Sala de Contatos"
        startButton.setOnClickListener {
            val intent = Intent(this, ContatosActivity::class.java)
            startActivity(intent)
        }
    }
}

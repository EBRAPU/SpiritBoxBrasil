package com.ebrapu.spiritboxbrasil

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.InputStream
import kotlin.random.Random

class ContatosActivity : AppCompatActivity() {

    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private val audioFiles = (1..2027).toList()
    private var isPlaying1 = false
    private var isPlaying2 = false

    private val requestMicrophonePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.i("ContatosActivity", "Permissão de microfone concedida")
            } else {
                Log.i("ContatosActivity", "Permissão de microfone negada")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contatos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Solicitar permissão de microfone ao iniciar a atividade
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestMicrophonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            Log.i("ContatosActivity", "Permissão de microfone já concedida")
        }

        // Configuração dos botões do Canal 1
        val conectarButton1 = findViewById<Button>(R.id.conectarButton1)
        val desconectarButton1 = findViewById<Button>(R.id.desconectarButton1)
        val ecoButton1 = findViewById<Button>(R.id.ecoButton1) // Novo botão para eco

        conectarButton1.setOnClickListener {
            isPlaying1 = true
            playRandomAudio(true)
            conectarButton1.visibility = View.GONE
            desconectarButton1.visibility = View.VISIBLE
        }

        desconectarButton1.setOnClickListener {
            isPlaying1 = false
            mediaPlayer1?.stop()
            mediaPlayer1?.release()
            mediaPlayer1 = null
            desconectarButton1.visibility = View.GONE
            conectarButton1.visibility = View.VISIBLE
        }

        ecoButton1.setOnClickListener {
            toggleEco()
        }

        // Configuração dos botões do Canal 2
        val conectarButton2 = findViewById<Button>(R.id.conectarButton2)
        val desconectarButton2 = findViewById<Button>(R.id.desconectarButton2)
        val ecoButton2 = findViewById<Button>(R.id.ecoButton2) // Novo botão para eco

        conectarButton2.setOnClickListener {
            isPlaying2 = true
            playRandomAudio(false)
            conectarButton2.visibility = View.GONE
            desconectarButton2.visibility = View.VISIBLE
        }

        desconectarButton2.setOnClickListener {
            isPlaying2 = false
            mediaPlayer2?.stop()
            mediaPlayer2?.release()
            mediaPlayer2 = null
            desconectarButton2.visibility = View.GONE
            conectarButton2.visibility = View.VISIBLE
        }

        ecoButton2.setOnClickListener {
            toggleEco()
        }

        // Carregue a imagem de fundo a partir dos assets
        val imageView = findViewById<ImageView>(R.id.backgroundImage)
        val inputStream: InputStream = assets.open("ITC_transcomunicação.jpg")
        val drawable = Drawable.createFromStream(inputStream, null)
        imageView.setImageDrawable(drawable)
    }

    private fun playRandomAudio(isCanal1: Boolean) {
        if (isCanal1) {
            if (!isPlaying1) return

            val randomAudioFile = audioFiles.random()
            mediaPlayer1?.release()

            val resID = resources.getIdentifier("vozes$randomAudioFile", "raw", packageName)
            mediaPlayer1 = MediaPlayer.create(this, resID).apply {
                setOnCompletionListener {
                    it.release()
                    if (isPlaying1) playRandomAudio(isCanal1)
                }
            }
            mediaPlayer1?.start()

        } else {
            if (!isPlaying2) return

            val randomAudioFile = audioFiles.random()
            mediaPlayer2?.release()

            val resID = resources.getIdentifier("vozes$randomAudioFile", "raw", packageName)
            mediaPlayer2 = MediaPlayer.create(this, resID).apply {
                setOnCompletionListener {
                    it.release()
                    if (isPlaying2) playRandomAudio(isCanal1)
                }
            }
            mediaPlayer2?.start()
        }
    }

    private fun toggleEco() {
        // Lógica para ativar/desativar o efeito eco
        // Pode envolver alterações no MediaPlayer ou processamento adicional
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer1?.release()
        mediaPlayer2?.release()
    }
}

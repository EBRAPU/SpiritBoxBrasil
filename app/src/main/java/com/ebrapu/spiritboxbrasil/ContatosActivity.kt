package com.ebrapu.spiritboxbrasil

import com.ebrapu.spiritboxbrasil.MyForegroundService
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import java.io.File
import java.io.IOException
import kotlin.random.Random
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ContatosActivity : AppCompatActivity() {

    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputUri: Uri? = null

    private val maxMaleFiles = 4695
    private val maxFemaleFiles = 4677
    private val maxFemaleFiles2 = 4410
    private val maxMaleFiles2 = 4000
    // Definindo as novas variáveis
    private val maxMaleReverbFiles = 2248
    private val maxMaleEcoFiles = 2270
    private val maxFemaleReverbFiles = 2220
    private val maxFemaleEcoFiles = 2220

    private var isPlaying1 = false
    private var isPlaying2 = false
    private var playbackSpeed1 = 0.70f // Velocidade padrão de reprodução (velocidade normal)
    private var playbackSpeed2 = 0.70f // Velocidade padrão de reprodução (velocidade normal)

    private lateinit var speedTextView1: TextView
    private lateinit var speedTextView2: TextView
    private lateinit var genderSpinner1: Spinner
    private lateinit var genderSpinner2: Spinner

    private lateinit var rotatingImageView: ImageView
    private var rotationAnimation: Animation? = null

    private val RECORD_REQUEST_CODE = 101

    private val isEcoOn = AtomicBoolean(false)
    private var audioThread: Thread? = null

    private val volumeFactorAtomic = AtomicReference(0.5f)
    private val delayBufferSize = AtomicInteger(44100)

    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contatos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val volumeSeekBar: SeekBar = findViewById(R.id.volumeSeekBar)
        val delaySeekBar: SeekBar = findViewById(R.id.delaySeekBar)

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeFactorAtomic.set(progress / 100f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        delaySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                delayBufferSize.set(44100 * progress / 1000)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val ecoButton: Button = findViewById(R.id.ecoButton)
        ecoButton.setOnClickListener {
            if (isEcoOn.get()) {
                stopEcho()
                ecoButton.text = "ECO OFF"
            } else {
                startEcho()
                ecoButton.text = "ECO ON"
            }
        }


        // Inicializar o ImageView e a animação
        rotatingImageView = findViewById(R.id.rotatingImage)
        rotationAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)


        // Mantenha a tela ligada enquanto a atividade estiver visível
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Iniciar o serviço em primeiro plano
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            startService(serviceIntent)
        }

        // Configuração dos botões do Canal 1
        val conectarButton1 = findViewById<Button>(R.id.conectarButton1)
        val desconectarButton1 = findViewById<Button>(R.id.desconectarButton1)
        val speedUpButton1 = findViewById<Button>(R.id.speedUpButton1)
        val speedDownButton1 = findViewById<Button>(R.id.speedDownButton1)
        speedTextView1 = findViewById(R.id.speedTextView1)
        genderSpinner1 = findViewById(R.id.genderSpinner1)

        conectarButton1.setOnClickListener {
            isPlaying1 = true
            playRandomAudio(true)
            conectarButton1.visibility = View.GONE
            desconectarButton1.visibility = View.VISIBLE

            // Iniciar animação
            updateImageVisibility() // Atualizar a visibilidade da imagem
            rotatingImageView.visibility = View.VISIBLE
            rotatingImageView.startAnimation(rotationAnimation)
        }

        desconectarButton1.setOnClickListener {
            isPlaying1 = false
            mediaPlayer1?.stop()
            desconectarButton1.visibility = View.GONE
            conectarButton1.visibility = View.VISIBLE

            // Parar animação
            updateImageVisibility() // Atualizar a visibilidade da imagem
            rotatingImageView.clearAnimation()
            rotatingImageView.visibility = View.GONE
        }

        speedUpButton1.setOnClickListener {
            if (playbackSpeed1 < 5.0f) { // Limite máximo de 5 vezes a velocidade normal
                playbackSpeed1 += 0.025f
                if (playbackSpeed1 > 5.0f) playbackSpeed1 = 5.0f // Garantir que a velocidade não exceda o máximo
                updateSpeedTextView()
                mediaPlayer1?.let { player -> setPlaybackSpeed(player, playbackSpeed1) }
            }
        }

        speedDownButton1.setOnClickListener {
            if (playbackSpeed1 > 0.05f) { // Limite mínimo de 0.05 vezes a velocidade normal
                playbackSpeed1 -= 0.025f
                if (playbackSpeed1 < 0.05f) playbackSpeed1 = 0.05f // Garantir que a velocidade não fique abaixo do mínimo
                updateSpeedTextView()
                mediaPlayer1?.let { player -> setPlaybackSpeed(player, playbackSpeed1) }
            }
        }

        // Configuração dos botões do Canal 2
        val conectarButton2 = findViewById<Button>(R.id.conectarButton2)
        val desconectarButton2 = findViewById<Button>(R.id.desconectarButton2)
        val speedUpButton2 = findViewById<Button>(R.id.speedUpButton2)
        val speedDownButton2 = findViewById<Button>(R.id.speedDownButton2)
        speedTextView2 = findViewById(R.id.speedTextView2)
        genderSpinner2 = findViewById(R.id.genderSpinner2)

        conectarButton2.setOnClickListener {
            isPlaying2 = true
            playRandomAudio(false)
            conectarButton2.visibility = View.GONE
            desconectarButton2.visibility = View.VISIBLE

            // Iniciar animação
            updateImageVisibility() // Atualizar a visibilidade da imagem
            rotatingImageView.visibility = View.VISIBLE
            rotatingImageView.startAnimation(rotationAnimation)
        }

        desconectarButton2.setOnClickListener {
            isPlaying2 = false
            mediaPlayer2?.stop()
            desconectarButton2.visibility = View.GONE
            conectarButton2.visibility = View.VISIBLE

            // Parar animação
            updateImageVisibility() // Atualizar a visibilidade da imagem
            rotatingImageView.clearAnimation()
            rotatingImageView.visibility = View.GONE
        }

        speedUpButton2.setOnClickListener {
            if (playbackSpeed2 < 5.0f) { // Limite máximo de 5 vezes a velocidade normal
                playbackSpeed2 += 0.025f
                if (playbackSpeed2 > 5.0f) playbackSpeed2 = 5.0f // Garantir que a velocidade não exceda o máximo
                updateSpeedTextView()
                mediaPlayer2?.let { player -> setPlaybackSpeed(player, playbackSpeed2) }
            }
        }

        speedDownButton2.setOnClickListener {
            if (playbackSpeed2 > 0.05f) { // Limite mínimo de 0.05 vezes a velocidade normal
                playbackSpeed2 -= 0.025f
                if (playbackSpeed2 < 0.05f) playbackSpeed2 = 0.05f // Garantir que a velocidade não fique abaixo do mínimo
                updateSpeedTextView()
                mediaPlayer2?.let { player -> setPlaybackSpeed(player, playbackSpeed2) }
            }
        }

        // Configuração do botão GRAVAR
        val recordButton = findViewById<Button>(R.id.recordButton)
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                recordButton.text = "GRAVAR"
                isRecording = false
            } else {
                startRecording()
                recordButton.text = "GRAVANDO"
                isRecording = true
            }
        }

        // Configuração do botão VOLTAR
        val voltarButton = findViewById<Button>(R.id.voltarButton)
        voltarButton.setOnClickListener {
            finish()
        }

        // Carregue a imagem de fundo a partir dos assets
        val imageView = findViewById<ImageView>(R.id.backgroundImage)
        val inputStream: InputStream = assets.open("ITC_transcomunicação.jpg")
        val drawable = Drawable.createFromStream(inputStream, null)
        imageView.setImageDrawable(drawable)

        // Verificar permissões ao iniciar a atividade
        checkPermissions()
    }

    private fun startRecording() {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateAndTime = dateFormat.format(Date())
        val fileName = "SpiritBox_${currentDateAndTime}.m4a"

        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC + "/SpiritBoxBrasil")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        outputUri = Uri.fromFile(file)

        val parcelFileDescriptor = contentResolver.openFileDescriptor(outputUri!!, "w")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(fileDescriptor)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateImageVisibility() {
        if (!isPlaying1 && !isPlaying2) {
            // Parar animação e esconder a imagem
            rotatingImageView.clearAnimation()
            rotatingImageView.visibility = View.GONE
        } else {
            // Iniciar animação e mostrar a imagem
            if (rotatingImageView.visibility != View.VISIBLE) {
                rotatingImageView.visibility = View.VISIBLE
                rotatingImageView.startAnimation(rotationAnimation)
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                reset() // Limpa o estado do MediaRecorder
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                release() // Libera os recursos do MediaRecorder
            }
        }
    }

    private fun playRandomAudio(isCanal1: Boolean) {
        if (isCanal1) {
            if (!isPlaying1) return

            val selectedGender = genderSpinner1.selectedItem.toString()
            val (startIndex, endIndex) = when (selectedGender) {
                "Masculino2" -> 1 to maxMaleFiles2
                "Feminino2" -> 1 to maxFemaleFiles2
                "MasculinoReverb" -> 1 to maxMaleReverbFiles
                "FemininoReverb" -> 1 to maxFemaleReverbFiles
                "MasculinoEco" -> 1 to maxMaleEcoFiles
                "FemininoEco" -> 1 to maxFemaleEcoFiles
                "Masculino1" -> 1 to maxMaleFiles
                "Feminino1" -> 1 to maxFemaleFiles
                "Padrão" -> 1 to (maxMaleFiles + maxFemaleFiles)
                else -> 1 to maxMaleFiles
            }

            val randomAudioFile = when (selectedGender) {
                "Masculino2" -> "masculino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino2" -> "feminino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "MasculinoReverb" -> "masculinoreverbdeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "FemininoReverb" -> "femininoreverbdeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "MasculinoEco" -> "masculinoecodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "FemininoEco" -> "femininoecodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Masculino1" -> "masculinodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino1" -> "femininodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Padrão" -> {
                    val isMale = Random.nextBoolean()
                    val fileIndex = if (isMale) {
                        "masculinodeusluzamormentoresdaluz${(startIndex..maxMaleFiles).random()}"
                    } else {
                        "femininodeusluzamormentoresdaluz${(startIndex..maxFemaleFiles).random()}"
                    }
                    fileIndex
                }
                else -> "masculinodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
            }

            mediaPlayer1?.release()

            val resID = resources.getIdentifier(randomAudioFile, "raw", packageName)
            mediaPlayer1 = MediaPlayer.create(this, resID).apply {
                setPlaybackParams(PlaybackParams().apply {
                    speed = playbackSpeed1
                })
                setOnCompletionListener {
                    it.release()
                    if (isPlaying1) playRandomAudio(isCanal1)
                    updateImageVisibility() // Atualizar a visibilidade da imagem
                }
            }
            mediaPlayer1?.start()

        } else {
            if (!isPlaying2) return

            val selectedGender = genderSpinner2.selectedItem.toString()
            val (startIndex, endIndex) = when (selectedGender) {
                "Masculino2" -> 1 to maxMaleFiles2
                "Feminino2" -> 1 to maxFemaleFiles2
                "MasculinoReverb" -> 1 to maxMaleReverbFiles
                "FemininoReverb" -> 1 to maxFemaleReverbFiles
                "MasculinoEco" -> 1 to maxMaleEcoFiles
                "FemininoEco" -> 1 to maxFemaleEcoFiles
                "Masculino1" -> 1 to maxMaleFiles
                "Feminino1" -> 1 to maxFemaleFiles
                "Padrão" -> 1 to (maxMaleFiles + maxFemaleFiles)
                else -> 1 to maxMaleFiles
            }

            val randomAudioFile = when (selectedGender) {
                "Masculino2" -> "masculino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino2" -> "feminino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "MasculinoReverb" -> "masculinoreverbdeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "FemininoReverb" -> "femininoreverbdeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "MasculinoEco" -> "masculinoecodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "FemininoEco" -> "femininoecodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Masculino1" -> "masculinodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino1" -> "femininodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Padrão" -> {
                    val isMale = Random.nextBoolean()
                    val fileIndex = if (isMale) {
                        "masculinodeusluzamormentoresdaluz${(startIndex..maxMaleFiles).random()}"
                    } else {
                        "femininodeusluzamormentoresdaluz${(startIndex..maxFemaleFiles).random()}"
                    }
                    fileIndex
                }
                else -> "masculinodeusluzamormentoresdaluz${(startIndex..endIndex).random()}"
            }

            mediaPlayer2?.release()

            val resID = resources.getIdentifier(randomAudioFile, "raw", packageName)
            mediaPlayer2 = MediaPlayer.create(this, resID).apply {
                setPlaybackParams(PlaybackParams().apply {
                    speed = playbackSpeed2
                })
                setOnCompletionListener {
                    it.release()
                    if (isPlaying2) playRandomAudio(isCanal1)
                    updateImageVisibility() // Atualizar a visibilidade da imagem
                }
            }
            mediaPlayer2?.start()
        }
    }


    private fun setPlaybackSpeed(mediaPlayer: MediaPlayer, speed: Float) {
        mediaPlayer.playbackParams = mediaPlayer.playbackParams.setSpeed(speed)
    }

    private fun updateSpeedTextView() {
        speedTextView1.text = String.format(": %.2f", playbackSpeed1)
        speedTextView2.text = String.format(": %.2f", playbackSpeed2)
    }

    //Modifique o método checkPermissions para chamar initializeAudio() quando as permissões forem concedidas

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), RECORD_REQUEST_CODE
            )
        } else {
            initializeAudio()
        }
    }

    // Modifique onRequestPermissionsResult para chamar initializeAudio() quando as permissões forem concedidas

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeAudio()
            } else {
                // Permissão negada
            }
        }
    }


    private fun initializeAudio() {
        val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            try {
                audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                audioTrack = AudioTrack.Builder()
                    .setAudioFormat(AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                    .setBufferSizeInBytes(bufferSize)
                    .build()
            } catch (e: SecurityException) {
                Log.e("EchoApp", "Permissão de áudio não concedida", e)
            }
        }
    }

    private fun startEcho() {
        if (isEcoOn.compareAndSet(false, true)) {
            try {
                audioRecord.startRecording()
                audioTrack.play()

                audioThread = Thread {
                    val buffer = ShortArray(AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT))
                    val circularBuffer = CircularBuffer(delayBufferSize.get())

                    while (isEcoOn.get()) {
                        val read = audioRecord.read(buffer, 0, buffer.size)
                        for (i in 0 until read) {
                            val delayedSample = circularBuffer.read()
                            val newSample = (buffer[i].toInt() + (delayedSample * volumeFactorAtomic.get()).toInt()).toShort()
                            circularBuffer.write(buffer[i])
                            buffer[i] = newSample
                        }
                        audioTrack.write(buffer, 0, read)
                    }

                    audioRecord.stop()
                    audioTrack.stop()
                }
                audioThread?.start()
            } catch (e: IllegalStateException) {
                Log.e("EchoApp", "Erro ao iniciar o eco", e)
                isEcoOn.set(false)
            }
        }
    }


    private fun stopEcho() {
        if (isEcoOn.compareAndSet(true, false)) {
            audioThread?.join()
        }
    }

    private class CircularBuffer(private val size: Int) {
        private val buffer = ShortArray(size)
        private var writeIndex = 0
        private var readIndex = 0

        fun write(value: Short) {
            buffer[writeIndex] = value
            writeIndex = (writeIndex + 1) % size
        }

        fun read(): Short {
            val value = buffer[readIndex]
            readIndex = (readIndex + 1) % size
            return value
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer1?.release()
        mediaPlayer2?.release()
        mediaRecorder?.release()
        stopEcho()
        audioRecord.release()
        audioTrack.release()
        stopService(Intent(this, MyForegroundService::class.java))
    }

}

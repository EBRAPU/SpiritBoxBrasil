package com.ebrapu.spiritboxbrasil

import com.ebrapu.spiritboxbrasil.MyForegroundService
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.WindowManager
import java.io.File
import java.io.IOException
import kotlin.random.Random
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ContatosActivity : AppCompatActivity(), SensorEventListener {

    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputUri: Uri? = null

    private val maxMaleFiles = 4695
    private val maxFemaleFiles = 4677
    private val maxFemaleFiles2 = 4410
    private val maxMaleFiles2 = 4000



    private var isPlaying1 = false
    private var isPlaying2 = false
    private var playbackSpeed1 = 0.80f // Velocidade padrão de reprodução (velocidade normal)
    private var playbackSpeed2 = 0.80f // Velocidade padrão de reprodução (velocidade normal)

    private lateinit var speedTextView1: TextView
    private lateinit var speedTextView2: TextView
    private lateinit var genderSpinner1: Spinner
    private lateinit var genderSpinner2: Spinner

    private val RECORD_REQUEST_CODE = 101

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contatos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
            registerSensors() // Iniciar captura dos sensores
        }

        desconectarButton1.setOnClickListener {
            isPlaying1 = false
            mediaPlayer1?.stop()
            desconectarButton1.visibility = View.GONE
            conectarButton1.visibility = View.VISIBLE
            unregisterSensors() // Parar captura dos sensores
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
            registerSensors() // Iniciar captura dos sensores
        }

        desconectarButton2.setOnClickListener {
            isPlaying2 = false
            mediaPlayer2?.stop()
            desconectarButton2.visibility = View.GONE
            conectarButton2.visibility = View.VISIBLE
            unregisterSensors() // Parar captura dos sensores
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

        // Inicializar sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private fun registerSensors() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensors() {
        sensorManager.unregisterListener(this)
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
                "Masculino1" -> 1 to maxMaleFiles
                "Feminino1" -> 1 to maxFemaleFiles
                "Padrão" -> 1 to (maxMaleFiles + maxFemaleFiles)
                else -> 1 to maxMaleFiles
            }

            val randomAudioFile = when (selectedGender) {
                "Masculino2" -> "masculino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino2" -> "feminino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
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
                }
            }
            mediaPlayer1?.start()

        } else {
            if (!isPlaying2) return

            val selectedGender = genderSpinner2.selectedItem.toString()
            val (startIndex, endIndex) = when (selectedGender) {
                "Masculino2" -> 1 to maxMaleFiles2
                "Feminino2" -> 1 to maxFemaleFiles2
                "Masculino1" -> 1 to maxMaleFiles
                "Feminino1" -> 1 to maxFemaleFiles
                "Padrão" -> 1 to (maxMaleFiles + maxFemaleFiles)
                else -> 1 to maxMaleFiles
            }

            val randomAudioFile = when (selectedGender) {
                "Masculino2" -> "masculino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
                "Feminino2" -> "feminino2deusluzamormentoresdaluz${(startIndex..endIndex).random()}"
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

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), RECORD_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Permissão negada
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer1?.release()
        mediaPlayer2?.release()
        mediaRecorder?.release()
        unregisterSensors() // Parar captura dos sensores ao destruir a atividade
        stopService(Intent(this, MyForegroundService::class.java)) // Parar o serviço
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // Processar dados do acelerômetro
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                // Faça algo com os dados do acelerômetro
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Processar dados do magnetômetro
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                // Faça algo com os dados do magnetômetro
            }
            Sensor.TYPE_GYROSCOPE -> {
                // Processar dados do giroscópio
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                // Faça algo com os dados do giroscópio
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Não precisa implementar para este exemplo
    }
}

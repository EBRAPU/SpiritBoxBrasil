package com.ebrapu.spiritboxbrasil

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class EfeitosFragment : Fragment() {

    private val isEcoOn = AtomicBoolean(false)
    private var audioThread: Thread? = null
    private val volumeFactorAtomic = AtomicReference(0.5f)
    private val delayBufferSize = AtomicInteger(44100)
    private lateinit var audioRecord: AudioRecord
    private lateinit var audioTrack: AudioTrack

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_efeitos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEcoControls(view)
        initializeAudio()
        loadSavedState()
    }

    private fun setupEcoControls(view: View) {
        val volumeSeekBar: SeekBar = view.findViewById(R.id.volumeSeekBar)
        val delaySeekBar: SeekBar = view.findViewById(R.id.delaySeekBar)
        val ecoButton: Button = view.findViewById(R.id.ecoButton)

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

        ecoButton.setOnClickListener {
            if (isEcoOn.get()) {
                stopEcho()
                ecoButton.text = "ECO OFF"
            } else {
                startEcho()
                ecoButton.text = "ECO ON"
            }
            saveState()
        }
    }

    private fun initializeAudio() {
        val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)

        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(44100)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .build()
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

    private fun saveState() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isEcoOn", isEcoOn.get())
        editor.putInt("volumeFactor", (volumeFactorAtomic.get() * 100).toInt())
        editor.putInt("delayBufferSize", delayBufferSize.get())
        editor.apply()
    }

    private fun loadSavedState() {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        isEcoOn.set(sharedPreferences.getBoolean("isEcoOn", false))
        volumeFactorAtomic.set(sharedPreferences.getInt("volumeFactor", 50) / 100f)
        delayBufferSize.set(sharedPreferences.getInt("delayBufferSize", 44100))

        // Update UI with the loaded state
        view?.findViewById<SeekBar>(R.id.volumeSeekBar)?.progress = (volumeFactorAtomic.get() * 100).toInt()
        view?.findViewById<SeekBar>(R.id.delaySeekBar)?.progress = delayBufferSize.get() * 1000 / 44100
        view?.findViewById<Button>(R.id.ecoButton)?.text = if (isEcoOn.get()) "ECO ON" else "ECO OFF"
    }

    override fun onResume() {
        super.onResume()
        if (isEcoOn.get()) {
            startEcho()
        }
    }

    override fun onPause() {
        super.onPause()
        stopEcho()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEcho()
        audioRecord.release()
        audioTrack.release()
    }
}

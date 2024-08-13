// FilesActivity.kt
package com.ebrapu.spiritboxbrasil

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FilesActivity : AppCompatActivity() {

    private lateinit var filesRecyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var fileAdapter: FileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)

        filesRecyclerView = findViewById(R.id.filesRecyclerView)
        backButton = findViewById(R.id.backButton)

        val filesDir = filesDir
        val recordingsDir = File(filesDir, "recordings")

        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val files = recordingsDir.listFiles()?.toList() ?: emptyList()

        fileAdapter = FileAdapter(this, files) { file ->
            // Implementar ação ao clicar em um arquivo, como reproduzir ou abrir
        }

        filesRecyclerView.layoutManager = LinearLayoutManager(this)
        filesRecyclerView.adapter = fileAdapter

        backButton.setOnClickListener {
            finish()
        }
    }
}

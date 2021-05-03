package com.example.seizenplayer.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.seizenplayer.R
import com.example.seizenplayer.models.MusicModel
import kotlinx.android.synthetic.main.activity_lyrics.*

class LyricsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val music = intent.getSerializableExtra("song") as MusicModel
        lyrics.text = application.assets.open("${music.title}.txt").bufferedReader().readText()
        lyrics.movementMethod = ScrollingMovementMethod()
        songName.text = music.songTitle
        author.text = music.songArtist
        albumName.text = music.songAlbum
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true;
    }
}
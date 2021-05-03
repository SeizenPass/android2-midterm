package com.example.seizenplayer.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seizenplayer.R
import com.example.seizenplayer.adapters.MusicAdapter
import com.example.seizenplayer.models.MusicModel
import kotlinx.android.synthetic.main.activity_lyrics.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.track_fragment.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private var music: ArrayList<MusicModel> = ArrayList()
    private var player: MediaPlayer? = null
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable:Runnable
    private lateinit var metadataRetriever: MediaMetadataRetriever
    private var currentSongId = -1
    private var calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        metadataRetriever = MediaMetadataRetriever()
        createNotificationChannel()
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        getMusicFromRawStorage()
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (player != null && fromUser) {
                    player!!.seekTo(progress * 1000)
                }
            }
        })
        play_pause_btn.setOnClickListener {
            if (player!!.isPlaying) {
                player!!.pause()
                play_pause_btn.text = ">"
            } else {
                player!!.start()
                play_pause_btn.text = "||"
            }
        }
        prev_btn.setOnClickListener {
            playPrevious()
        }
        next_song.setOnClickListener {
            playNext()
        }
        lyrics_btn.setOnClickListener {
            openLyrics()
        }
        settings_btn.setOnClickListener {
            openSetting()
        }
        notification_btn.setOnClickListener {
            timePicker()
        }
    }

    private fun timePicker() {
        calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val millis:Long = calendar.timeInMillis - System.currentTimeMillis()
            val intent: Intent = Intent(this, ReminderReceiver::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
            val alarmManager: AlarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val currentTime = System.currentTimeMillis()
            alarmManager.set(AlarmManager.RTC_WAKEUP, currentTime + millis, pendingIntent)
        }

        TimePickerDialog(this, timePicker, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    private fun openSetting() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openLyrics() {
        val intent = Intent(this, LyricsActivity::class.java).apply {
            putExtra("song", music[currentSongId])
        }
        startActivity(intent)
    }

    private fun playNext() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId++
            if (currentSongId >= music.size) {
                currentSongId = 0
            }
            initMusicPlayer(music[currentSongId])
            initializeSeekBar()
            player!!.start()
        }
    }

    private fun playPrevious() {
        if (currentSongId != -1 && player != null) {
            player!!.stop()
            currentSongId--
            if (currentSongId < 0) {
                currentSongId = music.size - 1
            }
            initMusicPlayer(music[currentSongId])
            initializeSeekBar()
            player!!.start()
        }
    }

    private fun getMusicFromRawStorage() {
        val songList = listOf(
            R.raw.ripheart,
            R.raw.bali,
            R.raw.cold,
            R.raw.curious,
            R.raw.sxsad,
            R.raw.datstick,
            R.raw.rapapapa,
            R.raw.yellow,
            R.raw.kids
        )
        for (rId in songList) {
            val mediaPath = Uri.parse("android.resource://$packageName/$rId")
            metadataRetriever.setDataSource(this, mediaPath)
            val songTitle = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "none"
            val songArtist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "none"
            val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "none"
            val model = MusicModel(resources.getResourceEntryName(rId), rId, songTitle, songArtist, album)
            music.add(model)
        }
        if (music.size > 0) {
            musicList.visibility = View.VISIBLE
            setupMusicListRecyclerView()
        } else {
            musicList.visibility = View.GONE
        }
    }

    private fun setupMusicListRecyclerView() {
        musicList.layoutManager = LinearLayoutManager(this)
        musicList.setHasFixedSize(true)

        val musicAdapter = MusicAdapter(this, music)
        musicList.adapter = musicAdapter

        musicAdapter.setOnClickListener(object : MusicAdapter.OnClickListener {
            override fun onClick(position: Int, model: MusicModel) {
                if (player != null) {
                    player!!.stop()
                }
                if (included_layout.visibility == View.GONE) {
                    included_layout.visibility = View.VISIBLE
                }
                initMusicPlayer(model)
                currentSongId = position
                initializeSeekBar()
                player!!.start()

            }
        })
    }

    private fun initMusicPlayer(model: MusicModel) {
        player = MediaPlayer.create(this@MainActivity, model.resId)
        player!!.setOnCompletionListener {
            val delay = prefs.getString("delay", "0")!!.toInt()
            if (delay != 0) {
                songTitle.text = "Break time!"
                seekBar.max = delay
                "${formatWithLeadingZeroes(delay / 60)}:${formatWithLeadingZeroes(delay % 60)}".also { totalDuration.text = it }
                val breakTime = System.currentTimeMillis()
                runnable = Runnable {
                    val spendTime = ((System.currentTimeMillis() - breakTime) / 1000).toInt()
                    seekBar.progress = spendTime
                    " ${formatWithLeadingZeroes(spendTime / 60)}:${formatWithLeadingZeroes(spendTime % 60)}".also { currentProgress.text = it }
                    if (spendTime < delay) {
                        handler.postDelayed(runnable, 1000)
                    } else {
                        playNext()
                    }
                }

            }
        }
    }

    private fun initializeSeekBar() {
        seekBar.max = player!!.seconds
        songTitle.text = music[currentSongId].songTitle
        "${formatWithLeadingZeroes(player!!.seconds / 60)}:${
            formatWithLeadingZeroes(player!!.seconds % 60)
        }".also { totalDuration.text = it }
        runnable = Runnable {
            seekBar.progress = player!!.currentSeconds
            //tv_pass.text = "${player.currentSeconds} sec"
            //val diff = player.seconds - player.currentSeconds
            //tv_due.text = "$diff sec"
            "${formatWithLeadingZeroes(seekBar.progress / 60)}:${
                formatWithLeadingZeroes(seekBar.progress % 60)
            }".also { currentProgress.text = it }
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SeizenPlayer Notification Channel"
            val descriptionText = "Channel for breaks."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notificationChannel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatWithLeadingZeroes(number: Int): String {
        return number.toString().padStart(2, '0')
    }

    private val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }

    private val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }
}
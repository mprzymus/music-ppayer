package pl.mprzymus.musicplayer

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import pl.mprzymus.musicplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mediaManager: MediaManager
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mediaManager = MediaManager(getTrackList(), applicationContext)

        binding.playButton.setOnClickListener {
            mediaManager.pauseButton()
            switchPlayButtonImage()
        }

        binding.timeBack.setOnClickListener { mediaManager.onChangeTime(-10) }
        binding.timeAhead.setOnClickListener { mediaManager.onChangeTime(10) }

        mediaManager.mediaPlayer.setOnCompletionListener {
            changeSong {
                mediaManager.nextSong(
                    applicationContext,
                    binding.seekBar
                )
            }
            mediaManager.mediaPlayer.start()
        }
        binding.nextSong.setOnClickListener {
            changeSong { mediaManager.nextSong(applicationContext, binding.seekBar) }

        }
        binding.previousSong.setOnClickListener {
            changeSong { mediaManager.previousSong(applicationContext, binding.seekBar) }
        }
        binding.stop.setOnClickListener {
            mediaManager.onStop()
            switchPlayButtonImage()
        }

        mediaManager.setSeekBarMaxValue(binding.seekBar)
        setTrackName()
        startSeekBarRefresh(mediaManager.mediaPlayer)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaManager.onSeekBarPositionChanged(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
        setTrackLength()
    }

    private fun changeSong(changeSong: () -> Unit) {
        handler.removeCallbacks(runnable)
        changeSong()
        setTrackName()
        startSeekBarRefresh(mediaManager.mediaPlayer)
        setTrackLength()
    }

    private fun setTrackLength() {
        val totalAsSeconds = mediaManager.mediaPlayer.duration / 1000
        val string = formatSecondsToMinutes(totalAsSeconds)
        binding.trackLength.text = string
    }

    private fun startSeekBarRefresh(mediaPlayer: MediaPlayer) {
        val seekBar = binding.seekBar
        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 100)
            val totalAsSeconds = mediaManager.mediaPlayer.currentPosition / 1000
            val string = formatSecondsToMinutes(totalAsSeconds)
            binding.trackPosition.text = string
        }
        handler.postDelayed(runnable, 100)
    }

    private fun formatSecondsToMinutes(totalAsSeconds: Int) =
        "${totalAsSeconds / 60}:${String.format("%02d",totalAsSeconds % 60)}"

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun switchPlayButtonImage() {
        if (mediaManager.isPlaying()) {
            binding.playButton.setImageDrawable(getDrawable(R.drawable.ic_media_pause))
        } else {
            binding.playButton.setImageDrawable(getDrawable(R.drawable.ic_media_play))
        }
    }

    private fun setTrackName() {
        val trackId = mediaManager.tracks[mediaManager.currentTrack]
        val title = resources.getResourceName(trackId)
        val endOfPathStr = "raw/"
        val endOfPathIndex = title.indexOf(endOfPathStr) + endOfPathStr.length
        binding.trackTittle.text = title.substring(endOfPathIndex)
    }
}

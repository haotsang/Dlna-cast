package com.haotsang.dlna

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.haotsang.dlna.databinding.ActivityMainBinding
import com.haotsang.dlna_lib.DlnaUtils
import com.haotsang.dlna_lib.control.IController
import com.haotsang.dlna_lib.control.MultiPointController
import com.haotsang.dlna_lib.manager.DLNADeviceManager
import org.cybergarage.upnp.Device

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private var mAdapter: DeviceAdapter? = null

    private var mController: IController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        initView()

    }

    private fun initView() {
        binding?.dlnaInput?.setText("set url")

        binding?.dialogDlnaToolbar?.apply {
            title = "DLNA投屏"
            menu.add(Menu.NONE, 0, 0, "Search").setIcon(R.drawable.ic_toolbar_reload)
                .setShowAsAction(
                    MenuItem.SHOW_AS_ACTION_ALWAYS
                )
            menu.findItem(0).setOnMenuItemClickListener {
                search()
                true
            }
        }

//        binding?.dialogDlnaRecycler?.emptyText = "未发现可用设备"// 请确定当前设备与投屏设备在同一wifi下 ,然后刷新重试
        binding?.dialogDlnaRecycler?.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding?.dialogDlnaRecycler?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = DeviceAdapter()
        binding?.dialogDlnaRecycler?.adapter = mAdapter
        binding?.dialogDlnaRecycler?.setOnItemClickListener { _, position ->
            DLNADeviceManager.getInstance().setCurrentDevice(mAdapter?.getDevice(position))
            play(binding?.dlnaInput?.text.toString())

            mAdapter?.selection = position
        }


        mController = MultiPointController()
        mController?.setPlayMonitor(object : IController.PlayerMonitor {
            override fun onPreparing() {}
            override fun onGetMediaDuration(totalTimeSeconds: Int) {
                binding?.seekBar?.max = totalTimeSeconds
                binding?.durationTxt?.text = DlnaUtils.formatTimeSeconds(totalTimeSeconds)
            }

            override fun onGetMaxVolume(max: Int) {
                binding?.volumeSeekBar?.max = max
            }

            override fun onMuteStatusChanged(mute: Boolean) {}
            override fun onVolumeChanged(current: Int) {
                binding?.volumeSeekBar?.progress = current
            }

            override fun onPlay() {
                binding?.playBtn?.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            }

            override fun onPause() {
                binding?.playBtn?.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }

            override fun onStop() {}
            override fun onError() {}
            override fun onProgressUpdated(currentTimeSeconds: Int) {
                binding?.seekBar?.progress = currentTimeSeconds
                binding?.currentDurationTxt?.text = DlnaUtils.formatTimeSeconds(currentTimeSeconds)
            }

            override fun onSeekComplete() {}
            override fun onComplete() {}
            override fun onPlayItemChanged() {}
        })

        binding?.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                controlPointView.updateProcess(progress);
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mController?.onSeekBegin()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seek(seekBar.progress)
            }
        })
        binding?.volumeSeekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
                mController?.setVolume(mDevice, seekBar.progress)
            }
        })

        var mute = false
        binding?.muteBtn?.setOnClickListener {
            setMute(!mute)
            if (mute) {
                binding?.muteBtn?.clearColorFilter()
            } else {
                binding?.muteBtn?.setColorFilter(ContextCompat.getColor(this, com.google.android.material.R.color.accent_material_dark))
            }

            mute = !mute
        }
        binding?.playBtn?.setOnClickListener { play() }
        binding?.stopBtn?.setOnClickListener { stop() }
        binding?.nextBtn?.setOnClickListener { seek(binding?.seekBar?.progress!! + 10) }
        binding?.previousBtn?.setOnClickListener { seek(binding?.seekBar?.progress!! - 10) }
        binding?.ffBtn?.setOnClickListener { seek(binding?.seekBar?.progress!! + 20) }
        binding?.rewBtn?.setOnClickListener { seek(binding?.seekBar?.progress!! - 20) }

    }

    override fun onStart() {
        super.onStart()
        if (mAdapter?.getDeviceList()?.isEmpty() == true) {
            search()
        }
    }

    override fun onStop() {
        super.onStop()
        stopDiscovery()
    }

    private fun seek(pausedTimeSeconds: Int) {
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.seek(mDevice, pausedTimeSeconds)
    }

    private fun setMute(mute: Boolean) {
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.setMute(mDevice, mute)
    }

    private fun play() {
        if (mController?.isPlaying() == true) {
            pause()
            return
        }

        if (mController?.isPaused() == true) {
            resumePlay(binding?.seekBar?.progress!!)
        } else {
            play(binding?.dlnaInput?.text.toString())
        }
    }

    private fun play(uri: String) {
        if (uri.isEmpty()) {
            return
        }
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.play(mDevice, uri)
    }

    private fun pause() {
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.pause(mDevice)
    }

    private fun resumePlay(pausedTimeSeconds: Int) {
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.resume(mDevice, pausedTimeSeconds)
    }

    private fun stop() {
        val mDevice = DLNADeviceManager.getInstance().getCurrentDevice() ?: return
        mController?.stop(mDevice)
    }

    private fun search() {
        stopDiscovery()
        startDiscovery()
    }

    private fun startDiscovery() {
        binding?.dialogDlnaProgressBar?.visibility = View.VISIBLE
        DLNADeviceManager.getInstance().startDiscovery(mListener)
    }

    private fun stopDiscovery() {
        binding?.dialogDlnaProgressBar?.visibility = View.GONE
        DLNADeviceManager.getInstance().stopDiscovery()
    }

    private val mListener: DLNADeviceManager.MediaRenderDeviceChangeListener =
        object : DLNADeviceManager.MediaRenderDeviceChangeListener {
            override fun onStarted() {
                binding?.dialogDlnaProgressBar?.visibility = View.VISIBLE
            }

            override fun onDeviceListChanged(list: List<Device>) {
                mAdapter?.setList(list)
            }

            override fun onFinished() {
                binding?.dialogDlnaProgressBar?.visibility = View.GONE
            }
        }

}
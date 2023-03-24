package com.example.miwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.IOException;
import java.util.Locale;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";

    private static final int DEFAULT_RTSP_PORT = 8086;

    private TextView mTextView;
    private SurfaceView mSurfaceView;
    private Surface mSurface;

    private boolean mIsStreaming;

    private IjkMediaPlayer mMediaPlayer;

    private String mIp;
    private int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransparentStatusBar(getWindow());
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.textView);
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(this);
    }

    private void setTransparentStatusBar(Window window) {
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mIsStreaming = false;
        mTextView.setText("");
        mSurfaceView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mSurfaceView.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int format, int width, int height) {
        mSurface = surfaceHolder.getSurface();
        mIp = SystemProperties.get("miwatch.streaming.ip", "null");
        if ("null".equals(mIp)) {
            mTextView.setText("\"miwatch.streaming.ip\" unspecified");
            return;
        }
        mPort = SystemProperties.getInt("miwatch.streaming.port", DEFAULT_RTSP_PORT);
        // See https://support.video.ibm.com/hc/en-us/articles/207852117-Internet-connection-and-recommended-encoding-settings
        // for Recommended Encoding Settings
        // URL = [ip-address]%s[h264|h265]-[kilo-bit-per-second]-[frame-rate]-[frame-width]-[frame-height]
        String url = String.format(Locale.US, "rtsp://%s:%d?h264=2000-30-%d-%d", mIp, mPort, width, height);
        Log.d(TAG, "startStreaming: " + url);
        mTextView.setText(url);
        startStreaming(url);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (mIsStreaming) {
            mIsStreaming = false;
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        }
    }

    public synchronized void startStreaming(String url) {
        if (mIsStreaming) {
            return;
        }

        try {
            mMediaPlayer = new IjkMediaPlayer();
//            mMediaPlayer.setOnErrorListener(this);

            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);

            // reduce the latency introduced by optional buffering
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer");

            // reduce the latency by flushing out packets immediately
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1);

            // set number of packets to buffer for handling of reordered packets
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reorder_queue_size", 1024 * 1024);

            // underlying protocol send/receive buffer size (in bytes)
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 5 * 1024 * 1024);

            // max memory used for buffering real-time frames
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtbufsize", 5 * 1024 * 1024);

            // maximum muxing or demuxing delay in microseconds
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "max_delay", 1_000_000);

            // Specify how many microseconds are analyzed to probe the input.
            // A higher value will enable detecting more accurate information, but will increase latency.
            // It defaults to 5,000,000 microseconds = 5 seconds.
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 5_000);

            // Set probing size in bytes, i.e. the size of the data to analyze to get stream information.
            // A higher value will enable detecting more information in case it is dispersed into the stream, but will increase latency.
            // Must be an integer not lesser than 32.
            // It is 5000000 by default.
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 50 * 1024);

            // number of bytes to probe file format (in bytes)
            // It is 1 << 20 by default.
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "formatprobesize", 1024);

            // number of frames used to probe fps
            // It is -1 by defult
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fpsprobesize", 5);

            // Do not limit the input buffer size, read as much data as possible from the input as soon as possible.
            // Enabled by default for realtime streams, where data may be dropped if not read in time.
            // Use this option to enable infinite buffers for all inputs, use -noinfbuf to disable it.
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);

            // pause output until enough packets have been read after stalling
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

            // drop frames when cpu is too slow
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);

            // automatically start playing on prepared
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);

            // drop frames in video whose fps is greater than max-fps
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30);

            // use MediaCodec for H264 and H265 codec
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-avc", 1);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1);

            // fourcc of overlay format
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12);

            // other non-official supported options, @see ff_ffplay.c for details
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 3000);
            mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "low_latency_mode", 1);

            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.start();
            mIsStreaming = true;
        } catch (IOException e) {
            Log.d(TAG, "startStreaming failed: ", e);
        }
    }
}
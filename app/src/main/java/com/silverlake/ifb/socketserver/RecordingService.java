package com.silverlake.ifb.socketserver;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {
    private static final String TAG = RecordingService.class.getSimpleName();

    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;

    private ScreenRecorder screenRecorder;

    private boolean running;
    private int width = 720;
    private int height = 1080;
    private int dpi;


    @Override
    public IBinder onBind(Intent intent) {
        return new RecordBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("service_thread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        serviceThread.start();
        running = false;
        mediaRecorder = new MediaRecorder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setMediaProject(MediaProjection project) {
        mediaProjection = project;
    }

    public boolean isRunning() {
        return running;
    }

    public void setConfig(int width, int height, int dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }

    public void setRunning(boolean isRunning) {
        this.running = isRunning;
    }

    public boolean startRecord() {
        if (mediaProjection == null || running) {
            return false;
        }

        final int bitrate = 6000000;

        screenRecorder = new ScreenRecorder(width, height, bitrate, dpi, mediaProjection, getSaveDirectory() + System.currentTimeMillis() + ".mp4");
        screenRecorder.start();

        running = true;
        return true;
    }

    public boolean stopRecord() {
        if (!running) {
            return false;
        }

        running = false;

        if (screenRecorder != null) {
            screenRecorder.quit();
        }
        return true;
    }

    public String getSaveDirectory() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

            File file = new File(rootDir);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    return null;
                }
            }
            Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();

            return rootDir;
        } else {
            return null;
        }
    }

    public class RecordBinder extends Binder {

        public RecordingService getRecordService() {
            return RecordingService.this;
        }
    }

}

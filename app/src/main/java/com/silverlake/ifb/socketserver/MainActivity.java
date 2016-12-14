package com.silverlake.ifb.socketserver;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int STORAGE_REQUEST_CODE = 102;
    private static final int AUDIO_REQUEST_CODE = 103;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;

    private RecordingService recordService;

    private Button btnStartServer, btnStopServer, btnStartRecording, btnStopRecording;
    private TextView tvIpAddress;
    private static final int portNumber = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        tvIpAddress = (TextView) findViewById(R.id.tvIpAddress);
        tvIpAddress.setText(getIpAddress());

        btnStartServer = (Button) findViewById(R.id.btnStartServer);
        btnStopServer = (Button) findViewById(R.id.btnStopServer);
        btnStartRecording = (Button) findViewById(R.id.btnStartRecording);
        btnStopRecording = (Button) findViewById(R.id.btnStopRecording);

        btnStartServer.setOnClickListener(this);
        btnStopServer.setOnClickListener(this);
        btnStartRecording.setOnClickListener(this);
        btnStopRecording.setOnClickListener(this);

        Intent intent = new Intent(this, RecordingService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);

        performRuntimePermission();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartServer:
                startServer();
                break;
            case R.id.btnStopServer:
                stopServer();
                break;
            case R.id.btnStartRecording:
                if (recordService != null && !recordService.isRunning()) {
                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                }
                break;
            case R.id.btnStopRecording:
                stopRecording();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            startRecording();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_REQUEST_CODE || requestCode == AUDIO_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    private void startServer() {
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
    }

    private void stopServer() {
        Intent intent = new Intent(this, SocketService.class);
        stopService(intent);
    }

    private void startRecording() {
        if (!recordService.isRunning()) {
            recordService.setMediaProject(mediaProjection);
            recordService.startRecord();
        }
    }

    private void stopRecording() {
        if (recordService.isRunning()) {
            recordService.stopRecord();
            recordService.setRunning(false);
            unbindService(connection);
        }
    }

    private String getIpAddress() {
        WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ip = Integer.reverseBytes(ip);
        }

        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress() + ":" + portNumber;
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }

    private void performRuntimePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST_CODE);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordingService.RecordBinder binder = (RecordingService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

}

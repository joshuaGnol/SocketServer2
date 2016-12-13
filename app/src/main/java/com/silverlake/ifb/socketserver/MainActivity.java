package com.silverlake.ifb.socketserver;

import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnStartServer, btnStopServer;
    private TextView tvIpAddress;
    private static final int portNumber = 8080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvIpAddress = (TextView) findViewById(R.id.tvIpAddress);
        tvIpAddress.setText(getIpAddress());

        btnStartServer = (Button) findViewById(R.id.btnStartServer);
        btnStopServer = (Button) findViewById(R.id.btnStopServer);

        btnStartServer.setOnClickListener(this);
        btnStopServer.setOnClickListener(this);
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

}

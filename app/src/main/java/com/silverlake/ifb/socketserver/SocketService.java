package com.silverlake.ifb.socketserver;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();
    private static final int WEB_SERVER_PORT_NUMBER = 8080;
    private static final int WEB_SOCKET_PORT_NUMBER = 6060;

    private AsyncHttpServer server;
    private AsyncHttpServer socketServer;
    private ArrayList<WebSocket> websockets = new ArrayList<>();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        startWebServer();
        startWebSocketSever();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        Toast.makeText(this, "Server Started.", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");

        if (server != null) {
            Toast.makeText(this, "Server Shutting Down.", Toast.LENGTH_SHORT).show();
            server.stop();
            AsyncServer.getDefault().stop();
        }

        if (socketServer != null) {
            Toast.makeText(this, "Shutting down socket server...", Toast.LENGTH_SHORT).show();
            socketServer.stop();
        }

        websockets.clear();

        super.onDestroy();
    }

    private void startWebServer() {
        server = new AsyncHttpServer();
        server.get("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String path = request.getPath();

                Log.d(TAG, "onConnected: ");
                Log.d(TAG, "onConnected: method: " + request.getMethod());
                Log.d(TAG, "onConnected: path: " + request.getPath());
                Log.d(TAG, "onConnected: contentType: " + request.getBody().getContentType());
                Log.d(TAG, "onConnected: headers: " + request.getHeaders().toString());

                if (path.equalsIgnoreCase("/")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("index.html");
                        response.setContentType("text/html");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "index.html", e);
                    }
                } else if (path.equalsIgnoreCase("/js/app.js")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("js/app.js");
                        response.setContentType("application/javascript");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "js/app.js", e);
                    }
                }
            }
        });

        server.listen(WEB_SERVER_PORT_NUMBER);
    }

    private void startWebSocketSever() {
        socketServer = new AsyncHttpServer();
        socketServer.websocket("/", new AsyncHttpServer.WebSocketRequestCallback() {

            @Override
            public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                Log.d(TAG, "onConnected: ");
                websockets.add(webSocket);

                Log.d(TAG, "onConnected: method: " + request.getMethod());
                Log.d(TAG, "onConnected: path: " + request.getPath());
                Log.d(TAG, "onConnected: contentType: " + request.getBody().getContentType());
                Log.d(TAG, "onConnected: headers: " + request.getHeaders().toString());

                webSocket.send("from server: hello first");

                webSocket.setStringCallback(new WebSocket.StringCallback() {

                    @Override
                    public void onStringAvailable(String s) {
                        Log.d(TAG, "onStringAvailable: s: " + s);
                        webSocket.send("from server: " + s);
                    }
                });
            }
        });

        socketServer.listen(WEB_SOCKET_PORT_NUMBER);
    }

    private void handle404(AsyncHttpServerResponse response, String path, Exception e) {
        Log.e(TAG, "Invalid URL: " + path, e);
        response.code(404);
        response.end();
    }
}

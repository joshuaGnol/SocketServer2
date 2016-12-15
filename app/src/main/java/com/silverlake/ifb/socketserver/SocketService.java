package com.silverlake.ifb.socketserver;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SocketService extends Service {
    private static final String TAG = SocketService.class.getSimpleName();

    private static final int WEB_SERVER_PORT_NUMBER = 8080;
    private static final int WEB_SOCKET_PORT_NUMBER = 6060;

    private boolean isRunning = false;
    private int width, height, dpi;

    private MediaProjection mediaProjection;
    private ScreenRecorder screenRecorder;

    private AsyncHttpServer server;
    private AsyncHttpServer socketServer;
    private ArrayList<WebSocket> websockets = new ArrayList<>();
    private WebSocket webSocket;

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
        return new ScreenMirroringBinder();
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

    public void setVideoConfig(int width, int height, int dpi, MediaProjection mediaProjection) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        this.mediaProjection = mediaProjection;
    }

    public void start() {
        Log.d(TAG, "start: ");
        if (!isRunning) {
            Log.d(TAG, "start: starting screen recording...");
            isRunning = true;
            final int bitrate = 1024000;
            screenRecorder = new ScreenRecorder(width, height, bitrate, dpi, mediaProjection, getSaveDirectory() + System.currentTimeMillis() + ".mp4");
            screenRecorder.start();
        }
    }

    public void stop() {
        Log.d(TAG, "stop: ");
        if (isRunning) {
            Log.d(TAG, "stop: stop recording...");
            isRunning = false;
            screenRecorder.quit();
        }
    }

    private void updateWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
        if (screenRecorder != null) {
            screenRecorder.setWebsocket(webSocket);
        }
    }

    private void startWebServer() {
        server = new AsyncHttpServer();
        server.get("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(AsyncHttpServerRequest request, AsyncHttpServerResponse response) {
                String path = request.getPath();

                Log.d(TAG, "onConnected: ");
                Log.d(TAG, "onConnected: method: " + request.getMethod());
                Log.d(TAG, "onConnected: path: " + path);
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
                } else if (path.equalsIgnoreCase("/style.css")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("style.css");
                        response.setContentType("text/css");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "style.css", e);
                    }
                } else if (path.equalsIgnoreCase("/icons/plus.png")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("icons/plus.png");
                        response.setContentType("image/png");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "icons/plus.png", e);
                    }
                } else if (path.equalsIgnoreCase("/icons/minus.png")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("icons/minus.png");
                        response.setContentType("image/png");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "icons/minus.png", e);
                    }
                } else if (path.equalsIgnoreCase("/icons/fullscreen.png")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("icons/fullscreen.png");
                        response.setContentType("image/png");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "icons/fullscreen.png", e);
                    }
                } else if (path.equalsIgnoreCase("/icons/reset.png")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("icons/reset.png");
                        response.setContentType("image/png");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "icons/reset.png", e);
                    }
                } else if (path.equalsIgnoreCase("/broadway/decoder.min.js")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("broadway/decoder.min.js");
                        response.setContentType("application/javascript");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "broadway/decoder.min.js", e);
                    }
                } else if (path.equalsIgnoreCase("/broadway/player.min.js")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("broadway/player.min.js");
                        response.setContentType("application/javascript");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "broadway/player.min.js", e);
                    }
                } else if (path.equalsIgnoreCase("/broadway/webglcanvas.min.js")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("broadway/webglcanvas.min.js");
                        response.setContentType("application/javascript");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "broadway/webglcanvas.min.js", e);
                    }
                } else if (path.equalsIgnoreCase("/receiver.js")) {
                    try {
                        AssetFileDescriptor assetFileDescriptor = getAssets().openFd("receiver.js");
                        response.setContentType("application/javascript");
                        response.sendStream(assetFileDescriptor.createInputStream(), assetFileDescriptor.getLength());
                    } catch (IOException e) {
                        e.printStackTrace();
                        handle404(response, "receiver.js", e);
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

//                webSocket.send("from server: hello first");

                updateWebSocket(webSocket);

//                webSocket.setStringCallback(new WebSocket.StringCallback() {
//
//                    @Override
//                    public void onStringAvailable(String s) {
//                        Log.d(TAG, "onStringAvailable: s: " + s);
//                        webSocket.send("from server: " + s);
//                    }
//                });
            }
        });

        socketServer.listen(WEB_SOCKET_PORT_NUMBER);
    }

    private void handle404(AsyncHttpServerResponse response, String path, Exception e) {
        Log.e(TAG, "Invalid URL: " + path, e);
        response.code(404);
        response.end();
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

    public class ScreenMirroringBinder extends Binder {

        public SocketService getRecordService() {
            return SocketService.this;
        }

    }
}

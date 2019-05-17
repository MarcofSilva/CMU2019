package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */

public class CommunicationManager implements  Runnable{

    private Socket socket = null;
    private Handler handler;
    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "CommunicationManager";
    private boolean isGroupOwner;

    public CommunicationManager(Socket socket, Handler handler, boolean isGroupOwner) {
        this.socket = socket;
        this.handler = handler;
        this.isGroupOwner = isGroupOwner;
    }

    @Override
    public void run() {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;
            handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this).sendToTarget();
            Log.d(TAG , "Writing");
            if(!isGroupOwner){
                sendImage(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/Camera/20181103_134455.jpg");
            }
            else {
                String ip=(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace("/","");
                Log.d(TAG, "ip of peer - " + ip);
                handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ, ip.length() , -1, ip.getBytes()).sendToTarget();
            }
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    String str = "";
                    if (bytes != -1 && isGroupOwner) {
                        byte[] newArray = Arrays.copyOfRange(buffer, 0, bytes);
                        str = new String(newArray, StandardCharsets.UTF_8);
                    }
                    if (str.endsWith("@") || bytes == -1) {
                        break;
                    }
                    // Send the obtained bytes to the UI Activity
                    Log.d(TAG, "Rec:" + String.valueOf(buffer));
                    handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void write(String msg) {
        final byte[] buffer = msg.getBytes();
        Thread thread = new Thread() {
            public void run() {
                try {
                    Log.d(TAG , "Writing hello2");
                    oStream.write(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }
        };
        thread.start();
    }

    public void sendImage(String path){
        final File imagefile = new File(path);
        final byte[] bytes = new byte[(int) imagefile.length()];
        Thread thread = new Thread() {
            public void run() {
                try {
                    Log.d(TAG, "Writing pic");
                    BufferedInputStream bis;
                    bis = new BufferedInputStream(new FileInputStream(imagefile));
                    bis.read(bytes, 0, bytes.length);

                    ObjectOutputStream oos = new ObjectOutputStream(oStream);
                    oos.write(bytes);
                    oos.flush();

                    socket.close();

                    final String sentMsg = "File sent to: " + socket.getInetAddress();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();

    }


}

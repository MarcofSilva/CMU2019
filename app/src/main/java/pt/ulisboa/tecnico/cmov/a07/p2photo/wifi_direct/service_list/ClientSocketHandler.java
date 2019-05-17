package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientSocketHandler extends Thread {
    private static final String TAG = "ClientSocketHandler";
    private TextView statusText;
    private InetAddress mAddress;
    private Handler handler;
    private Context context;
    private CommunicationManager com;
    public ClientSocketHandler(InetAddress groupOwnerAddress, View statusText, Context context, Handler handler) {
        this.mAddress = groupOwnerAddress;
        this.statusText = (TextView) statusText;
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), WiFiServiceDiscoveryActivity.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the client handler");
            com = new CommunicationManager(socket, handler, false);
            new Thread(com).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public CommunicationManager getChat() {
        return com;
    }

    //@Override
    public String doInBackground(Void... params) {
        Socket socket = new Socket();
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        String str = "";
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(mAddress.getHostAddress(), WiFiServiceDiscoveryActivity.SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            OutputStream os = new DataOutputStream(socket.getOutputStream());
            InputStream is = socket.getInputStream();
            byte[] contents = new byte[4096000];
            int bytesRead = 0;
            int totBytes = 0;
            int len;
            bytesRead = is.read(contents);
            totBytes = bytesRead;
            str = new String(contents, 0, bytesRead);
            while(bytesRead != -1) {
                if (str.endsWith("@")){
                    break;
                }
                totBytes += bytesRead;
                str += new String(contents, 0, bytesRead);
                bytesRead = is.read(contents);
            }
            os.write("hello@".getBytes());
            os.flush();

            os.close();
            socket.close();
            return str;
            /*OutputStream outputStream = socket.getOutputStream();
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            inputStream = cr.openInputStream(Uri.parse("path/to/picture.jpg"));
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();*/

        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        //catch logic
                    }
                }
            }
            return str;
        }
    }
    //@Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}
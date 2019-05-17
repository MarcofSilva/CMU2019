package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class GroupOwnerSocketHandler extends Thread {

    ServerSocket socket = null;
    private final int THREAD_COUNT = 10;
    private Handler handler;
    private static final String TAG = "GroupOwnerSocketHandler";
    private Context context;
    private TextView statusText;
    private HashMap<String, String> peers;
    private WiFiServiceDiscoveryActivity activity;

    public GroupOwnerSocketHandler(WiFiServiceDiscoveryActivity activity, Context context, View statusText, HashMap<String, String> peers, Handler handler) throws IOException {
        this.context = context;
        this.statusText = (TextView) statusText;
        this.peers = peers;
        this.activity = activity;
        try {
            socket = new ServerSocket(8880);
            this.handler = handler;
            Log.d("CommunicationManager", "Socket Started");
        } catch (IOException e) {
            e.printStackTrace();
            pool.shutdownNow();
            throw e;
        }
    }

    /**
     * A ThreadPool for client sockets.
     */
    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    @Override
    public void run() {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        //1 - fazer pedido ao servidor
        //recebo - lista de albuns
        //pedir catalogos de todos os albuns
        //comparar e ver o que é preciso
        //pedir todas de uma vez
        String dummyResponse = "ola;bla";
        while (true) {
            try {
                // A blocking operation. Initiate a ChatManager instance when
                // there is a new connection
                pool.execute(new CommunicationManager(socket.accept(), this.activity, handler, true, dummyResponse));
                Log.d(TAG, "Launching the I/O handler");
            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {
                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }

    //@Override
    protected String doInBackground(Void... params) {
        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        try (ServerSocket serverSocket = new ServerSocket(8880)){
            Socket client = serverSocket.accept();
            String res = "";
            for (String s : peers.values()){
                res += s + ";";
            }
            res += "@";

            OutputStream os = new DataOutputStream(client.getOutputStream());
            DataInputStream is = new DataInputStream(client.getInputStream());

            os.write(res.getBytes());
            os.flush();

            //copyFile(inputstream, new FileOutputStream(f));
            byte[] contents = new byte[4096000];
            int bytesRead = 0;

            bytesRead = is.read(contents);
            String str = new String(contents, 0, bytesRead);
            while(bytesRead != -1) {
                if (str.endsWith("@")){
                    break;
                }
                str += new String(contents, 0, bytesRead);
                bytesRead = is.read(contents);
            }
            os.close();
            is.close();

            serverSocket.close();
            //return f.getAbsolutePath();
            return str;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * Start activity that can handle the JPEG image
     */
    //@Override
    protected void onPostExecute(String result) {
        if (result != null) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}

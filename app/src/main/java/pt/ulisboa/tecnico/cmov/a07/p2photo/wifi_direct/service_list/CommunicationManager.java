package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private String albums;
    private WiFiServiceDiscoveryActivity activity;
    private AlbumsManager albumsManager;

    public CommunicationManager(Socket socket, WiFiServiceDiscoveryActivity activity, Handler handler, boolean isGroupOwner, String albums) {
        this.socket = socket;
        this.handler = handler;
        this.isGroupOwner = isGroupOwner;
        this.albums = albums;
        this.activity = activity;
        this.albumsManager = new AlbumsManager(activity);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        try {
            iStream = socket.getInputStream();
            oStream = socket.getOutputStream();
            handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this).sendToTarget();
            //GET MY USER ALBUM
            String userAlbums = albumsManager.listLocalAlbumsPhotos(new File(albumsManager.BASE_FOLDER_MINE));

            if (!isGroupOwner){
                Log.d(TAG, "hello im peer");
                //enviar string de useralbums
                write(userAlbums);
                //fica a espera de resposta do servidor com os albuns/catalogos
                String serverAns = read();
                //write fotos que me faltam a mim
                String missing = albumsManager.compareUserAlbums(userAlbums, serverAns);
                write(missing);
                //read fotos que faltam ao servidor
                String missingServer = read();
                //enviar fotos para o server
                String pics = albumsManager.photosToShare(missingServer);
                Log.d("novo", "images to write####### " + pics);
                write(pics);
                //get my pics
                String picsForMe = read();
                albumsManager.storeNewPhotos(picsForMe);


            }
            else{
                Log.d(TAG, "hi im groupowner");
                //receber user albums de cliente
                String clientAns = read();
                //String clientAns = "album1:marco::foto1,foto2";
                write(userAlbums);
                //esperar por fotos que faltam ao cliente
                String missingClient = read();
                //ver que fotos faltam e enviar fotos que eu quero
                String missing = albumsManager.compareUserAlbums(userAlbums, clientAns);
                write(missing);
                //read fotos para mim
                String picsForMe = read();
                albumsManager.storeNewPhotos(picsForMe);
                //write fotos para cliente
                String pics = albumsManager.photosToShare(missingClient);
                write(pics);

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

    private String read(){

            byte[] length = new byte[4];

            //get number of bytes to read

            int read = 0;
            int remaining = 4;
        try {
            while ((read = iStream.read(length, 0, Math.min(4, remaining))) > 0 && remaining > 0) {
                remaining -= read;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
            ByteBuffer wrapped = ByteBuffer.wrap(length);
            int receive_lenght = wrapped.getInt();

            byte[] result = new byte[receive_lenght];

            read = 0;
            int totalRead = 0;
            remaining = receive_lenght;
        try{
            while((read = iStream.read(result, 0, Math.min(result.length, remaining))) > 0) {
                totalRead += read;
                remaining -= read;
                System.out.println("read " + totalRead + " bytes.");
            }

        }catch (IOException e) {
            e.printStackTrace();
        }


/*
        int bytes;
        handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this).sendToTarget();
        String readMessage = "";
        while (true) {
            try {
                // Read from the InputStream
                bytes = iStream.read(buffer);

                if(bytes == -1){
                    if(readMessage.length() >= 0){
                        break;
                    }
                }
                else if (bytes > 0){
                    readMessage += new String(buffer, 0, bytes);
                    Log.d(TAG, readMessage);
                    if( bytes < buffer.length){
                        break;
                    }
                }

                // Send the obtained bytes to the UI Activity
                Log.d(TAG, "Rec:" + String.valueOf(buffer));

            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
            }
        }*/
        String received_message = new String(result);
        Log.d("novo","read_########## " + receive_lenght + " " + received_message + "#######");

        handler.obtainMessage(WiFiServiceDiscoveryActivity.MESSAGE_READ, received_message.getBytes().length, -1, received_message.getBytes()).sendToTarget(); //isto chama a funçao handlemessage da atividade
        return received_message;
    }

    public void write(String msg) {

        final byte[] buffer = msg.getBytes();
        Thread thread = new Thread() {
            public void run() {
                try {
                    byte[] lenght = ByteBuffer.allocate(4).putInt(buffer.length).array();
                    Log.d("novo","write_########## " + buffer.length + " " + new String(buffer) + "#############");
                    oStream.write(lenght);
                    oStream.flush();
                    ByteArrayInputStream fis = new ByteArrayInputStream(buffer);
                    int len=0;
                    while((len=fis.read(buffer))!=-1)
                    {
                        oStream.write(buffer,0,len);
                    }
                    oStream.flush();

                    /*Log.d(TAG , "Writing hello2");
                    oStream.write(buffer);
                    oStream.flush();*/
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

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

}

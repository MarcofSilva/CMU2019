package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateService extends Service {

    private final IBinder mBinder = new MyBinder();
    private Timer timer;
    private TimerTask timerTask;
    public static final String BROADCAST_ACTION = "pt.ulisboa.tecnico.updating";

    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";


    public static Activity _activity = null;

    class MyBinder extends Binder {
        UpdateService getService() {
            return UpdateService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d("Debug Cenas", "onCreate Service" );
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Debug Cenas", "onBind Service" );
        timer = new Timer();
        initializeTimerTask();
        //after 2secs execute and then 10s perioud
        timer.schedule(timerTask, 5000, 15000);
        return mBinder;
    }

    private void sendMyBroadCast(){
        try{
            Log.d("Debug Cenas", "sendMyBroadCast");
            Intent broadCastIntent = new Intent();
            broadCastIntent.setAction(BROADCAST_ACTION);
            sendBroadcast(broadCastIntent);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.d("Debug Cenas", "Timerrun: pooling server" );
                poolServer();
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.d("Debug Cenas", "onDestroy in Service" );
        stoptimertask();
        stopSelf();
    }

    public void poolServer(){
        Log.d("Debug Cenas", "poolserver: Pooling server");

        try {
            URL url = new URL(_activity.getString(R.string.serverAddress) + "/askForInvite");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(false);

            conn.setRequestProperty("Authorization", SessionHandler.readToken(_activity));

            Log.d("Debug cenas", "POOLED");

            InputStream in = new BufferedInputStream(conn.getInputStream());
            String response = SessionHandler.convertStreamToString(in);

            if(response.equals("Empty")){
                return;
            }
            else if(response.equals(NEED_AUTHENTICATION)){
                response = NEED_AUTHENTICATION + ";" + NEED_AUTHENTICATION;
                stoptimertask();
            }

            String[] resSplit = response.split(";");
            Invite invite = new Invite(resSplit[0], resSplit[1]);

            ContextClass context = (ContextClass) getApplicationContext();
            context.addInvite(invite);

            Log.d("Debug cenas", "added new invite");
            //sending ping for him to update himself
            sendMyBroadCast();

        } catch (Exception e) {
            Log.e("MYDEBUG", "Exception: " + e.getMessage());
        }
    }
}

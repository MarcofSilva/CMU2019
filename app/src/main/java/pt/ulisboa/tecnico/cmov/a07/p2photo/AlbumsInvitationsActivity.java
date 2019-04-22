package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import java.util.ArrayList;

public class AlbumsInvitationsActivity extends AppCompatActivity {

    ArrayList<Invite> _invites = new ArrayList<>();
    static CustomAdapterInvites _invitesCustomAdapter;
    private AcceptAlbumTask mAcceptALb = null;
    private AlbumsInvitationsActivity.MyBroadCastReceiver myBroadCastReceiver;
    private static final String NEED_AUTHENTICATION = "AuthenticationRequired";
    public static final String BROADCAST_ACTION = "pt.ulisboa.tecnico.updating";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums_invitations);

        _invitesCustomAdapter = new CustomAdapterInvites(_invites, this );

        ListView listView = findViewById(R.id.lvShowUsers);

        listView.setAdapter(_invitesCustomAdapter);

        ContextClass context = (ContextClass) getApplicationContext();
        ArrayList<Invite> aux = new ArrayList<>();
        aux.addAll(context.getInvites());

        for(int i = 0; i < aux.size(); i++){
            if(aux.get(i).get_albumName().equals(NEED_AUTHENTICATION) && aux.get(i).get_userAlbum().equals(NEED_AUTHENTICATION)){
                context.removeInvite(i);
                Toast.makeText(AlbumsInvitationsActivity.this, "Not properly authenticated. Login again.", Toast.LENGTH_LONG).show();
                //stopService();
                Intent logoutData = new Intent(getApplicationContext(), LoginActivity.class);
                AlbumsInvitationsActivity.this.startActivity(logoutData);
                AlbumsInvitationsActivity.this.finish();
            }
            else {
                //we can only add without clear because it is on create, so it should be empty
                _invitesCustomAdapter.add(aux.get(i));
            }
        }
        _invitesCustomAdapter.notifyDataSetChanged();

        myBroadCastReceiver = new MyBroadCastReceiver();
        registerMyReceiver();
        Log.d("Debug Cenas", "oncreate AlbumsInvitarionsActivity");
    }

    private void registerMyReceiver() {
        Log.d("Debug Cenas", "RegisterReceiver: registering MyReceiver");

        try{
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BROADCAST_ACTION);
            registerReceiver(myBroadCastReceiver, intentFilter);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    class MyBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            _invitesCustomAdapter.clear();
            ContextClass contextClass = (ContextClass) getApplicationContext();
            ArrayList<Invite> aux = new ArrayList<>();
            aux.addAll(contextClass.getInvites());
            for(int i = 0; i < aux.size(); i++){
                if(aux.get(i).get_albumName().equals(NEED_AUTHENTICATION) && aux.get(i).get_userAlbum().equals(NEED_AUTHENTICATION)){
                    contextClass.removeInvite(i);
                }
                else {
                    _invitesCustomAdapter.add(aux.get(i));
                }
            }
            _invitesCustomAdapter.notifyDataSetChanged();
        }
    }

    public void removeItem(int index){
        ContextClass context = (ContextClass) getApplicationContext();
        context.removeInvite(index);
    }

    public AcceptAlbumTask getmAcceptALb() {
        return mAcceptALb;
    }

    public void setmAcceptALb(AcceptAlbumTask mAcceptALb) {
        this.mAcceptALb = mAcceptALb;
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(myBroadCastReceiver);
        Log.d("Debug Cenas", "onDestroy: albums activity");
        super.onDestroy();
    }
}

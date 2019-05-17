package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.DropboxCreateFolderTask;
import pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.CreateFolderInStorageTask;

public class CustomAdapterInvites extends BaseAdapter {

    private final ArrayList<Invite> _invites;
    private final AlbumsInvitationsActivity _act;

    CustomAdapterInvites(ArrayList<Invite> invites, AlbumsInvitationsActivity act) {
        _invites = invites;
        _act = act;
    }

    @Override
    public int getCount() {
        return _invites.size();
    }

    @Override
    public Object getItem(int position) {
        return _invites.get(position);
    }

    void clear() {
        _invites.clear();
        notifyDataSetChanged();
    }

    public void add(Invite invite){
        _invites.add(invite);
    }

    public void addAll(ArrayList<Invite> invites){
        _invites.addAll(invites);
    }

    public void remove(int i){
        Log.d("Debug Cenas", "costumadapter index " + i + " size " +_invites.size() );
        _invites.remove(i);
    }


    public void removeItem(int position){ _invites.remove(position); }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = _act.getLayoutInflater().inflate(R.layout.activity_albums_invitations_items, parent, false);
        Invite invite = _invites.get(position);

        TextView textView = view.findViewById(R.id.invitation_text);
        textView.setText(invite.get_userAlbum() +": Join my album " + invite.get_albumName());
        Button acceptBtn = view.findViewById(R.id.invitation_accept);
        Button refuseBtn = view.findViewById(R.id.invitation_refuse);

        final ContextClass contextClass = (ContextClass) _act.getApplicationContext();

        acceptBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Invite invite = _invites.get(position);

                if(contextClass.getAppMode().equals(_act.getString(R.string.AppModeDropBox))) {
                    //Create slice of album for which user as been invited
                    DropboxCreateFolderTask createDropboxFolderTask = new DropboxCreateFolderTask(_act, DropboxClientFactory.getClient(), new DropboxCreateFolderTask.Callback() {
                        @Override
                        public void onFolderCreated(String catalogURL) {
                            Invite invite = _invites.get(position);
                            _invites.remove(position);
                            notifyDataSetChanged();
                            _act.removeItem(position);
                            invite.set_dropboxUrl(catalogURL);
                            _act.setmAcceptALb(new AcceptAlbumTask(invite.get_userAlbum(), invite.get_albumName(), invite.get_dropboxUrl(), "true", contextClass.getAppMode() ,_act));
                            _act.getmAcceptALb().execute((Void) null);
                        }
                    });
                    createDropboxFolderTask.execute(invite.get_albumName(), invite.get_userAlbum()); //Pass the album name and its creator
                }
                else {
                    //Create slice of album for which user as been invited
                    CreateFolderInStorageTask createFolderTask = new CreateFolderInStorageTask(_act, new CreateFolderInStorageTask.Callback() {
                        @Override
                        public void onFolderCreated() {
                            Invite invite = _invites.get(position);
                            _invites.remove(position);
                            notifyDataSetChanged();
                            _act.removeItem(position);
                            invite.set_dropboxUrl("DummyCatalogURL");
                            _act.setmAcceptALb(new AcceptAlbumTask(invite.get_userAlbum(), invite.get_albumName(), invite.get_dropboxUrl(), "true", contextClass.getAppMode(),_act));
                            _act.getmAcceptALb().execute((Void) null);
                        }
                    });
                    createFolderTask.execute(invite.get_albumName(), invite.get_userAlbum()); //Pass the album name and its creator
                }
            }
        });
        refuseBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Invite invite = _invites.get(position);
                _invites.remove(position);
                notifyDataSetChanged();
                _act.removeItem(position);
                if(contextClass.getAppMode().equals(_act.getString(R.string.AppModeWifiDirect))) {
                    invite.set_dropboxUrl("DummyCatalogURL");
                }
                _act.setmAcceptALb(new AcceptAlbumTask(invite.get_userAlbum(), invite.get_albumName(), invite.get_dropboxUrl(), "false",contextClass.getAppMode() ,_act));
                _act.getmAcceptALb().execute((Void) null);
            }
        });

        return view;
    }

}


package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;

public class ContextClass extends Application {

    private ArrayList<Invite> newInvites = new ArrayList<>();

    public void addInvite(Invite i){
        newInvites.add(i);
        Log.d("Debug Cenas", "AddInvite: Invites size: " + newInvites.size());
    }

    public ArrayList<Invite> getInvites(){
        Log.d("Debug Cenas", "Get invites " + newInvites.size());
        return newInvites;
    }
    public Invite getInvite(int i){
        Log.d("Debug Cenas", "Get invite at index " + i + " size: " + newInvites.size());
        return newInvites.get(i);
    }

    public void removeInvite(int i){
        newInvites.remove(i);
        Log.d("Debug Cenas", "Removed invite at index " + i + " size is now " + newInvites.size());
    }

}

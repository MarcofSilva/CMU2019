package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CostumAdapterUsers extends BaseAdapter {

    private final List<String> _users;
    private final Activity act;

    public CostumAdapterUsers(ArrayList<String> users, Activity act) {
        this._users = users;
        this.act = act;
    }

    @Override
    public int getCount() {
        return _users.size();
    }

    @Override
    public Object getItem(int position) {
        return _users.get(position);
    }

    public void clear() {
        _users.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<String> users){
        _users.addAll(users);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = act.getLayoutInflater().inflate(R.layout.activity_find_users_item, parent, false);

        String name = _users.get(position);

        TextView nameView = view.findViewById(R.id.find_user_text);

        nameView.setText(name);

        return view;
    }
}

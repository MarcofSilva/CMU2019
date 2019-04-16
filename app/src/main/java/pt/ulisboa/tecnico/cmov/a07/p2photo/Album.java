package pt.ulisboa.tecnico.cmov.a07.p2photo;

import java.util.ArrayList;
import java.util.List;

public class Album {

    private String _name;
    private List<String> _users = new ArrayList<>();
    private List<String> _photosUrl = new ArrayList<>();

    public Album(String name){
        _name = name;
    }

    public Album(String name, List<String> users){
        _name = name;
        _users = users;
    }

    public void addUser(String user){
        /*if(!_users.contains(user)){
            _users.add(user);
        }*/
        // TODO nao sei se a de cima funciona, porque n sei se o contains procura a string ou a referencia da string
        boolean contains = false;
        for (String s : _users){
            if(s.equals(user)){
                contains = true;
                break;
            }
        }
        if(!contains){
            _users.add(user);
        }
    }

    public void disallowUser(String user){
        for(String s : _users){
            if(s.equals(user)){
                //TODO pode dar probs porque altera enquanto itera a lista
                _users.remove(s);
                return;
            }
        }
    }


    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public List<String> get_users() {
        return _users;
    }

    public void set_users(List<String> _users) {
        this._users = _users;
    }

    public List<String> get_photosUrl() {
        return _photosUrl;
    }

    public void set_photosUrl(List<String> _photosUrl) {
        this._photosUrl = _photosUrl;
    }
}

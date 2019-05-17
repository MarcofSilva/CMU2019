package pt.ulisboa.tecnico.cmov.a07.p2photo;

import android.os.Parcel;
import android.os.Parcelable;

public class Invite implements Parcelable {

    private String _userAlbum;
    private String _albumName;
    private String _dropboxUrl;
    private String _accepted;

    public Invite(String userAlbum, String albumName){
        _userAlbum = userAlbum;
        _albumName = albumName;
    }

    public Invite(String userAlbum, String albumName, String publicKey, String dropboxUrl){
        _userAlbum = userAlbum;
        _albumName = albumName;
        _dropboxUrl = dropboxUrl;
    }

    protected Invite(Parcel in) {
        _userAlbum = in.readString();
        _albumName = in.readString();
        _dropboxUrl = in.readString();
        _accepted = in.readString();
    }

    public static final Creator<Invite> CREATOR = new Creator<Invite>() {
        @Override
        public Invite createFromParcel(Parcel in) {
            return new Invite(in);
        }

        @Override
        public Invite[] newArray(int size) {
            return new Invite[size];
        }
    };

    public String get_userAlbum() {
        return _userAlbum;
    }

    public void set_userAlbum(String _userAlbum) {
        this._userAlbum = _userAlbum;
    }

    public String get_albumName() {
        return _albumName;
    }

    public void set_albumName(String _albumName) {
        this._albumName = _albumName;
    }

    public String get_dropboxUrl() {
        return _dropboxUrl;
    }

    public void set_dropboxUrl(String _dropboxUrl) {
        this._dropboxUrl = _dropboxUrl;
    }

    public String get_accepted() {
        return _accepted;
    }

    public void set_accepted(String _accepted) {
        this._accepted = _accepted;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(_userAlbum);
        dest.writeString(_albumName);
        dest.writeString(_dropboxUrl);
        dest.writeString(_accepted);
    }
}

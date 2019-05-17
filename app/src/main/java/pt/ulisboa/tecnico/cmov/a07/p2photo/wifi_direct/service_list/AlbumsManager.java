package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;

public class AlbumsManager {
    public static String BASE_FOLDER;

    private AppCompatActivity activity;

    public AlbumsManager(AppCompatActivity act){
        this.activity = act;
        BASE_FOLDER = this.activity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(this.activity);
    }

    public String compareUserAlbums(String albumsU1, String albumsU2) {
        String[] u1AlbumSplit = albumsU1.split(";");
        String[] u2AlbumSplit = albumsU2.split(";");
        HashMap<String, String> inCommonU1 = new HashMap<>(); //albumid, photos
        HashMap<String, String> inCommonU2 = new HashMap<>(); //albumid, photos
        String res = "";
        for (String s1 : u1AlbumSplit){
            String u1Id = s1.split("::")[0];
            for(String s2 : u2AlbumSplit){
                String u2Id = s2.split("::")[0];
                if (u1Id.equals(u2Id)){
                    if(!inCommonU1.containsKey(u1Id)){
                        inCommonU1.put(u1Id, s1.split("::")[1]);
                        inCommonU2.put(u2Id, s2.split("::")[1]);
                    }
                }
            }

        }
        for (Map.Entry<String, String> entryu1 : inCommonU1.entrySet()) {
            String key = entryu1.getKey();
            Object value = entryu1.getValue();
        }
    return "";
    }

    //Following format =>  AlbumName1:Album1Creator::Photo1,Photo2;AlbumName2:Album2Creator::Photo1,Photo2
    public String listLocalAlbumsPhotos(File base) throws IOException {
        String name;
        String result = "";
        File[] files = base.listFiles();
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result += file.getName() + "::" + listLocalAlbumsPhotos(file) + ";";
                } else if ((name = file.getName()).endsWith(".jpg")) {
                    result += name + ",";
                }
            }
            result = result.substring(0, result.length() - 1);
        }
        Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        return result;
    }

    private String getPhotosNamesInFolder(File catalogFile) throws IOException {
        //Read urls
        BufferedReader reader = new BufferedReader(new FileReader(catalogFile));
        String photos = "";
        String line;

        while((line = reader.readLine()) != null) {
            photos += line + ",";
        }
        reader.close();
        return photos.substring(0, photos.length() - 1);
    }
}
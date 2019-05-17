package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;

public class AlbumsManager {
    public static String BASE_FOLDER_MINE;
    public static String BASE_FOLDER_CACHE;

    private AppCompatActivity activity;

    public AlbumsManager(AppCompatActivity act) {
        this.activity = act;
        BASE_FOLDER_MINE = this.activity.getFilesDir() + "/P2PHOTO/" + SessionHandler.readTUsername(this.activity);
        BASE_FOLDER_CACHE = this.activity.getCacheDir() + "/P2PHOTO/" + SessionHandler.readTUsername(this.activity);
    }

    public String compareUserAlbums(String albumsU1, String albumsU2) {
        String[] u1AlbumSplit = albumsU1.split(";");
        String[] u2AlbumSplit = albumsU2.split(";");
        HashMap<String, String> inCommonU1 = new HashMap<>(); //albumid, photos
        HashMap<String, String> inCommonU2 = new HashMap<>(); //albumid, photos
        String res = "";
        for (String s1 : u1AlbumSplit) {
            String u1Id = s1.split("::")[0];
            for (String s2 : u2AlbumSplit) {
                String u2Id = s2.split("::")[0];
                if (u1Id.equals(u2Id)) {
                    if (!inCommonU1.containsKey(u1Id)) {
                        inCommonU1.put(u1Id, s1.split("::")[1]);
                        inCommonU2.put(u2Id, s2.split("::")[1]);
                    }
                }
            }

        }
        for (Map.Entry<String, String> entryu2 : inCommonU2.entrySet()) {
            res += entryu2.getKey() + "::" + entryu2.getValue() + ";";
        }

        return res;
    }

    //use the mine base folder path
    //Following format =>  AlbumName1:Album1Creator::Photo1,Photo2;AlbumName2:Album2Creator::Photo1,Photo2
    public String listLocalAlbumsPhotos(File base) throws IOException {
        String name;
        String result = "";
        File[] files = base.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    result += file.getName() + "::" + listLocalAlbumsPhotos(file) + ";";
                } else if ((name = file.getName()).endsWith(".jpg")) {
                    result += name + ",";
                }
            }
            if(result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
        }
        return result;
    }

    private String getPhotosNamesInFolder(File catalogFile) throws IOException {
        //Read urls
        BufferedReader reader = new BufferedReader(new FileReader(catalogFile));
        String photos = "";
        String line;

        while ((line = reader.readLine()) != null) {
            photos += line + ",";
        }
        reader.close();
        if(photos.length() > 0) {
            return photos.substring(0, photos.length() - 1);
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String photosToShare(String photos) throws IOException {
        String[] u1AlbumSplit = photos.split(";");
        String res = "";
        for (String s1 : u1AlbumSplit) {
            String[] albumPhotosSplit = s1.split("::");
            String photoss = "";
            String[] photosSplit = albumPhotosSplit[1].split(",");
            for (String foto : photosSplit) {
                File fileImage = new File(BASE_FOLDER_MINE + "/" + albumPhotosSplit[0], foto);
                byte[] file = Files.readAllBytes(fileImage.toPath());
                String encodedImage = Base64.encodeToString(file, Base64.NO_WRAP);
                photos += encodedImage + ",";
            }
            if (photos.length() > 0) {
                photos = photos.substring(0, photos.length() - 1); //remove common
            }
            res += albumPhotosSplit[0] + "::" + photos + ";";
        }
        if (res.length() > 0) {
            return res.substring(0, res.length() - 1);
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void storeNewPhotos(String photosReceived) throws IOException {
        String[] u1AlbumSplit = photosReceived.split(";");

        for (String s1 : u1AlbumSplit) {
            String[] albumSplit = s1.split("::");
            String albumFolder = BASE_FOLDER_CACHE + "/" + albumSplit[0];
            File folder = new File(albumFolder);
            if(!folder.exists()) {
                folder.mkdirs();
            }
            String[] fotos = albumSplit[1].split(",");
            int i = 0;
            for(String foto : fotos) {
                byte[] imageBytes = Base64.decode(foto, Base64.NO_WRAP);
                File newImage = new File(albumFolder + "/" + (i+=1) + ".jpg");
                try {
                    newImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Files.write(newImage.toPath(), imageBytes);
            }
        }
    }
}
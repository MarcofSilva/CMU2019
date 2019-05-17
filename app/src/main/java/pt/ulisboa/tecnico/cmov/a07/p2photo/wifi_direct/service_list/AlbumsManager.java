package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AlbumsManager {
    private WiFiServiceDiscoveryActivity activity;

    public AlbumsManager(WiFiServiceDiscoveryActivity activity){
        this.activity = activity;
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
}

package pt.ulisboa.tecnico.cmov.a07.p2photo;

import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;


//Singleton instance of {@link DbxClientV2} and friends
public class DropboxClientFactory {

    private static DbxClientV2 sDbxClient;

    public static void init(String accessToken) {
        if (sDbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("client").build();
                    //.withHttpRequestor(new OkHttpRequestor(OkHttpRequestor.defaultOkHttpClient()))
                    //.build();

            sDbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    public static DbxClientV2 getClient() {
        if (sDbxClient == null) {
            throw new IllegalStateException("Client not initialized.");
        }
        return sDbxClient;
    }
}

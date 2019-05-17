package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;

/**
 * A structure to hold service information.
 */
public class WiFiP2pService {
    WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;
    Boolean isGroupOwner = false;

    public interface MessageTarget {
        public Handler getHandler();
    }
}

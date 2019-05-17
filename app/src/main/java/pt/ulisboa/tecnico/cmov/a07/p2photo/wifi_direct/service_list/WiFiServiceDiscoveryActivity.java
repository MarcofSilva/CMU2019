package pt.ulisboa.tecnico.cmov.a07.p2photo.wifi_direct.service_list;

import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.support.design.widget.Snackbar;


import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.ulisboa.tecnico.cmov.a07.p2photo.R;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * The main activity for the sample. This activity registers a local service and
 * perform discovery over Wi-Fi p2p network. It also hosts a couple of fragments
 * to manage chat operations. When the app is launched, the device publishes a
 * chat service and also tries to discover services published by other peers. On
 * selecting a peer published service, the app initiates a Wi-Fi P2P (Direct)
 * connection with the peer. On successful connection with a peer advertising
 * the same service, the app opens up sockets to initiate a chat.
 * {@code WiFiChatFragment} is then added to the the main activity which manages
 * the interface and messaging needs for a chat session.
 */


public class WiFiServiceDiscoveryActivity extends AppCompatActivity implements WiFiDirectServicesList.DeviceClickListener, Handler.Callback, WiFiP2pService.MessageTarget,
        ConnectionInfoListener {

    public static final String TAG = "wifidirectdemo";
    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    private WifiP2pManager manager;
    static final int SERVER_PORT = 8880;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler handler = new Handler(this);
    private WiFiDirectServicesList servicesList;
    private TextView statusTxtView;
    public List<WifiP2pDevice> currentPeers = new ArrayList<WifiP2pDevice>();
    public HashMap<String, String> currentPeersAddress = new HashMap(); //name, ip
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private HashMap<WifiP2pDevice, WiFiP2pService> services = new HashMap<>();

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        statusTxtView = (TextView) findViewById(R.id.status_text);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        startRegistrationAndDiscovery();
        servicesList = new WiFiDirectServicesList();
        getFragmentManager().beginTransaction().add(R.id.container_root, servicesList, "services").commit();
    }

    @Override
    protected void onRestart() {
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new ActionListener() {
                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }
            });
        }
        super.onStop();
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {

        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Map<String, String> record = new HashMap<String, String>();
                record.put(TXTRECORD_PROP_AVAILABLE, "visible");
                WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
                manager.addLocalService(channel, service, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        appendStatus("Added Local Service");
                    }

                    @Override
                    public void onFailure(int error) {
                        appendStatus("Failed to add a service");
                    }
                });
                discoverService();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "clear fail");

            }
        });

    }

    private void discoverService() {
        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        manager.setDnsSdResponseListeners(channel,
                new DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {
                        // A service has been discovered. Is this our app?
                        Log.d(TAG, "A service has been discovered. Is this our app?");
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                            if (!currentPeers.contains(srcDevice)) {
                                currentPeers.add(srcDevice);
                                currentPeersAddress.put(srcDevice.deviceName, srcDevice.deviceAddress);
                                // If an AdapterView is backed by this data, notify it
                                // of the change. For instance, if you have a ListView of
                                // available peers, trigger an update.
                                // Perform any other updates needed based on the new list of
                                // peers connected to the Wi-Fi P2P network.
                            }

                            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager().findFragmentByTag("services");
                            if (fragment != null) {
                                WiFiDirectServicesList.WiFiDevicesAdapter adapter = ((WiFiDirectServicesList.WiFiDevicesAdapter) fragment.getListAdapter());
                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;
                                services.put(service.device, service);
                                adapter.add(service);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "onBonjourServiceAvailable " + instanceName);
                            }
                        }
                    }
                }, new DnsSdTxtRecordListener() {
                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG,
                                device.deviceName + " is " + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });
        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest();
        discoverServices();
    }

    public void serviceRequest() {
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Added service discovery request");
                        appendStatus("Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        Log.d(TAG, "Failed adding service discovery request with code " + arg0);
                        appendStatus("Failed adding service discovery request");
                        serviceRequest();

                    }
                });
    }

    public void discoverServices() {
        manager.discoverServices(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Service discovery initiated");
                appendStatus("Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                Log.d(TAG, "Service discovery failed with code " + arg0);
                appendStatus("Service discovery failed");
                discoverServices();
            }
        });
    }

    @Override
    public void connectP2p(WiFiP2pService service) {
        if (currentPeers.size() != 0) {
            WifiP2pDevice device = currentPeers.get(0);

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;

            if (serviceRequest != null)
                manager.removeServiceRequest(channel, serviceRequest,
                        new ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int arg0) {
                            }
                        });
            manager.connect(channel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Connecting to service");
                    appendStatus("Connecting to service");
                }

                @Override
                public void onFailure(int errorCode) {
                    Log.d(TAG, "Failed connecting to service with code " + errorCode);
                    appendStatus("Failed connecting to service");
                }
            });
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Toast.makeText(this, readMessage, Toast.LENGTH_SHORT).show();
                Log.d(TAG, readMessage);
                break;
            case MY_HANDLE:
                Object obj = msg.obj;
        }
        return true;
    }

    @Override
    public void onResume() {
        mayRequestPermission(READ_EXTERNAL_STORAGE, REQUEST_READ_EXTERNAL_STORAGE);
        mayRequestPermission(WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_EXTERNAL_STORAGE);
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager().findFragmentByTag("services");
        if (fragment != null) {
            WiFiDirectServicesList.WiFiDevicesAdapter adapter = ((WiFiDirectServicesList.WiFiDevicesAdapter) fragment.getListAdapter());
            for (Map.Entry<WifiP2pDevice,WiFiP2pService> entry : services.entrySet()){
                if (entry.getKey().deviceAddress == p2pInfo.groupOwnerAddress.toString()){
                    WiFiP2pService ser = adapter.getService(entry.getValue());
                    ser.isGroupOwner = true;
                }

            }
            adapter.notifyDataSetChanged();
        }
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */
        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                handler = new GroupOwnerSocketHandler(getApplicationContext(), statusTxtView, currentPeersAddress, ((WiFiP2pService.MessageTarget) this).getHandler());
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
                return;
            }
        } else {
            Log.d(TAG, "Connected as peer");
            handler = new ClientSocketHandler(p2pInfo.groupOwnerAddress, statusTxtView, getApplicationContext(), ((WiFiP2pService.MessageTarget) this).getHandler());
            handler.start();
        }
        statusTxtView.setVisibility(View.GONE);
    }

    public void appendStatus(String status) {
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }

    private boolean mayRequestPermission(final String permission, final int requestCode ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(permission)) {
            Snackbar.make(statusTxtView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v) {
                    requestPermissions(new String[]{permission}, requestCode);
                }
            });
        } else {
            requestPermissions(new String[]{permission}, requestCode);
        }
        return false;
    }


}
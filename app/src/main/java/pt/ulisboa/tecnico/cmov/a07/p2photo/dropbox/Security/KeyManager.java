package pt.ulisboa.tecnico.cmov.a07.p2photo.dropbox.Security;

import android.app.Activity;
import android.util.Log;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.a07.p2photo.SessionHandler;


public class KeyManager {

    private static final KeyManager _keyManager = new KeyManager();

    private PublicKey _publicKey; // to send to the server to receive the secretkey
    private PrivateKey _privateKey; // to decrypt the url of another user
    private KeyPair _keypair;
    private KeyPairGenerator keyPairGen;
    private HashMap<String, byte[]> albumKeys = new HashMap<>();

    private KeyManager() {
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyPairGen.initialize(2048);
    }

    private static KeyManager getInstance() {
        return _keyManager;
    }

    public static void addAlbumKey(String albumName, byte[] albumKey){
        _keyManager.albumKeys.put(albumName, albumKey);
    }

    public static boolean containsKeyforAlbum(String albumName){
        if(_keyManager.albumKeys.containsKey(albumName)){
            if(_keyManager.albumKeys.get(albumName) != null){
                return true;
            }
        }
        return false;
    }

    public static void generateAlbumkey(String albumName){
        byte[] sliceKey = generateSecret();
        _keyManager.albumKeys.put(albumName, sliceKey);
    }

    //encrypts url with specific albumkey
    public static String encrypt(String albumName, String urlToEncrypt){
        Cipher cipher = null;
        String urlEncrypted = "";
        try {
            cipher = Cipher.getInstance("AES");
            byte[] key = _keyManager.albumKeys.get(albumName);
            cipher.init(Cipher.ENCRYPT_MODE, getAlbumKey(albumName, "AES"));
            Log.d("security", urlToEncrypt);
            byte[] log = urlToEncrypt.getBytes();
            byte[] encrypted = cipher.doFinal(urlToEncrypt.getBytes());
            urlEncrypted = KeyManager.byteArrayToHexString(encrypted);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return urlEncrypted;
    }

    public static String decrypt(String albumName, String urlToDecrypt){
        String urlDecrypted = "";
        try{
            Cipher cipher = Cipher.getInstance("AES");
            byte[] key = _keyManager.albumKeys.get(albumName);
            cipher.init(Cipher.DECRYPT_MODE, getAlbumKey(albumName,"AES"));
            byte[] decrypted = cipher.doFinal(KeyManager.hexStringToBytes(urlToDecrypt));
            urlDecrypted = new String(decrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return urlDecrypted;
    }

    public static byte[] generateSecret(){
        return generateSecret(16);
    }

    public static byte[] getAlbumKeyByName(String albumName){
        return _keyManager.albumKeys.get(albumName);
    }

    public static SecretKey getAlbumKey(String albumName, String algorithm) {
        if(_keyManager.albumKeys.get(albumName) == null) {
            return null;
        }
        return new SecretKeySpec(_keyManager.albumKeys.get(albumName), algorithm);
    }

    public static byte[] generateSecret(int numbytes) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[numbytes];
        secureRandom.nextBytes(key);
        return key;
    }

    public static KeyPair generateKeyPair(){
        if(_keyManager._keypair == null){
            KeyPair keyPair = _keyManager.keyPairGen.generateKeyPair();
            _keyManager._keypair = keyPair;
            _keyManager._privateKey = keyPair.getPrivate();
            _keyManager._publicKey = keyPair.getPublic();
        }

        return _keyManager._keypair;
    }

    public static KeyPair readKeyPair(String username, Activity activity){
        KeyPair kp = SessionHandler.readKeyPair(username, activity);
        if(kp.getPublic() == null || kp.getPrivate() == null){
            return null;
        }
        _keyManager._keypair = kp;
        _keyManager._privateKey = _keyManager._keypair.getPrivate();
        _keyManager._publicKey = _keyManager._keypair.getPublic();
        return _keyManager._keypair;
    }

    public static void writeKeyPair(String username, Activity activity){
        SessionHandler.writeKeyPair(_keyManager._keypair, username, activity);
    }

    public static byte[] encryptAlbumKey(String albumName, PublicKey key) {
        Cipher cipher = null;
        byte[] res = null;

        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            res = cipher.doFinal(_keyManager.albumKeys.get(albumName));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return res;
    }

    //decrypts with my private key, the album key that someone encrypted with my public
    public static byte[] decryptAlbumKey(byte[] input) {
        Cipher cipher = null;
        byte[] res = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE,_keyManager._privateKey);
            res = cipher.doFinal(input);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static String encrMyPrivate(String albumName) throws Exception {
        return KeyManager.byteArrayToHexString(encryptAlbumKey(albumName,_keyManager._publicKey));
    }

    public static PrivateKey byteArrayToPrivKey(byte[] bytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey byteArrayToPubKey(byte[] bytes) {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = null;
        try {
            kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }


    //Base64 Encoder and Decoder don't work for Android with API < 26 and the android used for testing with the lowest API version has API 23
    //As so the hexStringToBytes and byteArrayToHexString help us to convert byte arrays to strings and vice versa
    public static byte[] hexStringToBytes(String hexInputString){

        byte[] byteArray = new byte[hexInputString.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hexInputString.substring(2*i, 2*i+2), 16);
        }
        return byteArray;
    }

    public static String byteArrayToHexString(byte[] byteArray) {
        StringBuffer buffer = new StringBuffer();

        for(int i =0; i < byteArray.length; i++){
            String hex = Integer.toHexString(0xff & byteArray[i]);

            if(hex.length() == 1)
                buffer.append("0");

            buffer.append(hex);
        }
        return buffer.toString();
    }


    //shouldnt be used here
    /*public static PublicKey getPublicKey(){
        if(_keyManager._publicKey == null){
            //generateKeyPair();
        }
        return _keyManager._publicKey;
    }

    public static PrivateKey getPrivateKey(Activity activity){
        if(_keyManager._privateKey == null){
            try {
                String keyString = SessionHandler.readKey(activity);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                _keyManager._privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(_keyManager.hexStringToBytes(keyString)));

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
        return _keyManager._privateKey;
    }*/


}

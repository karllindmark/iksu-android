package com.ninetwozero.iksu.features.accounts;

import android.content.Context;

import com.google.firebase.crash.FirebaseCrash;
import com.ninetwozero.iksu.app.IksuApp;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

public class LoginUtil {
    private static final String CONFIDENTIALITY_KEY = IksuApp.class.getPackage().getName() + "_C";
    private static final String INTEGRITY_KEY = IksuApp.class.getPackage().getName() + "_I";
    private static final String KEYSTORE_NAME = "iksuKeyStore";

    private Context context;
    private KeyStore keyStore;
    private final File keyStoreFile;
    private final char[] keyStorePassword;

    public LoginUtil(final Context context, final String keyStoreKey) {
        keyStorePassword = keyStoreKey.toCharArray();

        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStoreFile = new File(context.getFilesDir(), KEYSTORE_NAME);
            if (keyStoreFile.exists()) {
                try(FileInputStream fileInputStream = new FileInputStream(keyStoreFile)) {
                    keyStore.load(fileInputStream, keyStorePassword);
                }
            } else {
                keyStore.load(null);
            }
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
            throw new IllegalStateException("Unsupported device :(");
        }
    }

    public String encryptPassword(final String plainString) {
        try {
            return AesCbcWithIntegrity.encrypt(plainString, getKeysFromKeyStore()).toString();
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
        }
        throw new IllegalStateException("Unable to encrypt passwords :(");
    }

    public String decryptPassword(final String encryptedString)  {
        try {
            return AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(encryptedString), getKeysFromKeyStore());
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
        }
        throw new IllegalStateException("Unable to decrypt passwords :(");
    }

    public void initKeystore()  {
        try {
            final AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.generateKey();
            if (!keyStore.containsAlias(CONFIDENTIALITY_KEY) || keyStore.containsAlias(INTEGRITY_KEY)) {
                keyStore.setEntry(CONFIDENTIALITY_KEY, new KeyStore.SecretKeyEntry(keys.getConfidentialityKey()), null);
                keyStore.setEntry(INTEGRITY_KEY, new KeyStore.SecretKeyEntry(keys.getIntegrityKey()), null);

                try (FileOutputStream fileOutputStream = new FileOutputStream(keyStoreFile)) {
                    keyStore.store(fileOutputStream, keyStorePassword);
                }
            }
        } catch (Exception ex) {
            FirebaseCrash.report(ex);
            throw new IllegalStateException("Unable to initialize the password encryption on your device :(");
        }
    }

    private AesCbcWithIntegrity.SecretKeys getKeysFromKeyStore() throws Exception {
        final KeyStore.SecretKeyEntry confidentialityKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(CONFIDENTIALITY_KEY, null);
        final KeyStore.SecretKeyEntry integrityKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(INTEGRITY_KEY, null);
        return new AesCbcWithIntegrity.SecretKeys(confidentialityKeyEntry.getSecretKey(), integrityKeyEntry.getSecretKey());
    }
}

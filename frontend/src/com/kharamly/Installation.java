package com.kharamly;

import java.io.*;
import java.util.*;

import android.content.*;
import android.provider.*;

/**
 * Source: http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
 * Modified by kamasheto
 */
public class Installation {
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";
    
    /**
     * Usage: 
     *    String installation = Installation.id(this); // called from an Activity
     *
     * @param context the context being called from
     * @return unique installation id
     */
    public synchronized static String id(Context context) {
        if (sID == null) {  
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation, false);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    /**
     * Since the ANDROID_ID might be changed in rare cases of 
     * a device being rooted and the value manipulated by the 
     * user, this could be used (in future releases) to override 
     * the installation file/id when detected. Future work needs 
     * be done to make sure this is handled. For now a randomly
     * generated UUID should suffice.
     * 
     * @param installation file to write into
     * @param random whether to write a randomly generated ID or 
     *               use the ANDROID_ID value
     */
    public static void writeInstallationFile(File installation, boolean random) throws IOException {
        String id = random ? UUID.randomUUID().toString() : Settings.Secure.ANDROID_ID;
        FileOutputStream out = new FileOutputStream(installation);
        out.write(id.getBytes());
        out.close();
    }
}

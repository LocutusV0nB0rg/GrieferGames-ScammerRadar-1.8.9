package de.newH1VE.griefergames.antiScammer;


import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;


public class ListUpdater {

    private static final File scammerFilePath = new File("LabyMod/antiScammer");


    public void updateScammerFile(File _scammerFile) {


        if (scammerFilePath.exists()) {

            try {

                URL url = new URL("https://coolertyp.scammer-radar.de/onlineScammer.json");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                BufferedInputStream in = new BufferedInputStream(url.openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(_scammerFile);
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }


            } catch (IOException ex) {
            }
        }

    }
}

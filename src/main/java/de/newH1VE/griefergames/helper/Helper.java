package de.newH1VE.griefergames.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.newH1VE.griefergames.GrieferGames;
import de.newH1VE.griefergames.antiScammer.Scammer;
import net.labymod.core.LabyModCore;
import net.labymod.utils.UUIDFetcher;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Helper {

    private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    private static final Type listType = new TypeToken<ArrayList<Scammer>>() { }.getType();
    private static final File onlineScammerFile = new File("LabyMod/antiScammer/onlineScammer.json");
    private static final File localScammerFile = new File("LabyMod/antiScammer/localScammer.json");
    private static final File scammerFilePath = new File("LabyMod/antiScammer");

    public Helper() {
        // do nothing :)
    }

    public void updateScammerLists() {

        final List<Scammer> onlineScammerList = GrieferGames.getAntiscammer().getOnlineScammerList();
        final List<Scammer> localScammerList = GrieferGames.getAntiscammer().getLocalScammerList();

        Thread thread = new Thread() {
            public void run() {
                boolean changedOnlineScammerList = false;
                boolean changedLocalScammerList = false;

                for (Scammer scammer : onlineScammerList) {
                    String playerName = UUIDFetcher.getName(UUID.fromString(scammer.uuid));
                    if (!playerName.equalsIgnoreCase(scammer.name)) {
                        scammer.name = playerName;
                        changedOnlineScammerList = true;
                    }
                }
                for (Scammer scammer : localScammerList) {
                    String playerName = UUIDFetcher.getName(UUID.fromString(scammer.uuid));
                    if (!playerName.equalsIgnoreCase(scammer.name)) {
                        scammer.name = playerName;
                        changedLocalScammerList = true;
                    }
                }
                if (changedOnlineScammerList) {

                    saveScammerFile(onlineScammerList, onlineScammerFile);
                }
                if (changedLocalScammerList) {
                    saveScammerFile(localScammerList, localScammerFile);
                }

                GrieferGames.getAntiscammer().setScammerList(new ArrayList<String>());
                GrieferGames.getAntiscammer().setOnlineScammerList(loadScammerFile(onlineScammerList, onlineScammerFile));
                GrieferGames.getAntiscammer().setLocalScammerList(loadScammerFile(localScammerList, localScammerFile));


            }
        };

        thread.start();

    }

    public List<Scammer> loadScammerFile(List<Scammer> _scammerList, File _scammerFile) {

        List<String> scammerList = GrieferGames.getAntiscammer().getScammerList();

        if (!scammerFilePath.exists()) {
            try {
                scammerFilePath.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        if (!_scammerFile.exists()) {
            try {
                _scammerFile.createNewFile();


                if (_scammerFile.equals(onlineScammerFile)) {
                    // get local resource as stream
                    InputStream stream = this.getClass().getClassLoader().getResourceAsStream("assets/minecraft/griefergames/scammer/onlineScammer.json");


                    // create byte array from stream
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    // write buffer to file
                    @SuppressWarnings("resource")
                    OutputStream outStream = new FileOutputStream(onlineScammerFile);
                    outStream.write(buffer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


        FileInputStream stream = null;
        try {
            stream = new FileInputStream(_scammerFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            _scammerList = gson.fromJson(IOUtils.toString(stream, StandardCharsets.UTF_8), listType);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (_scammerList != null) {
            for (Scammer scammer : _scammerList) {
                if (!scammerList.contains(scammer.name.toLowerCase())) {
                    scammerList.add(scammer.name.toLowerCase());
                    System.out.println(scammer.name);
                }
            }
            GrieferGames.getAntiscammer().setScammerList(scammerList);
            return _scammerList;
        }
        return new ArrayList<Scammer>();
    }

    public static void saveScammerFile(List<Scammer> scammerList, File scammerFile) {
        try {
            PrintWriter w = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(scammerFile), StandardCharsets.UTF_8), true);
            w.print(gson.toJson(scammerList));
            w.flush();
            w.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static List<String> getScammerOnServer() {
        List<String> scammerServerList = new ArrayList<String>();
        List<String> scammerList = GrieferGames.getAntiscammer().getScammerList();
        NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().sendQueue;
        Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.getPlayerInfoMap();
        try {
            for (NetworkPlayerInfo player : playerMap) {
                boolean found = false;
                if (player.getDisplayName() != null) {
                    IChatComponent playerDisplayName = player.getDisplayName();

                    String playerName = player.getGameProfile().getName();

                    for (String scammer : scammerList) {
                        if (playerName.equals(scammer)) {
                            scammerServerList.add(playerName);
                        }
                    }

                }

            }

        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return scammerServerList;
    }
}

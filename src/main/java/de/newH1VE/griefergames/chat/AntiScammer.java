package de.newH1VE.griefergames.chat;

import static net.labymod.utils.ModColor.BOLD;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.newH1VE.griefergames.GrieferGames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.*;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.newH1VE.griefergames.antiScammer.Scammer;
import net.labymod.core.LabyModCore;
import net.labymod.utils.ModColor;
import net.labymod.utils.UUIDFetcher;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.lwjgl.Sys;

public class AntiScammer extends Chat {
    ModColor modcolor = null;
    EnumChatFormatting chatformat = null;


    IChatComponent resetMsg = new ChatComponentText(" ")
            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RESET));


    private static Pattern msgUserGlobalChatRegex = Pattern.compile("^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");
    private static Pattern msgUserGlobalChatClanRegex = Pattern
            .compile("^(\\[[^\\]]+\\])\\s([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})\\s\\u00bb");
    private static Pattern privateMessageRegex = Pattern
            .compile("^\\[([A-Za-z\\-]+\\+?) \\\u2503 ((\\u007E)?\\w{1,16}) -> mir\\](.*)$");
    private static Pattern playerPaymentReceiveRegexp = Pattern.compile(
            "^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16}) hat dir \\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?) gegeben\\.$");
    private static Pattern privateMessageSentRegex = Pattern
            .compile("^\\[mir -> ([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})\\](.*)$");
    private static Pattern antiMagixPrefixRegex = Pattern.compile("([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");
    private String defaultAMPTablistReplacement = "%CLEAN%";


    List<String> scammerList = new ArrayList<String>();
    List<Scammer> onlineScammerList = new ArrayList<Scammer>();
    List<Scammer> localScammerList = new ArrayList<Scammer>();
    private static final File scammerFilePath = new File("LabyMod/antiScammer");
    private static final File onlineScammerFile = new File("LabyMod/antiScammer/onlineScammer.json");
    private static final File localScammerFile = new File("LabyMod/antiScammer/localScammer.json");
    private static final Gson gson = (new GsonBuilder()).setPrettyPrinting().create();
    Type listType = new TypeToken<ArrayList<Scammer>>() {
    }.getType();

    private List<Scammer> loadScammerFile(List<Scammer> _scammerList, File _scammerFile) {


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
                    //InputStream stream = Minecraft.getMinecraft().getResourceManager()
                    //       .getResource(new ResourceLocation("assets/minecraft/griefergames/scammer/onlineScammer.json"))
                    //    .getInputStream();


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
            return _scammerList;
        }
        return new ArrayList<Scammer>();
    }

    public void saveScammerFile(List<Scammer> scammerList, File scammerFile) {
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

    public AntiScammer() {
        onlineScammerList = loadScammerFile(onlineScammerList, onlineScammerFile);
        localScammerList = loadScammerFile(localScammerList, localScammerFile);
    }

    private boolean isGlobalMessage(String unformatted) {
        Matcher matcher = msgUserGlobalChatRegex.matcher(unformatted);
        Matcher matcher2 = msgUserGlobalChatClanRegex.matcher(unformatted);
        Matcher matcher3 = playerPaymentReceiveRegexp.matcher(unformatted);

        if (matcher3.find()) {
            return false;
        }

        if (matcher.find() && !getUserFromGlobalMessage(unformatted)
                .equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
            return true;
        } else if (matcher2.find() && !getUserFromGlobalMessage(unformatted)
                .equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
            return true;
        }
        return false;
    }

    private String getUserFromGlobalMessage(String unformatted) {
        String displayName = "";
        Matcher msgUserGlobalChat = msgUserGlobalChatRegex.matcher(unformatted);
        Matcher msgUserGlobalChatClan = msgUserGlobalChatClanRegex.matcher(unformatted);
        if (msgUserGlobalChat.find()) {
            displayName = msgUserGlobalChat.group(2);
        } else if (msgUserGlobalChatClan.find()) {
            displayName = msgUserGlobalChatClan.group(3);
        }
        return displayName;
    }

    @Override
    public String getName() {
        return "antiScammer";
    }

    @Override
    public boolean doAction(String unformatted, String formatted) {
        return true;
    }

    @Override
    public boolean doActionModifyChatMessage(IChatComponent msg) {
        String unformatted = msg.getUnformattedText();
        String formatted = msg.getFormattedText();

        return doAction(unformatted, formatted);
    }

    public List<String> getScammerOnServer()
    {
        List<String> scammerServerList = new ArrayList<String>();
        NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().sendQueue;
        Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.getPlayerInfoMap();
        try {
            for (NetworkPlayerInfo player : playerMap) {
                boolean found = false;
                if (player.getDisplayName() != null) {
                    IChatComponent playerDisplayName = player.getDisplayName();

                    String playerName = player.getGameProfile().getName();

                    for (Scammer scammer : onlineScammerList) {
                        if (playerName.equals(scammer.name)) {
                       scammerServerList.add(playerName);
                            }
                        }


                    for (Scammer scammer : localScammerList) {

                        if (playerName.equals(scammer.name)) {
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


    public void modifyTabList() {
        NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().sendQueue;
        Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.getPlayerInfoMap();

        try {
            for (NetworkPlayerInfo player : playerMap) {
                boolean found = false;
                if (player.getDisplayName() != null) {
                    IChatComponent playerDisplayName = player.getDisplayName();

                    String oldMessage = playerDisplayName.getFormattedText().replaceAll("\u00A7", "§");
                    if (oldMessage.indexOf("§k") != -1) {
                        oldMessage = oldMessage.replaceAll("§k", "");
                        oldMessage = oldMessage.replaceAll("§", "\u00A7");
                        playerDisplayName = new ChatComponentText(oldMessage);
                    }

                    if (playerDisplayName.getUnformattedText().startsWith("[AMP]")) {
                        playerDisplayName = playerDisplayName.getSiblings().get(playerDisplayName.getSiblings().size() - 1);
                    }

                    String fullItem = playerDisplayName.getUnformattedText();
                    String playerName = player.getGameProfile().getName();
                    IChatComponent newPlayerDisplayName = playerDisplayName;


                            for (Scammer scammer : onlineScammerList) {
                                if (playerName.equals(scammer.name)) {
                                    found = true;
                                    if (!fullItem.startsWith("[SCAMMER]")) {
                                        IChatComponent befsign = new ChatComponentText("[").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
                                        IChatComponent afsign = new ChatComponentText("] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
                                        IChatComponent sign = new ChatComponentText("SCAMMER").setChatStyle(new ChatStyle().setColor(chatformat).setBold(true));
                                        newPlayerDisplayName = befsign.appendSibling(sign).appendSibling(afsign).appendSibling(playerDisplayName);
                                    }
                                }
                            }

                            for (Scammer scammer : localScammerList) {

                                if (playerName.equals(scammer.name)) {
                                    found = true;

                                    if (!fullItem.startsWith("[SCAMMER]")) {
                                        IChatComponent befsign = new ChatComponentText("[").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
                                        IChatComponent afsign = new ChatComponentText("] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW));
                                        IChatComponent sign = new ChatComponentText("SCAMMER").setChatStyle(new ChatStyle().setColor(chatformat).setBold(true));
                                        newPlayerDisplayName = befsign.appendSibling(sign).appendSibling(afsign).appendSibling(playerDisplayName);
                                    }
                                }

                            }

                            if (fullItem.startsWith("[SCAMMER]") && found == false) {
                                newPlayerDisplayName = playerDisplayName.getSiblings().get(playerDisplayName.getSiblings().size() - 1);
                            }
                            player.setDisplayName(newPlayerDisplayName);
                        }
                    }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }


    private String getUserFromPrivateMessage(String unformatted) {
        String displayName = "";

        Matcher privateMessage = privateMessageRegex.matcher(unformatted);
        if (privateMessage.find()) {
            displayName = privateMessage.group(2);
        }

        Matcher privateSentMessage = privateMessageSentRegex.matcher(unformatted);
        if (privateSentMessage.find()) {
            displayName = privateSentMessage.group(2);
        }

        return displayName;
    }

    private boolean isPrivateMessage(String unformatted) {
        Matcher privateMessage = privateMessageRegex.matcher(unformatted);
        Matcher privateSentMessage = privateMessageSentRegex.matcher(unformatted);
        Matcher matcher3 = playerPaymentReceiveRegexp.matcher(unformatted);

        if (matcher3.find()) {
            return false;
        }

        if (unformatted.trim().length() > 0 && privateMessage.find())
            return true;


        if (unformatted.trim().length() > 0 && privateSentMessage.find())
            return true;

        return false;
    }

    @Override
    public IChatComponent modifyChatMessage(IChatComponent msg) {

        modcolor = getGG().getPrefixcolor();
        chatformat = getGG().getChatformat();
        String prefix = ModColor.RESET + " " + ModColor.GOLD + " [" + ModColor.RESET + "" + modcolor + "" + BOLD
                + "SCAMMER" + ModColor.RESET + " " + ModColor.GOLD + "] " + ModColor.RESET + "";

        String unformatted = msg.getUnformattedText();
        String formatted = msg.getFormattedText();

        IChatComponent newMsg = new ChatComponentText("");

        if (doAction(unformatted, formatted)) {
            String userName = "";

            if (isGlobalMessage(unformatted)) {
                userName = getUserFromGlobalMessage(unformatted);
            }

            if (isPrivateMessage(unformatted)) {

                userName = getUserFromPrivateMessage(unformatted);
            }

            if (userName.trim().length() > 0 && scammerList.contains(userName.toLowerCase())) {

                IChatComponent befScammerMsg = new ChatComponentText("[")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));
                IChatComponent scammerMsg = new ChatComponentText("SCAMMER")
                        .setChatStyle(new ChatStyle().setColor(chatformat).setBold(true));
                IChatComponent aftScammerMsg = new ChatComponentText("] ")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD));
                IChatComponent scammersign = new ChatComponentText("").appendSibling(befScammerMsg)
                        .appendSibling(scammerMsg).appendSibling(aftScammerMsg);


                if (isGlobalMessage(unformatted) && getGG().isMessageEnabled()) {
                    getApi().displayMessageInChat(
                            prefix + ModColor.YELLOW + BOLD + "Der Spieler " + ModColor.DARK_RED + BOLD + userName
                                    + ModColor.YELLOW + BOLD + " ist als Scammer hinterlegt. Achtung! " + prefix);
                }

                if (getGG().isPrefixEnabled()) {
                    newMsg.appendSibling(scammersign).appendSibling(resetMsg).appendSibling(msg);

                    return newMsg;
                }

            }

            return msg;
        }

        return super.modifyChatMessage(msg);
    }

    public void printPrefixLine() {
        modcolor = getGG().getPrefixcolor();
        String prefix = ModColor.RESET + "" + ModColor.GOLD + " [" + ModColor.RESET + "" + modcolor + "" + BOLD
                + "SCAMMER" + ModColor.RESET + "" + ModColor.GOLD + "] " + ModColor.RESET + "";

        getApi().displayMessageInChat(ModColor.BLACK + BOLD.toString() + "\u00a74\u00a7l\u00a7m----------" + prefix
                + ModColor.BLACK + BOLD.toString() + "\u00a74\u00a7l\u00a7m----------");
    }

    public void updateScammerLists() {

        Thread thread = new Thread() {
            public void run() {
                boolean changedOnlineScammerList = false;
                boolean changedLocalScammerList = false;

                for (Scammer scammer : onlineScammerList) {
                    String playerName = UUIDFetcher.getName(UUID.fromString(scammer.uuid));
                    if (!playerName.equalsIgnoreCase(scammer.name)) {
                        scammer.name = playerName;
                        changedOnlineScammerList = true;
                        // getApi().displayMessageInChat("Changed:" + playerName);
                    }
                }
                for (Scammer scammer : localScammerList) {
                    String playerName = UUIDFetcher.getName(UUID.fromString(scammer.uuid));
                    if (!playerName.equalsIgnoreCase(scammer.name)) {
                        scammer.name = playerName;
                        changedLocalScammerList = true;
                        // getApi().displayMessageInChat("Changed:" + playerName);
                    }
                }
                if (changedOnlineScammerList) {

                    saveScammerFile(onlineScammerList, onlineScammerFile);
                }
                if (changedLocalScammerList) {
                    saveScammerFile(localScammerList, localScammerFile);
                }

                scammerList = new ArrayList<String>();
                onlineScammerList = loadScammerFile(onlineScammerList, onlineScammerFile);
                localScammerList = loadScammerFile(localScammerList, localScammerFile);


            }
        };

        thread.start();
    }

    @Override
    public boolean doActionCommandMessage(String unformatted) {

        if (unformatted.toLowerCase().startsWith("/scammer reload")) {
            printPrefixLine();
            getApi().displayMessageInChat(ModColor.WHITE + "Liste wird geladen, bitte warten...");
            Thread thread = new Thread() {
                public void run() {
                    try {
                        scammerList = new ArrayList<String>();
                        onlineScammerList = loadScammerFile(onlineScammerList, onlineScammerFile);
                        localScammerList = loadScammerFile(localScammerList, localScammerFile);

                        getApi().displayMessageInChat(ModColor.WHITE + "Liste wurde neu geladen.");
                        printPrefixLine();

                    } catch (Exception e) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Liste konnte nicht geladen werden.");
                        printPrefixLine();

                        System.err.println(e);
                    }
                }
            };

            thread.start();

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer update")) {
            printPrefixLine();
            getApi().displayMessageInChat(ModColor.WHITE + "Liste wird aktualisiert, bitte warten...");
            printPrefixLine();

            try {

                updateScammerLists();

                printPrefixLine();
                getApi().displayMessageInChat(ModColor.WHITE + "Liste wurde aktualisiert.");
                printPrefixLine();

            } catch (Exception e) {
                printPrefixLine();
                getApi().displayMessageInChat(ModColor.WHITE + "Liste konnte nicht aktualisiert werden.");
                printPrefixLine();
                System.err.println(e);

            }

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer add")) {
            String[] commandArray = unformatted.split(" ");
            printPrefixLine();
            if (commandArray.length > 3) {

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer nur einen Namen angeben (/scammer add NAME)");
                printPrefixLine();
                return true;
            } else if (commandArray.length == 3) {
                final String playerName = commandArray[2].trim();
                if (scammerList.contains(playerName.toLowerCase())) {

                    getApi().displayMessageInChat(ModColor.WHITE + "Dieser Scammer ist bereits hinterlegt!");
                    printPrefixLine();
                    return true;
                } else {
                    final UUID playerUUID = UUIDFetcher.getUUID(playerName);
                    if (playerUUID != null) {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "UUID f\u00FCr " + playerName + " wird ermittelt, bitte warten...");

                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    scammerList.add(playerName.toLowerCase());
                                    localScammerList.add(new Scammer(playerName, playerUUID.toString()));

                                    for (Scammer scammer : localScammerList) {
                                        System.out.println(scammer.name);
                                    }

                                    saveScammerFile(localScammerList, localScammerFile);

                                    getApi().displayMessageInChat(
                                            ModColor.WHITE + playerName + " wurde als Scammer hinterlegt!");
                                    printPrefixLine();
                                } catch (Exception e) {
                                    getApi().displayMessageInChat(
                                            ModColor.WHITE + playerName + " konnte nicht hinterlegt werden.");
                                    printPrefixLine();
                                    System.err.println(e);
                                }
                            }
                        };

                        thread.start();
                        return true;
                    } else {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Der Spielername " + playerName + " konnte nicht gefunden werden!");
                        printPrefixLine();
                        return true;
                    }
                }
            } else {
                getApi().displayMessageInChat(ModColor.WHITE + "Bitte einen Namen angeben (/scammer add NAME)");
                printPrefixLine();
                return true;
            }
        } else if (unformatted.toLowerCase().startsWith("/scammer remove")) {
            String[] commandArray = unformatted.split(" ");
            printPrefixLine();
            if (commandArray.length > 3) {
                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer nur einen Namen angeben (/scammer remove NAME)");
                printPrefixLine();
            } else if (commandArray.length == 3) {
                final String playerName = commandArray[2].trim();
                if (scammerList.contains(playerName.toLowerCase())) {
                    final UUID playerUUID = UUIDFetcher.getUUID(playerName);
                    if (playerUUID != null) {
                        int localListIndex = -1;
                        for (Scammer scammer : localScammerList) {
                            if (scammer.name.equalsIgnoreCase(playerName.toLowerCase())) {
                                localListIndex = localScammerList.indexOf(scammer);
                            }
                        }

                        if (localListIndex < 0) {
                            getApi().displayMessageInChat(ModColor.WHITE + playerName
                                    + " kann nicht gel\u00F6scht werden, da er Online als Scammer hinterlegt wurde!");
                            printPrefixLine();
                        } else {
                            getApi().displayMessageInChat(
                                    ModColor.WHITE + playerName + " wird gel\u00F6scht, bitte warten...");

                            final int scammerListIndex = localListIndex;

                            Thread thread = new Thread() {
                                public void run() {
                                    try {
                                        scammerList.remove(playerName.toLowerCase());
                                        localScammerList.remove(scammerListIndex);

                                        saveScammerFile(localScammerList, localScammerFile);

                                        getApi().displayMessageInChat(
                                                ModColor.WHITE + playerName + " wurde als Scammer entfernt!");
                                        printPrefixLine();
                                    } catch (Exception e) {
                                        getApi().displayMessageInChat(
                                                ModColor.WHITE + playerName + " konnte nicht entfernt werden.");
                                        printPrefixLine();
                                        System.err.println(e);
                                    }
                                }
                            };

                            thread.start();
                        }
                    } else {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Der Spielername " + playerName + " konnte nicht gefunden werden!");
                        printPrefixLine();
                    }
                }
                else {
                    getApi().displayMessageInChat(
                            ModColor.WHITE + "Der Spielername " + playerName + " ist nicht auf der lokalen Liste vorhanden!");
                    printPrefixLine();
                }
            } else {
                getApi().displayMessageInChat(ModColor.WHITE + "Bitte einen Namen angeben (/scammer remove NAME)");
                printPrefixLine();
            }

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer help")) {

            printPrefixLine();
            getApi().displayMessageInChat(ModColor.WHITE + "/scammer help - Befehle ausgeben lassen.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer check - Einen Spielernamen anhand der Listen \u00FCberpr\u00FCfen oder alle auf diesem Server durch * sehen.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer add NAME - Einen Spieler zur lokalen Liste hinzuf\u00FCgen.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer remove NAME - Einen Spieler von der lokalen Liste entfernen.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer reload - Scammerliste aus dem Speicher neu laden.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer update - Namen auf den Listen anhand der UUID updaten.");
            getApi().displayMessageInChat(
                    ModColor.WHITE + "/scammer all - Alle Spielernamen von der lokalen Liste zeigen.");
            printPrefixLine();

            return true;

        } else if (unformatted.toLowerCase().startsWith("/scammer all")) {
            printPrefixLine();

            for (Scammer scammerobj : localScammerList) {
                getApi().displayMessageInChat(ModColor.WHITE + scammerobj.name);
            }

            printPrefixLine();

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer check")) {
            printPrefixLine();
            String[] commandArray = unformatted.split(" ");
            if (commandArray.length > 3 || commandArray.length < 3) {

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer einen Namen angeben (/scammer check NAME oder sehe alle Scammer auf diesem Server über /scammer check *)");
                printPrefixLine();
            } else if (commandArray.length == 3) {
                getApi().displayMessageInChat(ModColor.WHITE + "Der eingegebene Name wird \u00FCberpr\u00FCft. Bitte warten ...");

                final String playerName = commandArray[2].trim();

                if(playerName.equals("*"))
                {
                    List<String> scammerOnServer = getScammerOnServer();
                    if (scammerOnServer.size() > 0) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Folgende Scammer befinden sich auf diesem Server:");
                        for (String scammerName : scammerOnServer) {
                            getApi().displayMessageInChat(ModColor.GOLD + "[" + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER"
                                    + ModColor.GOLD + "]" + ModColor.WHITE + " " + scammerName);

                        }
                        printPrefixLine();
                        return true;
                    }
                    if (scammerOnServer.size() == 0) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Auf diesem Server befinden sich keine Scammer!");
                        printPrefixLine();
                        return true;
                    }

                } else {

                    try {
                        final UUID playerUUID = UUIDFetcher.getUUID(playerName);


                        for (Scammer scammer : onlineScammerList) {
                            if (scammer.uuid.equals(playerUUID.toString())) {
                                if (playerName.equals(scammer.name)) {
                                    scammer.name = playerName;
                                    saveScammerFile(onlineScammerList, onlineScammerFile);

                                    getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                            + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                            + " auf der [SCAMMER]Radar Liste hinterlegt!");
                                    printPrefixLine();

                                    return true;
                                }

                                getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                        + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                        + " auf der [SCAMMER]Radar hinterlegt! Sein vorheriger Name war: "
                                        + scammer.name + ". Der Spielername wurde geupdated.");
                                printPrefixLine();

                                return true;
                            }
                        }

                        for (Scammer scammer : localScammerList) {
                            if (scammer.uuid.equals(playerUUID.toString())) {
                                if (playerName.equals(scammer.name)) {
                                    String oldName = scammer.name;
                                    scammer.name = playerName;
                                    saveScammerFile(localScammerList, localScammerFile);

                                    getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                            + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                            + " auf deiner LOKALEN Liste hinterlegt!");
                                    printPrefixLine();

                                    return true;
                                }

                                getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                        + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                        + " auf deiner LOKALEN Liste hinterlegt! Sein vorheriger Name war: "
                                        + scammer.name + ". Der Spielername wurde geupdated.");
                                printPrefixLine();

                                return true;
                            }
                        }


                    } catch (Exception ex) {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Dieser Spielername " + playerName + " exisitiert nicht bei Mojang.");
                        printPrefixLine();
                        ex.printStackTrace();
                        return true;
                    }
                }

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Die UUID von " + playerName + " ist nicht auf den Listen hinterlegt!");
                printPrefixLine();
                return true;
            }

            return true;
        }

        if (unformatted.startsWith("/assets/minecraft/griefergames/scammer"))
            return true;

        return false;
    }

    @Override
    public boolean commandMessage(String unformatted) {
        return true;
    }
}

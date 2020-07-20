package de.newH1VE.griefergames;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.newH1VE.griefergames.chat.AntiScammer;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.MessageReceiveEvent;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.settings.elements.BooleanElement;
import net.labymod.settings.elements.ControlElement;
import net.labymod.settings.elements.SettingsElement;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.labymod.utils.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class GrieferGames extends LabyModAddon {
    private static GrieferGames griefergames;
    private String serverIp = "";
    private String secondServerIp = "";
    private boolean modenabled = true;
    private boolean prefixenabled = true;
    private boolean messageenabled = false;
    private boolean doAntiScammer = false;
    private AntiScammer antiscammer = new AntiScammer();
    private static Pattern msgStartsWithTime = Pattern.compile("^\\[(\\d{2}\\:){2}\\d{2}\\][^$]*$");

    public boolean isMessageEnabled() {
        return messageenabled;
    }

    public boolean isPrefixEnabled() {
        return prefixenabled;
    }

    public void setPrefixEnabled(boolean prefixenabled) {
        this.prefixenabled = prefixenabled;
    }

    private boolean isModEnabled() {
        return modenabled;
    }

    private void setModEnabled(boolean modenabled) {
        this.modenabled = modenabled;
    }

    private void setMessageEnabled(boolean messenabled) {
        this.messageenabled = messenabled;
    }

    public static GrieferGames getGrieferGames() {
        return griefergames;
    }

    private static void setGrieferGames(GrieferGames griefergames) {
        GrieferGames.griefergames = griefergames;
    }

    private String getServerIp() {
        return serverIp;
    }

    private void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    private String getSecondServerIp() {
        return secondServerIp;
    }

    private void setSecondServerIp(String secondServerIp) {
        this.secondServerIp = secondServerIp;
    }

    private boolean getDoAntiScammer() {
        return doAntiScammer;
    }

    private void setDoAntiScammer(boolean doAntiScammer) {
        this.doAntiScammer = doAntiScammer;
    }

    @Override
    public void onEnable() {
        System.out.println("[GrieferGames AntiScammer] enabled.");
        // set ip
        setServerIp("griefergames.net");
        setSecondServerIp("griefergames.de");

        // save instance
        setGrieferGames(this);

        getApi().getEventManager().registerOnJoin(new Consumer<ServerData>() {
            @Override
            public void accept(ServerData serverData) {
                boolean doAntiScammer = (serverData.getIp().toLowerCase().indexOf(getServerIp()) >= 0
                        || serverData.getIp().toLowerCase().indexOf(getSecondServerIp()) >= 0);
                setDoAntiScammer(doAntiScammer);
            }
        });

        this.getApi().getEventManager().register(new MessageModifyChatEvent() {
            public Object onModifyChatMessage(Object o) {
                if (isModEnabled())
                    return modifyChatMessage(o);

                return o;
            }

        });

        getApi().getEventManager().register(new MessageSendEvent() {
            public boolean onSend(String message) {

                if (!(isModEnabled() && getDoAntiScammer()))
                    return false;

                if (antiscammer.doActionCommandMessage(message)) {
                    return antiscammer.commandMessage(message);
                }

                return false;
            }
        });

        getApi().getEventManager().register(new MessageReceiveEvent() {
            public boolean onReceive(String formatted, String unformatted) {
                if (!(isModEnabled() && getDoAntiScammer()))
                    return false;

                if (antiscammer.doActionReceiveMessage(formatted, unformatted)) {
                    return antiscammer.receiveMessage(formatted, unformatted);
                }

                return false;
            }
        });

    }

    public Object modifyChatMessage(Object o) {
        if (!(isModEnabled() && getDoAntiScammer()))
            return o;

        try {
            IChatComponent msg = (IChatComponent) o;

            IChatComponent time = new ChatComponentText("");

            Matcher matcher = msgStartsWithTime.matcher(msg.func_150260_c());

            if (matcher.find()) {
                for (int i = 0; i < msg.func_150253_a().size() - 1; i++) {
                    time.func_150257_a(msg.func_150253_a().get(i));
                }

                msg = msg.func_150253_a().get(msg.func_150253_a().size() - 1);
            }

            if (antiscammer.doActionModifyChatMessage(msg)) {
                msg = antiscammer.modifyChatMessage(msg);
                if (time.func_150260_c().trim().length() > 0) {
                    msg = time.func_150257_a(msg);
                }
            }

            return msg;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public void onDisable() {
        System.out.println("[GrieferGames AntiScammer] disabled.");
    }

    @Override
    public void loadConfig() {
        if (getConfig().has("enabled"))
            setModEnabled(getConfig().get("enabled").getAsBoolean());

        if (getConfig().has("prefix enabled"))
            setPrefixEnabled(getConfig().get("prefix enabled").getAsBoolean());

        if (getConfig().has("message enabled"))
            setMessageEnabled(getConfig().get("message enabled").getAsBoolean());
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        final BooleanElement modEnabledBtn = new BooleanElement("Schutz aktivieren",
                new ControlElement.IconData(Material.DIAMOND_CHESTPLATE), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean modEnabled) {
                setModEnabled(modEnabled);
                getConfig().addProperty("enabled", modEnabled);
                saveConfig();
            }
        }, isModEnabled());
        list.add(modEnabledBtn);

        final BooleanElement modPrefixEnabled = new BooleanElement("Scammer Warnung \u00FCber Prefix",
                new ControlElement.IconData(Material.THIN_GLASS), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean prefEnabled) {
                setPrefixEnabled(prefEnabled);
                getConfig().addProperty("prefix enabled", prefEnabled);
                saveConfig();
            }
        }, isPrefixEnabled());
        list.add(modPrefixEnabled);

        final BooleanElement modMessageEnabled = new BooleanElement("Scammer Warnung \u00FCber separate Nachricht",
                new ControlElement.IconData(Material.PAPER), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean messEnabled) {
                setMessageEnabled(messEnabled);
                getConfig().addProperty("message enabled", messEnabled);
                saveConfig();
            }
        }, isMessageEnabled());
        list.add(modMessageEnabled);




    }
}

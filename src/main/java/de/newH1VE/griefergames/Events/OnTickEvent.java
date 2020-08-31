package de.newH1VE.griefergames.Events;

import de.newH1VE.griefergames.GrieferGames;
import net.labymod.core.LabyModCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class OnTickEvent {

    private ReentrantLock lock = new ReentrantLock();


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() && GrieferGames.getGrieferGames().isModEnabled() && GrieferGames.getGrieferGames().isTabListEnabled()
                && !Minecraft.getMinecraft().isSingleplayer()) {

            lock.lock();
            try {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        modifyTabList();

                    }
                });

                thread.start();
            } finally {
                Thread thread = new Thread() {
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lock.unlock();
                    }
                };

                thread.start();
            }

        }

    }

    public void modifyTabList() {

        List<String> scammerList = GrieferGames.getAntiscammer().getScammerList();
        EnumChatFormatting chatformat = GrieferGames.getGrieferGames().getChatformat();
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


                            for (String scammer : scammerList) {
                                if (playerName.equalsIgnoreCase(scammer)) {
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

}


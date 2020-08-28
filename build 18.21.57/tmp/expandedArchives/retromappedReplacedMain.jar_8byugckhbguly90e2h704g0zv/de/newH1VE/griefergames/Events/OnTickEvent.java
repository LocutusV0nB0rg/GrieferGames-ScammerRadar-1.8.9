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
import java.util.concurrent.locks.Lock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class OnTickEvent {

    private ReentrantLock lock = new ReentrantLock();


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.func_71410_x().field_71474_y.field_74321_H.func_151470_d() && GrieferGames.getGrieferGames().isModEnabled() && GrieferGames.getGrieferGames().isTabListEnabled()
                && !Minecraft.func_71410_x().func_71356_B()) {

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
                lock.unlock();
            }

        }

    }

    public void modifyTabList() {

        List<String> scammerList = GrieferGames.getAntiscammer().getScammerList();
        EnumChatFormatting chatformat = GrieferGames.getGrieferGames().getChatformat();
        NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().field_71174_a;
        Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.func_175106_d();

                try {
                    for (NetworkPlayerInfo player : playerMap) {
                        boolean found = false;
                        if (player.func_178854_k() != null) {
                            IChatComponent playerDisplayName = player.func_178854_k();

                            String oldMessage = playerDisplayName.func_150254_d().replaceAll("\u00A7", "§");
                            if (oldMessage.indexOf("§k") != -1) {
                                oldMessage = oldMessage.replaceAll("§k", "");
                                oldMessage = oldMessage.replaceAll("§", "\u00A7");
                                playerDisplayName = new ChatComponentText(oldMessage);
                            }

                            if (playerDisplayName.func_150260_c().startsWith("[AMP]")) {
                                playerDisplayName = playerDisplayName.func_150253_a().get(playerDisplayName.func_150253_a().size() - 1);
                            }

                            String fullItem = playerDisplayName.func_150260_c();
                            String playerName = player.func_178845_a().getName();
                            IChatComponent newPlayerDisplayName = playerDisplayName;


                            for (String scammer : scammerList) {
                                if (playerName.equalsIgnoreCase(scammer)) {
                                    found = true;
                                    if (!fullItem.startsWith("[SCAMMER]")) {
                                        IChatComponent befsign = new ChatComponentText("[").func_150255_a(new ChatStyle().func_150238_a(EnumChatFormatting.YELLOW));
                                        IChatComponent afsign = new ChatComponentText("] ").func_150255_a(new ChatStyle().func_150238_a(EnumChatFormatting.YELLOW));
                                        IChatComponent sign = new ChatComponentText("SCAMMER").func_150255_a(new ChatStyle().func_150238_a(chatformat).func_150227_a(true));
                                        newPlayerDisplayName = befsign.func_150257_a(sign).func_150257_a(afsign).func_150257_a(playerDisplayName);
                                    }
                                }
                            }

                            if (fullItem.startsWith("[SCAMMER]") && found == false) {
                                newPlayerDisplayName = playerDisplayName.func_150253_a().get(playerDisplayName.func_150253_a().size() - 1);
                            }
                            player.func_178859_a(newPlayerDisplayName);
                        }
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }


    }

}


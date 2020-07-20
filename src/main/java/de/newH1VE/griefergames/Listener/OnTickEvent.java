package de.newH1VE.griefergames.Listener;

import de.newH1VE.griefergames.GrieferGames;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class OnTickEvent {

    @SubscribeEvent
    public void onTick( TickEvent.ClientTickEvent event ) {
        if (Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() && GrieferGames.getGrieferGames().isModEnabled() && GrieferGames.getGrieferGames().isTabListEnabled() && !Minecraft.getMinecraft().isSingleplayer()) {
            GrieferGames.getAntiscammer().modifyTabList();
        }

    }

}


package de.newH1VE.griefergames.server;

import java.util.List;

import de.newH1VE.griefergames.GrieferGames;
import de.newH1VE.griefergames.chat.AntiScammer;
import de.newH1VE.griefergames.chat.Chat;
import net.labymod.api.LabyModAPI;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.MessageReceiveEvent;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.api.events.TabListEvent;
import net.labymod.servermanager.ChatDisplayAction;
import net.labymod.servermanager.Server;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static java.lang.Thread.sleep;

public class GrieferGamesServer extends Server {
	private Minecraft mc;
	private LabyModAPI api;
	private String subServer = "";
	public AntiScammer antiscammer = new AntiScammer();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private boolean enabled = true;

	protected GrieferGames getGG() {
		return GrieferGames.getGriefergames();
	}

	public Minecraft getMc() {
		return mc;
	}

	public void setMc(Minecraft mc) {
		this.mc = mc;
	}

	public LabyModAPI getApi() {
		return api;
	}

	private void setApi(LabyModAPI api) {
		this.api = api;
	}

	public GrieferGamesServer(Minecraft minecraft) {
		super("GrieferGames", GrieferGames.getGriefergames().getServerIp(),
				GrieferGames.getGriefergames().getSecondServerIp());

		setMc(minecraft);
		setApi(getGG().getApi());
	}

	public String getSubServer() {
		return subServer;
	}

	public void setSubServer(String subServer) {
		this.subServer = subServer.trim();
	}

	@Override
	public void fillSubSettings(List<SettingsElement> settings) {

	}

	@Override
	public void handlePluginMessage(String channelName, PacketBuffer packetBuffer) throws Exception {

	}

	@Override
	public void handleTabInfoMessage(TabListEvent.Type tabInfoType, String formatted, String clean) throws Exception {

	}

	@Override
	public void onJoin(ServerData serverData) {
		setSubServer("");

			this.getApi().getEventManager().register(new MessageModifyChatEvent() {

				@SubscribeEvent(priority = EventPriority.LOWEST)
				public Object onModifyChatMessage(Object o) {
					if(isEnabled())
					return modifyChatMessage(o);

					return o;
				}

			});



			getApi().getEventManager().register(new MessageSendEvent() {
				public boolean onSend(String message) {

					System.out.println("Is Enabled: " +  isEnabled() + "##############################");

					if (!isEnabled())
						return false;



						if (antiscammer.doActionCommandMessage(message)) {
							return antiscammer.commandMessage(message);
						}


					return false;
				}
			});

			getApi().getEventManager().register(new MessageReceiveEvent() {
				@Override
				public boolean onReceive(String formatted, String unformatted) {
					if (!isEnabled())
						return false;



						if (antiscammer.doActionReceiveMessage(formatted, unformatted)) {
							return antiscammer.receiveMessage(formatted, unformatted);
						}


					return false;
				}
			});

		}

	@Override
	public ChatDisplayAction handleChatMessage(String s, String s1) throws Exception {
		return null;
	}

	public Object modifyChatMessage(Object o) {
		if (!isEnabled())
			return o;

		try {
			IChatComponent msg = (IChatComponent) o;

			//List<Chat> chatModules = getGG().getChatModules();
			//for (Chat chatModule : chatModules) {

				if (antiscammer.doActionModifyChatMessage(msg)) {
					msg = antiscammer.modifyChatMessage(msg);
				}
			//}

			return msg;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
}
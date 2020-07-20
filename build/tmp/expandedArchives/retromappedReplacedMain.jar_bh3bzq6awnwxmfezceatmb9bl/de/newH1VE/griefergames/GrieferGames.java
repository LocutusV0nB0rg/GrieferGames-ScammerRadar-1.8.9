package de.newH1VE.griefergames;

import java.util.List;

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
import net.minecraft.util.IChatComponent;

public class GrieferGames extends LabyModAddon {
	private static GrieferGames griefergames;
	private String serverIp = "";
	private String secondServerIp = "";
	private boolean enabled = true;
	private boolean doAntiScammer = true;
	private AntiScammer antiscammer = new AntiScammer();

	private boolean isEnabled() {
		return enabled;
	}

	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
				if (isEnabled())
					return modifyChatMessage(o);

				return o;
			}

		});

		getApi().getEventManager().register(new MessageSendEvent() {
			public boolean onSend(String message) {

				if (!(isEnabled() && getDoAntiScammer()))
					return false;

				if (antiscammer.doActionCommandMessage(message)) {
					return antiscammer.commandMessage(message);
				}

				return false;
			}
		});

		getApi().getEventManager().register(new MessageReceiveEvent() {
			public boolean onReceive(String formatted, String unformatted) {
				if (!(isEnabled() && getDoAntiScammer()))
					return false;

				if (antiscammer.doActionReceiveMessage(formatted, unformatted)) {
					return antiscammer.receiveMessage(formatted, unformatted);
				}

				return false;
			}
		});

	}

	public Object modifyChatMessage(Object o) {
		if (!(isEnabled() && getDoAntiScammer()))
			return o;

		try {
			IChatComponent msg = (IChatComponent) o;

			if (antiscammer.doActionModifyChatMessage(msg)) {
				msg = antiscammer.modifyChatMessage(msg);
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
			setEnabled(getConfig().get("enabled").getAsBoolean());
	}

	@Override
	protected void fillSettings(List<SettingsElement> list) {
		final BooleanElement modEnabledBtn = new BooleanElement("Addon Enabled",
				new ControlElement.IconData(Material.LEVER), new Consumer<Boolean>() {
					@Override
					public void accept(Boolean modEnabled) {
						setEnabled(modEnabled);
						getConfig().addProperty("enabled", modEnabled);
						saveConfig();
					}
				}, isEnabled());
		list.add(modEnabledBtn);
	}
}

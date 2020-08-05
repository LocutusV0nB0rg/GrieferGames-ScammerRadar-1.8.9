package de.newH1VE.griefergames.chat;

import de.newH1VE.griefergames.GrieferGames;
import de.newH1VE.griefergames.helper.Helper;
import net.labymod.api.LabyModAPI;
import net.labymod.servermanager.ChatDisplayAction;
import net.minecraft.util.IChatComponent;

public class Chat {
	protected GrieferGames getGG() {
		return GrieferGames.getGrieferGames();
	}

	protected Helper getHelper() {
		return GrieferGames.getGrieferGames().getHelper();
	}

	protected LabyModAPI getApi() {
		return getGG().getApi();
	}

	public String getName() {
		return "chat";
	}

	public boolean doAction(String unformatted, String formatted) {
		return false;
	}

	public boolean doActionHandleChatMessage(String unformatted, String formatted) {
		return false;
	}

	public boolean doActionModifyChatMessage(IChatComponent msg) {
		return false;
	}

	public boolean doActionCommandMessage(String unformatted) {
		return false;
	}

	public ChatDisplayAction handleChatMessage(String unformatted, String formatted) {
		return ChatDisplayAction.NORMAL;
	}

	public IChatComponent modifyChatMessage(IChatComponent msg) {
		return msg;
	}

	public boolean commandMessage(String unformatted) {
		return false;
	}

	public boolean doActionReceiveMessage(String formatted, String unformatted) {
		return false;
	}

	public boolean receiveMessage(String formatted, String unformatted) {
		return false;
	}
}

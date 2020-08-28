package de.newH1VE.griefergames.antiScammer;


/** Represents an scammer.

 */
public class Scammer {
	public String name;
	public String uuid;


	/** creates an scammer
	 *
	 * @param name contains the players name
	 * @param uuid contains the players uuid
	 */
	public Scammer(String name, String uuid) {
		this.name = name;
		this.uuid = uuid;
	}
}
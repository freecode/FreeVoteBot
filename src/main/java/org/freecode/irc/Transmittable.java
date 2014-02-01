package org.freecode.irc;

/**
 * User: Shivam
 * Date: 28/07/13
 * Time: 21:40
 */
public abstract class Transmittable {
	public abstract String getRaw();
	public abstract String getCommand();
    public boolean isNotice(){
        return this instanceof Notice;
    }

    public boolean isPrivateMsg() {
        return this instanceof PrivateMsg;
    }

    public PrivateMsg asPrivateMsg() {
        return isPrivateMsg() ? (PrivateMsg) this : null;
    }

    public Notice asNotice() {
        return isNotice() ? (Notice) this : null;
    }

}

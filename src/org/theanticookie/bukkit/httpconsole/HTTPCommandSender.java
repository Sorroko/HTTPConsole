package org.theanticookie.bukkit.httpconsole;

import org.bukkit.craftbukkit.v1_8_R3.command.CraftConsoleCommandSender;



public class HTTPCommandSender extends CraftConsoleCommandSender {
	
	public StringBuilder sb = new StringBuilder();
	
	@Override
	public void sendMessage(String message){
		super.sendMessage(message);
		sb.append(message);
	}
	
	@Override
	public void sendMessage(String[] arg0){
		super.sendMessage(arg0);
		sb.append(arg0);
	}

	public String getOutput() {
		return sb.toString();
	}
	
}

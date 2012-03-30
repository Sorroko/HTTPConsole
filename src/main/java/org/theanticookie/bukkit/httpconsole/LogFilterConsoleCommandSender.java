package org.theanticookie.bukkit.httpconsole;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * Filters out console messages prefixed with "ConsoleCommandSender:"
 * @author BlueJeansAndRain
 */
public class LogFilterConsoleCommandSender implements Filter
{
	public boolean isLoggable( LogRecord logRecord )
	{
		String message = logRecord.getMessage();
		if ( message.startsWith( "ConsoleCommandSender:" ) )
			return false;
		
		return true;
	}
}

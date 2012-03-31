package org.theanticookie.bukkit.httpconsole;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

/**
 * Filters out log messages based on the configuration log level.
 * @author BlueJeansAndRain
 */
public class LogFilterLevel implements Filter
{
	private int level = 0;

	public LogFilterLevel( String level )
	{
		try
		{
			this.level = Level.parse( level.toUpperCase() ).intValue();
		}
		catch ( Exception e )
		{
			this.level = Level.SEVERE.intValue();
		}
	}

	public boolean isLoggable( LogRecord logRecord )
	{
		if ( logRecord.getLevel().intValue() >= level )
			return true;

		return false;
	}
}

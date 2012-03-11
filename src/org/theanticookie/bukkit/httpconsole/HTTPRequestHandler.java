package org.theanticookie.bukkit.httpconsole;

import org.theanticookie.bukkit.HTTPConsole;
import java.io.StringWriter;
import java.util.logging.Level;

/**
 * Interface for handling HTTPRequest objects.
 * 
 * @author BlueJeansAndRain
 */
public abstract class HTTPRequestHandler
{
	protected void log( String message, Object ... params )
	{
		HTTPConsole.log( Level.INFO, message, params );
	}
	
	public abstract boolean HandlePath( String path );
	public abstract boolean HandleRequest( HTTPRequest request, StringWriter output );
}

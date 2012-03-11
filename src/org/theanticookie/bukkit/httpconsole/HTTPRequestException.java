package org.theanticookie.bukkit.httpconsole;

/**
 * For badly formed or otherwise unacceptable HTTP requests.
 * @author BlueJeansAndRain
 */
public class HTTPRequestException extends Exception
{
	public HTTPRequestException( String message )
	{
		super( message );
	}
}

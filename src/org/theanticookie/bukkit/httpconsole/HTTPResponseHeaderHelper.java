package org.theanticookie.bukkit.httpconsole;

import org.theanticookie.bukkit.HTTPConsole;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Generator of standard HTTP response headers.
 *
 * @author BlueJeansAndRain
 */
public class HTTPResponseHeaderHelper
{
	private final static String RESPONSE_200 = "HTTP/1.1 200 OK";
	private final static String RESPONSE_204 = "HTTP/1.1 204 No Content";
	private final static String RESPONSE_400 = "HTTP/1.1 400 Bad Request";
	private final static String RESPONSE_403 = "HTTP/1.1 403 Forbidden";
	private final static String RESPONSE_404 = "HTTP/1.1 404 Not Found";
	private final static String RESPONSE_500 = "HTTP/1.1 500 Internal Server Error";
	private final static String RESPONSE_501 = "HTTP/1.1 501 Not Implemented";

	public static String createHeaders( int response_code )
	{
		String header = null;
		switch ( response_code )
		{
			case 200:
				header = RESPONSE_200;
				break;
			case 204:
				header = RESPONSE_204;
				break;
			case 400:
				header = RESPONSE_400;
				break;
			case 403:
				header = RESPONSE_403;
				break;
			case 404:
				header = RESPONSE_404;
				break;
			case 500:
				header = RESPONSE_500;
				break;
			case 501:
				header = RESPONSE_501;
				break;
			default:
				header = String.format( "HTTP/1.0 %d", response_code );
		}

		header = String.format( "%s\r\nConnection: close\r\nServer: Bukkit Minecraft %s %s\r\nContent-Type: text/html\r\nExpires: Sat, 01 Jan 2000 00:00:00 GMT\r\nCache-Control: no-cache, no-store\r\n\r\n", header, HTTPConsole.getPackageName(), HTTPConsole.getVersion() );
		return header;
	}

	public static void outputHeaders( int response_code, DataOutputStream output )
			throws IOException
	{
		 output.writeBytes( createHeaders( response_code ) );
	}

	public static void outputHeaders( int response_code, StringWriter output )
	{
		output.write( createHeaders( response_code ) );
	}
}

package org.theanticookie.bukkit.httpconsole;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.net.InetAddress;
import java.net.URLDecoder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Parse HTTP request data into an easily referenced object.
 * 
 * @author BlueJeansAndRain
 */
public class HTTPRequest
{
	public InetAddress address = null;
	public String method = null;
	public String path = null;
	public String query = null;
	public final HashMap<String, String> parameters = new HashMap<String, String>();
	public final HashMap<String, String> headers = new HashMap<String, String>();
	public Object data = null;

	public HTTPRequest( InputStream input, InetAddress address )
			throws IOException, HTTPRequestException
	{
		this.address = address;

		BufferedReader reader = new BufferedReader( new InputStreamReader( input ) );

		// Read the first line. Should be the HTTP request.
		String line = reader.readLine();

		String[] request_parts = line.split( "\\s+" );
		if ( request_parts.length < 3 )
			throw new HTTPRequestException( "Malformed HTTP request" );

		method = request_parts[0].toUpperCase();

		String[] path_parts = request_parts[1].split( "\\?", 2 );
		path = path_parts[0];

		while ( true )
		{
			line = reader.readLine();
			if ( line == null )
				throw new HTTPRequestException( "Stream ended unexpectedly" );
			else if ( line.equals( "" ) )
				break; // End of headers

			String[] header_parts = line.split( ":", 2 );

			String key = header_parts[0].trim().toUpperCase();
			if ( key.equals( "" ) )
				continue;

			headers.put( key, header_parts.length > 1 ? header_parts[1].trim() : "" );
		}

		String content_type = "";
		if ( method.equals( "GET" ) )
		{
			query = path_parts.length > 1 ? path_parts[1] : "";
			content_type = "application/x-www-form-urlencoded";
		}
		else if ( method.equals( "POST" ) )
		{
			int content_length = 0;
			if ( headers.containsKey( "CONTENT-LENGTH" ) )
			{
				content_length = Integer.parseInt( headers.get( "CONTENT-LENGTH" ) );
				if ( content_length <= 0 )
					throw new HTTPRequestException( "Content-length header is <= 0" );
			}
			else
			{
				throw new HTTPRequestException( "POST request did not contain a content-length header" );
			}
			
			char[] buffer = new char[content_length];
			if ( reader.read( buffer, 0, content_length ) != content_length )
				throw new HTTPRequestException( "Stream ended unexpectedly" );

			query = new String( buffer, 0, content_length );
			if ( headers.containsKey( "CONTENT-TYPE" ) )
				content_type = headers.get( "CONTENT-TYPE" ).toLowerCase();
		}

		if ( query == null )
		{
			query = "";
		}
		else if ( content_type.equals( "application/x-www-form-urlencoded" ) )
		{
			String[] params = query.split( "&" );
			for ( String param : params )
			{
				String[] param_parts = param.split( "=" );

				String key = URLDecoder.decode( param_parts[0].trim(), "UTF-8" );
				if ( key.equals( "" ) )
					continue;

				parameters.put( key, param_parts.length > 1 ? URLDecoder.decode( param_parts[1], "UTF-8" ) : "" );
			}
		}
		else if ( content_type.equals( "application/json" ) )
		{
			JSONParser json = new JSONParser();
			try
			{
				data = json.parse( query );
			}
			catch ( ParseException e )
			{
				throw new HTTPRequestException( "JSON parse error" );
			}
		}
	}
}

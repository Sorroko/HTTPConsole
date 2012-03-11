package org.theanticookie.bukkit.httpconsole;

import org.theanticookie.bukkit.HTTPConsole;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.StringWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Provides the framework for handling HTTP requests.
 *
 * @author BlueJeansAndRain
 */
public class HTTPServer extends Thread
{
	private ServerSocket socket = null;
	private final LinkedList<HTTPRequestHandler> request_handlers = new LinkedList<HTTPRequestHandler>();
	private final InetAddressFilter whitelist = new InetAddressFilter();
	private final InetAddressFilter blacklist = new InetAddressFilter();
	private final HashMap<String, Pattern> allowed_hosts = new HashMap<String, Pattern>();
	private volatile boolean stopping = false;

	private boolean deny_before_allow = true;
	private boolean always_log_refused_connections = false;

	/*public HTTPServer()
	{

	}*/

	private void requestHandler( Socket connection )
	{
		InetAddress inet_address = connection.getInetAddress();
		String address = inet_address.getHostAddress();
		HTTPConsole.log( Level.INFO, "Request recieved from %s", address );

		boolean ip_allowed = false;
		if ( deny_before_allow )
			ip_allowed = !blacklist.containsAddress( inet_address ) || whitelist.containsAddress( inet_address );
		else
			ip_allowed = whitelist.containsAddress( inet_address ) && !blacklist.containsAddress( inet_address );

		if ( !ip_allowed )
		{
			try {
				connection.close();
			} catch ( IOException e ) {} // Silently fail.

			if ( always_log_refused_connections )
				HTTPConsole.log( "Connection from %s refused (black/white list)", address );
			else
				HTTPConsole.log( Level.INFO, "Connection from %s refused (black/white list)", address );

			return;
		}
		
		DataOutputStream output = null;
		try
		{
			output = new DataOutputStream( connection.getOutputStream() );
		}
		catch ( IOException e )
		{
			HTTPConsole.logException( e, "Error creating writer for HTTP request output stream" );
			return;
		}

		try
		{
			HTTPRequest request = null;
			try
			{
				request = new HTTPRequest( connection.getInputStream(), inet_address );
			}
			catch ( IOException e )
			{
				HTTPConsole.logException( e, "Error reading HTTP request input stream" );
				return;
			}
			catch ( HTTPRequestException e )
			{
				HTTPConsole.log( Level.INFO, e.getMessage() );
				try
				{
					HTTPResponseHeaderHelper.outputHeaders( 400, output );
				}
				catch ( IOException ex )
				{
					HTTPConsole.logException( ex, "Error writing HTTP request output stream" );
				}

				return;
			}

			String host = request.headers.containsKey( "HOST" ) ? request.headers.get( "HOST" ).split( ":", 2 )[0] : "";
			if ( !allowed_hosts.isEmpty() && !host.equals( "127.0.0.1" ) )
			{
				boolean host_allowed = false;
				for ( String key : allowed_hosts.keySet() )
				{
					if ( allowed_hosts.get( key ).matcher( host ).matches() )
					{
						host_allowed = true;
						break;
					}
				}

				if ( !host_allowed )
				{
					try {
						connection.close();
					} catch ( IOException e ) {} // Silently fail.

					if ( always_log_refused_connections )
						HTTPConsole.log( "Connection from %s refused (invalid host: %s)", address, host );
					else
						HTTPConsole.log( Level.INFO, "Connection from %s refused (invalid host: %s)", address, host );

					return;
				}
			}

			boolean found = false;
			try
			{
				for ( HTTPRequestHandler handler : request_handlers )
				{
					if ( handler.HandlePath( request.path ) )
					{
						found = true;
						StringWriter safe_output = new StringWriter();
						if ( handler.HandleRequest( request, safe_output ) )
						{
							HTTPConsole.log( Level.INFO, "Request handled" );
							output.writeBytes( safe_output.toString() );
							return;
						}
					}
				}

				if ( !found )
				{
					HTTPResponseHeaderHelper.outputHeaders( 404, output );
					HTTPConsole.log( Level.INFO, "No matching request handler found for path \"%s\"", request.path );
				}
				else
				{
					HTTPResponseHeaderHelper.outputHeaders( 204, output );
					HTTPConsole.log( Level.INFO, "Matching request handler found but the request was left unhandled by the handler" );
				}
			}
			catch ( IOException e )
			{
				HTTPConsole.logException( e, "Error writing HTTP request output stream" );
				return;
			}
			catch ( Exception e )
			{
				HTTPConsole.logException( e, "An HTTP request handler failed" );
				try
				{
					HTTPResponseHeaderHelper.outputHeaders( 500, output );
				}
				catch ( IOException ex )
				{
					HTTPConsole.logException( ex, "Error writing HTTP request output stream" );
				}

				return;
			}
		}
		finally
		{
			try
			{
				output.flush();
				output.close();
			}
			catch ( IOException e )
			{
				// Silently fail.
			}
		}
	}

	public void setDenyBeforeAllow( boolean value )
	{
		deny_before_allow = value;
	}

	public void setAlwaysLogRefusedConnections( boolean value )
	{
		always_log_refused_connections = value;
	}

	public void addRequestHandler( HTTPRequestHandler handler )
	{
		request_handlers.add( handler );
	}

	public void removeRequestHandler( HTTPRequestHandler handler )
	{
		request_handlers.remove( handler );
	}

	public void addToWhitelist( String address )
			throws UnknownHostException
	{
		whitelist.add( address );
	}

	public void removeFromWhitelist( String address )
			throws UnknownHostException
	{
		//whitelist.remove( address );
		throw new UnsupportedOperationException( "removeFromWhiteList is not implemented." );
	}

	public void clearWhitelist()
	{
		whitelist.clear();
	}

	public void addToBlacklist( String address )
			throws UnknownHostException
	{
		blacklist.add( address );
	}

	public void removeFromBlacklist( String address )
			throws UnknownHostException
	{
		//blacklist.remove( address );
		throw new UnsupportedOperationException( "removeFromBlacklist is not implemented." );
	}

	public void clearBlacklist()
	{
		blacklist.clear();
	}

	public void addAllowedHost( String host )
	{
		if ( !host.matches( "(?i)[*a-z0-9-]+\\.([a-z0-9-]+\\.)*[a-z]{2,4}" ) )
			throw new IllegalArgumentException( "Not a valid hostname matching string." );

		host = host.replace( ".", "\\." ).replace( "*", "[^.]" );
		allowed_hosts.put( host, Pattern.compile( host ) );
	}

	public void removeAllowedHost( String host )
	{
		allowed_hosts.remove( host );
	}

	public void clearAllowedHosts()
	{
		allowed_hosts.clear();
	}

	public void start( String address, int port )
	{
		try
		{
			InetAddress listener_address = null;
			if ( !address.equalsIgnoreCase( "any" ) )
				listener_address = InetAddress.getByName( address );
			else
				address = "*"; // Just for display purposes below.
			
			socket = new ServerSocket( port, 0, listener_address );
		}
		catch ( Exception e )
		{
			HTTPConsole.logException( e, "Failed to create socket on %s:%d", address, port );
			return;
		}

		HTTPConsole.log( "Listening for connections on %s:%d", address, port );
		super.start();
	}

	@Override
	public void start()
	{
		start( "any", 80 );
	}

	public void stopServer()
	{
		stopping = true;
		try
		{
			socket.close();
		}
		catch ( IOException e )
		{
			HTTPConsole.logException( e, "Error while closing HTTP socket" );
			return;
		}

		try
		{
			this.join( 100 );
		}
		catch ( InterruptedException e )
		{
			HTTPConsole.log( Level.WARNING, "Interrupted wait for HTTP listener stop" );
		}

		if ( this.isAlive() )
			HTTPConsole.log( Level.WARNING, "HTTP listener thread didn't stop in a timely manner" );
		else
			HTTPConsole.log( Level.INFO, "HTTP listener thread stopped" );
	}

	@Override
	public void run()
	{
		while ( !socket.isClosed() )
		{
			try
			{
				Socket connection = socket.accept();

				try
				{
					requestHandler( connection );
				}
				finally
				{
					try
					{
						if ( !connection.isClosed() )
							connection.close();
					}
					catch ( IOException e )
					{
						HTTPConsole.log( Level.WARNING, "The connection was prematurely closed" );
					}
				}
			}
			catch ( Exception e )
			{
				if ( e instanceof java.io.IOException && stopping )
					return;
				
				HTTPConsole.logException( e, "Error while handling an HTTP request." );
			}
		}
	}
}

package org.theanticookie.bukkit.httpconsole;

import org.bukkit.Bukkit;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.json.simple.JSONObject;

/**
 * Handles path /console POST/GET requests by executing the requested console
 * command on the minecraft console.
 *
 * @author BlueJeansAndRain
 */
public class RequestHandler extends HTTPRequestHandler
{

    public RequestHandler(  )
    {
    }

    private String executeConsoleCommand( String command )
    {
        log( "Executing \"%s\"", command );
        StringWriter command_output = new StringWriter();
        HTTPCommandSender sender = new HTTPCommandSender();
        
        Bukkit.getServer().dispatchCommand(sender, command);

        return sender.getOutput().replaceAll("\\[m", "").replaceAll("(\\[[0-9][0-9]?;[0-9][0-9]?m)", "");
    }

    public boolean HandlePath( String path )
    {
        if ( path.equalsIgnoreCase( "/console" ) || path.equalsIgnoreCase( "/" ) )
            return true;

        return false;
    }

    public boolean HandleRequest( HTTPRequest request, StringWriter out )
    {
        if ( !request.method.equals( "GET" ) && !request.method.equals( "POST" ) )
            return false;

        String command = "";
        if ( request.parameters.containsKey( "command" ) )
        {
            command = request.parameters.get( "command" ).trim();
        }
        else if (  request.data instanceof JSONObject )
        {
            JSONObject json = (JSONObject)request.data;
            try
            {
                command = (String)json.get( "command" );
            }
            catch ( Exception e )
            {
                // Silently fail.
            }
        }

        String output = "";
        int response_code = 200;

        if(!(command.equals(""))){
            output = executeConsoleCommand( command );
        }

        if (output.equals("")) {
            response_code = 404;
            output = "Error: Invalid parameters";
        }


        HTTPResponseHeaderHelper.outputHeaders( response_code, out );
        out.write(output);

        return true;
    }
}

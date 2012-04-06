package org.theanticookie.bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.regex.Pattern;
//import java.util.HashMap;
//import org.bukkit.entity.Player;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.theanticookie.bukkit.httpconsole.RequestHandler;
import org.theanticookie.bukkit.httpconsole.HTTPServer;
import org.theanticookie.bukkit.httpconsole.LogFilterConsoleCommandSender;
import org.theanticookie.bukkit.httpconsole.LogFilterLevel;
import org.theanticookie.bukkit.httpconsole.ResourceManager;

/**
 * HTTPConsole plugin for Bukkit/Minecraft
 *
 * @author ryan7745
 * 
 * Original Author
 * @author BlueJeansAndRain
 */
public class HTTPConsole extends JavaPlugin
{
	public static boolean debugging = false;

	//private static final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
	private static final Logger logger = Logger.getLogger( "org.bukkit.BlueJeansAndRain.HTTPConsole" );

	private static PluginDescriptionFile package_description = null;
	private static ResourceManager resource_manager = null;
	private LogFilterConsoleCommandSender console_filter = null;
	private HTTPServer http = null;

	public static String getVersion()
	{
		return package_description.getVersion();
	}

	public static String getPackageName()
	{
		return package_description.getName();
	}
	
	private static String formatLogMessage( String message, Object ... params )
	{
		return String.format( "%s: %s", getPackageName(), String.format( message, params ) );
	}

	public static void rawLog( String message, Object ... params )
	{
		System.out.println( String.format( message, params ) );
	}

	public static void debug( String message, Object ... params )
	{
		if ( !debugging )
			return;

		rawLog( message, params );
	}

	public static void log( String message, Object ... params )
	{
		System.out.println( formatLogMessage( message, params ) );
	}

	public static void log( Level level, String message, Object ... params )
	{
		logger.log( level, formatLogMessage( message, params ) );
	}

	public static void logException( Exception e )
	{
		logger.log( Level.SEVERE, e.getMessage(), e );
	}

	public static void logException( Exception e, String message, Object ... params )
	{
		log( Level.SEVERE, message, params );
		logException( e );
	}
	
	public void enablePlugin()
	{
		this.getServer().getPluginManager().enablePlugin( this );
	}

	public void disablePlugin()
	{
		this.getServer().getPluginManager().disablePlugin( this );
	}

	public boolean generateDefaultConfigFile( File file )
	{
		try
		{
			resource_manager.writeResourceToFile( "/config.yml", file );
		}
		catch ( IOException e )
		{
			logException( e, "Failed to create config.yml" );
			return false;
		}

		return true;
	}
	
	public FileConfiguration getConf() {
		FileConfiguration config;
		if(!(this.getDataFolder().exists())){
			File config_file = new File( this.getDataFolder(), "config.yml" );
			if(!this.getDataFolder().mkdirs()){
				log( Level.SEVERE, "Error creating data folder" );
			} else {
				if(!config_file.exists()){
					generateDefaultConfigFile( config_file.getAbsoluteFile() );
					config = YamlConfiguration.loadConfiguration(config_file);
				} else {
					config = YamlConfiguration.loadConfiguration(config_file);
				}
			}
		} else {
			File config_file = new File( this.getDataFolder(), "config.yml" );
			if(!config_file.exists()){
				generateDefaultConfigFile( config_file.getAbsoluteFile() );
				config = YamlConfiguration.loadConfiguration(config_file);
			}
		}
		File config_file = new File( this.getDataFolder(), "config.yml" );
		config = YamlConfiguration.loadConfiguration(config_file);
		
		return config;
	}

    public void onEnable()
	{
		if ( package_description == null )
			package_description = getDescription();
		if ( resource_manager == null )
			resource_manager = new ResourceManager();
		
		FileConfiguration config = getConf();
		
		debugging = config.getBoolean( "debug", false );

		logger.setFilter( new LogFilterLevel( config.getString( "log-level", "severe" ) ) );

		console_filter = new LogFilterConsoleCommandSender();
		if ( config.getBoolean( "filter-command-sender", true ) )
		{
			// Filter out annoying duplicate messages sent by
			// ConsoleCommandSender.
			// * It's possible someone else will set the filter removing this
			//   one, but the worst that will happen is ConsoleCommandSender
			//   messages will be visible again.
			Logger.getLogger( "Minecraft" ).setFilter( console_filter );
		}

		try
		{
			http = new HTTPServer();
			
			List<Object> whitelist = (List<Object>) config.getList( "client-ip-whitelist", null );
			if(whitelist != null){
			for ( Object ipo : whitelist ){
				String ip = ipo.toString();
				http.addToWhitelist( ip );
			}
			}
			List<Object> blacklist = (List<Object>) config.getList( "client-ip-blacklist", null );
			if(blacklist != null){
			for ( Object ipe : blacklist ){
				String ip = ipe.toString();
				http.addToBlacklist( ip );
			}
			}
            http.setDenyBeforeAllow( Pattern.matches(
                "^(?i)\\s*deny\\s*,\\s*allow\\s*$",
                config.getString( "white-black-list-order", "Deny,Allow")
            ));

			List<Object> allowed_hosts = (List<Object>) config.getList( "allowed-hosts", null );
			if(allowed_hosts != null){
			for ( Object hosto : allowed_hosts ){
				String host = hosto.toString();
				http.addAllowedHost( host );
			}
			}

			http.addRequestHandler( new RequestHandler( this ) );
			http.setAlwaysLogRefusedConnections( config.getBoolean( "always-log-refused-connections", false ) );
			http.start( config.getString( "ip-address", "127.0.0.1" ), config.getInt( "port", 8765 ) );
		}
		catch( Exception e )
		{
			logException( e, "Error creating HTTP server" );
			disablePlugin();
			return;
		}
		
		rawLog( "%s %s is enabled", getPackageName(), getVersion() );
    }

	// NOTE: All registered events are automatically unregistered when a plugin is disabled
    public void onDisable()
	{
        http.stopServer();

		// Remove the ConsoleCommandSender filter if it hasn't been overwritten.
		Logger minecraft_logger = Logger.getLogger( "Minecraft" );
		if ( minecraft_logger.getFilter() == console_filter )
			minecraft_logger.setFilter( null );

        rawLog( "%s %s is disabled", getPackageName(), getVersion() );
    }
	
    /*public boolean isDebugging( final Player player )
	{
        if ( debugees.containsKey( player ) )
            return debugees.get( player );
        else
            return false;
    }

    public void setDebugging(final Player player, final boolean value)
	{
        debugees.put( player, value );
    }*/
}

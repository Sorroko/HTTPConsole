package org.theanticookie.bukkit.httpconsole;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;

/**
 * Open files stored in this jar as streams.
 *
 * @author Chris
 */
public class ResourceManager
{
	public InputStream getResourceInputStream( String path )
	{
		InputStream input = getClass().getResourceAsStream( path );
		return input;
	}

	public String getResourceText( String path )
			throws IOException
	{
		InputStream input = getResourceInputStream( path );
		InputStreamReader reader = new InputStreamReader( input );

		StringWriter writer = new StringWriter();
		char[] buffer = new char[1024];
		int count = 0;
		while ( reader.ready() )
		{
			count = reader.read( buffer, 0, 1024 );
			if ( count != -1 )
				writer.write( buffer, 0, count );
		}
		reader.close();
		
		return writer.toString();
	}

	public void writeResourceToFile( String path, File target )
			throws IOException
	{
		InputStream input = getResourceInputStream( path );
		InputStreamReader reader = new InputStreamReader( input );

		FileWriter writer = new FileWriter( target );

		char[] buffer = new char[1024];
		int count = 0;
		while ( reader.ready() )
		{
			count = reader.read( buffer, 0, 1024 );
			if ( count != -1 )
				writer.write( buffer, 0, count );
		}

		reader.close();
		writer.flush();
		writer.close();
	}
}

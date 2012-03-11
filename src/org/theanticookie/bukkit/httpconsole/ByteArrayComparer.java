package org.theanticookie.bukkit.httpconsole;

import org.theanticookie.bukkit.HTTPConsole;

/**
 * Takes to byte arrays on construction and then lets you compare individual
 * bytes by index.
 * - Index 0 is most the significant byte.
 * - byte arrays can be different lengths. The shorter array will be left padded
 *   with zeros.
 *
 * @author Chris
 */
public class ByteArrayComparer
{
	public byte[] a;
	public byte[] b;
	public int length;

	private byte[] longer;
	private byte[] shorter;
	private int sign = 1;
	private int length_difference = 0;

	private static ByteArrayComparer static_instance = null;

	private void initialize( byte[] a, byte[] b )
	{
		this.a = a;
		this.b = b;

		length_difference = a.length - b.length;
		if ( length_difference >= 0 )
		{
			longer = a;
			shorter = b;
		}
		else
		{
			longer = b;
			shorter = a;
			length_difference *= -1;
			sign = -1;
		}
		length = longer.length;
	}

	public ByteArrayComparer( byte[] a, byte[] b )
	{
		initialize( a, b );
	}

	private ByteArrayComparer()
	{
		// Used to create the initial static instance.
	}

	private static ByteArrayComparer GetInstance()
	{
		if ( static_instance == null )
			static_instance = new ByteArrayComparer();

		return static_instance;
	}

	public int compareByte( int i )
	{
		return compareByte( longer[i], i < length_difference ? 0 : shorter[i - length_difference] ) * sign;
	}

	// Unsigned byte comparison
	public static int compareByte( byte a, byte b )
	{
		return ( (int)a & 0xff ) - ( (int)b & 0xff );
	}

	public int compare()
	{
		for ( int i = 0, value = 0; i < length; i++ )
		{
			value = compareByte( i );
			if ( value != 0 )
				return value;
		}

		return 0;
	}

	public static int compare( byte[] a, byte[] b )
	{
		ByteArrayComparer instance = GetInstance();
		instance.initialize( a, b );
		return instance.compare();
	}

	// return values:
	//   0 = equal
	//  -1 = adjoining, a less than b.
	//   1 = adjoining, a greater than b.
	//  -2 = NOT adjoining, a less than b.
	//   2 = NOT adjoining, a greater than b.
	//  anything else = not adjoining.
	public int adjoin()
	{
		for ( int i = 0, value = 0; i < length; i++ )
		{
			value = compareByte( i );
			if ( value != 0 )
			{
				if ( length - i > 1 ) // Not the last byte.
					// Returns negative or positive 2.
					return value / Math.abs( value ) * 2;
				else
					return value;
			}
		}

		return 0;
	}

	public static int adjoin( byte[] a, byte[] b )
	{
		ByteArrayComparer instance = GetInstance();
		instance.initialize( a, b );
		return instance.adjoin();
	}
}

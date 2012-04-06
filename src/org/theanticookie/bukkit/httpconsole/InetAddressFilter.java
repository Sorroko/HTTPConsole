package org.theanticookie.bukkit.httpconsole;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.net.UnknownHostException;

/**
 * Provides a way to store ranges and individual ip addresses so that they can
 * be compared to another ip address to determine if the target ip address falls
 * within the range.
 *
 * @author Chris
 */
public class InetAddressFilter
{
	private static class Range implements Comparable<Range>
	{
		public byte[] from;
		public byte[] to;

        private void sanityCheck()
        {
            if ( ByteArrayComparer.compare( from, to ) > 0 )
            {
                // If from is greater than to, reverse them.
                byte[] temp = from;
                from = to;
                to = temp;
            }
        }

		public Range( byte[] from, byte[] to )
		{
			this.from = from;
			this.to = to;
            this.sanityCheck();
		}

		public Range( String ip )
            throws UnknownHostException
		{
			if ( ip.equalsIgnoreCase( "any" ) )
			{
				byte[] min = { 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0 };
				this.from = min;
				byte[] max = { -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1, -1,-1,-1,-1 };
				this.to = max;
			}
			else
			{
				String[] range = ip.split( "-", 2 );

				if ( range.length == 1 && ip.contains( "*" ) )
				{
					range = new String[2];
					range[0] = ip.replace( "*", "0" );
					range[1] = ip.replace( "*", "255" );
				}

				if ( range.length == 2 )
				{
					this.from = InetAddress.getByName( range[0] ).getAddress();
					this.to = InetAddress.getByName( range[1] ).getAddress();
                    this.sanityCheck();
				}
				else
				{
					this.from = InetAddress.getByName( ip ).getAddress();
					this.to = this.from;
				}
			}
		}

		public Range( String from, String to )
			throws UnknownHostException
		{
			this.from = InetAddress.getByName( from ).getAddress();
			this.to = InetAddress.getByName( to ).getAddress();
            this.sanityCheck();
		}

		public Range( InetAddress ip )
		{
			this.from = ip.getAddress();
			this.to = this.from;
		}

		public Range( InetAddress from, InetAddress to )
		{
			this.from = from.getAddress();
			this.to = to.getAddress();
            this.sanityCheck();
		}

		public int compareTo( Range other )
		{
            // Compare the range start addresses first, then compare their end
            //  addresses if the start addresses are equal.
			int value = ByteArrayComparer.compare( this.from, other.from );
			if ( value == 0 )
				value = ByteArrayComparer.compare( this.to, other.to );

			return value;
		}
	}

	private final ArrayList<Range> ranges = new ArrayList<Range>();

	public InetAddressFilter()
	{
		// Nothing to do.
	}

	private void add( Range range )
	{
		int index = Collections.binarySearch( ranges, range );
		if ( index > 0 )
			return; // The exact range already exists.
		
		ranges.add( ~index, range );
	}

	public void add( String ip )
		throws UnknownHostException
	{
		add( new Range( ip ) );
	}

	public void add( String from, String to )
		throws UnknownHostException
	{
		add( new Range( from, to ) );
	}

	public void add( InetAddress ip )
	{
		add( new Range( ip ) );
	}

	public void add( InetAddress from, InetAddress to )
	{
		add( new Range( from, to ) );
	}

	private void remove( Range range )
	{
		int index = Collections.binarySearch( ranges, range );
		if ( index >= 0 )
			ranges.remove( index );
	}

	public void remove( String ip )
		throws UnknownHostException
	{
		remove( new Range( ip ) );
	}

	public void remove( String from, String to )
		throws UnknownHostException
	{
		remove( new Range( from, to ) );
	}

	public void remove( InetAddress ip )
	{
		remove( new Range( ip ) );
	}

	public void remove( InetAddress from, InetAddress to )
	{
		remove( new Range( from, to ) );
	}

	public void clear()
	{
		ranges.clear();
	}

	public boolean isEmpty()
	{
		return ranges.isEmpty();
	}

	public boolean containsAddress( InetAddress ip )
	{
		// Simple iterative test until I figure out how to do the faster binary lookup.
		byte[] test_address = ip.getAddress();
		for ( Range range : ranges )
		{
            if (
                ByteArrayComparer.compare( test_address, range.from ) >= 0
                && ByteArrayComparer.compare( test_address, range.to ) <= 0
            ) {
                return true;
            }
		}

		return false;
	}

	public boolean containsAddress( String ip )
		throws UnknownHostException
	{
		return containsAddress( InetAddress.getByName( ip ) );
	}
}

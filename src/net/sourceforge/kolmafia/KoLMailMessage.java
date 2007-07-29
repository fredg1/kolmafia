/**
 * Copyright (c) 2005-2007, KoLmafia development team
 * http://kolmafia.sourceforge.net/
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  [1] Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *  [2] Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in
 *      the documentation and/or other materials provided with the
 *      distribution.
 *  [3] Neither the name "KoLmafia" nor the names of its contributors may
 *      be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

public class KoLMailMessage implements Comparable
{
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat( "EEEE, MMMM dd, yyyy, hh:mmaa", Locale.US );

	private String messageId;
	private String senderId;
	private String senderName;
	private String messageDate;
	private Date timestamp;
	private String messageHTML;

	private String completeHTML;

	public KoLMailMessage( String message )
	{
		// Blank lines are not displayed correctly
		this.completeHTML = StaticEntity.globalStringReplace( message, "<br><br>", "<br>&nbsp;<br>" );

		this.completeHTML = this.completeHTML.substring( this.completeHTML.indexOf( ">" ) + 1,
			this.completeHTML.indexOf( "reply</a>]" ) + 10 ) + this.completeHTML.substring( this.completeHTML.indexOf( "<br>" ) );

		this.messageId = message.substring( message.indexOf( "name=" ) + 6, message.indexOf( "\">" ) );
		StringTokenizer messageParser = new StringTokenizer( message, "<>" );

		String lastToken = messageParser.nextToken();
		while ( !lastToken.startsWith( "a " ) )
			lastToken = messageParser.nextToken();

		this.senderId = lastToken.substring( lastToken.indexOf( "who=" ) + 4, lastToken.length() - 1 );
		this.senderName = messageParser.nextToken();

		KoLmafia.registerPlayer( senderName, senderId );

		while ( !messageParser.nextToken().startsWith( "Date" ) );
		messageParser.nextToken();

		this.messageDate = messageParser.nextToken().trim();
		this.messageHTML = message.substring( message.indexOf( this.messageDate ) + this.messageDate.length() + 4 );

		try
		{
			// This attempts to parse the date from
			// the given string; note it may throw
			// an exception (but probably not)

			this.timestamp = TIMESTAMP_FORMAT.parse( this.messageDate );
		}
		catch ( Exception e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e, "Could not parse date \"" + this.messageDate + "\"" );

			// Initialize the date to the current time,
			// since that's about as close as it gets

			this.timestamp = new Date();
			this.messageDate = TIMESTAMP_FORMAT.format( this.timestamp );
		}
	}

	public String toString()
	{	return this.senderName + " @ " + this.messageDate;
	}

	public int compareTo( Object o )
	{	return o == null || !(o instanceof KoLMailMessage) ? -1 : this.messageId.compareTo( ((KoLMailMessage)o).messageId );
	}

	public boolean equals( Object o )
	{	return o == null || !(o instanceof KoLMailMessage) ? false : this.messageId.equals( ((KoLMailMessage)o).messageId );
	}

	public String getMessageId()
	{	return this.messageId;
	}

	public Date getTimestamp()
	{	return this.timestamp;
	}

	public String getCompleteHTML()
	{	return this.completeHTML;
	}

	public String getMessageHTML()
	{	return this.messageHTML.toString();
	}

	public String getSenderName()
	{	return this.senderName;
	}

	public String getSenderId()
	{	return this.senderId;
	}

	public String getDisplayHTML()
	{	return this.completeHTML;
	}
}

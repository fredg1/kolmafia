/*
 * Copyright (c) 2005-2021, KoLmafia development team
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
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION ) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE ) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.sourceforge.kolmafia.textui.parsetree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Location;

/**
 * A type of {@link ParseTreeNode}.
 * Unlike {@link Command}, the "same" {@link Symbol} can appear multiple times,
 * split between Definition and Reference.
 */
public abstract class Symbol
	extends ParseTreeNode
	implements Comparable<Symbol>
{
	public final String name;

	public final Location location;
	public final List<Location> references;

	public Symbol()
	{
		this( null, null );
	}

	public Symbol( final String name, final Location location )
	{
		this.name = name;
		this.location = location;
		this.references = new ArrayList<>();
	}

	public String getName()
	{
		return this.name;
	}

	public Location getLocation()
	{
		return this.location;
	}

	public Location getDefinitionLocation()
	{
		return this.location;
	}

	public List<Location> getReferenceLocations()
	{
		return this.references;
	}

	public void addReference( final Location location )
	{
		this.references.add( location );
	}

	public int compareTo( final Symbol o )
	{
		if ( !( o instanceof Symbol ) )
		{
			throw new ClassCastException();
		}
		if ( this.name == null )
		{
			return 1;
		}
		return this.name.compareToIgnoreCase( o.name );
	}


	/** For error propagation only */
	public static interface BadNode
	{}

	public boolean isBad()
	{
		return this instanceof BadNode;
	}
}

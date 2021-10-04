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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.lsp4j.Location;

public class TypeList
	implements Iterable<Type>
{
	private final Map<Type, List<Location>> list = new TreeMap<>();

	public boolean add( final Type n )
	{
		if ( this.find( n.getName() ) != null )
		{
			return false;
		}

		list.put( n, new ArrayList<>() );
		return true;
	}

	public Type find( final String name )
	{
		for ( Type currentType : this.list.keySet() )
		{
			if ( name != null && name.equalsIgnoreCase( currentType.getName() ) )
			{
				return currentType;
			}
		}

		return null;
	}

	public Iterator<Type> iterator()
	{
		return list.keySet().iterator();
	}

	public boolean contains( final Type type )
	{
		return list.containsKey( type );
	}

	public TypeList clone()
	{
		TypeList result = new TypeList();

		for ( Type type : this.list.keySet() )
		{
			result.add( type );
		}

		return result;
	}

	public void addReference( final Type type, final Location location )
	{
		List<Location> references = this.list.get( type );

		if ( references != null )
		{
			references.add( location );
		}
	}

	public List<Location> getReferences( final Type type )
	{
		return this.list.get( type );
	}
}

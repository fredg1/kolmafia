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

public class VariableList
	implements Iterable<Variable>
{
	private final Map<Variable, List<Location>> list = new TreeMap<>();

	public boolean add( final Variable n )
	{
		if ( this.find( n.getName() ) != null )
		{
			return false;
		}

		list.put( n, new ArrayList<>() );
		return true;
	}

	public Variable find( final String name )
	{
		for ( Variable variable : this.list.keySet() )
		{
			if ( variable != null && variable.getName().equalsIgnoreCase( name ) )
			{
				return variable;
			}
		}

		return null;
	}

	public Iterator<Variable> iterator()
	{
		return list.keySet().iterator();
	}

	public boolean contains( final Variable variable )
	{
		return list.containsKey( variable );
	}

	public VariableList clone()
	{
		VariableList result = new VariableList();

		for ( Variable variable : this.list.keySet() )
		{
			result.add( variable );
		}

		return result;
	}

	public void addReference( final Variable variable, final Location location )
	{
		List<Location> references = this.list.get( variable );

		if ( references != null )
		{
			references.add( location );
		}
	}

	public List<Location> getReferences( final Variable variable )
	{
		return this.list.get( variable );
	}
}

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

import java.io.PrintStream;

import java.lang.IllegalArgumentException;

import java.util.Arrays;

import net.sourceforge.kolmafia.KoLmafia;

import net.sourceforge.kolmafia.textui.DataTypes;
import net.sourceforge.kolmafia.textui.AshRuntime;
import net.sourceforge.kolmafia.textui.Parser;
import net.sourceforge.kolmafia.textui.ScriptRuntime;

public class SortBy
	extends Command
{
	private final VariableReference aggregate;
	private final Variable indexvar, valuevar;
	private final Value expr;

	// For runtime error messages
	String fileName;
	int lineNumber;

	public SortBy( final VariableReference aggregate, final Variable indexvar,
		       final Variable valuevar, final Value expr, final Parser parser )
	{
		this.aggregate = aggregate;
		this.indexvar = indexvar;
		this.valuevar = valuevar;
		this.expr = expr;
		this.fileName = parser.getShortFileName();
		this.lineNumber = parser.getLineNumber();
	}

	@Override
	public Value execute( final AshRuntime interpreter )
	{
		if ( !KoLmafia.permitsContinue() )
		{
			interpreter.setState( ScriptRuntime.State.EXIT );
			return null;
		}

		interpreter.traceIndent();
		if ( ScriptRuntime.isTracing() )
		{
			interpreter.trace( this.toString() );
		}

		AggregateValue map = (AggregateValue) this.aggregate.execute( interpreter );
		interpreter.captureValue( map );

		if ( interpreter.getState() == ScriptRuntime.State.EXIT )
		{
			interpreter.traceUnindent();
			return null;
		}

		Value[] keys = map.keys();
		Pair[] values = new Pair[ keys.length ];

		for ( int i = 0; i < keys.length; ++i )
		{
			Value index = keys[i];
			this.indexvar.setValue( interpreter, index );
			Value value = map.aref( index, interpreter );
			this.valuevar.setValue( interpreter, value );
			if ( ScriptRuntime.isTracing() )
			{
				interpreter.trace( "Element #" + i + ": " + index + " = " + value );
			}
			Value sortkey = this.expr.execute( interpreter );
			if ( interpreter.getState() == ScriptRuntime.State.EXIT )
			{
				interpreter.traceUnindent();
				return null;
			}
			interpreter.captureValue( sortkey );
			if ( ScriptRuntime.isTracing() )
			{
				interpreter.trace( "Key = " + sortkey );
			}
			values[i] = new Pair( sortkey, value );
		}

		try
		{
			Arrays.sort( values );
		}
		catch ( IllegalArgumentException e )
		{
			interpreter.setLineAndFile( this.fileName, this.lineNumber );
			throw interpreter.runtimeException( "Illegal argument exception during sort" );
		}

		for ( int i = 0; i < keys.length; ++i )
		{
			Value index = keys[ i ];
			map.aset( index, values[ i ].value, interpreter );
		}

		interpreter.traceUnindent();
		return DataTypes.VOID_VALUE;
	}

	@Override
	public String toString()
	{
		return "sort";
	}

	@Override
	public void print( final PrintStream stream, final int indent )
	{
		AshRuntime.indentLine( stream, indent );
		stream.println( "<SORT>" );
		this.aggregate.print( stream, indent + 1 );
		this.expr.print( stream, indent + 1 );
	}

	private static class Pair
		implements Comparable<Pair>
	{
		public Value key, value;

		public Pair( Value key, Value value )
		{
			this.key = key;
			this.value = value;
		}

		public int compareTo( Pair o )
		{
			return this.key.compareTo( o.key );
		}
	}
}

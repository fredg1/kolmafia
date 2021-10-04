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

import java.util.List;

import org.eclipse.lsp4j.Location;

import net.sourceforge.kolmafia.KoLmafia;

import net.sourceforge.kolmafia.textui.AshRuntime;
import net.sourceforge.kolmafia.textui.Parser;
import net.sourceforge.kolmafia.textui.ScriptRuntime;

public class FunctionInvocation
	extends FunctionCall
{
	private final BasicScope scope;
	private final Value name;
	private final Type type;

	public FunctionInvocation( final Location location, final BasicScope scope, final Type type, final Value name, final List<Value> params, final Parser parser )
	{
		super( location, null, params, parser );
		this.scope = scope;
		this.type = type;
		this.name = name;
	}

	@Override
	public Type getType()
	{
		return this.type;
	}

	@Override
	public Type getRawType()
	{
		return this.type;
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
			interpreter.trace( "Invoke: " + this );
			interpreter.trace( "Function name: " + this.name );
		}

		// Get the function name
		Value funcValue = this.name.execute( interpreter );

		if ( ScriptRuntime.isTracing() )
		{
			interpreter.trace( "[" + interpreter.getState() + "] <- " + funcValue );
		}

		if ( funcValue == null )
		{
			interpreter.traceUnindent();
			return null;
		}

		interpreter.setLineAndFile( this.fileName, this.lineNumber );

		String func = funcValue.toString();
		Function function = this.scope.findFunction( func, this.params );
		if ( function == null )
		{
			throw interpreter.undefinedFunctionException( func, this.params );
		}

		if ( !Operator.validCoercion( this.type, function.getType(), "return" ) )
		{
			throw interpreter.runtimeException( "Calling \"" + func + "\", which returns " + function.getType() + " but " + this.type + " expected" );
		}

		this.target = function;

		// Invoke it.
		Value result = super.execute( interpreter );
		interpreter.traceUnindent();

		return result;
	}

	@Override
	public String toString()
	{
		return "call " + this.type.toString() + " " + this.name.toString() + "()";
	}

	@Override
	public void print( final PrintStream stream, final int indent )
	{
		AshRuntime.indentLine( stream, indent );
		stream.println( "<INVOKE " + this.name.toString() + ">" );
		this.type.print( stream, indent + 1 );

		for ( Value current : this.params )
		{
			current.print( stream, indent + 1 );
		}
	}
}

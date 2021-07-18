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

package net.sourceforge.kolmafia.textui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;

import java.nio.charset.StandardCharsets;
import java.util.*;

import net.java.dev.spellcast.utilities.DataUtilities;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import static org.eclipse.lsp4j.DiagnosticSeverity.*;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import net.sourceforge.kolmafia.KoLConstants;
import net.sourceforge.kolmafia.KoLmafiaCLI;
import net.sourceforge.kolmafia.StaticEntity;

import net.sourceforge.kolmafia.objectpool.IntegerPool;

import net.sourceforge.kolmafia.persistence.EffectDatabase;
import net.sourceforge.kolmafia.persistence.ItemDatabase;
import net.sourceforge.kolmafia.persistence.MonsterDatabase;
import net.sourceforge.kolmafia.persistence.SkillDatabase;

import net.sourceforge.kolmafia.preferences.Preferences;

import net.sourceforge.kolmafia.textui.Parser.Line.Token;

import net.sourceforge.kolmafia.textui.parsetree.AggregateType;
import net.sourceforge.kolmafia.textui.parsetree.AggregateType.BadAggregateType;
import net.sourceforge.kolmafia.textui.parsetree.ArrayLiteral;
import net.sourceforge.kolmafia.textui.parsetree.Assignment;
import net.sourceforge.kolmafia.textui.parsetree.BasicScope;
import net.sourceforge.kolmafia.textui.parsetree.BasicScript;
import net.sourceforge.kolmafia.textui.parsetree.Catch;
import net.sourceforge.kolmafia.textui.parsetree.Command;
import net.sourceforge.kolmafia.textui.parsetree.CompositeReference;
import net.sourceforge.kolmafia.textui.parsetree.Concatenate;
import net.sourceforge.kolmafia.textui.parsetree.Conditional;
import net.sourceforge.kolmafia.textui.parsetree.Else;
import net.sourceforge.kolmafia.textui.parsetree.ElseIf;
import net.sourceforge.kolmafia.textui.parsetree.ForEachLoop;
import net.sourceforge.kolmafia.textui.parsetree.ForLoop;
import net.sourceforge.kolmafia.textui.parsetree.Function;
import net.sourceforge.kolmafia.textui.parsetree.Function.BadFunction;
import net.sourceforge.kolmafia.textui.parsetree.Function.MatchType;
import net.sourceforge.kolmafia.textui.parsetree.FunctionCall;
import net.sourceforge.kolmafia.textui.parsetree.FunctionInvocation;
import net.sourceforge.kolmafia.textui.parsetree.FunctionReturn;
import net.sourceforge.kolmafia.textui.parsetree.If;
import net.sourceforge.kolmafia.textui.parsetree.IncDec;
import net.sourceforge.kolmafia.textui.parsetree.JavaForLoop;
import net.sourceforge.kolmafia.textui.parsetree.Loop;
import net.sourceforge.kolmafia.textui.parsetree.LoopBreak;
import net.sourceforge.kolmafia.textui.parsetree.LoopContinue;
import net.sourceforge.kolmafia.textui.parsetree.MapLiteral;
import net.sourceforge.kolmafia.textui.parsetree.Operation;
import net.sourceforge.kolmafia.textui.parsetree.Operator;
import net.sourceforge.kolmafia.textui.parsetree.PluralValue;
import net.sourceforge.kolmafia.textui.parsetree.RecordType;
import net.sourceforge.kolmafia.textui.parsetree.RecordType.BadRecordType;
import net.sourceforge.kolmafia.textui.parsetree.RepeatUntilLoop;
import net.sourceforge.kolmafia.textui.parsetree.Scope;
import net.sourceforge.kolmafia.textui.parsetree.ScriptExit;
import net.sourceforge.kolmafia.textui.parsetree.ScriptState;
import net.sourceforge.kolmafia.textui.parsetree.SortBy;
import net.sourceforge.kolmafia.textui.parsetree.StaticScope;
import net.sourceforge.kolmafia.textui.parsetree.Switch;
import net.sourceforge.kolmafia.textui.parsetree.SwitchScope;
import net.sourceforge.kolmafia.textui.parsetree.TernaryExpression;
import net.sourceforge.kolmafia.textui.parsetree.Try;
import net.sourceforge.kolmafia.textui.parsetree.Type;
import net.sourceforge.kolmafia.textui.parsetree.Type.BadType;
import net.sourceforge.kolmafia.textui.parsetree.Type.TypeReference;
import net.sourceforge.kolmafia.textui.parsetree.TypeDef;
import net.sourceforge.kolmafia.textui.parsetree.UserDefinedFunction;
import net.sourceforge.kolmafia.textui.parsetree.Value;
import net.sourceforge.kolmafia.textui.parsetree.VarArgType;
import net.sourceforge.kolmafia.textui.parsetree.Variable;
import net.sourceforge.kolmafia.textui.parsetree.Variable.BadVariable;
import net.sourceforge.kolmafia.textui.parsetree.VariableList;
import net.sourceforge.kolmafia.textui.parsetree.VariableReference;
import net.sourceforge.kolmafia.textui.parsetree.WhileLoop;

import net.sourceforge.kolmafia.utilities.ByteArrayStream;
import net.sourceforge.kolmafia.utilities.CharacterEntities;
import net.sourceforge.kolmafia.utilities.StringUtilities;

/*
Scope
	Typedef
		Identifier (exactly 1)
	Type
		existing type
		Record
			( Type + Identifier ) (0+)
		AggregateType
			AggregateType
				AggregateType
					(...) (e.g. boolean [string][string][string] [...] )
	Command (one of:)
		Return
			Expression
				Expression
					Expression
						(...)
				Value
				Operator
		BasicScript
		While
			Expression
			+	LoopScope (one of:)
					Scope
					Command
		Foreach
			Identifier + Value + LoopScope
		JavaFor
			Type + Identifier + Expression (0-1)
			+ Expression
			+ PreIncDec (0-1) + VariableReference + PostIncDec (0-1) + Assignment
			+ LoopScope
		For
			Identifier + Expression + Expression + Expression (0-1)
			+ LoopScope
		Repeat
			LoopScope + Expression
		Switch
			Expression
			+	(1+):
				Expression (0-1)
				+	Type
					Command
					Variables
		Conditional
			Expression
			+	(1+)
				BlockOrSingleCommand
				+ Expression (0-1)
		Try
			BlockOrSingleCommand (one of:)
				Block
				SingleCommandScope
					Command (exactly 1)
			+ BlockOrSingleCommand
		Catch
			BlockOrSingleCommand
		Static
			CommandOrDeclaration
				Type
				Command
				Variables
			Scope
		Sort
			VariableReference + Expression
		Remove
			Expression
		Block
			Scope
		Value
			Expression
			Number
			String
				Expression
				Literal
			TypedConstant
				Type
				String
				Literal
			NewRecord
				Identifier
				+	AggregateLiteral
					Expression
			CatchValue
				Block
				Expression
			PreIncDec
				VariableReference
			Invoke
				Type
				+	Expression
					Identifier + VariableReference
				+	Parameters
						Expression (0+)
				+	PostCall
						VariableReference
			Call
				ScopedIdentifier
				+ Parameters
				+ PostCall
			Type + ( AggregateLiteral / VariableReference )
	Function
		Identifier + ( Type + Variable ) (0+)
		+	BlockOrSingleCommand
	Variables
		Variable (1+)
			Identifier
	AggregateLiteral
		AggregateLiteral
			AggregateLiteral
				(...)
		Expression
*/
public class Parser
{
	public static final String APPROX = "\u2248";
	public static final String PRE_INCREMENT = "++X";
	public static final String PRE_DECREMENT = "--X";
	public static final String POST_INCREMENT = "X++";
	public static final String POST_DECREMENT = "X--";

	// Variables used during parsing

	private final String fileName;
	private final String shortFileName;
	private String scriptName;
	private final InputStream istream;

	private Line currentLine;
	private int currentIndex;
	private Token currentToken;

	private final Map<File, Long> imports;
	private Function mainMethod = null;
	private String notifyRecipient = null;

	final List<AshDiagnostic> diagnostics = new ArrayList<>();

	public Parser()
	{
		this( null, null, null );
	}

	public Parser( final File scriptFile, final Map<File, Long> imports )
	{
		this( scriptFile, null, imports );
	}

	public Parser( final File scriptFile, final InputStream stream, final Map<File, Long> imports )
	{
		this.imports = ( imports != null ) ? imports : new TreeMap<>();

		if ( scriptFile != null )
		{
			this.fileName = scriptFile.getPath();
			this.shortFileName = this.fileName.substring( this.fileName.lastIndexOf( File.separator ) + 1 );
			this.istream = DataUtilities.getInputStream( scriptFile );
		}
		else if ( stream != null )
		{
			this.fileName = null;
			this.shortFileName = null;
			this.istream = stream;
		}
		else
		{
			this.fileName = null;
			this.shortFileName = null;
			this.istream = null;
			return;
		}

		try
		{
			final LineNumberReader commandStream = new LineNumberReader( new InputStreamReader( this.istream, StandardCharsets.UTF_8 ) );
			this.currentLine = new Line( commandStream );
			while ( this.currentLine.content != null &&
			        this.currentLine.content.length() == 0 )
			{
				this.currentLine = this.currentLine.nextLine;
			}
			this.currentIndex = this.currentLine.offset;
		}
		catch ( Exception e )
		{
			// If any part of the initialization fails,
			// then throw an exception.

			// If using the LSP, should we also print in the GCLI or something?
			this.error( this.fileName + " could not be accessed" );
		}
		finally
		{
			try
			{
				this.istream.close();
			}
			catch ( IOException e )
			{
			}
		}
	}

	public Scope parse()
	{
		Token firstToken = this.currentToken();
		Scope scope = this.parseFile( null );

		scope.setScopeLocation( this.makeLocation( firstToken, this.peekPreviousToken() ) );

		if ( this.currentLine.nextLine != null )
		{
			this.error( "Script parsing error; thought we reached the end of the file" );
		}

		return scope;
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public String getShortFileName()
	{
		return this.shortFileName;
	}

	public String getScriptName()
	{
		return ( this.scriptName != null ) ?
		       this.scriptName :
		       this.shortFileName;
	}

	public int getLineNumber()
	{
		return this.currentLine.lineNumber;
	}

	public Map<File, Long> getImports()
	{
		return this.imports;
	}

	public Function getMainMethod()
	{
		return this.mainMethod;
	}

	public String getNotifyRecipient()
	{
		return this.notifyRecipient;
	}

	public static Scope getExistingFunctionScope()
	{
		return new Scope( RuntimeLibrary.functions, null, DataTypes.simpleTypes );
	}

	// **************** Parser *****************

	private static final HashSet<String> multiCharTokens = new HashSet<String>();
	private static final HashSet<String> reservedWords = new HashSet<String>();

	static
	{
		// Tokens
		multiCharTokens.add( "==" );
		multiCharTokens.add( "!=" );
		multiCharTokens.add( "<=" );
		multiCharTokens.add( ">=" );
		multiCharTokens.add( "||" );
		multiCharTokens.add( "&&" );
		multiCharTokens.add( "//" );
		multiCharTokens.add( "/*" );
		multiCharTokens.add( "<<" );
		multiCharTokens.add( ">>" );
		multiCharTokens.add( ">>>" );
		multiCharTokens.add( "++" );
		multiCharTokens.add( "--" );
		multiCharTokens.add( "**" );
		multiCharTokens.add( "+=" );
		multiCharTokens.add( "-=" );
		multiCharTokens.add( "*=" );
		multiCharTokens.add( "/=" );
		multiCharTokens.add( "%=" );
		multiCharTokens.add( "**=" );
		multiCharTokens.add( "&=" );
		multiCharTokens.add( "^=" );
		multiCharTokens.add( "|=" );
		multiCharTokens.add( "<<=" );
		multiCharTokens.add( ">>=" );
		multiCharTokens.add( ">>>=" );
		multiCharTokens.add( "..." );

		// Constants
		reservedWords.add( "true" );
		reservedWords.add( "false" );

		// Operators
		reservedWords.add( "contains" );
		reservedWords.add( "remove" );
		reservedWords.add( "new" );

		// Control flow
		reservedWords.add( "if" );
		reservedWords.add( "else" );
		reservedWords.add( "foreach" );
		reservedWords.add( "in" );
		reservedWords.add( "for" );
		reservedWords.add( "from" );
		reservedWords.add( "upto" );
		reservedWords.add( "downto" );
		reservedWords.add( "by" );
		reservedWords.add( "while" );
		reservedWords.add( "repeat" );
		reservedWords.add( "until" );
		reservedWords.add( "break" );
		reservedWords.add( "continue" );
		reservedWords.add( "return" );
		reservedWords.add( "exit" );
		reservedWords.add( "switch" );
		reservedWords.add( "case" );
		reservedWords.add( "default" );
		reservedWords.add( "try" );
		reservedWords.add( "catch" );
		reservedWords.add( "finally" );
		reservedWords.add( "static" );

		// Data types
		reservedWords.add( "void" );
		reservedWords.add( "boolean" );
		reservedWords.add( "int" );
		reservedWords.add( "float" );
		reservedWords.add( "string" );
		reservedWords.add( "buffer" );
		reservedWords.add( "matcher" );
		reservedWords.add( "aggregate" );

		reservedWords.add( "item" );
		reservedWords.add( "location" );
		reservedWords.add( "class" );
		reservedWords.add( "stat" );
		reservedWords.add( "skill" );
		reservedWords.add( "effect" );
		reservedWords.add( "familiar" );
		reservedWords.add( "slot" );
		reservedWords.add( "monster" );
		reservedWords.add( "element" );
		reservedWords.add( "coinmaster" );

		reservedWords.add( "record" );
		reservedWords.add( "typedef" );
	}

	private static boolean isReservedWord( final String name )
	{
		return name != null && Parser.reservedWords.contains( name.toLowerCase() );
	}

	public Scope importFile( final String fileName, final Scope scope )
	{
		List<File> matches = KoLmafiaCLI.findScriptFile( fileName );
		if ( matches.size() > 1 )
		{
			StringBuilder s = new StringBuilder();
			for ( File f : matches )
			{
				if ( s.length() > 0 )
					s.append( "; " );
				s.append( f.getPath() );
			}
			this.error( "too many matches for " + fileName + ": " + s );

			return scope;
		}
		if ( matches.size() == 0 )
		{
			this.error( fileName + " could not be found" );

			return scope;
		}

		File scriptFile = matches.get( 0 );

		if ( this.imports.containsKey( scriptFile ) )
		{
			return scope;
		}

		this.imports.put( scriptFile, scriptFile.lastModified() );

		Parser parser = new Parser( scriptFile, null, this.imports );
		Scope result = parser.parseFile( scope );

		if ( parser.currentLine.nextLine != null )
		{
			parser.error( "Script parsing error; thought we reached the end of the file" );
		}

		for ( AshDiagnostic diagnostic : parser.diagnostics )
		{
			this.diagnostics.add( diagnostic );
		}

		if ( parser.mainMethod != null )
		{	// Make imported script's main() available under a different name
			UserDefinedFunction f = new UserDefinedFunction(
				parser.mainMethod.getName() + "@" +
					parser.getScriptName().replace( ".ash", "" )
						.replaceAll( "[^a-zA-Z0-9]", "_" ),
				parser.mainMethod.getType(),
				parser.mainMethod.getDefinitionLocation(),
				parser.mainMethod.getVariableReferences() );
			f.setScope( ((UserDefinedFunction)parser.mainMethod).getScope() );
			f.setVariableReferences( parser.mainMethod.getVariableReferences() );
			result.addFunction( f );
		}

		return result;
	}

	private Scope parseCommandOrDeclaration( final Scope result, final Type expectedType )
	{
		Position typeStart = this.here();

		Type t = this.parseType( result, true );

		// If there is no data type, it's a command of some sort
		if ( t == null )
		{
			Command c = this.parseCommand( expectedType, result, false, false, false );
			if ( c != null )
			{
				result.addCommand( c, this );
			}
			else
			{
				this.error( "command or declaration required" );
			}
		}
		else if ( this.parseVariables( t, result ) )
		{
			if ( ";".equals( this.currentToken().value ) )
			{
				this.readToken(); //read ;
			}
			else
			{
				this.unexpectedTokenError( ";", this.currentToken() );
			}
		}
		else
		{
			//Found a type but no function or variable to tie it to
			this.error( typeStart, "Type given but not used to declare anything" );
		}

		return result;
	}

	private Scope parseFile( final Scope startScope )
	{
		Scope scope = startScope != null ? startScope :
		              new Scope( (VariableList) null, Parser.getExistingFunctionScope() );

		this.parseScriptName();
		this.parseNotify();
		this.parseSince();

		Directive importDirective;
		while ( ( importDirective = this.parseImport() ) != null )
		{
			scope = this.importFile( importDirective.value, scope );
		}

		return this.parseScope( scope, null, scope.getParentScope(), true, false, false );
	}

	private Scope parseScope( final Type expectedType,
	                          final VariableList variables,
	                          final BasicScope parentScope,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
	{
		Scope result = new Scope( variables, parentScope );
		return this.parseScope( result, expectedType, parentScope, false, allowBreak, allowContinue );
	}

	private Scope parseScope( final Scope result,
	                          final Type expectedType,
	                          final BasicScope parentScope,
	                          final boolean wholeFile,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
	{
		Position previousPosition = null;
		while ( !this.atEndOfFile() )
		{
			// Infinite loop prevention
			if ( !this.madeProgress( previousPosition, previousPosition = this.here() ) )
			{
				if ( !wholeFile )
				{
					break;
				}

				// If we're at the top scope of a file, and we reached a node we
				// couldn't parse, just read the current token and continue
				this.readToken();

				this.error( previousPosition, "Empty or unknown node" );
				continue;
			}

			if ( this.parseTypedef( result ) )
			{
				if ( ";".equals( this.currentToken().value ) )
				{
					this.readToken(); //read ;
				}
				else
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}

				continue;
			}

			Type t = this.parseType( result, true );

			// If there is no data type, it's a command of some sort
			if ( t == null )
			{
				// See if it's a regular command
				Command c = this.parseCommand( expectedType, result, false, allowBreak, allowContinue );
				if ( c != null )
				{
					result.addCommand( c, this );
				}

				continue;
			}

			// If this is a new record definition, enter it
			if ( t.getType() == DataTypes.TYPE_RECORD && ";".equals( this.currentToken().value ) )
			{
				this.readToken(); // read ;
				continue;
			}

			Function f = this.parseFunction( t, result );
			if ( f != null )
			{
				if ( "main".equalsIgnoreCase( f.getName() ) )
				{
					if ( parentScope.getParentScope() == null )
					{
						this.mainMethod = f;
					}
					else
					{
						this.error( f.getDefinitionLocation(), "main method must appear at top level" );
					}
				}

				continue;
			}

			if ( this.parseVariables( t, result ) )
			{
				if ( ";".equals( this.currentToken().value ) )
				{
					this.readToken(); //read ;
				}
				else
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}

				continue;
			}

			if ( ( t.getBaseType() instanceof AggregateType ) && "{".equals( this.currentToken().value ) )
			{
				this.readToken(); // read {
				result.addCommand( this.parseAggregateLiteral( result, (AggregateType) t ), this );
			}
			else
			{
				//Found a type but no function or variable to tie it to
				this.error( "Type given but not used to declare anything" );
			}
		}

		return result;
	}

	private Type parseRecord( final BasicScope parentScope )
	{
		if ( !"record".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token recordStartToken = this.currentToken();

		this.readToken(); // read record

		if ( ";".equals( this.currentToken().value ) )
		{
			this.error( this.currentToken(), "Record name expected" );

			return new BadRecordType( null, this.makeLocation( recordStartToken ) );
		}

		// Allow anonymous records
		String recordName = null;

		boolean recordError = false;
		boolean recordSyntaxError = false;

		if ( !"{".equals( this.currentToken().value ) )
		{
			// Named record
			recordName = this.currentToken().value;

			if ( !this.parseIdentifier( recordName ) )
			{
				this.error( this.currentToken(), "Invalid record name '" + recordName + "'" );
				recordError = recordSyntaxError = true;

				recordName = null;
			}
			else if ( Parser.isReservedWord( recordName ) )
			{
				this.error( this.currentToken(), "Reserved word '" + recordName + "' cannot be a record name" );
				recordError = true;

				recordName = null;
			}
			else if ( parentScope.findType( recordName ) != null )
			{
				this.error( this.currentToken(), "Record name '" + recordName + "' is already defined" );
				recordError = true;

				recordName = null;
			}

			this.readToken(); // read name
		}

		if ( "{".equals( this.currentToken().value ) )
		{
			this.readToken(); // read {
		}
		else
		{
			this.unexpectedTokenError( "{", this.currentToken() );

			return new BadRecordType( recordName, this.makeLocation( recordStartToken, this.peekPreviousToken() ) );
		}

		// Loop collecting fields
		List<Type> fieldTypes = new ArrayList<Type>();
		List<String> fieldNames = new ArrayList<String>();

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.here() ) )
		{
			boolean fieldError = false;
			boolean fieldSyntaxError = false;

			if ( this.atEndOfFile() )
			{
				if ( !recordSyntaxError )
				{
					this.unexpectedTokenError( "}", this.currentToken() );
				}
				recordError = recordSyntaxError = true;
				break;
			}

			if ( "}".equals( this.currentToken().value ) )
			{
				if ( fieldTypes.isEmpty() && !recordError )
				{
					this.error( this.currentToken(), "Record field(s) expected" );
					recordError = true;
				}

				this.readToken(); // read }
				break;
			}

			// Get the field type
			Type fieldType = this.parseType( parentScope, true );
			if ( fieldType == null )
			{
				if ( !fieldSyntaxError )
				{
					this.error( this.currentToken(), "Type name expected" );
				}
				recordError = recordSyntaxError = fieldError = fieldSyntaxError = true;
			}

			// Get the field name
			String fieldName = this.currentToken().value;
			if ( ";".equals( fieldName ) )
			{
				if ( !fieldSyntaxError )
				{
					this.error( this.currentToken(), "Field name expected" );
				}
				recordError = recordSyntaxError = fieldError = fieldSyntaxError = true;

				fieldName = null;
				// don't read
			}
			else if ( !this.parseIdentifier( fieldName ) )
			{
				if ( !fieldSyntaxError )
				{
					this.error( this.currentToken(), "Invalid field name '" + fieldName + "'" );
				}
				recordError = recordSyntaxError = fieldError = fieldSyntaxError = true;

				fieldName = null;
				// don't read
			}
			else if ( Parser.isReservedWord( fieldName ) )
			{
				if ( !fieldError )
				{
					this.error( this.currentToken(), "Reserved word '" + fieldName + "' cannot be used as a field name" );
				}
				recordError = fieldError = true;

				fieldName = null;
				this.readToken(); // read name
			}
			else if ( fieldNames.contains( fieldName ) )
			{
				if ( !fieldError )
				{
					this.error( this.currentToken(), "Field name '" + fieldName + "' is already defined" );
				}
				recordError = fieldError = true;

				fieldName = null;
				this.readToken(); // read name
			}
			else
			{
				this.readToken(); // read name
			}

			if ( ";".equals( this.currentToken().value ) )
			{
				this.readToken(); // read ;
			}
			else
			{
				if ( !fieldSyntaxError )
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}
				recordError = recordSyntaxError = fieldError = fieldSyntaxError = true;
			}

			if ( fieldType != null && fieldName != null )
			{
				fieldTypes.add( fieldType );
				fieldNames.add( fieldName.toLowerCase() );
			}
		}

		Location recordDefinition = this.makeLocation( recordStartToken, this.peekPreviousToken() );

		String[] fieldNameArray = new String[ fieldNames.size() ];
		Type[] fieldTypeArray = new Type[ fieldTypes.size() ];
		fieldNames.toArray( fieldNameArray );
		fieldTypes.toArray( fieldTypeArray );

		RecordType rec =
			new RecordType(
				recordName != null ? recordName :
					( "(anonymous record " + Integer.toHexString( Arrays.hashCode( fieldNameArray ) ) + ")" ),
				fieldNameArray, fieldTypeArray, recordDefinition );

		if ( recordName != null )
		{
			// Enter into type table
			parentScope.addType( rec );
		}

		return rec;
	}

	private Function parseFunction( final Type functionType, final Scope parentScope )
	{
		if ( !this.parseIdentifier( this.currentToken().value ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		Token functionNameToken = this.currentToken();
		String functionName = this.currentToken().value;

		boolean functionError = false;

		this.readToken(); //read Function name

		if ( Parser.isReservedWord( functionName ) )
		{
			this.error( functionNameToken, "Reserved word '" + functionName + "' cannot be used as a function name" );
			functionError = true;
		}

		this.readToken(); //read (

		VariableList paramList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		boolean vararg = false;

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.here() ) )
		{
			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( ")", this.currentToken() );
				functionError = true;
				break;
			}

			if ( ")".equals( this.currentToken().value ) )
			{
				this.readToken(); //read )
				break;
			}

			Token parameterTypeToken = this.currentToken();
			boolean parameterError = false;

			Type paramType = this.parseType( parentScope, false );
			if ( paramType == null )
			{
				if ( !parameterError )
				{
					this.unexpectedTokenError( ")", this.currentToken() );
				}
				functionError = parameterError = true;

				break;
			}

			if ( "...".equals( this.currentToken().value ) )
			{
				// Make a vararg type out of the previously parsed type.
				paramType = new VarArgType( paramType );
				paramType = new TypeReference( paramType, this.makeLocation( parameterTypeToken, this.currentToken() ) );

				this.readToken(); //read ...
			}

			Token paramNameToken = this.currentToken();

			Variable param = this.parseVariable( paramType, null );
			if ( param == null )
			{
				if ( !parameterError )
				{
					this.unexpectedTokenError( "identifier", this.currentToken() );
				}
				functionError = parameterError = true;

				continue;
			}

			if ( vararg )
			{
				if ( !functionError )
				{
					if ( paramType instanceof TypeReference &&
					     ((TypeReference) paramType).getTarget() instanceof VarArgType )
					{
						// We can only have a single vararg parameter
						this.error( paramType.getLocation(), "Only one vararg parameter is allowed" );
					}
					else
					{
						// The single vararg parameter must be the last one
						this.error( paramType.getLocation(), "The vararg parameter must be the last one" );
					}
					// The check uses functionError, but we set parameterError to true anyway.
					// This is not a mistake.
					functionError = parameterError = true;
				}
			}
			else if ( !paramList.add( param ) )
			{
				if ( !parameterError )
				{
					this.error( paramNameToken, "Variable " + param.getName() + " is already defined" );
				}
				functionError = parameterError = true;
			}
			else
			{
				variableReferences.add( new VariableReference( param ) );
			}

			if ( paramType instanceof TypeReference &&
			     ((TypeReference) paramType).getTarget() instanceof VarArgType )
			{
				// Only one vararg is allowed
				vararg = true;
			}

			if ( !")".equals( this.currentToken().value ) )
			{
				if ( ",".equals( this.currentToken().value ) )
				{
					this.readToken(); //read comma
				}
				else
				{
					if ( !parameterError )
					{
						this.unexpectedTokenError( ",", this.currentToken() );
					}
					functionError = parameterError = true;
				}
			}
		}

		// Add the function to the parent scope before we parse the
		// function scope to allow recursion.

		Location functionLocation = this.makeLocation( functionNameToken, this.peekLastToken() );

		UserDefinedFunction f = new UserDefinedFunction( functionName, functionType, functionLocation, variableReferences );

		if ( !functionError && f.overridesLibraryFunction() )
		{
			this.overridesLibraryFunctionError( functionLocation, f );
			functionError = true;
		}

		UserDefinedFunction existing = parentScope.findFunction( f );

		if ( !functionError && existing != null && existing.getScope() != null )
		{
			this.multiplyDefinedFunctionError( functionLocation, f );
			functionError = true;
		}

		if ( !functionError && vararg )
		{
			Function clash = parentScope.findVarargClash( f );

			if ( clash != null )
			{
				this.varargClashError( functionLocation, f, clash );
				functionError = true;
			}
		}

		// Add new function or replace existing forward reference

		UserDefinedFunction result = functionError ? f : parentScope.replaceFunction( existing, f );

		if ( ";".equals( this.currentToken().value ) )
		{
			// Return forward reference
			this.readToken(); // ;
			return result;
		}

		Scope scope = this.parseBlockOrSingleCommand( functionType, paramList, parentScope, false, false, false );

		result.setScope( scope );
		if ( !result.assertBarrier() && !functionType.equals( DataTypes.TYPE_VOID ) )
		{
			this.error( functionLocation, "Missing return value" );
		}

		return result;
	}

	private boolean parseVariables( final Type t, final BasicScope parentScope )
	{
		while ( true )
		{
			Variable v = this.parseVariable( t, parentScope );
			if ( v == null )
			{
				return false;
			}

			if ( ",".equals( this.currentToken().value ) )
			{
				this.readToken(); //read ,
				continue;
			}

			return true;
		}
	}

	private Variable parseVariable( final Type t, final BasicScope scope )
	{
		if ( !this.parseIdentifier( this.currentToken().value ) )
		{
			return null;
		}

		String variableName = this.currentToken().value;

		Position variableStart = this.here();
		Token variableToken = this.currentToken();
		boolean variableError = false;

		this.readToken(); // If parsing of Identifier succeeded, go to next token.

		Variable result;

		if ( Parser.isReservedWord( variableName ) )
		{
			this.error( variableToken, "Reserved word '" + variableName + "' cannot be a variable name" );
			result = new BadVariable( variableName, t, this.makeLocation( variableToken ) );
			variableError = true;
		}
		else if ( scope != null && scope.findVariable( variableName ) != null )
		{
			this.error( variableToken, "Variable " + variableName + " is already defined" );
			result = new BadVariable( variableName, t, this.makeLocation( variableToken ) );
			variableError = true;
		}
		else
		{
			result = new Variable( variableName, t, this.makeLocation( variableToken ) );
		}

		// If we are parsing a parameter declaration (if "scope" is null), we are done.
		// Otherwise, we must initialize the variable.

		Value rhs;

		Type ltype = t.getBaseType();
		if ( "=".equals( this.currentToken().value ) )
		{
			this.readToken(); // read =

			if ( !variableError && scope == null )
			{
				// this is a function's parameter declaration
				this.error( variableStart, "Cannot initialize parameter " + variableName );
				variableError = true;
			}

			if ( "{".equals( this.currentToken().value ) )
			{
				this.readToken(); // read {
				if ( !( ltype instanceof AggregateType ) )
				{
					if ( !variableError && !ltype.isBad() )
					{
						this.error( variableStart, "Cannot initialize " + variableName + " of type " + t + " with an aggregate literal" );
						variableError = true;
					}
					rhs = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );
				}
				else
				{
					rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
				}
			}
			else
			{
				rhs = this.parseExpression( scope );
			}

			if ( rhs == null )
			{
				if ( !variableError )
				{
					this.error( variableStart, "Expression expected" );
					variableError = true;
				}
			}
			else
			{
				rhs = this.autoCoerceValue( t, rhs, scope );
				if ( !variableError && !Operator.validCoercion( ltype, rhs.getType(), "assign" ) )
				{
					this.error( variableStart, "Cannot store " + rhs.getType() + " in " + variableName + " of type " + ltype );
					variableError = true;
				}
			}
		}
		else if ( "{".equals( this.currentToken().value ) )
		{
			this.readToken(); // read {

			if ( ltype instanceof AggregateType )
			{
				rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
			}
			else
			{
				if ( !variableError && !ltype.isBad() )
				{
					this.error( variableStart, "Cannot initialize " + variableName + " of type " + t + " with an aggregate literal" );
					variableError = true;
				}
				rhs = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );
			}
		}
		else
		{
			rhs = null;
		}

		if ( scope != null && !result.isBad() )
		{
			scope.addVariable( result );
			VariableReference lhs = new VariableReference( result );
			scope.addCommand( new Assignment( lhs, rhs ), this );
		}

		return result;
	}

	private Value autoCoerceValue( Type ltype, final Value rhs, final BasicScope scope )
	{
		// DataTypes.TYPE_ANY has no name
		if ( ltype == null || ltype.getName() == null )
		{
			return rhs;
		}

		// Error propagation
		if ( ltype.isBad() || rhs.getType().isBad() )
		{
			return Value.BAD_VALUE;
		}

		if ( ltype instanceof TypeReference )
		{
			ltype = ((TypeReference) ltype).getTarget();
		}

		// If the types are the same no coercion needed
		// A TypeDef or a RecordType match names for equal.
		Type rtype = rhs.getRawType();
		if ( ltype.equals( rtype ) )
		{
			return rhs;
		}

		// Look for a function:  LTYPE to_LTYPE( RTYPE )
		String name = "to_" + ltype.getName();
		List<Value> params = Collections.singletonList( rhs );

		// A typedef can overload a coercion function to a basic type or a typedef
		if ( ltype instanceof TypeDef || ltype instanceof RecordType )
		{
			Function target = scope.findFunction( name, params, MatchType.EXACT );
			if ( target != null && target.getType().equals( ltype ) )
			{
				return new FunctionCall( rhs.getLocation(), target, params, this );
			}
		}

		if ( ltype instanceof AggregateType )
		{
			return rhs;
		}

		if ( rtype instanceof TypeDef || rtype instanceof RecordType )
		{
			Function target = scope.findFunction( name, params, MatchType.EXACT );
			if ( target != null && target.getType().equals( ltype ) )
			{
				return new FunctionCall( rhs.getLocation(), target, params, this );
			}
		}

		// No overloaded coercions found for typedefs or records
		return rhs;
	}

	private List<Value> autoCoerceParameters( final Function target, final List<Value> params, final BasicScope scope )
	{
		ListIterator<VariableReference> refIterator = target.getVariableReferences().listIterator();
		ListIterator<Value> valIterator = params.listIterator();
		VariableReference vararg = null;
		VarArgType varargType = null;

		while ( ( vararg != null || refIterator.hasNext() ) && valIterator.hasNext() )
		{
			// A VarArg parameter will consume all remaining values
			VariableReference currentParam = ( vararg != null ) ? vararg : refIterator.next();
			Type paramType = currentParam.getRawType();

			// If have found a vararg, remember it.
			if ( vararg == null && paramType instanceof VarArgType )
			{
				vararg = currentParam;
				varargType = ((VarArgType) paramType);
			}

			// If we are matching a vararg, coerce to data type
			if ( vararg != null )
			{
				paramType = varargType.getDataType();
			}

			Value currentValue = valIterator.next();
			Value coercedValue = this.autoCoerceValue( paramType, currentValue, scope );
			valIterator.set( coercedValue );
		}

		return params;
	}

	private boolean parseTypedef( final Scope parentScope )
	{
		if ( !"typedef".equalsIgnoreCase( this.currentToken().value ) )
		{
			return false;
		}

		Token typedefToken = this.currentToken();
		boolean typedefError = false;

		this.readToken(); // read typedef

		Type t = this.parseType( parentScope, true );
		if ( t == null )
		{
			if ( !typedefError )
			{
				this.error( typedefToken, this.currentToken(), "Missing data type for typedef" );
				typedefError = true;
			}

			t = new BadType( null, null );
		}

		String typeName = this.currentToken().value;
		Token typeToken = this.currentToken();

		if ( ";".equals( typeName ) )
		{
			if ( !typedefError )
			{
				this.error( typeToken, "Type name expected" );
				typedefError = true;
			}

			typeName = null;
			// don't read
		}
		else if ( !this.parseIdentifier( typeName ) )
		{
			if ( !typedefError )
			{
				this.error( typeToken, "Invalid type name '" + typeName + "'" );
				typedefError = true;
			}

			typeName = null;
			// don't read
		}
		else if ( Parser.isReservedWord( typeName ) )
		{
			if ( !typedefError )
			{
				this.error( typeToken, "Reserved word '" + typeName + "' cannot be a type name" );
				typedefError = true;
			}

			typeName = null;
			this.readToken(); // read name
		}
		else
		{
			this.readToken(); // read name
		}

		Type existingType = parentScope.findType( typeName );
		if ( existingType != null )
		{
			if ( existingType.getBaseType().equals( t ) ||
			     existingType.isBad() )
			{
				// It is OK to redefine a typedef with an equivalent type
				return true;
			}

			if ( !typedefError )
			{
				this.error( typeToken, "Type name '" + typeName + "' is already defined" );
				typedefError = true;
			}
		}
		else if ( typeName != null )
		{
			// Add the type to the type table
			TypeDef type = new TypeDef( typeName, t, this.makeLocation( typedefToken, this.peekPreviousToken() ) );
			parentScope.addType( type );
		}

		return true;
	}

	private Command parseCommand( final Type functionType,
	                              final BasicScope scope,
	                              final boolean noElse,
	                              final boolean allowBreak,
	                              final boolean allowContinue )
	{
		Command result;

		if ( "break".equalsIgnoreCase( this.currentToken().value ) )
		{
			if ( allowBreak )
			{
				result = new LoopBreak( this.makeLocation( this.currentToken() ) );
			}
			else
			{
				this.error( this.currentToken(), "Encountered 'break' outside of loop" );

				result = new ScriptState.BadScriptState( this.makeLocation( this.currentToken() ) );
			}

			this.readToken(); //break
		}

		else if ( "continue".equalsIgnoreCase( this.currentToken().value ) )
		{
			if ( allowContinue )
			{
				result = new LoopContinue( this.makeLocation( this.currentToken() ) );
			}
			else
			{
				this.error( this.currentToken(), "Encountered 'continue' outside of loop" );

				result = new ScriptState.BadScriptState( this.makeLocation( this.currentToken() ) );
			}

			this.readToken(); //continue
		}

		else if ( "exit".equalsIgnoreCase( this.currentToken().value ) )
		{
			result = new ScriptExit( this.makeLocation( this.currentToken() ) );
			this.readToken(); //exit
		}

		else if ( ( result = this.parseReturn( functionType, scope ) ) != null )
		{
		}
		else if ( ( result = this.parseBasicScript() ) != null )
		{
			// basic_script doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseWhile( functionType, scope ) ) != null )
		{
			// while doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseForeach( functionType, scope ) ) != null )
		{
			// foreach doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseJavaFor( functionType, scope ) ) != null )
		{
			// for doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseFor( functionType, scope ) ) != null )
		{
			// for doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseRepeat( functionType, scope ) ) != null )
		{
		}
		else if ( ( result = this.parseSwitch( functionType, scope, allowContinue ) ) != null )
		{
			// switch doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseConditional( functionType, scope, noElse, allowBreak, allowContinue ) ) != null )
		{
			// loop doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseTry( functionType, scope, allowBreak, allowContinue ) ) != null )
		{
			// try doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseCatch( functionType, scope, allowBreak, allowContinue ) ) != null )
		{
			// standalone catch doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseStatic( functionType, scope ) ) != null )
		{
			// try doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseSort( scope ) ) != null )
		{
		}
		else if ( ( result = this.parseRemove( scope ) ) != null )
		{
		}
		else if ( ( result = this.parseBlock( functionType, null, scope, noElse, allowBreak, allowContinue ) ) != null )
		{
			// {} doesn't have a ; token
			return result;
		}
		else if ( ( result = this.parseValue( scope ) ) != null )
		{
		}
		else
		{
			return null;
		}

		if ( ";".equals( this.currentToken().value ) )
		{
			this.readToken(); // ;
		}
		else
		{
			this.unexpectedTokenError( ";", this.currentToken() );
		}

		return result;
	}

	private Type parseType( final BasicScope scope, final boolean records )
	{
		if ( !this.parseIdentifier( this.currentToken().value ) )
		{
			return null;
		}

		Type valType;

		if ( ( valType = this.parseRecord( scope ) ) != null )
		{
			if ( !records )
			{
				this.error( valType.getLocation(), "Record creation is not allowed here" );
			}
		}
		else if ( ( valType = scope.findType( this.currentToken().value ) ) != null )
		{
			valType = new TypeReference( valType, this.makeLocation( this.currentToken() ) );
			this.readToken();
		}
		// We can safely assume that two non-reserved identifiers in a row
		// are a type-variable pair.
		else if ( !Parser.isReservedWord( this.currentToken().value ) &&
		          this.parseIdentifier( this.nextToken() ) &&
		          !Parser.isReservedWord( this.nextToken() ) &&
		          //FIXME
		          // ... or we WOULD be able to safely assume it, had ASH not
		          // have 'foreach x in y' and 'for x from a to b' ...
		          // The best we can do is see if the next token is a variable
		          // reference, but we may need to just remove this... :(
		          scope.findVariable( this.nextToken(), true ) == null &&
		          // yup, we remove it. It's not enough, since 'call' is also possible.
		          // may become available again once we are able to get rid of the 'replaceToken' in 'parseValue'
		          false )
		{
			this.error( this.currentToken(), "Unknown type " + this.currentToken().value );

			valType = new BadType( this.currentToken().value, this.makeLocation( this.currentToken() ) );
			this.readToken();

			/* However, this only checks for single word (simple) types.
			   We'd need to also check for misspelled aggregate types,
			   but it is sadly not currently possible;
			   since ASH requires those to be declared with brackets,
			   we would need a way to look multiple tokens forward for
			   a combination of "[", ",", "]", numbers and/or identifiers,
			   and THEN make sure all that is followed by a valid identifier.
			   But we only have access to nextToken. :( */
		}
		else
		{
			return null;
		}

		if ( "[".equals( this.currentToken().value ) )
		{
			return this.parseAggregateType( valType, scope );
		}

		return valType;
	}

	private Value parseAggregateLiteral( final BasicScope scope, final AggregateType aggr )
	{
		Type index = aggr.getIndexType();
		Type data = aggr.getDataType();

		List<Value> keys = new ArrayList<Value>();
		List<Value> values = new ArrayList<Value>();

		Position aggregateStart = this.here();
		boolean aggregateError = false;

		// If index type is an int, it could be an array or a map
		boolean arrayAllowed = index.equals( DataTypes.INT_TYPE );

		// Assume it is a map.
		boolean isArray = false;

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.here() ) )
		{
			Value lhs;

			Position keyStart = this.here();

			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( "}", this.currentToken() );
				aggregateError = true;
				break;
			}

			if ( "}".equals( this.currentToken().value ) )
			{
				this.readToken(); // read }
				break;
			}

			// If we know we are reading an ArrayLiteral or haven't
			// yet ensured we are reading a MapLiteral, allow any
			// type of Value as the "key"
			Type dataType = data.getBaseType();

			if ( "{".equals( this.currentToken().value ) )
			{
				this.readToken(); // read {

				if ( !isArray && !arrayAllowed && !aggr.isBad() )
				{
					// We know this is a map, but they placed
					// an aggregate literal as a key
					if ( !aggregateError )
					{
						this.error( keyStart, "Expected a key of type " + index.toString() + ", found an aggregate" );
						aggregateError = true;
					}
				}

				if ( dataType instanceof AggregateType )
				{
					lhs = this.parseAggregateLiteral( scope, (AggregateType) dataType );
				}
				else
				{
					if ( !dataType.isBad() && !aggregateError )
					{
						this.error( keyStart, "Expected an element of type " + dataType.toString() + ", found an aggregate" );
						aggregateError = true;
					}

					lhs = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );
				}
			}
			else
			{
				lhs = this.parseExpression( scope );
			}

			if ( lhs == null )
			{
				if ( !aggregateError )
				{
					this.error( keyStart, "Script parsing error; couldn't figure out value of aggregage key" );
					aggregateError = true;
				}

				lhs = Value.BAD_VALUE;
			}

			String delim = this.currentToken().value;

			// If this could be an array and we haven't already
			// decided it is one, if the delimiter is a comma,
			// parse as an ArrayLiteral
			if ( arrayAllowed )
			{
				if ( ",".equals( delim ) || "}".equals( delim ) )
				{
					isArray = true;
				}
				arrayAllowed = false;
			}

			if ( !":".equals( delim ) )
			{
				// If parsing an ArrayLiteral, accumulate only values
				if ( isArray )
				{
					// The value must have the correct data type
					lhs = this.autoCoerceValue( data, lhs, scope );
					if ( !aggregateError && !Operator.validCoercion( dataType, lhs.getType(), "assign" ) )
					{
						this.error( keyStart, "Invalid array literal; cannot assign type " +
							dataType.toString() +
								" to type " +
							lhs.getType().toString() );
						aggregateError = true;
					}

					values.add( lhs );
				}
				else if ( !aggregateError )
				{
					this.unexpectedTokenError( ":", this.currentToken() );
					aggregateError = true;
				}

				// Move on to the next value
				if ( ",".equals( this.currentToken().value ) )
				{
					this.readToken(); // read ,
				}

				continue;
			}

			// We are parsing a MapLiteral
			this.readToken(); // read :

			if ( isArray && !aggregateError )
			{
				// not really correct since, to get here, what we got so far must have matched
				// the value's datatype, but we can't tell what they put after the :, so just assume
				// it's a key:value pair anyway
				this.error( keyStart, "cannot include keys when making an array literal" );
				aggregateError = true;
			}

			Value rhs;

			Position valueStart = this.here();

			if ( "{".equals( this.currentToken().value ) )
			{
				this.readToken(); // read {

				if ( dataType instanceof AggregateType )
				{
					rhs = this.parseAggregateLiteral( scope, (AggregateType) dataType );
				}
				else
				{
					if ( !dataType.isBad() && !aggregateError )
					{
						this.error( valueStart, "Expected a value of type " + dataType.toString() + ", found an aggregate" );
						aggregateError = true;
					}

					rhs = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );
				}
			}
			else
			{
				rhs = this.parseExpression( scope );
			}

			if ( rhs == null )
			{
				if ( !aggregateError )
				{
					this.error( keyStart, "Script parsing error; couldn't figure out value of aggregage value" );
					aggregateError = true;
				}

				rhs = Value.BAD_VALUE;
			}

			// Check that each type is valid via validCoercion
			lhs = this.autoCoerceValue( index, lhs, scope );
			rhs = this.autoCoerceValue( data, rhs, scope );
			if ( !aggregateError )
			{
				if ( !Operator.validCoercion( index, lhs.getType(), "assign" ) )
				{
					this.error( valueStart, "Invalid map literal; cannot assign type " +
							dataType.toString() +
							" to key of type " +
							lhs.getType().toString() );
					aggregateError = true;
				}

				if ( !Operator.validCoercion( data, rhs.getType(), "assign" ) )
				{
					this.error( valueStart, "Invalid map literal; cannot assign type " +
							dataType.toString() +
							" to value of type " +
							rhs.getType().toString() );
					aggregateError = true;
				}
			}

			keys.add( lhs );
			values.add( rhs );

			// Move on to the next value
			if ( ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // read ,
			}
		}

		if ( isArray )
		{
			int size = aggr.getSize();
			if ( !aggregateError && size > 0 && size < values.size() )
			{
				this.error( aggregateStart, "Array has " + size + " elements but " + values.size() + " initializers." );
				aggregateError = true;
			}
		}

		return aggregateError ? Value.BAD_VALUE :
		       isArray ? new ArrayLiteral( aggr, values ) :
		       new MapLiteral( aggr, keys, values );
	}

	private Type parseAggregateType( Type dataType, final BasicScope scope )
	{
		this.readToken(); // [ or ,

		Type indexType = null;
		boolean empty = false;
		int size = 0;

		Position typeStart = this.here();
		Token indexToken = this.currentToken();

		if ( this.readIntegerToken( this.currentToken().value ) )
		{
			size = StringUtilities.parseInt( this.currentToken().value );
			this.readToken(); // integer
		}
		else if ( this.parseIdentifier( this.currentToken().value ) )
		{
			String name = this.currentToken().value;
			this.readToken(); // type name

			indexType = scope.findType( name );

			if ( indexType != null )
			{
				indexType = new TypeReference( indexType, this.makeLocation( indexToken ) );

				if ( !indexType.isPrimitive() )
				{
					if ( !dataType.isBad() )
					{
						this.error( indexToken, "Index type '" + name + "' is not a primitive type" );
					}

					indexType = new BadType( name, this.makeLocation( indexToken ) );
				}
			}
			else
			{
				if ( !dataType.isBad() )
				{
					this.error( indexToken, "Invalid type name '" + name + "'" );
				}

				indexType = new BadType( name, this.makeLocation( indexToken ) );
			}
		}
		else if ( "]".equals( this.currentToken().value ) )
		{
			/* Technical debt: while it was probably only intended to
			   allow something in the likes of "string[]" , the way
			   it was implemented allowed syntaxes such as "int[int,int,]"
			   (note the trailing comma) while not allowing "int[,,]".

			   This means:
			   int [][][]
			   int [0][0][0]
			   int [][0][0]
			   int [][][0]
			   int [0][][0]
			   int [][0][]
			   int [0][0][]
			   int [0][][]
			   int [0,0,0]
			   int [0,0][0]
			   int [0][0,0]
			   int [0,][0]
			   int [0][0,]
			   int [][0,]
			   int [0,][]
			   int [0,0,]
			   are ALL perfectly valid equivalents
			   (any "0" can be swapped with "int"), but
			   int [,][]
			   int [,][0]
			   int [][,]
			   int [0][,]
			   are illegal and need to generate an error.

			   This means we can't just "assume nothing means 0".
			   We need a dedicated state preventing a comma from following.*/
			empty = true;
		}
		else
		{
			if ( !dataType.isBad() )
			{
				this.error( indexToken, "Missing index token" );
			}

			return new AggregateType( dataType, new BadType( null, null ) );
		}

		if ( ",".equals( this.currentToken().value ) ||
		     ( "]".equals( this.currentToken().value ) &&
		       "[".equals( this.nextToken() ) ) )
		{
			if ( "]".equals( this.currentToken().value ) )
			{
				this.readToken(); // ]
			}
			else if ( empty )
			{
				this.error( typeStart, "\"Nothing\" can only be used as the only and/or last index of a bracket group." );
			}

			dataType = this.parseAggregateType( dataType, scope );
		}
		else if ( "]".equals( this.currentToken().value ) )
		{
			this.readToken(); // ]
		}
		else
		{
			this.unexpectedTokenError( empty ? "]" : ", or ]", this.currentToken() );
		}

		if ( indexType != null )
		{
			return new AggregateType( dataType, indexType );
		}

		return new AggregateType( dataType, size );
	}

	private boolean parseIdentifier( final String identifier )
	{
		if ( identifier == null )
		{
			return false;
		}

		if ( !Character.isLetter( identifier.charAt( 0 ) ) && identifier.charAt( 0 ) != '_' )
		{
			return false;
		}

		for ( int i = 1; i < identifier.length(); ++i )
		{
			if ( !Character.isLetterOrDigit( identifier.charAt( i ) ) && identifier.charAt( i ) != '_' )
			{
				return false;
			}
		}

		return true;
	}

	private boolean parseScopedIdentifier( final String identifier )
	{
		if ( !Character.isLetter( identifier.charAt( 0 ) ) && identifier.charAt( 0 ) != '_' )
		{
			return false;
		}

		for ( int i = 1; i < identifier.length(); ++i )
		{
			if ( !Character.isLetterOrDigit( identifier.charAt( i ) ) && identifier.charAt( i ) != '_' && identifier.charAt( i ) != '@' )
			{
				return false;
			}
		}

		return true;
	}

	private FunctionReturn parseReturn( final Type expectedType, final BasicScope parentScope )
	{
		if ( !"return".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token returnStartToken = this.currentToken();
		Position returnStart = this.here();

		this.readToken(); //return

		if ( ";".equals( this.currentToken().value ) )
		{
			if ( expectedType == null || !expectedType.equals( DataTypes.TYPE_VOID ) )
			{
				this.error( returnStart, "Return needs " + expectedType + " value" );
			}

			return new FunctionReturn( this.makeLocation( returnStartToken ), null, DataTypes.VOID_TYPE );
		}

		if ( expectedType != null && expectedType.equals( DataTypes.TYPE_VOID ) )
		{
			this.error( returnStart, "Cannot return a value from a void function" );
		}

		Value value = this.parseExpression( parentScope );

		if ( value != null )
		{
			value = this.autoCoerceValue( expectedType, value, parentScope );
		}
		else
		{
			this.error( "Expression expected" );

			value = Value.BAD_VALUE;
		}

		if ( expectedType != null && !Operator.validCoercion( expectedType, value.getType(), "return" ) )
		{
			this.error( returnStart, "Cannot return " + value.getType() + " value from " + expectedType + " function" );
		}

		Location returnLocation = this.makeLocation( returnStartToken, this.peekPreviousToken() );
		return new FunctionReturn( returnLocation, value, expectedType );
	}

	private Scope parseSingleCommandScope( final Type functionType,
	                                       final BasicScope parentScope,
	                                       final boolean noElse,
	                                       final boolean allowBreak,
	                                       final boolean allowContinue )
	{
		Command command = this.parseCommand( functionType, parentScope, noElse, allowBreak, allowContinue );
		if ( command == null )
		{
			if ( ";".equals( this.currentToken().value ) )
			{
				this.readToken(); // ;
			}
			else
			{
				this.unexpectedTokenError( ";", this.currentToken() );
			}

			Scope result = new Scope( parentScope );
			result.setScopeLocation( this.make0WidthLocation() );

			return result;
		}
		return new Scope( command, parentScope );
	}

	private Scope parseBlockOrSingleCommand( final Type functionType,
	                                         final VariableList variables,
	                                         final BasicScope parentScope,
	                                         final boolean noElse,
	                                         final boolean allowBreak,
	                                         final boolean allowContinue )
	{
		Scope scope = this.parseBlock( functionType, variables, parentScope, noElse, allowBreak, allowContinue );
		if ( scope != null )
		{
			return scope;
		}
		return this.parseSingleCommandScope( functionType, parentScope, noElse, allowBreak, allowContinue );
	}

	private Scope parseBlock( final Type functionType,
	                          final VariableList variables,
	                          final BasicScope parentScope,
	                          final boolean noElse,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
	{
		if ( !"{".equals( this.currentToken().value ) )
		{
			return null;
		}

		Token blockStartToken = this.currentToken();

		this.readToken(); // {

		Scope scope = this.parseScope( functionType, variables, parentScope, allowBreak, allowContinue );

		if ( "}".equals( this.currentToken().value ) )
		{
			this.readToken(); //read }
		}
		else
		{
			this.unexpectedTokenError( "}", this.currentToken() );
		}

		Location blockLocation = this.makeLocation( blockStartToken, this.peekPreviousToken() );
		scope.setScopeLocation( blockLocation );

		return scope;
	}

	private Conditional parseConditional( final Type functionType,
	                                      final BasicScope parentScope,
	                                      final boolean noElse,
	                                      final boolean allowBreak,
	                                      final boolean allowContinue )
	{
		if ( !"if".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token conditionalStartToken = this.currentToken();

		this.readToken(); // if

		boolean ifError = false;

		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (
		}
		else if ( !ifError )
		{
			this.unexpectedTokenError( "(", this.currentToken() );
			ifError = true;
		}

		Position conditionStart = this.here();

		Value condition = this.parseExpression( parentScope );

		Location conditionLocation = this.makeLocation( conditionStart );

		if ( ")".equals( this.currentToken().value ) )
		{
			this.readToken(); // )
		}
		else if ( !ifError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			ifError = true;
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			if ( !ifError )
			{
				this.error( conditionLocation, "\"if\" requires a boolean conditional expression" );
				ifError = true;
			}

			condition = Value.BAD_VALUE;
		}

		If result = null;
		boolean elseFound = false;
		boolean finalElse = false;

		do
		{
			boolean elseError = false;

			Scope scope = parseBlockOrSingleCommand( functionType, null, parentScope, !elseFound, allowBreak, allowContinue );

			Location conditionalLocation = this.makeLocation( conditionalStartToken, this.peekPreviousToken() );

			if ( result == null )
			{
				result = new If( conditionalLocation, scope, condition );
			}
			else if ( finalElse )
			{
				result.addElseLoop( new Else( conditionalLocation, scope, condition ) );
			}
			else
			{
				result.addElseLoop( new ElseIf( conditionalLocation, scope, condition ) );
			}

			if ( !noElse && "else".equalsIgnoreCase( this.currentToken().value ) )
			{
				conditionalStartToken = this.currentToken();

				if ( finalElse && !elseError )
				{
					this.error( "Else without if" );
					elseError = true;
				}

				if ( "if".equalsIgnoreCase( this.nextToken() ) )
				{
					this.readToken(); //else
					this.readToken(); //if

					if ( "(".equals( this.currentToken().value ) )
					{
						this.readToken(); //(
					}
					else if ( !elseError )
					{
						this.unexpectedTokenError( "(", this.currentToken() );
						elseError = true;
					}

					conditionStart = this.here();

					condition = this.parseExpression( parentScope );

					conditionLocation = this.makeLocation( conditionStart );

					if ( ")".equals( this.currentToken().value ) )
					{
						this.readToken(); // )
					}
					else if ( !elseError )
					{
						this.unexpectedTokenError( ")", this.currentToken() );
						elseError = true;
					}

					if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
					{
						if ( !elseError )
						{
							this.error( conditionLocation, "\"if\" requires a boolean conditional expression" );
							elseError = true;
						}

						condition = Value.BAD_VALUE;
					}
				}
				else
				//else without condition
				{
					this.readToken(); //else
					condition = DataTypes.TRUE_VALUE;
					finalElse = true;
				}

				elseFound = true;
				continue;
			}

			elseFound = false;
		}
		while ( elseFound );

		return result;
	}

	private BasicScript parseBasicScript()
	{
		if ( !"cli_execute".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		if ( !"{".equals( this.nextToken() ) )
		{
			return null;
		}

		Token basicScriptStartToken = this.currentToken();

		this.readToken(); // cli_execute
		this.readToken(); // {

		ByteArrayStream ostream = new ByteArrayStream();

		while ( true )
		{
			if ( "}".equals( this.currentToken().value ) )
			{
				this.readToken(); // }
				break;
			}

			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( "}", this.currentToken() );
				break;
			}

			this.currentToken = null;
			this.currentLine.tokens.removeLast();

			final String line = this.restOfLine();

			try
			{
				ostream.write( line.getBytes() );
				ostream.write( KoLConstants.LINE_BREAK.getBytes() );
			}
			catch ( Exception e )
			{
				// Byte array output streams do not throw errors,
				// other than out of memory errors.

				StaticEntity.printStackTrace( e );
			}

			if ( line.length() > 0 )
			{
				this.currentLine.makeToken( line.length() );
			}
			this.currentLine = this.currentLine.nextLine;
			this.currentIndex = this.currentLine.offset;
		}

		Location basicScriptLocation = this.makeLocation( basicScriptStartToken, this.peekPreviousToken() );
		return new BasicScript( basicScriptLocation, ostream );
	}

	private Loop parseWhile( final Type functionType, final BasicScope parentScope )
	{
		if ( !"while".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token whileStartToken = this.currentToken();

		this.readToken(); // while

		boolean whileError = false;

		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (
		}
		else if ( !whileError )
		{
			this.unexpectedTokenError( "(", this.currentToken() );
			whileError = true;
		}

		Position conditionStart = this.here();

		Value condition = this.parseExpression( parentScope );

		Location conditionLocation = this.makeLocation( conditionStart );

		if ( ")".equals( this.currentToken().value ) )
		{
			this.readToken(); // )
		}
		else if ( !whileError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			whileError = true;
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			if ( !whileError )
			{
				this.error( conditionLocation, "\"while\" requires a boolean conditional expression" );
				whileError = true;
			}

			condition = Value.BAD_VALUE;
		}

		Scope scope = this.parseLoopScope( functionType, null, parentScope );

		Location whileLocation = this.makeLocation( whileStartToken, this.peekPreviousToken() );
		return new WhileLoop( whileLocation, scope, condition );
	}

	private Loop parseRepeat( final Type functionType, final BasicScope parentScope )
	{
		if ( !"repeat".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token repeatStartToken = this.currentToken();

		this.readToken(); // repeat

		boolean repeatError = false;

		Scope scope = this.parseLoopScope( functionType, null, parentScope );

		if ( "until".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken(); // until
		}
		else if ( !repeatError )
		{
			this.unexpectedTokenError( "until", this.currentToken() );
			repeatError = true;
		}

		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (
		}
		else if ( !repeatError )
		{
			this.unexpectedTokenError( "(", this.currentToken() );
			repeatError = true;
		}

		Position conditionStart = this.here();

		Value condition = this.parseExpression( parentScope );

		Location conditionLocation = this.makeLocation( conditionStart );

		if ( ")".equals( this.currentToken().value ) )
		{
			this.readToken(); // )
		}
		else if ( !repeatError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			repeatError = true;
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			if ( !repeatError )
			{
				this.error( conditionLocation, "\"repeat\" requires a boolean conditional expression" );
				repeatError = true;
			}

			condition = Value.BAD_VALUE;
		}

		Location repeatLocation = this.makeLocation( repeatStartToken, this.peekPreviousToken() );
		return new RepeatUntilLoop( repeatLocation, scope, condition );
	}

	private Switch parseSwitch( final Type functionType, final BasicScope parentScope, final boolean allowContinue )
	{
		if ( !"switch".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token switchStartToken = this.currentToken();

		this.readToken(); // switch

		boolean switchError = false;

		if ( !"(".equals( this.currentToken().value ) && !"{".equals( this.currentToken().value ) )
		{
			this.unexpectedTokenError( "( or {", this.currentToken() );
			switchError = true;
		}

		Value condition = DataTypes.TRUE_VALUE;
		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (

			Position conditionStart = this.here();

			condition = this.parseExpression( parentScope );

			Location conditionLocation = this.makeLocation( conditionStart );

			if ( ")".equals( this.currentToken().value ) )
			{
				this.readToken(); // )
			}
			else if ( !switchError )
			{
				this.unexpectedTokenError( ")", this.currentToken() );
				switchError = true;
			}

			if ( condition == null )
			{
				if ( !switchError )
				{
					this.error( conditionLocation, "\"switch ()\" requires an expression" );
					switchError = true;
				}

				condition = Value.BAD_VALUE;
			}
		}

		Type type = condition.getType();

		Token switchScopeStartToken = this.currentToken();

		if ( "{".equals( this.currentToken().value ) )
		{
			this.readToken(); // {
		}
		else if ( !switchError )
		{
			this.unexpectedTokenError( "{", this.currentToken() );
			switchError = true;
		}

		List<Value> tests = new ArrayList<Value>();
		List<Integer> indices = new ArrayList<Integer>();
		int defaultIndex = -1;

		SwitchScope scope = new SwitchScope( parentScope );
		int currentIndex = 0;
		Integer currentInteger = null;

		Map<Value, Integer> labels = new TreeMap<>();
		boolean constantLabels = true;

		while ( true )
		{
			boolean caseError = false;

			if ( "case".equalsIgnoreCase( this.currentToken().value ) )
			{
				this.readToken(); // case

				Position testStart = this.here();

				Value test = this.parseExpression( parentScope );

				Location testLocation = this.makeLocation( testStart );

				if ( test == null )
				{
					if ( !caseError )
					{
						this.error( testLocation, "Case label needs to be followed by an expression" );
					}
					switchError = caseError = true;

					test = Value.BAD_VALUE;
				}

				if ( ":".equals( this.currentToken().value ) )
				{
					this.readToken(); // :
				}
				else
				{
					if ( !caseError )
					{
						this.unexpectedTokenError( ":", this.currentToken() );
					}
					switchError = caseError = true;
				}

				if ( !test.getType().equals( type ) &&
				     !type.isBad() &&
				     !test.getType().isBad() )
				{
					if ( !caseError )
					{
						this.error( testLocation, "Switch conditional has type " + type + " but label expression has type " + test.getType() );
					}
					switchError = caseError = true;

					test = Value.BAD_VALUE;
				}

				if ( currentInteger == null )
				{
					currentInteger = IntegerPool.get( currentIndex );
				}

				if ( test.getClass() == Value.class )
				{
					if ( labels.get( test ) != null )
					{
						if ( !caseError )
						{
							this.error( testLocation, "Duplicate case label: " + test );
						}
						switchError = caseError = true;
					}
					else if ( test != Value.BAD_VALUE )
					{
						labels.put( test, currentInteger );
					}
				}
				else if ( test != Value.BAD_VALUE ) // just in case we make BAD_<x> their own class (which may happen)
				{
					constantLabels = false;
				}


				tests.add( test );
				indices.add( currentInteger );
				scope.resetBarrier();

				continue;
			}

			if ( "default".equalsIgnoreCase( this.currentToken().value ) )
			{
				this.readToken(); // default

				if ( ":".equals( this.currentToken().value ) )
				{
					this.readToken(); // :
				}
				else
				{
					if ( !caseError )
					{
						this.unexpectedTokenError( ":", this.currentToken() );
					}
					switchError = caseError = true;
				}

				if ( defaultIndex == -1 )
				{
					defaultIndex = currentIndex;
				}
				else
				{
					if ( !caseError )
					{
						this.error( "Only one default label allowed in a switch statement" );
					}
					switchError = caseError = true;
				}

				scope.resetBarrier();

				continue;
			}

			Type t = this.parseType( scope, true );

			// If there is no data type, it's a command of some sort
			if ( t == null )
			{
				// See if it's a regular command
				Command c = this.parseCommand( functionType, scope, false, true, allowContinue );
				if ( c != null )
				{
					scope.addCommand( c, this );
					currentIndex = scope.commandCount();
					currentInteger = null;
					continue;
				}

				// No type and no command -> done.
				break;
			}

			if ( !this.parseVariables( t, scope ) )
			{
				//Found a type but no function or variable to tie it to
				if ( !caseError )
				{
					this.error( "Type given but not used to declare anything" );
				}
				switchError = caseError = true;
			}

			if ( ";".equals( this.currentToken().value ) )
			{
				this.readToken(); //read ;
			}
			else
			{
				if ( !caseError )
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}
				switchError = caseError = true;
			}

			currentIndex = scope.commandCount();
			currentInteger = null;
		}

		if ( "}".equals( this.currentToken().value ) )
		{
			this.readToken(); // }
		}
		else if ( !switchError )
		{
			this.unexpectedTokenError( "}", this.currentToken() );
			switchError = true;
		}

		Location switchLocation = this.makeLocation( switchStartToken, this.peekPreviousToken() );
		Location switchScopeLocation = this.makeLocation( switchScopeStartToken, this.peekPreviousToken() );

		scope.setScopeLocation( switchScopeLocation );

		return new Switch( switchLocation, condition, tests, indices, defaultIndex, scope,
		                   constantLabels ? labels : null );
	}

	private Try parseTry( final Type functionType, final BasicScope parentScope,
	                      final boolean allowBreak, final boolean allowContinue )
	{
		if ( !"try".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token tryStartToken = this.currentToken();

		this.readToken(); // try

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		// catch clauses would be parsed here

		Scope finalClause;

		if ( "finally".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken(); // finally

			finalClause = this.parseBlockOrSingleCommand( functionType, null, body, false, allowBreak, allowContinue );
		}
		else
		{
			// this would not be an error if at least one catch was present
			this.error( "\"try\" without \"finally\" is pointless" );
			finalClause = new Scope( body );
		}

		Location tryLocation = this.makeLocation( tryStartToken, this.peekPreviousToken() );
		return new Try( tryLocation, body, finalClause );
	}

	private Catch parseCatch( final Type functionType, final BasicScope parentScope,
	                          final boolean allowBreak, final boolean allowContinue )
	{
		if ( !"catch".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token catchStartToken = this.currentToken();

		this.readToken(); // catch

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		Location catchLocation = this.makeLocation( catchStartToken, this.peekPreviousToken() );
		return new Catch( catchLocation, body );
	}

	private Catch parseCatchValue( final BasicScope parentScope )
	{
		if ( !"catch".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token catchStartToken = this.currentToken();

		this.readToken(); // catch

		Command body;
		if ( ( body = this.parseBlock( null, null, parentScope, true, false, false ) ) != null )
		{
		}
		else if ( ( body = this.parseExpression( parentScope ) ) != null )
		{
		}
		else
		{
			this.error( "\"catch\" requires a block or an expression" );
			body = Value.BAD_VALUE;
		}

		Location catchLocation = this.makeLocation( catchStartToken, this.peekPreviousToken() );
		return new Catch( catchLocation, body );
	}

	private Scope parseStatic( final Type functionType, final BasicScope parentScope )
	{
		if ( !"static".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token staticStartToken = this.currentToken();

		this.readToken(); // static

		Scope result = new StaticScope( parentScope );

		if ( !"{".equals( this.currentToken().value ) )	// body is a single call
		{
			return this.parseCommandOrDeclaration( result, functionType );
		}

		this.readToken(); //read {

		this.parseScope( result, functionType, parentScope, false, false, false );

		if ( "}".equals( this.currentToken().value ) )
		{
			this.readToken(); //read }
		}
		else
		{
			this.unexpectedTokenError( "}", this.currentToken() );
		}

		Location staticLocation = this.makeLocation( staticStartToken, this.peekPreviousToken() );
		result.setScopeLocation( staticLocation );

		return result;
	}

	private SortBy parseSort( final BasicScope parentScope )
	{
		// sort aggregate by expr

		if ( !"sort".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		if ( this.nextToken() == null ||
		     "(".equals( this.nextToken() ) ||
		     "=".equals( this.nextToken() ) )
		{	// it's a call to a function named sort(), or an assigment to
			// a variable named sort, not the sort statement.
			return null;
		}

		Token sortStartToken = this.currentToken();

		this.readToken(); // sort

		Position aggregateStart = this.here();

		// Get an aggregate reference
		Value aggregate = this.parseVariableReference( parentScope );
		AggregateType type;

		if ( !( aggregate instanceof VariableReference ) ||
		     !( aggregate.getType().getBaseType() instanceof AggregateType ) )
		{
			if ( aggregate == null || !aggregate.getType().isBad() )
			{
				this.error( aggregateStart, "Aggregate reference expected" );
			}

			String name = null;
			Location location = null;
			if ( aggregate instanceof VariableReference )
			{
				name = ((VariableReference) aggregate).target.getName();
				location = ((VariableReference) aggregate).target.getLocation();
			}

			aggregate = new VariableReference( new BadVariable( name, new BadAggregateType( null ), location ) );
		}

		type = (AggregateType) aggregate.getType().getBaseType();

		if ( "by".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken();	// by
		}
		else
		{
			this.unexpectedTokenError( "by", this.currentToken() );
		}

		// Define key variables of appropriate type
		VariableList varList = new VariableList();
		Variable valuevar = new Variable( "value", type.getDataType(), this.make0WidthLocation() );
		varList.add( valuevar );
		Variable indexvar = new Variable( "index", type.getIndexType(), this.make0WidthLocation() );
		varList.add( indexvar );

		// Parse the key expression in a new scope containing 'index' and 'value'
		Scope scope = new Scope( varList, parentScope );
		Value expr = this.parseExpression( scope );

		Location sortLocation = this.makeLocation( sortStartToken, this.peekPreviousToken() );
		return new SortBy( sortLocation, (VariableReference) aggregate, indexvar, valuevar, expr, this );
	}

	private Loop parseForeach( final Type functionType, final BasicScope parentScope )
	{
		// foreach key [, key ... ] in aggregate { scope }

		if ( !"foreach".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token foreachStartToken = this.currentToken();

		this.readToken(); // foreach

		List<String> names = new ArrayList<String>();
		List<Location> locations = new ArrayList<>();

		while ( true )
		{
			String name = this.currentToken().value;

			Token nameToken = this.currentToken();

			if ( !this.parseIdentifier( name ) ||
			     // foreach in aggregate (i.e. no key)
			     "in".equalsIgnoreCase( this.currentToken().value ) &&
			     !"in".equalsIgnoreCase( this.nextToken() ) &&
			     !",".equals( this.nextToken() ) )
			{
				this.error( nameToken, "Key variable name expected" );

				if ( ",".equals( this.currentToken().value ) )
				{
					this.readToken(); // ,
					continue;
				}

				this.readToken(); // unknown
				break;
			}
			else if ( Parser.isReservedWord( name ) )
			{
				this.error( nameToken, "Reserved word '" + name + "' cannot be a key variable name" );
				names.add( null );
				locations.add( null );
			}
			else if ( names.contains( name ) )
			{
				this.error( nameToken, "Key variable '" + name + "' is already defined" );
				names.add( null );
				locations.add( null );
			}
			else
			{
				names.add( name );
				locations.add( this.makeLocation( nameToken ) );
			}

			this.readToken(); // name

			if ( ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // ,
				continue;
			}

			if ( "in".equalsIgnoreCase( this.currentToken().value ) )
			{
				this.readToken(); // in
				break;
			}

			this.unexpectedTokenError( "in", this.currentToken() );
			break;
		}

		// Get an aggregate reference
		Position aggregateStart = this.here();
		Value aggregate = this.parseValue( parentScope );

		if ( aggregate == null || !( aggregate.getType().getBaseType() instanceof AggregateType ) )
		{
			this.error( aggregateStart, "Aggregate reference expected" );

			aggregate = Value.BAD_VALUE;
		}

		// Define key variables of appropriate type
		VariableList varList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		Type type = aggregate.getType().getBaseType();

		for ( int i = 0; i < names.size(); i++ )
		{
			String name = names.get( i );
			Location location = locations.get( i );

			Type itype;
			if ( type == null )
			{
				this.error( location, "Too many key variables specified" );

				break;
			}

			if ( type instanceof AggregateType )
			{
				itype = ( (AggregateType) type ).getIndexType();
				type = ( (AggregateType) type ).getDataType();
			}
			else if ( type.isBad() )
			{
				itype = new BadType( null, null );
			}
			else
			{	// Variable after all key vars holds the value instead
				itype = type;
				type = null;
			}

			if ( name != null )
			{
				Variable keyvar = new Variable( name, itype, location );
				varList.add( keyvar );
				variableReferences.add( new VariableReference( keyvar ) );
			}
		}

		// Parse the scope with the list of keyVars
		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		Location foreachLocation = this.makeLocation( foreachStartToken, this.peekPreviousToken() );

		// Add the foreach node with the list of varRefs
		return new ForEachLoop( foreachLocation, scope, variableReferences, aggregate, this );
	}

	private Loop parseFor( final Type functionType, final BasicScope parentScope )
	{
		// for identifier from X [upto|downto|to|] Y [by Z]? {scope }

		if ( !"for".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		if ( !this.parseIdentifier( this.nextToken() ) )
		{
			return null;
		}

		Token forStartToken = this.currentToken();

		this.readToken(); // for

		String name = this.currentToken().value;
		Token nameToken = this.currentToken();

		boolean forError = false, forSyntaxError = false;

		Variable indexvar;

		if ( Parser.isReservedWord( name ) )
		{
			if ( !forError )
			{
				this.error( nameToken, "Reserved word '" + name + "' cannot be an index variable name" );
				forError = true;
			}

			indexvar = new BadVariable( name, DataTypes.INT_TYPE, this.makeLocation( nameToken ) );
		}
		else if ( parentScope.findVariable( name ) != null )
		{
			if ( !forError )
			{
				this.error( nameToken, "Index variable '" + name + "' is already defined" );
				forError = true;
			}

			indexvar = new BadVariable( name, DataTypes.INT_TYPE, this.makeLocation( nameToken ) );
		}
		else
		{
			indexvar = new Variable( name, DataTypes.INT_TYPE, this.makeLocation( nameToken ) );
		}

		this.readToken(); // name

		if ( "from".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken(); // from
		}
		else
		{
			if ( !forSyntaxError )
			{
				this.unexpectedTokenError( "from", this.currentToken() );
			}
			forError = forSyntaxError = true;
		}

		Value initial = this.parseExpression( parentScope );

		if ( initial == null )
		{
			if ( !forSyntaxError )
			{
				this.error( "Expression for initial value expected" );
			}
			forError = forSyntaxError = true;

			initial = Value.BAD_VALUE;
		}

		int direction = 0;

		if ( "upto".equalsIgnoreCase( this.currentToken().value ) )
		{
			direction = 1;
			this.readToken(); // upto
		}
		else if ( "downto".equalsIgnoreCase( this.currentToken().value ) )
		{
			direction = -1;
			this.readToken(); // downto
		}
		else if ( "to".equalsIgnoreCase( this.currentToken().value ) )
		{
			direction = 0;
			this.readToken(); // to
		}
		else
		{
			if ( !forSyntaxError )
			{
				this.unexpectedTokenError( "to, upto, or downto", this.currentToken() );
			}
			forError = forSyntaxError = true;
		}

		Value last = this.parseExpression( parentScope );

		if ( last == null )
		{
			if ( !forSyntaxError )
			{
				this.error( "Expression for floor/ceiling value expected" );
			}
			forError = forSyntaxError = true;

			last = Value.BAD_VALUE;
		}

		Value increment = DataTypes.ONE_VALUE;
		if ( "by".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken(); // by
			increment = this.parseExpression( parentScope );

			if ( increment == null )
			{
				if ( !forSyntaxError )
				{
					this.error( "Expression for increment value expected" );
				}
				forError = forSyntaxError = true;

				increment = Value.BAD_VALUE;
			}
		}

		// Put index variable onto a list
		VariableList varList = new VariableList();
		varList.add( indexvar );

		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		Location forLocation = this.makeLocation( forStartToken, this.peekPreviousToken() );
		return new ForLoop( forLocation, scope, new VariableReference( indexvar ), initial, last, increment, direction, this );
	}

	private Loop parseJavaFor( final Type functionType, final BasicScope parentScope )
	{
		if ( !"for".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		Token javaForStartToken = this.currentToken();

		this.readToken(); // for
		this.readToken(); // (

		Token loopScopeStartToken = this.currentToken();

		// Parse variables and initializers

		Scope scope = new Scope( parentScope );
		List<Assignment> initializers = new ArrayList<Assignment>();

		boolean javaForError = false, javaForSyntaxError = false;

		// Parse each initializer in the context of scope, adding
		// variable to variable list in the scope, and saving
		// initialization expressions in initializers.

		while ( !";".equals( this.currentToken().value ) )
		{
			Type t = this.parseType( scope, true );

			String name = this.currentToken().value;
			Token nameToken = this.currentToken();

			if ( !this.parseIdentifier( name ) || Parser.isReservedWord( name ) )
			{
				if ( !javaForSyntaxError )
				{
					this.error( nameToken, "Identifier required" );
				}
				javaForError = javaForSyntaxError = true;

				name = null;
			}

			VariableReference lhs;

			// If there is no data type, it is using an existing variable
			if ( t == null )
			{
				Variable variable = parentScope.findVariable( name );
				if ( variable != null )
				{
					lhs = new VariableReference( variable, this.makeLocation( nameToken ) );
				}
				else
				{
					if ( !javaForError )
					{
						this.error( nameToken, "Unknown variable '" + name + "'" );
						javaForError = true;
					}

					variable = new BadVariable( name, new BadType( null, null ), this.makeLocation( nameToken ) );
					lhs = new VariableReference( variable );
				}

				t = variable.getType();
			}
			else
			{
				// Create variable and add it to the scope
				Variable variable = scope.findVariable( name, true );
				if ( variable == null )
				{
					variable = new Variable( name, t, this.makeLocation( nameToken ) );

					scope.addVariable( variable );
					lhs = new VariableReference( variable );
				}
				else
				{
					if ( !javaForError )
					{
						this.error( nameToken, "Variable '" + name + "' already defined" );
						javaForError = true;
					}

					lhs = new VariableReference( variable, this.makeLocation( nameToken ) );
				}
			}

			this.readToken(); // name

			Value rhs = null;

			if ( "=".equals( this.currentToken().value ) )
			{
				this.readToken(); // =

				rhs = this.parseExpression( scope );

				if ( rhs == null )
				{
					if ( !javaForSyntaxError )
					{
						this.error( "Expression expected" );
					}
					javaForError = javaForSyntaxError = true;

					rhs = Value.BAD_VALUE;
				}

				Type ltype = t.getBaseType();
				rhs = this.autoCoerceValue( t, rhs, scope );
				Type rtype = rhs.getType();

				if ( !Operator.validCoercion( ltype, rtype, "assign" ) )
				{
					if ( !javaForError )
					{
						this.error( "Cannot store " + rtype + " in " + name + " of type " + ltype );
						javaForError = true;
					}

					rhs = Value.BAD_VALUE;
				}

			}

			Assignment initializer = new Assignment( lhs, rhs );

			initializers.add( initializer );

			if ( ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // ,

				if ( ";".equals( this.currentToken().value ) )
				{
					if ( !javaForSyntaxError )
					{
						this.error( "Identifier expected" );
					}
					javaForError = javaForSyntaxError = true;
				}
			}
		}

		if ( ";".equals( this.currentToken().value ) )
		{
			this.readToken(); // ;
		}
		else
		{
			if ( !javaForSyntaxError )
			{
				this.unexpectedTokenError( ";", this.currentToken() );
			}
			javaForError = javaForSyntaxError = true;
		}

		// Parse condition in context of scope

		Value condition =
			( ";".equals( this.currentToken().value ) ) ?
			DataTypes.TRUE_VALUE : this.parseExpression( scope );

		if ( ";".equals( this.currentToken().value ) )
		{
			this.readToken(); // ;
		}
		else
		{
			if ( !javaForSyntaxError )
			{
				this.unexpectedTokenError( ";", this.currentToken() );
			}
			javaForError = javaForSyntaxError = true;
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE && !condition.getType().isBad() )
		{
			if ( !javaForError )
			{
				this.error( "\"for\" requires a boolean conditional expression" );
				javaForError = true;
			}

			condition = Value.BAD_VALUE;
		}

		// Parse incrementers in context of scope

		List<Command> incrementers = new ArrayList<>();

		while ( !this.atEndOfFile() && !")".equals( this.currentToken().value ) )
		{
			Value value = this.parsePreIncDec( scope );
			if ( value != null )
			{
				incrementers.add( value );
			}
			else
			{
				value = this.parseVariableReference( scope );
				if ( !( value instanceof VariableReference ) )
				{
					if ( !javaForSyntaxError )
					{
						this.error( "Variable reference expected" );
					}
					javaForError = javaForSyntaxError = true;

					value = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) );
				}

				VariableReference ref = (VariableReference) value;
				Value lhs = this.parsePostIncDec( ref );

				if ( lhs == ref )
				{
					Assignment incrementer = this.parseAssignment( scope, ref );

					if ( incrementer != null )
					{
						incrementers.add( incrementer );
					}
					else if ( !javaForError )
					{
						this.error( "Variable '" + ref.getName() + "' not incremented" );
						javaForError = true;
					}
				}
				else
				{
					incrementers.add( lhs );
				}
			}

			if ( ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // ,

				if ( this.atEndOfFile() || ")".equals( this.currentToken().value ) )
				{
					if ( !javaForSyntaxError )
					{
						this.error( "Identifier expected" );
					}
					javaForError = javaForSyntaxError = true;
				}
			}
		}

		if ( ")".equals( this.currentToken().value ) )
		{
			this.readToken(); // )
		}
		else
		{
			if ( !javaForSyntaxError )
			{
				this.unexpectedTokenError( ")", this.currentToken() );
			}
			javaForError = javaForSyntaxError = true;
		}

		// Parse scope body
		this.parseLoopScope( scope, functionType, parentScope );

		Location loopScopeLocation = this.makeLocation( loopScopeStartToken, this.peekPreviousToken() );
		Location javaForLocation = this.makeLocation( javaForStartToken, this.peekPreviousToken() );

		scope.setScopeLocation( loopScopeLocation );

		return new JavaForLoop( javaForLocation, scope, initializers, condition, incrementers );
	}

	private Scope parseLoopScope( final Type functionType, final VariableList varList, final BasicScope parentScope )
	{
		Scope result = new Scope( varList, parentScope );

		Token loopScopeStartToken = this.currentToken();

		this.parseLoopScope( result, functionType, parentScope );

		Location loopScopeLocation = this.makeLocation( loopScopeStartToken, this.peekPreviousToken() );
		result.setScopeLocation( loopScopeLocation );

		return result;
	}

	private Scope parseLoopScope( final Scope result, final Type functionType, final BasicScope parentScope )
	{
		if ( "{".equals( this.currentToken().value ) )
		{
			// Scope is a block

			this.readToken(); // {

			this.parseScope( result, functionType, parentScope, false, true, true );

			if ( "}".equals( this.currentToken().value ) )
			{
				this.readToken(); // }
			}
			else
			{
				this.unexpectedTokenError( "}", this.currentToken() );
			}
		}
		else
		{
			// Scope is a single command
			Command command = this.parseCommand( functionType, result, false, true, true );
			if ( command == null )
			{
				if ( ";".equals( this.currentToken().value ) )
				{
					this.readToken(); // ;
				}
				else
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}
			}
			else
			{
				result.addCommand( command, this );
			}
		}

		return result;
	}

	private Value parseNewRecord( final BasicScope scope )
	{
		if ( !"new".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token newRecordStartToken = this.currentToken();

		this.readToken();

		if ( !this.parseIdentifier( this.currentToken().value ) )
		{
			this.unexpectedTokenError( "Record name", this.currentToken() );

			return Value.BAD_VALUE;
		}

		String name = this.currentToken().value;
		Type type = scope.findType( name );

		Token nameToken = this.currentToken();
		boolean newRecordError = false, newRecordSyntaxError = false;

		this.readToken(); //name

		if ( type != null )
		{
			// Make one (to add to the type's references), but don't use it
			// (since we'd want to fetch its target immediately)
			new TypeReference( type, this.makeLocation( nameToken ) );
		}

		if ( !( type instanceof RecordType ) )
		{
			if ( type == null || !type.isBad() )
			{
				if ( !newRecordSyntaxError )
				{
					this.error( nameToken, "'" + name + "' is not a record type" );
				}
				newRecordError = newRecordSyntaxError = true;
			}

			type = new BadRecordType( null, this.makeLocation( nameToken ) );
		}

		RecordType target = (RecordType) type;

		List<Value> params = new ArrayList<>();
		String [] names = target.getFieldNames();
		Type [] types = target.getFieldTypes();
		int param = 0;

		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); //(

			Position previousPosition = null;
			while ( this.madeProgress( previousPosition, previousPosition = this.here() ) )
			{
				if ( this.atEndOfFile() )
				{
					this.unexpectedTokenError( ")", this.currentToken() );
					newRecordError = newRecordSyntaxError = true;
					break;
				}

				if ( ")".equals( this.currentToken().value ) )
				{
					this.readToken(); // )
					break;
				}

				Type currentType;
				// Shouldn't be used if blank, but just in case, to avoid another OutOfBoundsException
				String errorMessageFieldName = "";

				if ( param < types.length )
				{
					currentType = types[param];
					errorMessageFieldName = " (" + names[param] + ")";
				}
				else
				{
					if ( !newRecordSyntaxError )
					{
						this.error( this.currentToken(), "Too many field initializers for record " + name );
					}
					newRecordError = newRecordSyntaxError = true;

					currentType = new BadType( null, null );
				}

				Type expected = currentType.getBaseType();
				Value val;

				if ( ",".equals( this.currentToken().value ) )
				{
					val = DataTypes.VOID_VALUE;
				}
				else if ( "{".equals( this.currentToken().value ) )
				{
					this.readToken(); // read {

					if ( expected instanceof AggregateType )
					{
						val = this.parseAggregateLiteral( scope, (AggregateType) expected );
					}
					else
					{
						val = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );
					}
				}
				else
				{
					val = this.parseExpression( scope );
				}

				if ( val == null )
				{
					if ( !newRecordSyntaxError )
					{
						this.error( "Expression expected for field #" + ( param + 1 ) + errorMessageFieldName );
					}
					newRecordError = newRecordSyntaxError = true;

					val = Value.BAD_VALUE;
				}

				if ( val != DataTypes.VOID_VALUE )
				{
					val = this.autoCoerceValue( currentType, val, scope );
					Type given = val.getType();
					if ( !Operator.validCoercion( expected, given, "assign" ) )
					{
						if ( !newRecordError )
						{
							this.error( given + " found when " + expected + " expected for field #" + ( param + 1 ) + errorMessageFieldName );
							newRecordError = true;
						}

						val = Value.BAD_VALUE;
					}
				}

				params.add( val );
				param++;

				if ( ",".equals( this.currentToken().value ) )
				{
					this.readToken(); // ,
				}
				else if ( !")".equals( this.currentToken().value ) &&
				          !newRecordSyntaxError )
				{
					this.warning( this.make0WidthLocation( this.currentToken().getStart() ), "Missing comma between record field initializers" );
				}
			}
		}

		Location newRecordLocation = this.makeLocation( newRecordStartToken, this.peekPreviousToken() );

		return target.initialValueExpression( params );
	}

	private FunctionCall parseCall( final BasicScope scope )
	{
		return this.parseCall( scope, null );
	}

	private FunctionCall parseCall( final BasicScope scope, final Value firstParam )
	{
		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		if ( !this.parseScopedIdentifier( this.currentToken().value ) )
		{
			return null;
		}

		String name = this.currentToken().value;
		Token nameToken = this.currentToken();

		this.readToken(); //name

		List<Value> params = this.parseParameters( scope, firstParam );
		Function target = scope.findFunction( name, params );

		if ( target != null )
		{
			params = this.autoCoerceParameters( target, params, scope );
		}
		else
		{
			target = new BadFunction( name );

			// Don't make an error if the function couldn't be found
			// because the user messed up one of the parameters
			// (since that means we already made one earlier)
			boolean error = true;

			for ( Value param : params )
			{
				if ( param.getType().isBad() )
				{
					error = false;
					break;
				}
			}

			if ( error )
			{
				this.error( nameToken, Parser.undefinedFunctionMessage( name, params ) );
			}
		}

		Location functionCallLocation = this.makeLocation( nameToken, this.peekPreviousToken() );
		return new FunctionCall( functionCallLocation, target, params, this );
	}

	private List<Value> parseParameters( final BasicScope scope, final Value firstParam )
	{
		if ( !"(".equals( this.currentToken().value ) )
		{
			return null;
		}

		this.readToken(); //(

		List<Value> params = new ArrayList<Value>();
		if ( firstParam != null )
		{
			params.add( firstParam );
		}

		while ( true )
		{
			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( ")", this.currentToken() );
				break;
			}

			if ( ")".equals( this.currentToken().value ) )
			{
				this.readToken(); // )
				break;
			}

			Value val = this.parseExpression( scope );
			if ( val != null )
			{
				params.add( val );
			}

			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( ")", this.currentToken() );
				break;
			}

			if ( !",".equals( this.currentToken().value ) )
			{
				if ( !")".equals( this.currentToken().value ) )
				{
					this.unexpectedTokenError( ")", this.currentToken() );
					break;
				}
				continue;
			}

			this.readToken(); // ,

			if ( this.atEndOfFile() )
			{
				this.unexpectedTokenError( "parameter", this.currentToken() );
				break;
			}

			if ( ")".equals( this.currentToken().value ) )
			{
				this.unexpectedTokenError( "parameter", this.currentToken() );
				// we'll break out at the start of the next loop
			}
		}

		return params;
	}

	private FunctionInvocation parseInvoke( final BasicScope scope )
	{
		if ( !"call".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token invokeStartToken = this.currentToken();

		this.readToken(); // call

		Type type = this.parseType( scope, false );

		// You can omit the type, but then this function invocation
		// cannot be used in an expression

		if ( type == null )
		{
			type = DataTypes.VOID_TYPE;
		}

		String current = this.currentToken().value;
		Value name = null;

		if ( "(".equals( current ) )
		{
			name = this.parseExpression( scope );

			if ( name == null || !name.getType().equals( DataTypes.STRING_TYPE ) && !name.getType().isBad() )
			{
				this.error( "String expression expected for function name" );

				name = Value.BAD_VALUE;
			}
		}
		else
		{
			name = this.parseVariableReference( scope );

			if ( !( name instanceof VariableReference ) )
			{
				this.error( "Variable reference expected for function name" );

				name = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) );;
			}
		}

		List<Value> params;

		if ( "(".equals( this.currentToken().value ) )
		{
			params = this.parseParameters( scope, null );
		}
		else
		{
			this.unexpectedTokenError( "(", this.currentToken() );

			params = new ArrayList<>();
		}

		Location invokeLocation = this.makeLocation( invokeStartToken, this.peekPreviousToken() );
		return new FunctionInvocation( invokeLocation, scope, type, name, params, this );
	}

	private Assignment parseAssignment( final BasicScope scope, final VariableReference lhs )
	{
		Token operToken = this.currentToken();
		String operStr = this.currentToken().value;
		if ( !"=".equals( operStr ) &&
		     !"+=".equals( operStr ) &&
		     !"-=".equals( operStr ) &&
		     !"*=".equals( operStr ) &&
		     !"/=".equals( operStr ) &&
		     !"%=".equals( operStr ) &&
		     !"**=".equals( operStr ) &&
		     !"&=".equals( operStr ) &&
		     !"^=".equals( operStr ) &&
		     !"|=".equals( operStr ) &&
		     !"<<=".equals( operStr ) &&
		     !">>=".equals( operStr ) &&
		     !">>>=".equals( operStr ) )
		{
			return null;
		}

		Type ltype = lhs.getType().getBaseType();
		boolean isAggregate = ( ltype instanceof AggregateType );
		boolean assignmentError = false;

		if ( isAggregate && !"=".equals( operStr ) && !assignmentError )
		{
			this.error( "Cannot use '" + operStr + "' on an aggregate" );
			assignmentError = true;
		}

		Operator oper = new Operator( this.makeLocation( operToken ), operStr, this );
		this.readToken(); // oper

		Value rhs;

		if ( "{".equals( this.currentToken().value ) )
		{
			Position aggregateStart = this.here();

			this.readToken(); // read {

			if ( isAggregate )
			{
				rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
			}
			else
			{
				rhs = this.parseAggregateLiteral( scope, new BadAggregateType( null ) );

				if ( !assignmentError && "=".equals( operStr ) ) // otherwise the coercion check can catch this instead
				{
					this.error( aggregateStart, "Cannot use an aggregate literal for type " + lhs.getType() );
					assignmentError = true;
				}
			}
		}
		else
		{
			rhs = this.parseExpression( scope );
		}

		if ( rhs == null )
		{
			if ( !assignmentError )
			{
				this.error( "Expression expected" );
				assignmentError = true;
			}

			rhs = Value.BAD_VALUE;
		}

		rhs = this.autoCoerceValue( lhs.getRawType(), rhs, scope );
		if ( !oper.validCoercion( lhs.getType(), rhs.getType() ) && !assignmentError )
		{
			String error =
				oper.isLogical() ?
				( oper + " requires an integer or boolean expression and an integer or boolean variable reference" ) :
				oper.isInteger() ?
				( oper + " requires an integer expression and an integer variable reference" ) :
				( "Cannot store " + rhs.getType() + " in " + lhs + " of type " + lhs.getType() );
			this.error( error );
			assignmentError = true;
		}

		Operator op = null;

		if ( !"=".equals( operStr ) )
		{
			op = new Operator( this.makeLocation( this.makeInlineRange( operToken.range.getStart(), operStr.length() - 1 ) ),
			                   operStr.substring( 0, operStr.length() - 1 ), this );
		}

		return new Assignment( lhs, rhs, op );
	}

	private Value parseRemove( final BasicScope scope )
	{
		if ( !"remove".equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Value lhs = this.parseExpression( scope );

		if ( lhs == null )
		{
			this.error( "Bad 'remove' statement" );

			lhs = Value.BAD_VALUE;
		}

		return lhs;
	}

	private Value parsePreIncDec( final BasicScope scope )
	{
		if ( this.nextToken() == null )
		{
			return null;
		}

		// --[VariableReference]
		// ++[VariableReference]

		if ( !"++".equals( this.currentToken().value ) &&
		     !"--".equals( this.currentToken().value ) )
		{
			return null;
		}

		Token operToken = this.currentToken();
		String operStr = "++".equals( this.currentToken().value ) ? Parser.PRE_INCREMENT : Parser.PRE_DECREMENT;

		this.readToken(); // oper

		Value lhs = this.parseVariableReference( scope );
		if ( lhs == null )
		{
			this.error( "Variable reference expected" );

			lhs = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) );
		}

		int ltype = lhs.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT && !lhs.getType().isBad() )
		{
			this.error( operStr + " requires a numeric variable reference" );
		}

		Operator oper = new Operator( this.makeLocation( operToken ), operStr, this );

		return new IncDec( (VariableReference) lhs, oper );
	}

	private Value parsePostIncDec( final VariableReference lhs )
	{
		// [VariableReference]++
		// [VariableReference]--

		if ( !"++".equals( this.currentToken().value ) &&
		     !"--".equals( this.currentToken().value ) )
		{
			return lhs;
		}

		Token operToken = this.currentToken();
		String operStr = "++".equals( this.currentToken().value ) ? Parser.POST_INCREMENT : Parser.POST_DECREMENT;

		this.readToken(); // oper

		int ltype = lhs.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT && !lhs.getType().isBad() )
		{
			this.error( operStr + " requires a numeric variable reference" );
		}

		Operator oper = new Operator( this.makeLocation( operToken ), operStr, this );

		return new IncDec( lhs, oper );
	}

	private Value parseExpression( final BasicScope scope )
	{
		return this.parseExpression( scope, null );
	}

	private Value parseExpression( final BasicScope scope, final Operator previousOper )
	{
		if ( ";".equals( this.currentToken().value ) )
		{
			return null;
		}

		Value lhs = null;
		Value rhs = null;
		Operator oper = null;

		if ( "!".equals( this.currentToken().value ) )
		{
			Token operatorToken = this.currentToken();
			String operator = this.currentToken().value;
			this.readToken(); // !
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				this.error( "Value expected" );

				lhs = Value.BAD_VALUE;
			}

			lhs = this.autoCoerceValue( DataTypes.BOOLEAN_TYPE, lhs, scope );
			lhs = new Operation( lhs, new Operator( this.makeLocation( operatorToken ), operator, this ) );
			if ( lhs.getType() != DataTypes.BOOLEAN_TYPE && !lhs.getType().isBad() )
			{
				this.error( "\"!\" operator requires a boolean value" );
			}
		}
		else if ( "~".equals( this.currentToken().value ) )
		{
			Token operatorToken = this.currentToken();
			String operator = this.currentToken().value;
			this.readToken(); // ~
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				this.error( "Value expected" );

				lhs = Value.BAD_VALUE;
			}

			lhs = new Operation( lhs, new Operator( this.makeLocation( operatorToken ), operator, this ) );
			if ( lhs.getType() != DataTypes.INT_TYPE && lhs.getType() != DataTypes.BOOLEAN_TYPE && !lhs.getType().isBad() )
			{
				this.error( "\"~\" operator requires an integer or boolean value" );
			}
		}
		else if ( "-".equals( this.currentToken().value ) )
		{
			// See if it's a negative numeric constant
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				// Nope. Unary minus.
				Token operatorToken = this.currentToken();
				String operator = this.currentToken().value;
				this.readToken(); // -
				if ( ( lhs = this.parseValue( scope ) ) == null )
				{
					this.error( "Value expected" );

					lhs = Value.BAD_VALUE;
				}

				lhs = new Operation( lhs, new Operator( this.makeLocation( operatorToken ), operator, this ) );
			}
		}
		else if ( "remove".equals( this.currentToken().value ) )
		{
			Token operatorToken = this.currentToken();
			String operator = this.currentToken().value;
			this.readToken(); // remove

			lhs = this.parseVariableReference( scope );
			if ( lhs == null || !( lhs instanceof CompositeReference ) )
			{
				if ( lhs == null || !lhs.getType().isBad() )
				{
					this.error( "Aggregate reference expected" );
				}

				if ( !( lhs instanceof VariableReference ) )
				{
					lhs = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) );
				}
			}

			lhs = new Operation( lhs, new Operator( this.makeLocation( operatorToken ), operator, this ) );
		}
		else if ( ( lhs = this.parseValue( scope ) ) == null )
		{
			return null;
		}

		boolean expressionError = false, expressionSyntaxError = false;

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.here() ) )
		{
			oper = this.parseOperator( this.currentToken() );

			if ( oper == null )
			{
				return lhs;
			}

			if ( previousOper != null && !oper.precedes( previousOper ) )
			{
				return lhs;
			}

			if ( ":".equals( this.currentToken().value ) )
			{
				return lhs;
			}

			if ( "?".equals( this.currentToken().value ) )
			{
				this.readToken(); // ?

				Value conditional = lhs;

				if ( conditional.getType() != DataTypes.BOOLEAN_TYPE &&
				     !conditional.getType().isBad() && !expressionError )
				{
					this.error( "Non-boolean expression " + conditional + " (" + conditional.getType() + ")" );
					expressionError = true;
				}

				if ( ( lhs = this.parseExpression( scope, null ) ) == null )
				{
					if ( !expressionSyntaxError )
					{
						this.error( "Value expected in left hand side" );
					}
					expressionError = expressionSyntaxError = true;

					lhs = Value.BAD_VALUE;
				}

				if ( ":".equals( this.currentToken().value ) )
				{
					this.readToken(); // :
				}
				else
				{
					if ( !expressionSyntaxError )
					{
						this.unexpectedTokenError( ":", this.currentToken() );
					}
					expressionError = expressionSyntaxError = true;
				}

				if ( ( rhs = this.parseExpression( scope, null ) ) == null )
				{
					if ( !expressionSyntaxError )
					{
						this.error( "Value expected" );
					}
					expressionError = expressionSyntaxError = true;

					lhs = Value.BAD_VALUE;
				}

				if ( !oper.validCoercion( lhs.getType(), rhs.getType() ) && !expressionError )
				{
					this.error( "Cannot choose between " + lhs + " (" + lhs.getType() + ") and " + rhs + " (" + rhs.getType() + ")" );
					expressionError = true;
				}

				lhs = new TernaryExpression( conditional, lhs, rhs );
			}
			else
			{
				this.readToken(); //operator

				if ( ( rhs = this.parseExpression( scope, oper ) ) == null )
				{
					if ( !expressionSyntaxError )
					{
						this.error( "Value expected" );
					}
					expressionError = expressionSyntaxError = true;

					rhs = Value.BAD_VALUE;
				}


				Type ltype = lhs.getType();
				Type rtype = rhs.getType();

				if ( oper.equals( "+" ) && ( ltype.equals( DataTypes.TYPE_STRING ) || rtype.equals( DataTypes.TYPE_STRING ) ) )
				{
					// String concatenation
					if ( !ltype.equals( DataTypes.TYPE_STRING ) )
					{
						lhs = this.autoCoerceValue( DataTypes.STRING_TYPE, lhs, scope );
					}
					if ( !rtype.equals( DataTypes.TYPE_STRING ) )
					{
						rhs = this.autoCoerceValue( DataTypes.STRING_TYPE, rhs, scope );
					}
					if ( lhs instanceof Concatenate )
					{
						Concatenate conc = (Concatenate) lhs;
						conc.addString( rhs );
					}
					else
					{
						lhs = new Concatenate( lhs, rhs );
					}
				}
				else
				{
					rhs = this.autoCoerceValue( ltype, rhs, scope );
					if ( !oper.validCoercion( ltype, rhs.getType() ) && !expressionError )
					{
						this.error( "Cannot apply operator " + oper + " to " + lhs + " (" + lhs.getType() + ") and " + rhs + " (" + rhs.getType() + ")" );
						expressionError = true;
					}
					lhs = new Operation( lhs, rhs, oper );
				}
			}
		}

		if ( !expressionError )
		{
			// should NOT happen
			this.error( "Internal error; got stuck while parsing expressions" );
		}

		return lhs;
	}

	private Value parseValue( final BasicScope scope )
	{
		if ( ";".equals( this.currentToken().value ) )
		{
			return null;
		}

		Value result = null;

		// Parse parenthesized expressions
		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (

			result = this.parseExpression( scope );

			if ( ")".equals( this.currentToken().value ) )
			{
				this.readToken(); // )
			}
			else
			{
				this.unexpectedTokenError( ")", this.currentToken() );
			}
		}

		// Parse constant values
		// true and false are reserved words

		else if ( "true".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken();
			result = DataTypes.TRUE_VALUE;
		}

		else if ( "false".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken();
			result = DataTypes.FALSE_VALUE;
		}

		else if ( "__FILE__".equals( this.currentToken().value ) )
		{
			this.readToken();
			result = new Value( String.valueOf( this.shortFileName ) );
		}

		// numbers
		else if ( ( result = this.parseNumber() ) != null )
		{
		}

		else if ( ( result = this.parseString( scope ) ) != null )
		{
		}

		else if ( ( result = this.parseTypedConstant( scope ) ) != null )
		{
		}

		else if ( ( result = this.parseNewRecord( scope ) ) != null )
		{
		}

		else if ( ( result = this.parseCatchValue( scope ) ) != null )
		{
		}

		else if ( ( result = this.parsePreIncDec( scope ) ) != null )
		{
			return result;
		}

		else if ( ( result = this.parseInvoke( scope ) ) != null )
		{
		}

		else if ( ( result = this.parseCall( scope ) ) != null )
		{
		}

		else
		{
			Token anchor = this.currentToken();

			Type baseType = this.parseType( scope, false );
			if ( baseType != null && baseType.getBaseType() instanceof AggregateType )
			{
				if ( "{".equals( this.currentToken().value ) )
				{
					this.readToken(); // {
					result = this.parseAggregateLiteral( scope, (AggregateType) baseType.getBaseType() );
				}
				else
				{
					this.unexpectedTokenError( "{", this.currentToken() );
					// don't parse. We don't know if they just didn't put anything.

					result = Value.BAD_VALUE;
				}
			}
			else
			{
				if ( baseType != null )
				{
					this.rewindBackTo( anchor );
				}
				if ( ( result = this.parseVariableReference( scope ) ) != null )
				{
				}
			}
		}

		while ( result != null && ( ".".equals( this.currentToken().value ) || "[".equals( this.currentToken().value ) ) )
		{
			Variable current = new Variable( result.getType() );
			current.setExpression( result );

			VariableReference ref = new VariableReference( current, result.getLocation() );

			result = this.parseVariableReference( scope, ref );
		}

		if ( result instanceof VariableReference )
		{
			VariableReference ref = (VariableReference) result;
			Assignment value = this.parseAssignment( scope, ref );
			return ( value != null ) ? value : this.parsePostIncDec( ref );
		}

		return result;
	}

	private Value parseNumber()
	{
		int sign = 1;

		if ( "-".equals( this.currentToken().value ) )
		{
			String next = this.nextToken();

			if ( !".".equals( next ) && !this.readIntegerToken( next ) )
			{
				// Unary minus
				return null;
			}

			sign = -1;
			this.readToken(); // Read -
		}

		if ( ".".equals( this.currentToken().value ) )
		{
			this.readToken();
			String fraction = this.currentToken().value;

			if ( !this.readIntegerToken( fraction ) )
			{
				this.unexpectedTokenError( "numeric value", this.currentToken() );

				return new Value( (double) 0 );
			}

			this.readToken(); // integer
			return new Value( sign * StringUtilities.parseDouble( "0." + fraction ) );
		}

		String integer = this.currentToken().value;
		if ( !this.readIntegerToken( integer ) )
		{
			return null;
		}

		this.readToken(); // integer

		if ( ".".equals( this.currentToken().value ) )
		{
			String fraction = this.nextToken();
			if ( !this.readIntegerToken( fraction ) )
			{
				return new Value( sign * StringUtilities.parseLong( integer ) );
			}

			this.readToken(); // .
			this.readToken(); // fraction

			return new Value( sign * StringUtilities.parseDouble( integer + "." + fraction ) );
		}

		return new Value( sign * StringUtilities.parseLong( integer ) );
	}

	private boolean readIntegerToken( final String token )
	{
		if ( token == null )
		{
			return false;
		}

		for ( int i = 0; i < token.length(); ++i )
		{
			if ( !Character.isDigit( token.charAt( i ) ) )
			{
				return false;
			}
		}

		return true;
	}

	private Value parseString( final BasicScope scope )
	{
		if ( !"\"".equals( this.currentToken().value ) &&
		     !"'".equals( this.currentToken().value ) &&
		     !"`".equals( this.currentToken().value ) )
		{
			return null;
		}

		// Directly work with currentLine - ignore any "tokens" you meet until
		// the string is closed

		this.currentToken = null;
		this.currentLine.tokens.removeLast();

		char startCharacter = this.restOfLine().charAt( 0 );
		char stopCharacter = startCharacter;
		boolean template = startCharacter == '`';

		Concatenate conc = null;
		StringBuilder resultString = new StringBuilder();
		for ( int i = 1; ; ++i )
		{
			final String line = this.restOfLine();

			if ( i == line.length() )
			{
				if ( i > 0 )
				{
					this.currentLine.makeToken( i );
					this.currentIndex += i;
				}

				// Plain strings can't span lines
				this.error( "No closing " + stopCharacter + " found" );

				Value result = new Value( resultString.toString() );

				if ( conc == null )
				{
					return result;
				}
				else
				{
					conc.addString( result );
					return conc;
				}
			}

			char ch = line.charAt( i );

			// Handle escape sequences
			if ( ch == '\\' )
			{
				i = this.parseEscapeSequence( resultString, ++i );
				continue;
			}

			// Handle template substitutions
			if ( template && ch == '{' )
			{
				// Move the current token to the expression
				this.currentToken = this.currentLine.makeToken( i );
				this.readToken(); // read the string so far
				this.readToken(); // read {

				Value rhs = this.parseExpression( scope );

				// Skip comments before the next token, look at what it is, then discard said token
				if ( "}".equals( this.currentToken().value ) )
				{
					this.currentToken = null;
					this.currentLine.tokens.removeLast();

					this.currentLine.makeToken( 1 ); // }
					// read manually to not skip whitespaces after the curly brace
					this.currentIndex += 1;
				}
				else
				{
					this.unexpectedTokenError( "}", this.currentToken() );
					this.currentToken = null;
					this.currentLine.tokens.removeLast();
				}

				// Set i to -1 so that it is set to zero by the loop as the currentLine has been shortened
				i = -1;

				Value lhs = new Value( resultString.toString() );
				if ( conc == null )
				{
					conc = new Concatenate( lhs, rhs );
				}
				else
				{
					conc.addString( lhs );
					conc.addString( rhs );
				}

				resultString.setLength( 0 );
				continue;
			}

			if ( ch == stopCharacter )
			{
				// +1 to include + get rid of stop character token
				this.currentToken = this.currentLine.makeToken( i + 1 );
				this.readToken();

				Value result = new Value( resultString.toString() );

				if ( conc == null )
				{
					return result;
				}
				else
				{
					conc.addString( result );
					return conc;
				}
			}
			resultString.append( ch );
		}
	}

	private int parseEscapeSequence( final StringBuilder resultString, int i )
	{
		final String line = this.restOfLine();

		if ( i == line.length() )
		{
			resultString.append( '\n' );
			this.currentLine.makeToken( i );
			this.currentLine = this.currentLine.nextLine;
			this.currentIndex = this.currentLine.offset;
			return -1;
		}

		char ch = line.charAt( i );

		switch ( ch )
		{
		case 'n':
			resultString.append( '\n' );
			break;

		case 'r':
			resultString.append( '\r' );
			break;

		case 't':
			resultString.append( '\t' );
			break;

		case 'x':
			try
			{
				int hex08 = Integer.parseInt( line.substring( i + 1, i + 3 ), 16 );
				resultString.append( (char) hex08 );
				i += 2;
			}
			catch ( IndexOutOfBoundsException | NumberFormatException e )
			{
				this.error( "Hexadecimal character escape requires 2 digits" );

				resultString.append( ch );
			}
			break;

		case 'u':
			try
			{
				int hex16 = Integer.parseInt( line.substring( i + 1, i + 5 ), 16 );
				resultString.append( (char) hex16 );
				i += 4;
			}
			catch ( IndexOutOfBoundsException | NumberFormatException e )
			{
				this.error( "Unicode character escape requires 4 digits" );

				resultString.append( ch );
			}
			break;

		default:
			if ( Character.isDigit( ch ) )
			{
				try
				{
					int octal = Integer.parseInt( line.substring( i, i + 3 ), 8 );
					resultString.append( (char) octal );
					i += 2;
					break;
				}
				catch ( IndexOutOfBoundsException | NumberFormatException e )
				{
					this.error( "Octal character escape requires 3 digits" );
				}
			}
			resultString.append( ch );
		}

		return i;
	}

	private Value parseLiteral( final Type type, final String element )
	{
		Value value = DataTypes.parseValue( type, element, false );
		if ( value == null )
		{
			if ( !type.isBad() )
			{
				this.error( "Bad " + type.toString() + " value: \"" + element + "\"" );
			}

			return Value.BAD_VALUE;
		}

		if ( !StringUtilities.isNumeric( element ) )
		{
			String fullName = value.toString();
			if ( !element.equalsIgnoreCase( fullName ) )
			{
				String s1 = CharacterEntities.escape( StringUtilities.globalStringReplace( element, ",", "\\," ).replaceAll( "(?<= ) ", "\\\\ " ) );
				String s2 = CharacterEntities.escape( StringUtilities.globalStringReplace( fullName, ",", "\\," ).replaceAll( "(?<= ) ", "\\\\ " ) );
				List<String> names = new ArrayList<String>();
				if ( type == DataTypes.ITEM_TYPE )
				{
					int itemId = (int)value.contentLong;
					String name = ItemDatabase.getItemName( itemId );
					int[] ids = ItemDatabase.getItemIds( name, 1, false );
					for ( int id : ids )
					{
						String s3 = "$item[[" + id + "]" + name + "]";
						names.add( s3 );
					}
				}
				else if ( type == DataTypes.EFFECT_TYPE )
				{
					int effectId = (int)value.contentLong;
					String name = EffectDatabase.getEffectName( effectId );
					int[] ids = EffectDatabase.getEffectIds( name, false );
					for ( int id : ids )
					{
						String s3 = "$effect[[" + id + "]" + name + "]";
						names.add( s3 );
					}
				}
				else if ( type == DataTypes.MONSTER_TYPE )
				{
					int monsterId = (int)value.contentLong;
					String name = MonsterDatabase.findMonsterById( monsterId ).getName();
					int[] ids = MonsterDatabase.getMonsterIds( name, false );
					for ( int id : ids )
					{
						String s3 = "$monster[[" + id + "]" + name + "]";
						names.add( s3 );
					}
				}
				else if ( type == DataTypes.SKILL_TYPE )
				{
					int skillId = (int)value.contentLong;
					String name = SkillDatabase.getSkillName( skillId );
					int[] ids = SkillDatabase.getSkillIds( name, false );
					for ( int id : ids )
					{
						String s3 = "$skill[[" + id + "]" + name + "]";
						names.add( s3 );
					}
				}

				if ( names.size() > 1 )
				{
					this.warning(
						"Multiple matches for \"" + s1 + "\"; using \"" + s2 + "\".",
						"Clarify by using one of:" +
							KoLConstants.LINE_BREAK +
							String.join( KoLConstants.LINE_BREAK, names ) );
				}
				else
				{
					this.warning( "Changing \"" + s1 + "\" to \"" + s2 + "\" would get rid of this message." );
				}
			}
		}

		return value;
	}

	private Value parseTypedConstant( final BasicScope scope )
	{
		if ( !"$".equals( this.currentToken().value ) )
		{
			return null;
		}

		Token typedConstantStartToken = this.currentToken();

		this.readToken(); // read $

		Token typedConstantTypeToken = this.currentToken();
		String name = this.currentToken().value;
		Type type = null;
		boolean plurals = false;
		boolean typedConstantError = false, typedConstantSyntaxError = false;

		if ( this.parseIdentifier( name ) )
		{
			type = scope.findType( name );

			if ( type == null )
			{
				StringBuilder buf = new StringBuilder( name );
				int length = name.length();

				if ( name.endsWith( "ies" ) )
				{
					buf.delete( length - 3, length );
					buf.insert( length - 3, "y" );
				}
				else if ( name.endsWith( "es" ) )
				{
					buf.delete( length - 2, length );
				}
				else if ( name.endsWith( "s" ) )
				{
					buf.deleteCharAt( length - 1 );
				}
				else if ( name.endsWith( "a" ) )
				{
					buf.deleteCharAt( length - 1 );
					buf.insert( length - 1, "um" );
				}

				type = scope.findType( buf.toString() );

				plurals = true;
			}

			this.readToken();
		}

		if ( type == null )
		{
			if ( !typedConstantSyntaxError )
			{
				this.error( typedConstantTypeToken, "Unknown type " + name );
			}
			typedConstantError = typedConstantSyntaxError = true;

			type = new BadType( name, this.makeLocation( typedConstantTypeToken ) );
		}
		else
		{
			type = new TypeReference( type, this.makeLocation( typedConstantTypeToken ) );
		}

		if ( !type.isPrimitive() && !type.isBad() )
		{
			if ( !typedConstantError )
			{
				this.error( typedConstantTypeToken, "Non-primitive type " + name );
				typedConstantError = true;
			}

			type = new BadType( name, this.makeLocation( typedConstantTypeToken ) );
		}

		if ( "[".equals( this.currentToken().value ) )
		{
			this.readToken();
		}
		else
		{
			if ( !typedConstantSyntaxError )
			{
				this.unexpectedTokenError( "[", this.currentToken() );
			}

			return Value.BAD_VALUE;
		}

		if ( plurals )
		{
			Value value = this.parsePluralConstant( scope, type );
			if ( value != null )
			{
				return value;	// explicit list of values
			}
			value = type.allValues();
			if ( value != null )
			{
				return value;	// implicit enumeration
			}

			if ( !typedConstantSyntaxError )
			{
				this.error( "Can't enumerate all " + name );
			}

			return Value.BAD_VALUE;
		}

		StringBuilder resultString = new StringBuilder();

		int level = 1;
		for ( int i = 0; ; ++i )
		{
			final String line = this.restOfLine();

			if ( i == line.length() )
			{
				if ( i > 0 )
				{
					this.currentLine.makeToken( i );
					this.currentIndex += i;
				}

				if ( !typedConstantError )
				{
					this.error( "No closing ] found" );
				}

				String input = resultString.toString().trim();
				return this.parseLiteral( type, input );
			}

			char c = line.charAt( i );
			if ( c == '\\' )
			{
				i = this.parseEscapeSequence( resultString, ++i );
			}
			else if ( c == '[' )
			{
				level++;
				resultString.append( c );
			}
			else if ( c == ']' )
			{
				if ( --level > 0 )
				{
					resultString.append( c );
					continue;
				}
				this.currentLine.makeToken( i );
				this.currentIndex += i;
				this.readToken(); // read ]
				String input = resultString.toString().trim();
				return this.parseLiteral( type, input );
			}
			else
			{
				resultString.append( c );
			}
		}
	}

	private PluralValue parsePluralConstant( final BasicScope scope, final Type type )
	{
		// Directly work with currentLine - ignore any "tokens" you meet until
		// the string is closed

		List<Value> list = new ArrayList<>();
		int level = 1;
		boolean slash = false;

		StringBuilder resultString = new StringBuilder();
		for ( int i = 0; ; ++i )
		{
			final String line = this.restOfLine();

			if ( i == line.length() )
			{
				if ( i > 0 )
				{
					this.currentLine.makeToken( i );
				}

				this.currentLine = this.currentLine.nextLine;
				this.currentIndex = this.currentLine.offset;

				if ( this.currentLine.content == null )
				{
					this.error( "No closing ] found" );

					String element = resultString.toString().trim();
					if ( element.length() != 0 )
					{
						list.add( this.parseLiteral( type, element ) );
					}

					if ( list.size() == 0 )
					{
						// Empty list - caller will interpret this specially
						return null;
					}
					return new PluralValue( type, list );
				}

				i = -1;
				continue;
			}

			char ch = line.charAt( i );

			// Handle escape sequences
			if ( ch == '\\' )
			{
				i = this.parseEscapeSequence( resultString, ++i );
				continue;
			}

			// Potentially handle comments
			// If we've already seen a slash
			if ( slash )
			{
				slash = false;
				if ( ch == '/' )
				{
					this.currentLine.makeToken( i - 1 );
					this.currentIndex += i - 1;
					// Throw away the rest of the line
					this.currentLine.makeComment( this.restOfLine().length() );
					this.currentIndex += this.restOfLine().length();
					i = -1;
					continue;
				}
				resultString.append( '/' );
			}
			else if ( ch == '/' )
			{
				slash = true;
				continue;
			}

			// Allow start char without escaping
			if ( ch == '[' )
			{
				level++;
				resultString.append( ch );
				continue;
			}

			// Match non-initial start char
			if ( ch == ']' && --level > 0 )
			{
				resultString.append( ch );
				continue;
			}

			if ( ch != ']' && ch != ',' )
			{
				resultString.append( ch );
				continue;
			}

			// Add a new element to the list
			String element = resultString.toString().trim();
			resultString.setLength( 0 );
			if ( element.length() != 0 )
			{
				list.add( this.parseLiteral( type, element ) );
			}

			if ( ch == ']' )
			{
				this.currentLine.makeToken( i );
				this.currentIndex += i;
				this.readToken(); // read ]
				if ( list.size() == 0 )
				{
					// Empty list - caller will interpret this specially
					return null;
				}
				return new PluralValue( type, list );
			}
		}
	}

	private Operator parseOperator( final Token oper )
	{
		if ( !this.isOperator( oper.value ) )
		{
			return null;
		}

		return new Operator( this.makeLocation( oper ), oper.value, this );
	}

	private boolean isOperator( final String oper )
	{
		return "!".equals( oper ) ||
		       "?".equals( oper ) ||
		       ":".equals( oper ) ||
		       "*".equals( oper ) ||
		       "**".equals( oper ) ||
		       "/".equals( oper ) ||
		       "%".equals( oper ) ||
		       "+".equals( oper ) ||
		       "-".equals( oper ) ||
		       "&".equals( oper ) ||
		       "^".equals( oper ) ||
		       "|".equals( oper ) ||
		       "~".equals( oper ) ||
		       "<<".equals( oper ) ||
		       ">>".equals( oper ) ||
		       ">>>".equals( oper ) ||
		       "<".equals( oper ) ||
		       ">".equals( oper ) ||
		       "<=".equals( oper ) ||
		       ">=".equals( oper ) ||
		       "==".equals( oper ) ||
		       Parser.APPROX.equals( oper ) ||
		       "!=".equals( oper ) ||
		       "||".equals( oper ) ||
		       "&&".equals( oper ) ||
		       "contains".equals( oper ) ||
		       "remove".equals( oper );
	}

	private Value parseVariableReference( final BasicScope scope )
	{
		if ( !this.parseIdentifier( this.currentToken().value ) )
		{
			return null;
		}

		Token variableToken = this.currentToken();

		String name = this.currentToken().value;
		Variable var = scope.findVariable( name, true );

		this.readToken(); // read name

		VariableReference reference;

		if ( var != null )
		{
			reference = new VariableReference( var, this.makeLocation( variableToken ) );
		}
		else
		{
			this.error( variableToken, "Unknown variable '" + name + "'" );

			var = new BadVariable( name, new BadType( null, null ), this.makeLocation( variableToken ) );
			reference = new VariableReference( var );
		}

		return this.parseVariableReference( scope, reference );
	}

	/**
	 * Look for an index/key, and return the corresponding data, expecting
	 * {@code var} to be a {@link AggregateType}/{@link RecordType}.
	 * <p>
	 * E.g. {@code map.key}, {@code array[0]}
	 * <p>
	 * May also return a {@link FunctionCall} if the chain ends with/is a function call
	 * <p>
	 * E.g. {@code var.function()}
	 */
	private Value parseVariableReference( final BasicScope scope, final VariableReference var )
	{
		VariableReference current = var;
		Type type = var.getType();
		List<Value> indices = new ArrayList<Value>();

		boolean parseAggregate = "[".equals( this.currentToken().value );
		boolean variableReferenceError = false, variableReferenceSyntaxError = false;

		while ( "[".equals( this.currentToken().value ) ||
		        ".".equals( this.currentToken().value ) ||
		        parseAggregate && ",".equals( this.currentToken().value ) )
		{
			Value index;

			type = type.getBaseType();

			if ( "[".equals( this.currentToken().value ) || ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // read [ or ,
				parseAggregate = true;

				if ( !( type instanceof AggregateType ) )
				{
					if ( !variableReferenceError && !type.isBad() )
					{
						Location location = this.makeLocation( current.getLocation(), this.makeLocation( this.peekPreviousToken() ) );
						String message;
						if ( indices.isEmpty() )
						{
							message = "Variable '" + var.getName() + "' cannot be indexed";
						}
						else
						{
							message = "Too many keys for '" + var.getName() + "'";
						}
						this.error( location, message );
						variableReferenceError = true;
					}

					type = new BadAggregateType( null );
				}

				AggregateType atype = (AggregateType) type;
				index = this.parseExpression( scope );
				if ( index == null )
				{
					if ( !variableReferenceSyntaxError )
					{
						this.error( this.currentToken(), "Index for '" + current.getName() + "' expected" );
					}
					variableReferenceError = variableReferenceSyntaxError = true;

					index = Value.BAD_VALUE;
				}

				if ( !variableReferenceError &&
				     !index.getType().getBaseType().equals( atype.getIndexType().getBaseType() ) &&
				     !index.getType().isBad() &&
				     !atype.getIndexType().isBad() )
				{
					this.error( "Index for '" + current.getName() + "' has wrong data type " +
							"(expected " + atype.getIndexType() + ", got " + index.getType() + ")" );
					variableReferenceError = true;
				}

				type = atype.getDataType();
			}
			else
			{
				this.readToken(); // read .

				// Maybe it's a function call with an implied "this" parameter.

				if ( "(".equals( this.nextToken() ) )
				{
					return this.parseCall( scope, current );
				}

				type = type.asProxy();
				if ( !( type instanceof RecordType ) )
				{
					if ( !type.isBad() )
					{
						// See this as a syntax error, since we don't know yet
						// if what follows is even an identifier.
						if ( !variableReferenceSyntaxError )
						{
							this.error( current.getLocation(), "Record expected" );
						}
						variableReferenceError = variableReferenceSyntaxError = true;
					}

					type = new BadRecordType( null, null );
				}

				RecordType rtype = (RecordType) type;

				Token fieldToken = this.currentToken();
				String field = this.currentToken().value;
				if ( this.parseIdentifier( field ) )
				{
					this.readToken(); // read name
				}
				else
				{
					if ( !variableReferenceSyntaxError )
					{
						this.error( fieldToken, "Field name expected" );
					}
					variableReferenceError = variableReferenceSyntaxError = true;

					field = null;
				}

				index = rtype.getFieldIndex( field );
				if ( index == null )
				{
					if ( !variableReferenceError )
					{
						this.error( fieldToken, "Invalid field name '" + field + "'" );
						variableReferenceError = true;
					}

					index = Value.BAD_VALUE;
					type = new BadType( null, null );
				}
				else
				{
					type = rtype.getDataType( index );
				}
			}

			indices.add( index );

			if ( parseAggregate && "]".equals( this.currentToken().value ) )
			{
				this.readToken(); // read ]
				parseAggregate = false;
			}

			Location currentLocation = this.makeLocation( current.getLocation(), this.makeLocation( this.peekPreviousToken() ) );
			current = new CompositeReference( current.target, currentLocation, indices, this );
		}

		if ( parseAggregate && !variableReferenceSyntaxError )
		{
			this.unexpectedTokenError( "]", this.currentToken() );
		}

		return current;
	}

	private class Directive
	{
		final String value;
		final Range range;

		Directive( final String value, final Range range )
		{
			this.value = value;
			this.range = range;
		}
	}

	private Directive parseDirective( final String directive )
	{
		if ( !directive.equalsIgnoreCase( this.currentToken().value ) )
		{
			return null;
		}

		Token directiveToken = this.currentToken();
		Token directiveValueToken = null;

		this.readToken(); //directive

		if ( this.atEndOfFile() )
		{
			this.unexpectedTokenError( "<", this.currentToken() );

			return null;
		}

		// atEndOfFile() calls currentToken() (which we still want, to trim whitespaces and skip comments).
		// Remove the token that was generated.
		this.currentToken = null;
		this.currentLine.tokens.removeLast();

		String resultString = null;
		int endIndex = -1;
		final String line = this.restOfLine();
		final char firstChar = line.charAt( 0 );

		for ( char ch : new char[] { '<', '\'', '"' } )
		{
			if ( ch != firstChar )
			{
				continue;
			}

			if ( ch == '<' )
			{
				ch = '>';
			}

			endIndex = line.indexOf( ch, 1 );

			if ( endIndex == -1 )
			{
				endIndex = line.indexOf( ";" );

				if ( endIndex == -1 )
				{
					endIndex = line.length();
				}

				resultString = line.substring( 0, endIndex );
				directiveValueToken = this.currentToken = this.currentLine.makeToken( endIndex );
				this.readToken();

				this.error( directiveValueToken, "No closing " + ch + " found" );

				break;
			}

			resultString = line.substring( 1, endIndex );
			// +1 to include + get rid of '>', '\'' or '"' token
			directiveValueToken = this.currentToken = this.currentLine.makeToken( endIndex + 1 );
			this.readToken();

			break;
		}

		if ( endIndex == -1 )
		{
			endIndex = line.indexOf( ";" );

			if ( endIndex == -1 )
			{
				endIndex = line.length();
			}

			resultString = line.substring( 0, endIndex );
			directiveValueToken = this.currentToken = this.currentLine.makeToken( endIndex );
			this.readToken();
		}

		if ( ";".equals( this.currentToken().value ) )
		{
			this.readToken(); //read ;
		}

		return new Directive( resultString, this.makeRange( directiveToken, directiveValueToken ) );
	}

	private void parseScriptName()
	{
		Directive scriptDirective = this.parseDirective( "script" );
		if ( this.scriptName == null && scriptDirective != null )
		{
			this.scriptName = scriptDirective.value;
		}
	}

	private void parseNotify()
	{
		Directive notifyDirective = this.parseDirective( "notify" );
		if ( this.notifyRecipient == null && notifyDirective != null )
		{
			this.notifyRecipient = notifyDirective.value;
		}
	}

	private void parseSince()
	{
		Directive sinceDirective = this.parseDirective( "since" );
		if ( sinceDirective != null )
		{
			// enforce "since" directives RIGHT NOW at parse time
			this.enforceSince( sinceDirective.value, sinceDirective.range );
		}
	}

	private Directive parseImport()
	{
		return this.parseDirective( "import" );
	}

	// **************** Tokenizer *****************

	private static final char BOM = '\ufeff';

	private Token currentToken()
	{
		// If we've already parsed a token, return it
		if ( this.currentToken != null )
		{
			return this.currentToken;
		}

		boolean inMultiLineComment = false;

		// Repeat until we get a token
		while ( true )
		{
			// at "end of file"
			if ( this.currentLine.content == null )
			{
				// will make an "end of file" token
				return this.currentToken = this.currentLine.makeToken( 0 );
			}

			final String restOfLine = this.restOfLine();

			if ( inMultiLineComment )
			{
				final int commentEnd = restOfLine.indexOf( "*/" );

				if ( commentEnd == -1 )
				{
					this.currentLine.makeComment( restOfLine.length() );

					this.currentLine = this.currentLine.nextLine;
					this.currentIndex = this.currentLine.offset;
				}
				else
				{
					this.currentToken = this.currentLine.makeComment( commentEnd + 2 );
					this.readToken();
					inMultiLineComment = false;
				}

				continue;
			}

			if ( restOfLine.length() == 0 )
			{
				this.currentLine = this.currentLine.nextLine;
				this.currentIndex = this.currentLine.offset;
				continue;
			}

			// "#" was "supposed" to start a whole-line comment,
			// but a bad implementation made it act just like "//"

			// "//" starts a comment which consumes the rest of the line
			if ( restOfLine.startsWith( "#" ) ||
			     restOfLine.startsWith( "//" ) )
			{
				this.currentLine.makeComment( restOfLine.length() );

				this.currentLine = this.currentLine.nextLine;
				this.currentIndex = this.currentLine.offset;
				continue;
			}

			// "/*" starts a comment which is terminated by "*/"
			if ( restOfLine.startsWith( "/*" ) )
			{
				final int commentEnd = restOfLine.indexOf( "*/", 2 );

				if ( commentEnd == -1 )
				{
					this.currentLine.makeComment( restOfLine.length() );

					this.currentLine = this.currentLine.nextLine;
					this.currentIndex = this.currentLine.offset;
					inMultiLineComment = true;
				}
				else
				{
					this.currentToken = this.currentLine.makeComment( commentEnd + 2 );
					this.readToken();
				}

				continue;
			}

			return this.currentToken = this.currentLine.makeToken( this.tokenLength( restOfLine ) );
		}
	}

	private String nextToken()
	{
		int offset = this.currentToken().restOfLineStart;
		Line line = this.currentLine;
		boolean inMultiLineComment = false;

		while ( true )
		{
			// at "end of file"
			if ( line.content == null )
			{
				return null;
			}

			final String restOfLine = line.substring( offset ).trim();

			if ( inMultiLineComment )
			{
				final int commentEnd = restOfLine.indexOf( "*/" );

				if ( commentEnd == -1 )
				{
					line = line.nextLine;
					offset = line.offset;
				}
				else
				{
					offset += commentEnd + 2;
					inMultiLineComment = false;
				}

				continue;
			}

			// "#" was "supposed" to start a whole-line comment,
			// but a bad implementation made it act just like "//"

			if ( restOfLine.length() == 0 ||
			     restOfLine.startsWith( "#" ) ||
			     restOfLine.startsWith( "//" ) )
			{
				line = line.nextLine;
				offset = line.offset;
				continue;
			}

			if ( restOfLine.startsWith( "/*" ) )
			{
				offset += 2;
				inMultiLineComment = true;
				continue;
			}

			return restOfLine.substring( 0, this.tokenLength( restOfLine ) );
		}
	}

	/**
	 * Forget every token up to {@code destinationToken},
	 * so that we can resume parsing from there
	 */
	private void rewindBackTo( final Token destinationToken )
	{
		this.currentToken();

		while ( this.currentToken != destinationToken )
		{
			this.currentLine.tokens.removeLast();

			while ( this.currentLine.tokens.isEmpty() )
			{
				// Don't do null checks. If previousLine is null, it means we never
				// saw the destination token, meaning we'd want to throw an error anyway.
				this.currentLine = this.currentLine.previousLine;
			}

			this.currentToken = this.currentLine.tokens.getLast();
			this.currentIndex = this.currentToken.getStart().getCharacter();
		}
	}

	/** Finds the last token that was *read* */
	private final Token peekPreviousToken()
	{
		if ( this.currentToken != null )
		{
			// Temporarily remove currentToken
			this.currentLine.tokens.removeLast();
		}

		final Token previousToken = this.peekLastToken();

		if ( this.currentToken != null )
		{
			// Add the previously removed token back in
			this.currentLine.tokens.addLast( this.currentToken );
		}

		return previousToken;
	}

	private final Token peekLastToken()
	{
		return this.currentLine.peekLastToken();
	}

	private void readToken()
	{
		// at "end of file"
		if ( this.currentToken().getLine().content == null )
		{
			return;
		}

		this.currentIndex = this.currentToken.restOfLineStart;
		this.currentToken = null;
	}

	private int tokenLength( final String s )
	{
		int result;
		if ( s == null )
		{
			return 0;
		}

		for ( result = 0; result < s.length(); result++ )
		{
			if ( result + 3 < s.length() && this.tokenString( s.substring( result, result + 4 ) ) )
			{
				return result == 0 ? 4 : result;
			}

			if ( result + 2 < s.length() && this.tokenString( s.substring( result, result + 3 ) ) )
			{
				return result == 0 ? 3 : result;
			}

			if ( result + 1 < s.length() && this.tokenString( s.substring( result, result + 2 ) ) )
			{
				return result == 0 ? 2 : result;
			}

			if ( this.tokenChar( s.charAt( result ) ) )
			{
				return result == 0 ? 1 : result;
			}
		}

		return result; //== s.length()
	}

	private boolean tokenChar( final char ch )
	{
		switch ( ch )
		{
		case ' ':
		case '\t':
		case '.':
		case ',':
		case '{':
		case '}':
		case '(':
		case ')':
		case '$':
		case '!':
		case '~':
		case '+':
		case '-':
		case '=':
		case '"':
		case '`':
		case '\'':
		case '*':
		case '/':
		case '%':
		case '|':
		case '^':
		case '&':
		case '[':
		case ']':
		case ';':
		case '<':
		case '>':
		case '?':
		case ':':
		case '\u2248':
			return true;
		}
		return false;
	}

	private boolean tokenString( final String s )
	{
		return Parser.multiCharTokens.contains( s );
	}

	private String restOfLine()
	{
		return this.currentLine.substring( this.currentIndex );
	}

	private boolean atEndOfFile()
	{
		this.currentToken();

		return this.currentLine.content == null;
	}

	private boolean madeProgress( final Position previousPosition, final Position currentPosition )
	{
		return previousPosition == null ||
		       previousPosition.getLine() < currentPosition.getLine() ||
		       previousPosition.getCharacter() < currentPosition.getCharacter();
	}

	final class Line
	{
		private final String content;
		private final int lineNumber;
		private final int offset;

		private final Deque<Token> tokens = new LinkedList<>();

		private final Line previousLine;
		private final Line nextLine;

		private Line( final LineNumberReader commandStream )
		{
			this( commandStream, new LinkedList<>() );
		}

		private Line( final LineNumberReader commandStream, final Deque<Line> lines )
		{
			this.previousLine = lines.peekLast();
			lines.addLast( this );

			int offset = 0;
			String line;

			try
			{
				line = commandStream.readLine();
			}
			catch ( IOException e )
			{
				// This should not happen.  Therefore, print
				// a stack trace for debug purposes.

				StaticEntity.printStackTrace( e );
				line = null;
			}

			if ( line != null )
			{
				// If the line starts with a Unicode BOM, remove it.
				if ( line.length() > 0 &&
				     line.charAt( 0 ) == Parser.BOM )
				{
					line = line.substring( 1 );
					offset += 1;
				}

				// Remove whitespace at front and end
				final String trimmed = line.trim();
				final int ltrim = line.indexOf( trimmed );

				if ( ltrim > 0 )
				{
					offset += ltrim;
				}

				line = trimmed;
			}

			this.content = line;

			if ( line != null )
			{
				this.lineNumber = commandStream.getLineNumber();
				this.offset = offset;

				this.nextLine = new Line( commandStream, lines );
			}
			else
			{
				// We are the "end of file"
				// (or there was an IOException when reading)
				this.lineNumber = this.previousLine != null ? this.previousLine.lineNumber : 0;
				this.offset = this.previousLine != null ? this.previousLine.offset : 0;

				this.nextLine = null;
			}
		}

		private String substring( final int beginIndex )
		{
			// substract "offset" from beginIndex, since
			// we already removed it
			return this.content.substring( beginIndex - this.offset );
		}

		private Token makeToken( final int tokenLength )
		{
			final Token newToken = new Token( tokenLength );
			this.tokens.addLast( newToken );
			return newToken;
		}

		private Token makeComment( final int commentLength )
		{
			final Token newToken = new Comment( commentLength );
			this.tokens.addLast( newToken );
			return newToken;
		}

		private Token peekLastToken()
		{
			if ( this.tokens.isEmpty() )
			{
				if ( this.previousLine == null )
				{
					return null;
				}

				return this.previousLine.peekLastToken();
			}

			final Token lastToken = this.tokens.peekLast();

			if ( lastToken instanceof Comment )
			{
				// Temporarily remove it before digging deeper
				this.tokens.removeLast();
				final Token previousToken = this.peekLastToken();
				this.tokens.addLast( lastToken );

				return previousToken;
			}

			return lastToken;
		}

		public String toString()
		{
			return this.content;
		}

		class Token
		{
			final String value;
			final Range range;
			final String followingWhitespaces;
			final int restOfLineStart;

			private Token( final int tokenLength )
			{
				final int offset;

				if ( !Line.this.tokens.isEmpty() )
				{
					offset = Line.this.tokens.getLast().restOfLineStart;
				}
				else
				{
					offset = Line.this.offset;
				}

				final String lineRemainder;

				if ( Line.this.content == null )
				{
					// At end of file
					this.value = ";";
					// Going forward, we can just assume lineRemainder
					// is an empty string.
					lineRemainder = "";
				}
				else
				{
					final String lineRemainderWithToken = Line.this.substring( offset );

					this.value = lineRemainderWithToken.substring( 0, tokenLength );
					lineRemainder = lineRemainderWithToken.substring( tokenLength );
				}

				// 0-indexed line
				final int lineNumber = Math.max( 0, Line.this.lineNumber - 1 );
				this.range = new Range(
					new Position( lineNumber, offset ),
					new Position( lineNumber, offset + tokenLength ) );

				final int lTrim = lineRemainder.indexOf( lineRemainder.trim() );

				this.followingWhitespaces = lineRemainder.substring( 0, lTrim );

				this.restOfLineStart = offset + tokenLength + lTrim;
			}

			/** The Line in which this token exists */
			final Line getLine()
			{
				return Line.this;
			}

			final Position getStart()
			{
				return this.range.getStart();
			}

			final Position getEnd()
			{
				return this.range.getEnd();
			}

			boolean equals( final String s )
			{
				if ( this.value == null )
				{
					return s == null;
				}

				return this.value.equals( s );
			}

			public String toString()
			{
				return this.value;
			}
		}

		private class Comment
			extends Token
		{
			Comment( final int commentLength )
			{
				super( commentLength );
			}
		}
	}

	private Position here() // FIXME temporarily made short. Rename to getCurrentPosition()
	{
		// 0-indexed
		int lineNumber = Math.max( 0, this.currentLine.lineNumber - 1 );
		return new Position( lineNumber, this.currentIndex );
	}

	private Range rangeToHere( final Position start )
	{
		return new Range( start != null ? start : this.here(), this.here() );
	}

	private Range makeInlineRange( final Position start, final int offset )
	{
		Position end = new Position( start.getLine(), start.getCharacter() + offset );

		return offset >= 0 ?
		       new Range( start, end ) :
		       new Range( end, start );
	}

	private Range makeRange( final Location start, final Location end )
	{
		return new Range( start.getRange().getStart(), end.getRange().getEnd() );
	}

	private Range makeRange( final Token start, final Token end )
	{
		return new Range( start.getStart(), end.getEnd() );
	}

	// temporary, we want to not need this
	private Range make0WidthRange()
	{
		return this.rangeToHere( this.here() );
	}

	private Location makeLocation( final Position start )
	{
		return this.makeLocation( this.rangeToHere( start ) );
	}

	private Location makeLocation( final Location start, final Location end )
	{
		return this.makeLocation( this.makeRange( start, end ) );
	}

	private Location makeLocation( final Token token )
	{
		return this.makeLocation( token.range );
	}

	private Location makeLocation( final Token start, final Token end )
	{
		return this.makeLocation( this.makeRange( start, end ) );
	}

	private Location makeLocation( final Range range )
	{
		String uri = this.fileName != null ? this.fileName : this.istream.toString();
		return new Location( uri, range );
	}

	// temporary, we want to not need this
	private Location make0WidthLocation()
	{
		return this.makeLocation( this.here() );
	}

	private Location make0WidthLocation( final Position position )
	{
		return this.makeLocation( new Range( position, position ) );
	}

	// **************** Parse errors *****************

	class AshDiagnostic
	{
		final String sourceUri;
		final Range range;
		final DiagnosticSeverity severity;
		final String message1;
		final String message2;
		List<DiagnosticRelatedInformation> relatedInformation;

		private AshDiagnostic( final Location location, final DiagnosticSeverity severity, final String message )
		{
			this( location, severity, message, "" );
		}

		private AshDiagnostic( final Location location, final DiagnosticSeverity severity, final String message1, final String message2 )
		{
			this.sourceUri = location.getUri();
			this.range = location.getRange();
			this.severity = severity;
			this.message1 = message1;
			this.message2 = message2;
		}

		public String toString()
		{
			StringBuilder result = new StringBuilder();

			result.append( this.message1 );
			result.append( " (" );

			if ( Parser.this.shortFileName == null )
			{
				result.append( Preferences.getString( "commandLineNamespace" ) );
			}
			else
			{
				result.append( Parser.this.shortFileName );
				result.append( ", line " + ( this.range.getStart().getLine() + 1 ) );
			}

			result.append( ", char " + ( this.range.getStart().getCharacter() + 1 ) );

			if ( !this.range.getStart().equals( this.range.getEnd() ) )
			{
				result.append( " to " );

				if ( this.range.getStart().getLine() < this.range.getEnd().getLine() )
				{
					result.append( "line " + ( this.range.getEnd().getLine() + 1 ) );

					if ( this.range.getEnd().getCharacter() > 0 )
					{
						result.append( ", " );
					}
				}

				if ( this.range.getStart().getCharacter() < this.range.getEnd().getCharacter() )
				{
					result.append( "char " + ( this.range.getEnd().getCharacter() + 1 ) );
				}
			}

			result.append( ")" );

			if ( this.message2 != null && this.message2.length() > 0 )
			{
				result.append( " " + message2 );
			}

			return result.toString();
		}

		public Diagnostic toLspDiagnostic()
		{
			String message = this.message1;

			if ( message2 != null && message2.length() > 0 )
			{
				message += KoLConstants.LINE_BREAK + message2;
			}

			Diagnostic diagnostic = new Diagnostic( this.range, message, this.severity, StaticEntity.getVersion() );

			if ( this.relatedInformation != null )
			{
				diagnostic.setRelatedInformation( relatedInformation );
			}

			return diagnostic;
		}
	}

	private void unexpectedTokenError( final String expected, final Token found )
	{
		String foundString = found.value;

		if ( found.getLine().content == null )
		{
			foundString = "end of file";
		}

		this.error( found, "Expected " + expected + ", found " + foundString );
	}

	private void multiplyDefinedFunctionError( final Location location, final Function f )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' defined multiple times.";
		this.error( location, buffer );
	}

	private void overridesLibraryFunctionError( final Location location, final Function f )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' overrides a library function.";
		this.error( location, buffer );
	}

	private void varargClashError( final Location location, final Function f, final Function clash )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' clashes with existing function '" +
				clash.getSignature() +
				"'.";
		this.error( location, buffer );
	}

	public final void sinceError( final String current, final String target, final Range directiveRange, final boolean targetIsRevision )
	{
		String template;
		if ( targetIsRevision )
		{
			template = "'%s' requires revision r%s of kolmafia or higher (current: r%s).  Up-to-date builds can be found at https://ci.kolmafia.us/.";
		}
		else
		{
			template = "'%s' requires version %s of kolmafia or higher (current: %s).  Up-to-date builds can be found at https://ci.kolmafia.us/.";
		}

		this.error( this.makeLocation( directiveRange ), String.format( template, this.shortFileName, target, current ) );
	}

	public static String undefinedFunctionMessage( final String name, final List<Value> params )
	{
		StringBuilder buffer = new StringBuilder();
		buffer.append( "Function '" );
		Parser.appendFunctionCall( buffer, name, params );
		buffer.append( "' undefined.  This script may require a more recent version of KoLmafia and/or its supporting scripts." );
		return buffer.toString();
	}

	private void enforceSince( String revision, final Range directiveRange )
	{
		try
		{
			if ( revision.startsWith( "r" ) ) // revision
			{
				revision = revision.substring( 1 );
				int targetRevision = Integer.parseInt( revision );
				int currentRevision = StaticEntity.getRevision();
				if ( currentRevision < targetRevision )
				{
					this.sinceError( String.valueOf( currentRevision ), revision, directiveRange, true );
					return;
				}
			}
			else // version (or syntax error)
			{
				String [] target = revision.split( "\\." );
				if ( target.length != 2 )
				{
					this.error( directiveRange, "invalid 'since' format" );
					return;
				}

				int targetMajor = Integer.parseInt( target[ 0 ] );
				int targetMinor = Integer.parseInt( target[ 1 ] );

				// strip "KoLMafia v" from the front
				String currentVersion = StaticEntity.getVersion();
				currentVersion = currentVersion.substring( currentVersion.indexOf( "v" ) + 1 );

				// Strip " rxxxx" from end
				int rindex = currentVersion.indexOf( " r" );
				if ( rindex != -1 )
				{
					currentVersion = currentVersion.substring( 0, rindex );
				}

				String [] current = currentVersion.split( "\\." );
				int currentMajor = Integer.parseInt( current[ 0 ] );
				int currentMinor = Integer.parseInt( current[ 1 ] );

				if ( targetMajor > currentMajor || ( targetMajor == currentMajor && targetMinor > currentMinor ) )
				{
					this.sinceError( currentVersion, revision, directiveRange, false );
					return;
				}
			}
		}
		catch ( NumberFormatException e )
		{
			this.error( directiveRange, "invalid 'since' format" );
		}
	}

	public final void error( final String msg )
	{
		this.error( msg, "" );
	}

	public final void error( final String msg1, final String msg2 )
	{
		this.error( this.here(), msg1, msg2 );
	}

	public final void error( final Position start, final String msg )
	{
		this.error( start, msg, "" );
	}

	public final void error( final Position start, final String msg1, final String msg2 )
	{
		this.error( this.rangeToHere( start ), msg1, msg2 );
	}

	public final void error( final Token token, final String msg )
	{
		this.error( token, msg, "" );
	}

	public final void error( final Token token, final String msg1, final String msg2 )
	{
		this.error( token.range, msg1, msg2 );
	}

	public final void error( final Token start, final Token end, final String msg )
	{
		this.error( start, end, msg, "" );
	}

	public final void error( final Token start, final Token end, final String msg1, final String msg2 )
	{
		this.error( this.makeRange( start, end ), msg1, msg2 );
	}

	public final void error( final Range range, final String msg )
	{
		this.error( range, msg, "" );
	}

	public final void error( final Range range, final String msg1, final String msg2 )
	{
		this.error( this.makeLocation( range ), msg1, msg2 );
	}

	public final void error( final Location location, final String msg )
	{
		this.error( location, msg, "" );
	}

	public final void error( final Location location, final String msg1, final String msg2 )
	{
		this.diagnostics.add( new AshDiagnostic( location != null ? location : this.make0WidthLocation(), Error, msg1, msg2 ) );
	}

	public final void warning( final String msg )
	{
		this.warning( msg, "" );
	}

	public final void warning( final String msg1, final String msg2 )
	{
		this.warning( this.here(), msg1, msg2 );
	}

	public final void warning( final Position start, final String msg )
	{
		this.warning( start, msg, "" );
	}

	public final void warning( final Position start, final String msg1, final String msg2 )
	{
		this.warning( this.rangeToHere( start ), msg1, msg2 );
	}

	public final void warning( final Token token, final String msg )
	{
		this.warning( token, msg, "" );
	}

	public final void warning( final Token token, final String msg1, final String msg2 )
	{
		this.warning( token.range, msg1, msg2 );
	}

	public final void warning( final Token start, final Token end, final String msg )
	{
		this.warning( start, end, msg, "" );
	}

	public final void warning( final Token start, final Token end, final String msg1, final String msg2 )
	{
		this.warning( new Range( start.getStart(), end.getEnd() ), msg1, msg2 );
	}

	public final void warning( final Range range, final String msg )
	{
		this.warning( range, msg, "" );
	}

	public final void warning( final Range range, final String msg1, final String msg2 )
	{
		this.warning( this.makeLocation( range ), msg1, msg2 );
	}

	public final void warning( final Location location, final String msg )
	{
		this.warning( location, msg, "" );
	}

	public final void warning( final Location location, final String msg1, final String msg2 )
	{
		this.diagnostics.add( new AshDiagnostic( location != null ? location : this.make0WidthLocation(), Warning, msg1, msg2 ) );
	}

	private static void appendFunctionCall( final StringBuilder buffer, final String name, final List<Value> params )
	{
		buffer.append( name );
		buffer.append( "(" );

		String sep = " ";
		for ( Value current : params )
		{
			buffer.append( sep );
			sep = ", ";
			buffer.append( current.getType() );
		}

		buffer.append( " )" );
	}

	public static String getLineAndFile( final String fileName, final int lineNumber )
	{
		if ( fileName == null )
		{
			return "(" + Preferences.getString( "commandLineNamespace" ) + ")";
		}

		return "(" + fileName + ", line " + lineNumber + ")";
	}

	public static void printIndices( final List<Value> indices, final PrintStream stream, final int indent )
	{
		if ( indices == null )
		{
			return;
		}

		for ( Value current : indices )
		{
			AshRuntime.indentLine( stream, indent );
			stream.println( "<KEY>" );
			current.print( stream, indent + 1 );
		}
	}
}

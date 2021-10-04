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

import java.net.URI;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
import net.sourceforge.kolmafia.textui.parsetree.Value.LocatedValue;
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
	private final URI fileURI;
	private final long modificationDate;
	private String scriptName;
	private final InputStream istream;

	private Line currentLine;
	private int currentIndex;
	private Token currentToken;

	private final Map<File, Parser> imports;
	private Function mainMethod = null;
	private String notifyRecipient = null;

	public final List<AshDiagnostic> diagnostics = new ArrayList<>();

	public Parser()
	{
		this( null, null, null );
	}

	public Parser( final File scriptFile, final Map<File, Parser> imports )
	{
		this( scriptFile, null, imports );
	}

	public Parser( final File scriptFile, final InputStream stream, final Map<File, Parser> imports )
	{
		this.imports = imports != null ? imports : new TreeMap<>();

		this.istream = stream != null ? stream :
		               scriptFile != null ? DataUtilities.getInputStream( scriptFile ) :
		               null;

		if ( scriptFile != null )
		{
			this.fileName = scriptFile.getPath();
			this.shortFileName = this.fileName.substring( this.fileName.lastIndexOf( File.separator ) + 1 );
			this.fileURI = scriptFile.toURI();
			this.modificationDate = scriptFile.lastModified();

			if ( this.imports.isEmpty() )
			{
				this.imports.put( scriptFile, this );
			}
		}
		else
		{
			this.fileName = null;
			this.shortFileName = null;
			this.fileURI = null;
			this.modificationDate = 0L;
		}

		if ( this.istream == null )
		{
			return;
		}

		try
		{
			final LineNumberReader commandStream = new LineNumberReader( new InputStreamReader( this.istream, StandardCharsets.UTF_8 ) );
			this.currentLine = new Line( commandStream );

			Line line = this.currentLine;
			while ( line.content != null )
			{
				line = new Line( commandStream, line );
			}

			// Move up to the first non-empty line
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
		throws InterruptedException
	{
		if ( this.istream == null )
		{
			throw new RuntimeException( "Parser was not properly initialized before parsing was attempted" );
		}

		Scope scope = this.parseScope( null, null, null, Parser.getExistingFunctionScope(), false, false );

		if ( this.currentLine.nextLine != null )
		{
			throw this.parseException( "Script parsing error" );
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
		if ( this.istream == null )
		{
			return 0;
		}

		return this.currentLine.lineNumber;
	}

	public Map<File, Parser> getImports()
	{
		return this.imports;
	}

	public long getModificationDate()
	{
		return this.modificationDate;
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
		return new Scope( RuntimeLibrary.functions.clone(), null, DataTypes.simpleTypes.clone() );
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
		throws InterruptedException
	{
		return this.importFile( fileName, scope, null );
	}

	private Scope importFile( final String fileName, final Scope scope, final Location location )
		throws InterruptedException
	{
		if ( Thread.interrupted() )
		{
			throw new InterruptedException();
		}

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
			if ( location != null )
			{
				this.error( location, "too many matches for " + fileName + ": " + s );
			}
			else
			{
				this.error( "too many matches for " + fileName + ": " + s );
			}

			return scope;
		}
		if ( matches.size() == 0 )
		{
			if ( location != null )
			{
				this.error( location, fileName + " could not be found" );
			}
			else
			{
				this.error( fileName + " could not be found" );
			}

			return scope;
		}

		File scriptFile = matches.get( 0 );

		if ( this.imports.containsKey( scriptFile ) )
		{
			return scope;
		}

		this.imports.put( scriptFile, scriptFile.lastModified() );

		Parser parser = new Parser( scriptFile, null, this.imports );
		Scope result = parser.parseScope( scope, null, null, scope.getParentScope(), false, false );
		if ( parser.currentLine.nextLine != null )
		{
			throw this.parseException( "Script parsing error" );
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

	protected Parser getParser( final File scriptFile )
	{
		return new Parser( scriptFile, null, this.imports );
	}

	private Scope parseCommandOrDeclaration( final Scope result, final Type expectedType )
		throws InterruptedException
	{
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
				result.addCommand( c, this );
			}
			else
			{
				this.error( "command or declaration required" );
			}
		}
		else if ( this.parseVariables( t, result ) )
		{
			if ( this.currentToken().equals( ";" ) )
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
			throw this.parseException( "Type given but not used to declare anything" );
		}

		return result;
	}

	private Scope parseFile( final Scope startScope )
		throws InterruptedException
	{
		Scope scope = startScope != null ? startScope :
		              new Scope( (VariableList) null, Parser.getExistingFunctionScope() );

		this.parseScriptName();
		this.parseNotify();
		this.parseSince();

		Directive importDirective;
		while ( ( importDirective = this.parseImport() ) != null )
		{
			scope = this.importFile( importDirective.value, scope, this.makeLocation( importDirective.range ) );
		}

		this.parseScope( scope, null, scope.getParentScope(), true, false, false );

		if ( this.currentLine.nextLine != null )
		{
			this.error( "Script parsing error; thought we reached the end of the file" );
		}

		return scope;
	}

	private Scope parseScope( final Type expectedType,
	                          final VariableList variables,
	                          final BasicScope parentScope,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
		throws InterruptedException
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
		throws InterruptedException
	{
		Position previousPosition = null;
		while ( !this.atEndOfFile() )
		{
			// Infinite loop prevention
			if ( !this.madeProgress( previousPosition, previousPosition = this.getCurrentPosition() ) )
			{
				if ( !wholeFile )
				{
					break;
				}

				// If we're at the top scope of a file, and we reached a node we
				// couldn't parse, just read the current token and continue
				this.readToken();

				this.error( this.peekLastToken(), "Empty or unknown node" );
				continue;
			}

			if ( this.parseTypedef( result ) )
			{
				if ( this.currentToken().equals( ";" ) )
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
			if ( t.getType() == DataTypes.TYPE_RECORD && this.currentToken().equals( ";" ) )
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
				if ( this.currentToken().equals( ";" ) )
				{
					this.readToken(); //read ;
				}
				else
				{
					this.readToken(); //read ;
				}
				else
				{
					this.unexpectedTokenError( ";", this.currentToken() );
				}

				continue;
			}

			if ( ( t.getBaseType() instanceof AggregateType ) && this.currentToken().equals( "{" ) )
			{
				result.addCommand( this.parseAggregateLiteral( result, (AggregateType) t ), this );
			}
			else
			{
				//Found a type but no function or variable to tie it to
				this.error( this.makeLocation( t.getLocation(), this.currentToken() ), "Type given but not used to declare anything" );
			}
		}

		return result;
	}

	private Type parseRecord( final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "record" ) )
		{
			return null;
		}

		Token recordStartToken = this.currentToken();

		this.readToken(); // read record

		if ( this.currentToken().equals( ";" ) )
		{
			this.error( this.currentToken(), "Record name expected" );

			return new BadRecordType( null, this.makeLocation( recordStartToken ) );
		}

		// Allow anonymous records
		String recordName = null;

		boolean recordError = false;
		boolean recordSyntaxError = false;

		if ( !this.currentToken().equals( "{" ) )
		{
			// Named record
			recordName = this.currentToken().content;

			if ( !this.parseIdentifier( recordName ) )
			{
				throw this.parseException( "Invalid record name '" + recordName + "'" );
			}
			else if ( Parser.isReservedWord( recordName ) )
			{
				throw this.parseException( "Reserved word '" + recordName + "' cannot be a record name" );
			}
			else if ( parentScope.findType( recordName ) != null )
			{
				this.error( recordDefinition, "Reserved word '" + recordName + "' cannot be a record name" );
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

		if ( this.currentToken().equals( "{" ) )
		{
			this.readToken(); // read {
		}
		else
		{
			this.readToken(); // read {
		}
		else
		{
			this.parseException( "{", this.currentToken() );

		// Loop collecting fields
		List<Type> fieldTypes = new ArrayList<Type>();
		List<String> fieldNames = new ArrayList<String>();

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.getCurrentPosition() ) )
		{
			if ( this.atEndOfFile() )
			{
				throw this.parseException( "}", this.currentToken() );
			}

			if ( this.currentToken().equals( "}" ) )
			{
				if ( fieldTypes.isEmpty() )
				{
					throw this.parseException( "Record field(s) expected" );
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

			if ( fieldType.getBaseType().equals( DataTypes.VOID_TYPE ) )
			{
				throw this.parseException( "Non-void field type expected" );
			}

			// Get the field name
			Token fieldName = this.currentToken();
			if ( fieldName.equals( ";" ) )
			{
				throw this.parseException( "Field name expected" );
			}
			else if ( !this.parseIdentifier( fieldName.content ) )
			{
				throw this.parseException( "Invalid field name '" + fieldName + "'" );
			}
			else if ( Parser.isReservedWord( fieldName.content ) )
			{
				if ( !fieldError )
				{
					this.error( this.currentToken(), "Reserved word '" + fieldName + "' cannot be used as a field name" );
				}
				recordError = fieldError = true;

				fieldName = null;
				this.readToken(); // read name
			}
			else if ( fieldNames.contains( fieldName.content ) )
			{
				this.readToken(); // read name
			}
			else
			{
				this.readToken(); // read name
			}

			fieldTypes.add( fieldType );
			fieldNames.add( fieldName.content.toLowerCase() );

			if ( this.currentToken().equals( ";" ) )
			{
				this.readToken(); // read ;
			}
			else
			{
				throw this.parseException( ";", this.currentToken() );
			}
		}

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
		throws InterruptedException
	{
		if ( !this.parseIdentifier( this.currentToken().content ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		Token functionName = this.currentToken();

		if ( Parser.isReservedWord( functionName.content ) )
		{
			this.error( functionNameToken, "Reserved word '" + functionName + "' cannot be used as a function name" );
			functionError = true;
		}

		this.readToken(); //read (

		VariableList paramList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		boolean vararg = false;

		while ( true )
		{
			if ( this.currentToken().equals( ")" ) )
			{
				this.readToken(); //read )
				break;
			}
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

			if ( this.currentToken().equals( "..." ) )
			{
				// Make a vararg type out of the previously parsed type.
				paramType = new VarArgType( paramType );
				paramType = new TypeReference( paramType, this.makeLocation( parameterTypeToken, this.currentToken() ) );

				this.readToken(); //read ...
			}

			Token paramNameToken = this.currentToken();

			Variable param = this.parseVariable( paramType, parentScope, false );
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
				throw this.parseException( "Parameter " + param.getName() + " is already defined" );
			}
			else
			{
				variableReferences.add( new VariableReference( param ) );
			}

			if ( !this.currentToken().equals( ")" ) )
			{
				// Only one vararg is allowed
				vararg = true;
			}

				if ( this.currentToken().equals( "," ) )
				{
					this.readToken(); //read comma
				}
				else
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

		UserDefinedFunction f = new UserDefinedFunction( functionName.content, functionType, variableReferences );

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

		if ( this.currentToken().equals( ";" ) )
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
		throws InterruptedException
	{
		while ( true )
		{
			Variable v = this.parseVariable( t, parentScope, true );
			if ( v == null )
			{
				return false;
			}

			if ( this.currentToken().equals( "," ) )
			{
				this.readToken(); //read ,
				continue;
			}

			return true;
		}
	}

	private Variable parseVariable( final Type t, final BasicScope scope, final boolean allowInitialization )
		throws InterruptedException
	{
		if ( !this.parseIdentifier( this.currentToken().content ) )
		{
			return null;
		}

		Token variableName = this.currentToken();
		Variable result;

		if ( Parser.isReservedWord( variableName.content ) )
		{
			throw this.parseException( "Reserved word '" + variableName + "' cannot be a variable name" );
		}
		else if ( scope != null && scope.findVariable( variableName.content ) != null )
		{
			throw this.parseException( "Variable " + variableName + " is already defined" );
		}
		else
		{
			result = new Variable( variableName.content, t );
		}

		this.readToken(); // If parsing of Identifier succeeded, go to next token.

		Variable result;

		if ( Parser.isReservedWord( variableName ) )
		{
			this.error( variableToken, "Reserved word '" + variableName + "' cannot be a variable name" );
			result = new BadVariable( variableName, t, this.makeLocation( variableToken ) );
			variableError = true;
		}
		else if ( allowInitialization && scope.findVariable( variableName ) != null )
		{
			this.error( variableToken, "Variable " + variableName + " is already defined" );
			result = new BadVariable( variableName, t, this.makeLocation( variableToken ) );
			variableError = true;
		}
		else
		{
			result = new Variable( variableName, t, this.makeLocation( variableToken ) );
		}

		// If we are parsing a parameter declaration, we are done.
		// Otherwise, we must initialize the variable.

		LocatedValue rhs;

		Token postVariableToken = this.currentToken();

		Type ltype = t.getBaseType();
		if ( this.currentToken().equals( "=" ) )
		{
			this.readToken(); // read =

			if ( this.currentToken().equals( "{" ) )
			{
				if ( ltype instanceof AggregateType )
				{
					rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
				}
				else
				{
					rhs = this.parseAggregateLiteral( scope, new BadAggregateType() );

					if ( !variableError && allowInitialization && !ltype.isBad() )
					{
						Location errorLocation = rhs != null ? rhs.location :
							this.makeLocation( this.peekLastToken() );

						this.error( errorLocation, "Cannot initialize " + variableName + " of type " + t + " with an aggregate literal" );
						variableError = true;
					}
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

			if ( rhs != null )
			{
				rhs = this.autoCoerceValue( t, rhs, scope );
				if ( !Operator.validCoercion( ltype, rhs.getType(), "assign" ) )
				{
					throw this.parseException( "Cannot store " + rhs.getType() + " in " + variableName + " of type " + ltype );
				}
			}
			else
			{
				throw this.parseException( "Expression expected" );
			}
		}
		else if ( this.currentToken().equals( "{" ) && ltype instanceof AggregateType )
		{
			rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
		}
		else
		{
			rhs = null;
		}

		scope.addVariable( result );
		VariableReference lhs = new VariableReference( variableName.content, scope );
		scope.addCommand( new Assignment( lhs, rhs ), this );

		return result;
	}

	private LocatedValue autoCoerceValue( Type ltype, final LocatedValue rhs, final BasicScope scope )
	{
		// DataTypes.TYPE_ANY has no name
		if ( ltype == null || ltype.getName() == null )
		{
			return rhs;
		}

		// Error propagation
		if ( ltype.isBad() || rhs.value.getType().isBad() )
		{
			return Value.BAD_VALUE.wrap( rhs.location );
		}

		if ( ltype instanceof TypeReference )
		{
			ltype = ((TypeReference) ltype).getTarget();
		}

		// If the types are the same no coercion needed
		// A TypeDef or a RecordType match names for equal.
		Type rtype = rhs.value.getRawType();
		if ( ltype.equals( rtype ) )
		{
			return rhs;
		}

		// Look for a function:  LTYPE to_LTYPE( RTYPE )
		String name = "to_" + ltype.getName();
		List<Value> params = Collections.singletonList( rhs.value );

		// A typedef can overload a coercion function to a basic type or a typedef
		if ( ltype instanceof TypeDef || ltype instanceof RecordType )
		{
			Function target = scope.findFunction( name, params, MatchType.EXACT );
			if ( target != null && target.getType().equals( ltype ) )
			{
				scope.addReference( target, rhs.location );
				return new FunctionCall( target, params, this )
					.wrap( rhs.location );
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
				scope.addReference( target, rhs.location );
				return new FunctionCall( target, params, this )
					.wrap( rhs.location );
			}
		}

		// No overloaded coercions found for typedefs or records
		return rhs;
	}

	private List<LocatedValue> autoCoerceParameters( final Function target, final List<LocatedValue> params, final BasicScope scope )
	{
		ListIterator<VariableReference> refIterator = target.getVariableReferences().listIterator();
		ListIterator<LocatedValue> valIterator = params.listIterator();
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

			LocatedValue currentValue = valIterator.next();
			LocatedValue coercedValue = this.autoCoerceValue( paramType, currentValue, scope );
			valIterator.set( coercedValue );
		}

		return params;
	}

	private boolean parseTypedef( final Scope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "typedef" ) )
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

		Token typeName = this.currentToken();

		if ( typeName.equals( ";" ) )
		{
			throw this.parseException( "Type name expected" );
		}
		else if ( !this.parseIdentifier( typeName.content ) )
		{
			if ( !typedefError )
			{
				this.error( typeToken, "Invalid type name '" + typeName + "'" );
				typedefError = true;
			}

			typeName = null;
			// don't read
		}
		else if ( Parser.isReservedWord( typeName.content ) )
		{
			this.readToken(); // read name
		}
		else
		{
			this.readToken(); // read name
		}

		Type existingType = parentScope.findType( typeName.content );
		if ( existingType != null )
		{
			if ( existingType.getBaseType().equals( t ) ||
			     existingType.isBad() )
			{
				// It is OK to redefine a typedef with an equivalent type
				return true;
			}
				
			throw this.parseException( "Type name '" + typeName + "' is already defined" );
		}
		else
		{
			// Add the type to the type table
			TypeDef type = new TypeDef( typeName.content, t );
			parentScope.addType( type );
		}

		return true;
	}

	private Command parseCommand( final Type functionType,
	                              final BasicScope scope,
	                              final boolean noElse,
	                              final boolean allowBreak,
	                              final boolean allowContinue )
		throws InterruptedException
	{
		Command result;

		if ( this.currentToken().equalsIgnoreCase( "break" ) )
		{
			if ( allowBreak )
			{
				result = new LoopBreak( this.makeLocation( this.currentToken() ) );
			}
			else
			{
				result = new LoopBreak();
			}
			else
			{
				this.error( commandStart, "Encountered 'break' outside of loop" );

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
				result = new LoopContinue();
			}
			else
			{
				this.error( commandStart, "Encountered 'continue' outside of loop" );

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
		else
		{
			LocatedValue value = this.parseValue( scope );

			if ( value == null )
			{
				return null;
			}

			result = value.value;
		}

		if ( this.currentToken().equals( ";" ) )
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
		throws InterruptedException
	{
		if ( !this.parseIdentifier( this.currentToken().content ) )
		{
			return null;
		}

		Type valType = scope.findType( this.currentToken().content );
		if ( valType == null )
		{
			if ( !records )
			{
				valType = this.parseRecord( scope );

				if ( valType == null )
				{
					return null;
				}

				if ( this.currentToken().equals( "[" ) )
				{
					return this.parseAggregateType( valType, scope );
				}

				return valType;
			}
		}
		else if ( ( valType = scope.findType( this.currentToken().value ) ) != null )
		{
			scope.addReference( valType, this.makeLocation( this.currentToken() ) );
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

		this.readToken();

		if ( this.currentToken().equals( "[" ) )
		{
			return this.parseAggregateType( valType, scope );
		}

		return valType;
	}

	/**
	 * Parses the content of an aggregate literal, e.g., `{1:true, 2:false, 3:false}`.
	 *
	 * <p>The presence of the opening bracket "{" is ALWAYS assumed when entering this method,
	 * and as such, MUST be checked before calling it. This method will never return null.
	 */
	private Value parseAggregateLiteral( final BasicScope scope, final AggregateType aggr )
	{
		this.readToken(); // read {

		Type index = aggr.getIndexType();
		Type data = aggr.getDataType();

		List<Value> keys = new ArrayList<Value>();
		List<Value> values = new ArrayList<Value>();

		boolean aggregateError = false;

		// If index type is an int, it could be an array or a map
		boolean arrayAllowed = index.equals( DataTypes.INT_TYPE );

		// Assume it is a map.
		boolean isArray = false;

		while ( true )
		{
			if ( this.atEndOfFile() )
			{
				throw this.parseException( "}", this.currentToken() );
			}

			if ( this.currentToken().equals( "}" ) )
			{
				this.readToken(); // read }
				break;
			}

			Value lhs;

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
			if ( ( isArray || arrayAllowed ) && this.currentToken().equals( "{" ) && dataType instanceof AggregateType )
			{
				lhs = parseAggregateLiteral( scope, (AggregateType) dataType );
			}
			else
			{
				lhs = this.parseExpression( scope );
			}

			if ( lhs == null )
			{
				Location errorLocation = this.makeLocation( this.currentToken() );

				if ( !aggregateError )
				{
					this.error( errorLocation, "Script parsing error; couldn't figure out value of aggregage key" );
					aggregateError = true;
				}

				lhs = Value.BAD_VALUE.wrap( errorLocation );
			}

			Token delim = this.currentToken();

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
					if ( !aggregateError && !Operator.validCoercion( dataType, lhs.value.getType(), "assign" ) )
					{
						this.error( lhs.location, "Invalid array literal; cannot assign type " +
							dataType.toString() +
								" to type " +
							lhs.value.getType().toString() );
						aggregateError = true;
					}

					values.add( lhs.value );
				}
				else if ( !aggregateError )
				{
					this.unexpectedTokenError( ":", this.currentToken() );
					aggregateError = true;
				}

				// Move on to the next value
				if ( delim.equals( "," ) )
				{
					this.readToken(); // read ;
				}
				else if ( !delim.equals( "}" ) )
				{
					throw this.parseException( "}", delim );
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
				this.error( lhs.location, "cannot include keys when making an array literal" );
				aggregateError = true;
			}

			Value rhs;
			if ( this.currentToken().equals( "{" ) && dataType instanceof AggregateType )
			{
				rhs = parseAggregateLiteral( scope, (AggregateType) dataType );
			}
			else
			{
				rhs = this.parseExpression( scope );
			}

			if ( rhs == null )
			{
				Location errorLocation = this.makeLocation( this.currentToken() );

				if ( !aggregateError )
				{
					this.error( errorLocation, "Script parsing error; couldn't figure out value of aggregage value" );
					aggregateError = true;
				}

				rhs = Value.BAD_VALUE.wrap( errorLocation );
			}

			// Check that each type is valid via validCoercion
			lhs = this.autoCoerceValue( index, lhs, scope );
			rhs = this.autoCoerceValue( data, rhs, scope );
			if ( !aggregateError )
			{
				if ( !Operator.validCoercion( index, lhs.value.getType(), "assign" ) )
				{
					this.error( lhs.location, "Invalid map literal; cannot assign type " +
							dataType.toString() +
							" to key of type " +
							lhs.value.getType().toString() );
					aggregateError = true;
				}

				if ( !Operator.validCoercion( data, rhs.value.getType(), "assign" ) )
				{
					this.error( rhs.location, "Invalid map literal; cannot assign type " +
							dataType.toString() +
							" to value of type " +
							rhs.value.getType().toString() );
					aggregateError = true;
				}
			}

			keys.add( lhs.value );
			values.add( rhs.value );

			// Move on to the next value
			if ( this.currentToken().equals( "," ) )
			{
				this.readToken(); // read ,
			}
			else if ( !this.currentToken().equals( "}" ) )
			{
				throw this.parseException( "}", this.currentToken() );
			}
		}

		Location aggregateLiteralLocation = this.makeLocation( aggregateLiteralStartToken, this.peekPreviousToken() );

		if ( isArray )
		{
			int size = aggr.getSize();
			if ( !aggregateError && size > 0 && size < values.size() )
			{
				this.error( aggregateLiteralLocation, "Array has " + size + " elements but " + values.size() + " initializers." );
				aggregateError = true;
			}
		}

		Value result = aggregateError ? Value.BAD_VALUE :
		               isArray ? new ArrayLiteral( aggr, values ) :
		               new MapLiteral( aggr, keys, values );

		return result.wrap( aggregateLiteralLocation );
	}

	private Type parseAggregateType( Type dataType, final BasicScope scope )
		throws InterruptedException
	{
		Token separatorToken = this.currentToken();

		this.readToken(); // [ or ,

		Type indexType = null;
		int size = 0;

		if ( this.currentToken().equals( "]" ) )
		{
			if ( !separatorToken.equals( "[" ) )
			{
				throw this.parseException( "Missing index token" );
			}
		}
		else if ( this.readIntegerToken( this.currentToken().content ) )
		{
			size = StringUtilities.parseInt( this.currentToken().content );
			this.readToken(); // integer
		}
		else if ( this.parseIdentifier( this.currentToken().content ) )
		{
			indexType = scope.findType( this.currentToken().content );

			if ( indexType != null )
			{
				if ( !indexType.isPrimitive() )
				{
					throw this.parseException( "Index type '" + this.currentToken() + "' is not a primitive type" );
				}
			}
			else
			{
				throw this.parseException( "Invalid type name '" + this.currentToken() + "'" );
			}

			this.readToken(); // type name
		}
		else
		{
			throw this.parseException( "Missing index token" );
		}

		if ( this.currentToken().equals( "," ) ||
		     ( this.currentToken().equals( "]" ) &&
		       "[".equals( this.nextToken() ) ) )
		{
			if ( this.currentToken().equals( "]" ) )
			{
				this.readToken(); // ]
			}

			dataType = this.parseAggregateType( dataType, scope );
		}
		else if ( this.currentToken().equals( "]" ) )
		{
			this.readToken(); // ]
		}
		else
		{
			throw this.parseException( ", or ]", this.currentToken() );
		}

		return indexType != null ?
			new AggregateType( dataType, indexType ) :
			new AggregateType( dataType, size );
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
			if ( !Character.isLetterOrDigit( identifier.charAt( i ) ) && identifier.charAt( i ) != '_' && identifier.charAt( i ) != '@' )
			{
				return false;
			}
		}

		return true;
	}

	private FunctionReturn parseReturn( final Type expectedType, final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "return" ) )
		{
			return null;
		}

		Token returnStartToken = this.currentToken();

		this.readToken(); //return

		if ( expectedType == null )
		{
			throw this.parseException( "Cannot return when outside of a function" );
		}

		if ( this.currentToken().equals( ";" ) )
		{
			if ( expectedType != null && !expectedType.equals( DataTypes.TYPE_VOID ) )
			{
				throw this.parseException( "Return needs " + expectedType + " value" );
			}

			return new FunctionReturn( this.makeLocation( returnStartToken ), null, DataTypes.VOID_TYPE );
		}

		LocatedValue value = this.parseExpression( parentScope );

		if ( value != null )
		{
			value = this.autoCoerceValue( expectedType, value, parentScope );
		}
		else
		{
			value = this.autoCoerceValue( expectedType, value, parentScope );
		}
		else
		{
			this.error( "Expression expected" );

			value = Value.BAD_VALUE.wrap( errorLocation );
		}


		if ( expectedType == null )
		{
		}
		else if ( expectedType.equals( DataTypes.TYPE_VOID ) )
		{
			this.error( value.location, "Cannot return a value from a void function" );
		}
		else if ( !Operator.validCoercion( expectedType, value.value.getType(), "return" ) )
		{
			this.error( value.location, "Cannot return " + value.value.getType() + " value from " + expectedType + " function" );
		}

		Location returnLocation = this.makeLocation( returnStartToken, this.peekPreviousToken() );
		return new FunctionReturn( returnLocation, value.value, expectedType );
	}

	private Scope parseSingleCommandScope( final Type functionType,
	                                       final BasicScope parentScope,
	                                       final boolean noElse,
	                                       final boolean allowBreak,
	                                       final boolean allowContinue )
		throws InterruptedException
	{
		Scope result = new Scope( parentScope );

		ParseTreeNode command = this.parseCommand( functionType, parentScope, noElse, allowBreak, allowContinue );
		if ( command != null )
		{
			result.addCommand( command, this );
		}
		else
		{
			if ( this.currentToken().equals( ";" ) )
			{
				this.readToken(); // ;
			}
			else
			{
				this.readToken(); // ;
			}
			else
			{
				this.unexpectedTokenError( ";", this.currentToken() );
			}
		}

		return result;
	}

	private Scope parseBlockOrSingleCommand( final Type functionType,
	                                         final VariableList variables,
	                                         final BasicScope parentScope,
	                                         final boolean noElse,
	                                         final boolean allowBreak,
	                                         final boolean allowContinue )
		throws InterruptedException
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
		throws InterruptedException
	{
		if ( !this.currentToken().equals( "{" ) )
		{
			return null;
		}

		Token blockStartToken = this.currentToken();

		this.readToken(); // {

		Scope scope = this.parseScope( functionType, variables, parentScope, allowBreak, allowContinue );

		if ( this.currentToken().equals( "}" ) )
		{
			this.readToken(); //read }
		}
		else
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
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "if" ) )
		{
			return null;
		}

		Token conditionalStartToken = this.currentToken();

		this.readToken(); // if

		if ( this.currentToken().equals( "(" ) )
		{
			this.readToken(); // (
		}
		else
		{
			throw this.parseException( "(", this.currentToken() );
		}

		Value condition = this.parseExpression( parentScope );

		if ( this.currentToken().equals( ")" ) )
		{
			this.readToken(); // )
		}
		else
		{
			this.readToken(); // )
		}
		else if ( !ifError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			ifError = true;
		}

		if ( condition == null ||
		     !condition.value.getType().isBad() &&
		     !condition.value.getType().equals( DataTypes.BOOLEAN_TYPE ) )
		{
			Location errorLocation = condition != null ? condition.location :
				this.makeLocation( this.currentToken() );

			if ( !ifError )
			{
				this.error( errorLocation, "\"if\" requires a boolean conditional expression" );
				ifError = true;
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
				result = new If( conditionalLocation, scope, condition.value );
			}
			else if ( finalElse )
			{
				result.addElseLoop( new Else( conditionalLocation, scope, condition.value ) );
			}
			else
			{
				result.addElseLoop( new ElseIf( conditionalLocation, scope, condition.value ) );
			}

			if ( !noElse && this.currentToken().equalsIgnoreCase( "else" ) )
			{
				conditionalStartToken = this.currentToken();

				if ( finalElse && !elseError )
				{
					this.error( conditionalStartToken, "Else without if" );
					elseError = true;
				}

				this.readToken(); //else
				if ( this.currentToken().equalsIgnoreCase( "if" ) )
				{
					this.readToken(); //if

					if ( this.currentToken().equals( "(" ) )
					{
						this.readToken(); //(
					}
					else
					{
						this.unexpectedTokenError( "(", this.currentToken() );
						elseError = true;
					}

					condition = this.parseExpression( parentScope );

					if ( this.currentToken().equals( ")" ) )
					{
						this.readToken(); // )
					}
					else
					{
						this.unexpectedTokenError( ")", this.currentToken() );
						elseError = true;
					}

					if ( condition == null ||
					     !condition.value.getType().isBad() &&
					     !condition.value.getType().equals( DataTypes.BOOLEAN_TYPE ) )
					{
						throw this.parseException( "\"if\" requires a boolean conditional expression" );
					}
				}
				else
				//else without condition
				{
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
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "cli_execute" ) )
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

			if ( this.currentToken().equals( "}" ) )
			{
				this.readToken(); // }
				break;
			}

			this.clearCurrentToken();

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
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "while" ) )
		{
			return null;
		}

		Token whileStartToken = this.currentToken();

		this.readToken(); // while

		if ( this.currentToken().equals( "(" ) )
		{
			this.readToken(); // (
		}
		else
		{
			throw this.parseException( "(", this.currentToken() );
		}

		Value condition = this.parseExpression( parentScope );

		if ( this.currentToken().equals( ")" ) )
		{
			this.readToken(); // )
		}
		else
		{
			this.readToken(); // )
		}
		else if ( !whileError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			whileError = true;
		}

		if ( condition == null ||
		     !condition.value.getType().isBad() &&
		     !condition.value.getType().equals( DataTypes.BOOLEAN_TYPE ) )
		{
			Location errorLocation = condition != null ? condition.location :
				this.makeLocation( this.currentToken() );

			if ( !whileError )
			{
				this.error( errorLocation, "\"while\" requires a boolean conditional expression" );
				whileError = true;
			}

		Scope scope = this.parseLoopScope( functionType, null, parentScope );

		Location whileLocation = this.makeLocation( whileStartToken, this.peekPreviousToken() );
		return new WhileLoop( whileLocation, scope, condition.value );
	}

	private Loop parseRepeat( final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "repeat" ) )
		{
			return null;
		}

		Token repeatStartToken = this.currentToken();

		this.readToken(); // repeat

		boolean repeatError = false;

		Scope scope = this.parseLoopScope( functionType, null, parentScope );

		if ( this.currentToken().equalsIgnoreCase( "until" ) )
		{
			this.readToken(); // until
		}
		else
		{
			this.unexpectedTokenError( "until", this.currentToken() );
			repeatError = true;
		}

		if ( this.currentToken().equals( "(" ) )
		{
			this.readToken(); // (
		}
		else
		{
			throw this.parseException( "(", this.currentToken() );
		}

		Value condition = this.parseExpression( parentScope );

		if ( this.currentToken().equals( ")" ) )
		{
			this.readToken(); // )
		}
		else
		{
			this.readToken(); // )
		}
		else if ( !repeatError )
		{
			this.unexpectedTokenError( ")", this.currentToken() );
			repeatError = true;
		}

		if ( condition == null ||
		     !condition.value.getType().isBad() &&
		     !condition.value.getType().equals( DataTypes.BOOLEAN_TYPE ) )
		{
			Location errorLocation = condition != null ? condition.location :
				this.makeLocation( this.currentToken() );

			if ( !repeatError )
			{
				this.error( errorLocation, "\"repeat\" requires a boolean conditional expression" );
				repeatError = true;
			}

		return new RepeatUntilLoop( scope, condition );
	}

	private Switch parseSwitch( final Type functionType, final BasicScope parentScope, final boolean allowContinue )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "switch" ) )
		{
			return null;
		}

		Token switchStartToken = this.currentToken();

		this.readToken(); // switch

		if ( !this.currentToken().equals( "(" ) && !this.currentToken().equals( "{" ) )
		{
			throw this.parseException( "( or {", this.currentToken() );
		}

		Value condition = DataTypes.TRUE_VALUE;
		if ( this.currentToken().equals( "(" ) )
		{
			this.readToken(); // (

			condition = this.parseExpression( parentScope );

			if ( this.currentToken().equals( ")" ) )
			{
				this.readToken(); // )
			}
			else
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
				Location errorLocation = this.makeLocation( this.currentToken() );

				if ( !switchError )
				{
					this.error( errorLocation, "\"switch ()\" requires an expression" );
					switchError = true;
				}

				condition = Value.BAD_VALUE.wrap( errorLocation );
			}
		}

		Type type = condition.value.getType();

		if ( this.currentToken().equals( "{" ) )
		{
			this.readToken(); // {
		}
		else
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
			if ( this.currentToken().equalsIgnoreCase( "case" ) )
			{
				this.readToken(); // case

				Position testStart = this.here();

				Value test = this.parseExpression( parentScope );

				if ( test == null )
				{
					throw this.parseException( "Case label needs to be followed by an expression" );
				}

				if ( this.currentToken().equals( ":" ) )
				{
					this.readToken(); // :
				}
				else
				{
					if ( !caseError )
					{
						this.error( errorLocation, "Case label needs to be followed by an expression" );
					}
					switchError = caseError = true;

					test = Value.BAD_VALUE.wrap( errorLocation );
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

				if ( currentInteger == null )
				{
					currentInteger = IntegerPool.get( currentIndex );
				}

				if ( test.value.getClass() == Value.class )
				{
					if ( labels.get( test.value ) != null )
					{
						if ( !caseError )
						{
							this.error( test.location, "Duplicate case label: " + test.value );
						}
						switchError = caseError = true;
					}
					else if ( test.value != Value.BAD_VALUE )
					{
						labels.put( test.value, currentInteger );
					}
				}
				else if ( test.value != Value.BAD_VALUE ) // just in case we make BAD_<x> their own class (which may happen)
				{
					constantLabels = false;
				}


				tests.add( test.value );
				indices.add( currentInteger );
				scope.resetBarrier();

				continue;
			}

			if ( this.currentToken().equalsIgnoreCase( "default" ) )
			{
				Token defaultToken = this.currentToken();

				this.readToken(); // default

				if ( this.currentToken().equals( ":" ) )
				{
					this.readToken(); // :
				}
				else
				{
					this.readToken(); // :
				}

				if ( defaultIndex == -1 )
				{
					defaultIndex = currentIndex;
				}
				else
				{
					if ( !caseError )
					{
						this.parseException( ":", this.currentToken() );
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
				throw this.parseException( "Type given but not used to declare anything" );
			}

			if ( this.currentToken().equals( ";" ) )
			{
				this.readToken(); //read ;
			}
			else
			{
				throw this.parseException( ";", this.currentToken() );
			}

			currentIndex = scope.commandCount();
			currentInteger = null;
		}

		if ( this.currentToken().equals( "}" ) )
		{
			this.readToken(); // }
		}
		else
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

		return new Switch( switchLocation, condition.value, tests, indices, defaultIndex, scope,
		                   constantLabels ? labels : null );
	}

	private Try parseTry( final Type functionType, final BasicScope parentScope,
	                      final boolean allowBreak, final boolean allowContinue )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "try" ) )
		{
			return null;
		}

		Token tryStartToken = this.currentToken();

		this.readToken(); // try

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		// catch clauses would be parsed here

		Scope finalClause;

		if ( this.currentToken().equalsIgnoreCase( "finally" ) )
		{
			this.readToken(); // finally

			finalClause = this.parseBlockOrSingleCommand( functionType, null, body, false, allowBreak, allowContinue );
		}
		else
		{
			// this would not be an error if at least one catch was present
			this.error( this.makeLocation( tryStartToken, this.peekPreviousToken() ), "\"try\" without \"finally\" is pointless" );
			finalClause = new Scope( body );
		}

		Location tryLocation = this.makeLocation( tryStartToken, this.peekPreviousToken() );
		return new Try( tryLocation, body, finalClause );
	}

	private Catch parseCatch( final Type functionType, final BasicScope parentScope,
	                          final boolean allowBreak, final boolean allowContinue )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "catch" ) )
		{
			return null;
		}

		this.readToken(); // catch

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		return new Catch( body );
	}

	private LocatedValue parseCatchValue( final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "catch" ) )
		{
			return null;
		}

		Token catchStartToken = this.currentToken();

		this.readToken(); // catch

		ParseTreeNode body = this.parseBlock( null, null, parentScope, true, false, false );
		if ( body == null )
		{
			Value value = this.parseExpression( parentScope );
			if ( value != null )
			{
				body = value;
			}
			else
			{
				throw this.parseException( "\"catch\" requires a block or an expression" );
			}
		}

		return new Catch( body );
	}

	private Scope parseStatic( final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "static" ) )
		{
			return null;
		}

		Token staticStartToken = this.currentToken();

		this.readToken(); // static

		Scope result = new StaticScope( parentScope );

		if ( this.currentToken().equals( "{" ) )
		{
			this.readToken(); //read {

			this.parseScope( result, functionType, parentScope, false, false );

			if ( this.currentToken().equals( "}" ) )
			{
				this.readToken(); //read }
			}
			else
			{
				throw this.parseException( "}", this.currentToken() );
			}
		}
		else	// body is a single call
		{
			this.parseCommandOrDeclaration( result, functionType );
		}

		Location staticLocation = this.makeLocation( staticStartToken, this.peekPreviousToken() );
		result.setScopeLocation( staticLocation );

		return result;
	}

	private SortBy parseSort( final BasicScope parentScope )
		throws InterruptedException
	{
		// sort aggregate by expr

		if ( !this.currentToken().equalsIgnoreCase( "sort" ) )
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

		// Get an aggregate reference
		LocatedValue aggregate = this.parseVariableReference( parentScope );
		AggregateType type;

		if ( aggregate == null ||
		     !( aggregate.value instanceof VariableReference ) ||
		     !( aggregate.value.getType().getBaseType() instanceof AggregateType ) )
		{
			Location errorLocation = aggregate != null ? aggregate.location :
				this.makeLocation( this.currentToken() );

			if ( aggregate == null || !aggregate.value.getType().isBad() )
			{
				this.error( errorLocation, "Aggregate reference expected" );
			}

			aggregate = new VariableReference( new BadVariable( null, new BadAggregateType(), null ) )
				.wrap( errorLocation );
		}

		if ( this.currentToken().equalsIgnoreCase( "by" ) )
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

		LocatedValue expr = this.parseExpression( scope );

		if ( expr == null )
		{
			Location errorLocation = this.makeLocation( this.currentToken() );

			this.error( errorLocation, "Expression expected" );

			expr = Value.BAD_VALUE.wrap( errorLocation );
		}

		if ( expr == null )
		{
			throw this.parseException( "Expression expected" );
		}

		return new SortBy( (VariableReference) aggregate, indexvar, valuevar, expr, this );
	}

	private Loop parseForeach( final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		// foreach key [, key ... ] in aggregate { scope }

		if ( !this.currentToken().equalsIgnoreCase( "foreach" ) )
		{
			return null;
		}

		Token foreachStartToken = this.currentToken();

		this.readToken(); // foreach

		List<String> names = new ArrayList<String>();
		List<Location> locations = new ArrayList<>();

		while ( true )
		{
			Token name = this.currentToken();

			if ( !this.parseIdentifier( name.content ) ||
			     // "foreach in aggregate" (i.e. no key)
			     name.equalsIgnoreCase( "in" ) &&
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
			else if ( Parser.isReservedWord( name.content ) )
			{
				this.error( nameToken, "Reserved word '" + name + "' cannot be a key variable name" );
				names.add( null );
				locations.add( null );
			}
			else if ( names.contains( name.content ) )
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
			else
			{
				names.add( name.content );
			}

			this.readToken(); // name

			if ( this.currentToken().equals( "," ) )
			{
				this.readToken(); // ,
				continue;
			}

			if ( this.currentToken().equalsIgnoreCase( "in" ) )
			{
				this.readToken(); // in
				break;
			}

			this.unexpectedTokenError( "in", this.currentToken() );
			break;
		}

		// Get an aggregate reference
		LocatedValue aggregate = this.parseValue( parentScope );

		if ( aggregate == null || !( aggregate.value.getType().getBaseType() instanceof AggregateType ) )
		{
			Location errorLocation = aggregate != null ? aggregate.location :
				this.makeLocation( this.currentToken() );

			this.error( errorLocation, "Aggregate reference expected" );

			aggregate = Value.BAD_VALUE.wrap( errorLocation );
		}

		// Define key variables of appropriate type
		VariableList varList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		Type type = aggregate.value.getType().getBaseType();

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
			else if ( type instanceof AggregateType )
			{
				Variable keyvar = new Variable( name, itype, location );
				varList.add( keyvar );
				variableReferences.add( new VariableReference( keyvar ) );
			}
			else
			{	// Variable after all key vars holds the value instead
				itype = type;
				type = null;
			}

			Variable keyvar = new Variable( name, itype, makeLocation( start ) );
			varList.add( keyvar );
			variableReferences.add( new VariableReference( keyvar ) );
		}

		// Parse the scope with the list of keyVars
		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		Location foreachLocation = this.makeLocation( foreachStartToken, this.peekPreviousToken() );

		// Add the foreach node with the list of varRefs
		return new ForEachLoop( foreachLocation, scope, variableReferences, aggregate.value, this );
	}

	private Loop parseFor( final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		// for identifier from X [upto|downto|to|] Y [by Z]? {scope }

		if ( !this.currentToken().equalsIgnoreCase( "for" ) )
		{
			return null;
		}

		if ( !this.parseIdentifier( this.nextToken() ) )
		{
			return null;
		}

		Token forStartToken = this.currentToken();

		this.readToken(); // for

		Token name = this.currentToken();

		if ( Parser.isReservedWord( name.content ) )
		{
			throw this.parseException( "Reserved word '" + name + "' cannot be an index variable name" );
		}
		else if ( parentScope.findVariable( name.content ) != null )
		{
			throw this.parseException( "Index variable '" + name + "' is already defined" );
		}

		Variable indexvar;

		if ( this.currentToken().equalsIgnoreCase( "from" ) )
		{
			this.readToken(); // from
		}
		else
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

		Value initial = this.parseExpression( parentScope );

		if ( initial == null )
		{
			throw this.parseException( "Expression for initial value expected" );
		}

		int direction = 0;

		if ( this.currentToken().equalsIgnoreCase( "upto" ) )
		{
			direction = 1;
			this.readToken(); // upto
		}
		else if ( this.currentToken().equalsIgnoreCase( "downto" ) )
		{
			direction = -1;
			this.readToken(); // downto
		}
		else if ( this.currentToken().equalsIgnoreCase( "to" ) )
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

		LocatedValue last = this.parseExpression( parentScope );

		if ( last == null )
		{
			throw this.parseException( "Expression for floor/ceiling value expected" );
		}

		Value increment = DataTypes.ONE_VALUE;
		if ( this.currentToken().equalsIgnoreCase( "by" ) )
		{
			this.readToken(); // by
			increment = this.parseExpression( parentScope );

			if ( increment == null )
			{
				throw this.parseException( "Expression for increment value expected" );
			}
		}

		// Create integer index variable
		Variable indexvar = new Variable( name.content, DataTypes.INT_TYPE );

		// Put index variable onto a list
		VariableList varList = new VariableList();
		varList.add( indexvar );

		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		Location forLocation = this.makeLocation( forStartToken, this.peekPreviousToken() );
		return new ForLoop( forLocation, scope, new VariableReference( indexvar ), initial.value, last.value, increment.value, direction, this );
	}

	private Loop parseJavaFor( final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "for" ) )
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

		while ( !this.currentToken().equals( ";" ) )
		{
			Type t = this.parseType( scope, true );

			Token name = this.currentToken();
			Variable variable;

			if ( !this.parseIdentifier( name.content ) || Parser.isReservedWord( name.content ) )
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
				variable = parentScope.findVariable( name.content );
				if ( variable == null )
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
				if ( scope.findVariable( name.content, true ) != null )
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

					parentScope.addReference( variable, this.makeLocation( nameToken ) );

					lhs = new VariableReference( variable );
				}

				// Create variable and add it to the scope
				variable = new Variable( name.content, t );
				scope.addVariable( variable );
			}

			this.readToken(); // name

			VariableReference lhs = new VariableReference( name.content, scope );
			Value rhs = null;

			if ( this.currentToken().equals( "=" ) )
			{
				this.readToken(); // =

				rhs = this.parseExpression( scope );

				if ( rhs == null )
				{
					Location errorLocation = this.makeLocation( this.currentToken() );

					if ( !javaForSyntaxError )
					{
						this.error( errorLocation, "Expression expected" );
					}
					javaForError = javaForSyntaxError = true;

					rhs = Value.BAD_VALUE.wrap( errorLocation );
				}

				Type ltype = t.getBaseType();
				rhs = this.autoCoerceValue( t, rhs, scope );
				Type rtype = rhs.value.getType();

				if ( !Operator.validCoercion( ltype, rtype, "assign" ) )
				{
					if ( !javaForError )
					{
						this.error( rhs.location, "Cannot store " + rtype + " in " + name + " of type " + ltype );
						javaForError = true;
					}

					rhs = Value.BAD_VALUE.wrap( rhs.location );
				}

			}

			Assignment initializer = new Assignment( lhs, rhs.value );

			initializers.add( initializer );

			if ( this.currentToken().equals( "," ) )
			{
				this.readToken(); // ,

				if ( this.currentToken().equals( ";" ) )
				{
					if ( !javaForSyntaxError )
					{
						this.error( this.currentToken(), "Identifier expected" );
					}
					javaForError = javaForSyntaxError = true;
				}
			}
		}

		if ( this.currentToken().equals( ";" ) )
		{
			this.readToken(); // ;
		}
		else
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
			( this.currentToken().equals( ";" ) ) ?
			DataTypes.TRUE_VALUE : this.parseExpression( scope );

		if ( this.currentToken().equals( ";" ) )
		{
			this.readToken(); // ;
		}
		else
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

		// Parse incrementers in context of scope

		List<Command> incrementers = new ArrayList<>();

		while ( !this.atEndOfFile() && !this.currentToken().equals( ")" ) )
		{
			LocatedValue value = this.parsePreIncDec( scope );
			if ( value != null )
			{
				incrementers.add( value.value );
			}
			else
			{
				value = this.parseVariableReference( scope );
				if ( value == null || !( value.value instanceof VariableReference ) )
				{
					Location errorLocation = value != null ? value.location :
						this.makeLocation( this.currentToken() );

					if ( !javaForSyntaxError && ( value == null || value.value != Value.BAD_VALUE ) )
					{
						this.error( errorLocation, "Variable reference expected" );
					}
					javaForError = javaForSyntaxError = true;

					value = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) )
						.wrap( errorLocation );
				}

				LocatedValue lhs = this.parsePostIncDec( value );

				if ( lhs == value )
				{
					LocatedValue incrementer = this.parseAssignment( scope, value );

					if ( incrementer != null )
					{
						incrementers.add( incrementer.value );
					}
					else
					{
						incrementers.add( incrementer );
					}
					else if ( !javaForError )
					{
						this.error( value.location, "Variable '" + ((VariableReference) value.value).getName() + "' not incremented" );
						javaForError = true;
					}
				}
				else
				{
					incrementers.add( lhs.value );
				}
			}

			if ( this.currentToken().equals( "," ) )
			{
				this.readToken(); // ,

				if ( this.atEndOfFile() || this.currentToken().equals( ")" ) )
				{
					if ( !javaForSyntaxError )
					{
						this.error( this.currentToken(), "Identifier expected" );
					}
					javaForError = javaForSyntaxError = true;
				}
			}
		}

		if ( this.currentToken().equals( ")" ) )
		{
			this.readToken(); // )
		}
		else
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

		return new JavaForLoop( javaForLocation, scope, initializers, condition.value, incrementers );
	}

	private Scope parseLoopScope( final Type functionType, final VariableList varList, final BasicScope parentScope )
		throws InterruptedException
	{
		Scope result = new Scope( varList, parentScope );

		Token loopScopeStartToken = this.currentToken();

		this.parseLoopScope( result, functionType, parentScope );

		Location loopScopeLocation = this.makeLocation( loopScopeStartToken, this.peekPreviousToken() );
		result.setScopeLocation( loopScopeLocation );

		return result;
	}

	private Scope parseLoopScope( final Scope result, final Type functionType, final BasicScope parentScope )
		throws InterruptedException
	{
		if ( this.currentToken().equals( "{" ) )
		{
			// Scope is a block

			this.readToken(); // {

			this.parseScope( result, functionType, parentScope, false, true, true );

			if ( this.currentToken().equals( "}" ) )
			{
				this.readToken(); // }
			}
			else
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
				if ( this.currentToken().equals( ";" ) )
				{
					this.readToken(); // ;
				}
				else
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

	private LocatedValue parseNewRecord( final BasicScope scope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "new" ) )
		{
			return null;
		}

		Token newRecordStartToken = this.currentToken();

		this.readToken();

		if ( !this.parseIdentifier( this.currentToken().content ) )
		{
			throw this.parseException( "Record name", this.currentToken() );
		}

		String name = this.currentToken().content;
		Type type = scope.findType( name );

		Token nameToken = this.currentToken();
		boolean newRecordError = false, newRecordSyntaxError = false;

		this.readToken(); //name

		if ( type != null )
		{
			scope.addReference( type, this.makeLocation( nameToken ) );
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

		if ( this.currentToken().equals( "(" ) )
		{
			this.readToken(); //(

			while ( true )
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

				if ( this.currentToken().equals( ")" ) )
				{
					this.readToken(); // )
					break;
				}

				Type currentType;
				String errorMessageFieldName = "";

				if ( param < types.length )
				{
					currentType = types[param];
					errorMessageFieldName = " (" + names[param] + ")";
				}
				else
				{
					throw this.parseException( "Too many field initializers for record " + name );
				}

				Type expected = currentType.getBaseType();
				LocatedValue val;

				if ( ",".equals( this.currentToken().value ) )
				{
					val = DataTypes.VOID_VALUE.wrap( this.make0WidthLocation() );
				}
				else if ( "{".equals( this.currentToken().value ) )
				{
					val = this.parseAggregateLiteral( scope, (AggregateType) expected );
				}
				else
				{
					val = this.parseExpression( scope );
				}

				if ( val == null )
				{
					throw this.parseException( "Expression expected for field #" + ( param + 1 ) + errorMessageFieldName );
				}

				if ( val.value != DataTypes.VOID_VALUE )
				{
					val = this.autoCoerceValue( currentType, val, scope );
					Type given = val.value.getType();
					if ( !Operator.validCoercion( expected, given, "assign" ) )
					{
						throw this.parseException( given + " found when " + expected + " expected for field #" + ( param + 1 ) + errorMessageFieldName );
					}
				}

				params.add( val.value );
				param++;

				if ( this.currentToken().equals( "," ) )
				{
					this.readToken(); // ,
				}
				else if ( !this.currentToken().equals( ")" ) )
				{
					throw this.parseException( ", or )", this.currentToken() );
				}
			}
		}

		Location newRecordLocation = this.makeLocation( newRecordStartToken, this.peekPreviousToken() );
		return target.initialValueExpression( params ).wrap( newRecordLocation );
	}

	private LocatedValue parseCall( final BasicScope scope )
		throws InterruptedException
	{
		return this.parseCall( scope, null );
	}

	private LocatedValue parseCall( final BasicScope scope, final LocatedValue firstParam )
		throws InterruptedException
	{
		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		if ( !this.parseScopedIdentifier( this.currentToken().content ) )
		{
			return null;
		}

		Token name = this.currentToken();
		this.readToken(); //name

		this.readToken(); //name

		List<Value> params = this.parseParameters( scope, firstParam );
		Function target = scope.findFunction( name.content, params );

		if ( target != null )
		{
			params = this.autoCoerceParameters( target, params, scope );

			scope.addReference( target, functionCallLocation );
		}
		else
		{
			throw this.undefinedFunctionException( name.content, params );
		}
		else
		{
			this.error( nameLocation, Parser.undefinedFunctionMessage( name, params ) );

		FunctionCall call = new FunctionCall( target, params, this );

		return this.parsePostCall( scope, call );
	}

	private List<LocatedValue> parseParameters( final BasicScope scope, final LocatedValue firstParam )
		throws InterruptedException
	{
		if ( !this.currentToken().equals( "(" ) )
		{
			return null;
		}

		this.readToken(); //(

		List<LocatedValue> params = new ArrayList<>();
		if ( firstParam != null )
		{
			params.add( firstParam );
		}

		while ( true )
		{
			if ( this.atEndOfFile() )
			{
				throw this.parseException( ")", this.currentToken() );
			}

			if ( this.currentToken().equals( ")" ) )
			{
				this.readToken(); // )
				break;
			}

			LocatedValue val = this.parseExpression( scope );
			if ( val != null )
			{
				params.add( val );
			}

			if ( this.atEndOfFile() )
			{
				throw this.parseException( ")", this.currentToken() );
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
				throw this.parseException( "parameter", this.currentToken() );
			}

			if ( ")".equals( this.currentToken().value ) )
			{
				this.unexpectedTokenError( "parameter", this.currentToken() );
				// we'll break out at the start of the next loop
			}
		}

		return params;
	}

	private Value parsePostCall( final BasicScope scope, final FunctionCall call )
	{
		Value result = call;
		while ( result != null && this.currentToken().equals( "." ) )
		{
			Variable current = new Variable( result.getType() );
			current.setExpression( result );

			result = this.parseVariableReference( scope, new VariableReference( current ) );
		}

		return result;
	}

	private Value parseInvoke( final BasicScope scope )
	{
		if ( !this.currentToken().equalsIgnoreCase( "call" ) )
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

		Token current = this.currentToken();
		Value name = null;

		if ( current.equals( "(" ) )
		{
			name = this.parseExpression( scope );

			if ( name == null || !name.value.getType().equals( DataTypes.STRING_TYPE ) && !name.value.getType().isBad() )
			{
				Location errorLocation = name != null ? name.location :
					this.makeLocation( this.currentToken() );

				this.error( errorLocation, "String expression expected for function name" );

				name = Value.BAD_VALUE.wrap( errorLocation );
			}
		}
		else
		{
			name = this.parseVariableReference( scope );

			if ( name == null || !( name.value instanceof VariableReference ) )
			{
				Location errorLocation = name != null ? name.location :
					this.makeLocation( this.currentToken() );

				if ( name == null || name.value != Value.BAD_VALUE )
				{
					this.error( errorLocation, "Variable reference expected for function name" );
				}

				name = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) )
					.wrap( errorLocation );
			}
		}

		List<LocatedValue> params;

		if ( this.currentToken().equals( "(" ) )
		{
			params = this.parseParameters( scope, null );
		}
		else
		{
			throw this.parseException( "(", this.currentToken() );
		}

		return new FunctionInvocation( scope, type, name.value, params.stream().map( param -> param.value ).collect( Collectors.toList() ), this )
			.wrap( this.makeLocation( invokeStartToken, this.peekPreviousToken() ) );
	}

	private LocatedValue parseAssignment( final BasicScope scope, final LocatedValue lhs )
		throws InterruptedException
	{
		Token operStr = this.currentToken();
		if ( !operStr.equals( "=" ) &&
		     !operStr.equals( "+=" ) &&
		     !operStr.equals( "-=" ) &&
		     !operStr.equals( "*=" ) &&
		     !operStr.equals( "/=" ) &&
		     !operStr.equals( "%=" ) &&
		     !operStr.equals( "**=" ) &&
		     !operStr.equals( "&=" ) &&
		     !operStr.equals( "^=" ) &&
		     !operStr.equals( "|=" ) &&
		     !operStr.equals( "<<=" ) &&
		     !operStr.equals( ">>=" ) &&
		     !operStr.equals( ">>>=" ) )
		{
			return null;
		}

		Type ltype = lhs.value.getType().getBaseType();
		boolean isAggregate = ( ltype instanceof AggregateType );
		boolean assignmentError = false;

		if ( isAggregate && !"=".equals( operStr ) && !assignmentError )
		{
			this.error( operToken, "Cannot use '" + operStr + "' on an aggregate" );
			assignmentError = true;
		}

		Operator oper = new Operator( operStr.content, this );
		this.readToken(); // oper

		LocatedValue rhs;

		if ( this.currentToken().equals( "{" ) )
		{
			if ( isAggregate )
			{
				rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
			}
			else
			{
				rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
			}
			else
			{
				rhs = this.parseAggregateLiteral( scope, AggregateType.BAD_AGGREGATE );

				if ( !assignmentError && "=".equals( operStr ) ) // otherwise the coercion check can catch this instead
				{
					Location errorLocation = rhs != null ? rhs.location :
						this.makeLocation( this.peekPreviousToken() );

					this.error( errorLocation, "Cannot use an aggregate literal for type " + lhs.value.getType() );
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
			throw this.parseException( "Expression expected" );
		}

		rhs = this.autoCoerceValue( lhs.value.getRawType(), rhs, scope );
		if ( !oper.validCoercion( lhs.value.getType(), rhs.value.getType() ) && !assignmentError )
		{
			String error =
				oper.isLogical() ?
				( oper + " requires an integer or boolean expression and an integer or boolean variable reference" ) :
				oper.isInteger() ?
				( oper + " requires an integer expression and an integer variable reference" ) :
				( "Cannot store " + rhs.value.getType() + " in " + lhs.value + " of type " + lhs.value.getType() );
			this.error( this.makeLocation( lhs.location, rhs.location ), error );
			assignmentError = true;
		}

		Operator op = null;

		if ( !"=".equals( operStr ) )
		{
			op = new Operator( this.makeLocation( this.makeInlineRange( operToken.range.getStart(), operStr.length() - 1 ) ),
			                   operStr.substring( 0, operStr.length() - 1 ), this );
		}

		return new Assignment( (VariableReference) lhs.value, rhs.value, op )
			.wrap( this.makeLocation( lhs.location, rhs.location ) );
	}

	private Value parseRemove( final BasicScope scope )
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( "remove" ) )
		{
			return null;
		}

		LocatedValue lhs = this.parseExpression( scope );

		if ( lhs == null )
		{
			this.error( this.currentToken(), "Bad 'remove' statement" );

			return Value.BAD_VALUE;
		}

		return lhs.value;
	}

	private LocatedValue parsePreIncDec( final BasicScope scope )
		throws InterruptedException
	{
		if ( this.nextToken() == null )
		{
			return null;
		}

		// --[VariableReference]
		// ++[VariableReference]

		if ( !this.currentToken().equals( "++" ) &&
		     !this.currentToken().equals( "--" ) )
		{
			return null;
		}

		operStr = this.currentToken().equals( "++" ) ? Parser.PRE_INCREMENT : Parser.PRE_DECREMENT;
		this.readToken(); // oper

		lhs = this.parseVariableReference( scope );
		if ( lhs == null )
		{
			Location errorLocation = lhs != null ? lhs.location :
				this.makeLocation( this.currentToken() );

			if ( lhs == null || lhs.value != Value.BAD_VALUE )
			{
				this.error( errorLocation, "Variable reference expected" );
			}

			lhs = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) )
				.wrap( errorLocation );
		}

		int ltype = lhs.value.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT && !lhs.value.getType().isBad() )
		{
			this.error( lhs.location, operStr + " requires a numeric variable reference" );
		}

		Operator oper = new Operator( this.makeLocation( operToken ), operStr, this );

		return new IncDec( (VariableReference) lhs.value, oper )
			.wrap( this.makeLocation( operToken, this.peekPreviousToken() ) );
	}

	private LocatedValue parsePostIncDec( final LocatedValue lhs )
		throws InterruptedException
	{
		if ( lhs == null || !( lhs.value instanceof VariableReference ) )
		{
			return lhs;
		}

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

		int ltype = lhs.value.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT && !lhs.value.getType().isBad() )
		{
			this.error( lhs.location, operStr + " requires a numeric variable reference" );
		}

		Operator oper = new Operator( this.makeLocation( operToken ), operStr, this );

		return new IncDec( (VariableReference) lhs.value, oper )
			.wrap( this.makeLocation( lhs.location, oper.getLocation() ) );
	}

	private LocatedValue parseExpression( final BasicScope scope )
		throws InterruptedException
	{
		return this.parseExpression( scope, null );
	}

	private LocatedValue parseExpression( final BasicScope scope, final Operator previousOper )
		throws InterruptedException
	{
		if ( this.currentToken().equals( ";" ) )
		{
			return null;
		}

		LocatedValue lhs = null;
		LocatedValue rhs = null;
		Operator oper = null;

		Token operator = this.currentToken();
		if ( operator.equals( "!" ) )
		{
			this.readToken(); // !
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				Location errorLocation = this.makeLocation( this.currentToken() );

				this.error( errorLocation, "Value expected" );

				lhs = Value.BAD_VALUE.wrap( errorLocation );
			}

			lhs = this.autoCoerceValue( DataTypes.BOOLEAN_TYPE, lhs, scope );
			lhs = new Operation( lhs, new Operator( operator.content, this ) );
			if ( lhs.getType() != DataTypes.BOOLEAN_TYPE )
			{
				this.error( lhs.location, "\"!\" operator requires a boolean value" );
			}
		}
		else if ( operator.equals( "~" ) )
		{
			this.readToken(); // ~
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				Location errorLocation = this.makeLocation( this.currentToken() );

				this.error( errorLocation, "Value expected" );

				lhs = Value.BAD_VALUE.wrap( errorLocation );
			}

			lhs = new Operation( lhs, new Operator( operator.content, this ) );
			if ( lhs.getType() != DataTypes.INT_TYPE && lhs.getType() != DataTypes.BOOLEAN_TYPE )
			{
				this.error( lhs.location, "\"~\" operator requires an integer or boolean value" );
			}
		}
		else if ( operator.equals( "-" ) )
		{
			// See if it's a negative numeric constant
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				// Nope. Unary minus.
				this.readToken(); // -
				if ( ( lhs = this.parseValue( scope ) ) == null )
				{
					Location errorLocation = this.makeLocation( this.currentToken() );

					this.error( errorLocation, "Value expected" );

					lhs = Value.BAD_VALUE.wrap( errorLocation );
				}

				lhs = new Operation( lhs, new Operator( operator.content, this ) );
			}
		}
		else if ( operator.equals( "remove" ) )
		{
			this.readToken(); // remove

			lhs = this.parseVariableReference( scope );
			if ( lhs == null || !( lhs.value instanceof CompositeReference ) )
			{
				Location errorLocation = lhs != null ? lhs.location :
					this.makeLocation( this.currentToken() );

				if ( lhs == null || !lhs.value.getType().isBad() )
				{
					this.error( errorLocation, "Aggregate reference expected" );
				}

				if ( lhs == null || !( lhs.value instanceof VariableReference ) )
				{
					lhs = new VariableReference( new BadVariable( null, new BadType( null, null ), null ) )
						.wrap( errorLocation );
				}
			}

			lhs = new Operation( lhs, new Operator( operator.content, this ) );
		}
		else if ( ( lhs = this.parseValue( scope ) ) == null )
		{
			return null;
		}

		boolean expressionError = false, expressionSyntaxError = false;

		Position previousPosition = null;
		while ( this.madeProgress( previousPosition, previousPosition = this.getCurrentPosition() ) )
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

				LocatedValue conditional = lhs;

				if ( !expressionError &&
				     !conditional.value.getType().isBad() &&
				     !conditional.value.getType().equals( DataTypes.BOOLEAN_TYPE ) )
				{
					this.error( conditional.location, "Non-boolean expression " + conditional.value + " (" + conditional.value.getType() + ")" );
					expressionError = true;
				}

				if ( ( lhs = this.parseExpression( scope, null ) ) == null )
				{
					Location errorLocation = this.makeLocation( this.currentToken() );

					if ( !expressionSyntaxError )
					{
						this.error( errorLocation, "Value expected in left hand side" );
					}
					expressionError = expressionSyntaxError = true;

					lhs = Value.BAD_VALUE.wrap( errorLocation );
				}

				if ( this.currentToken().equals( ":" ) )
				{
					this.readToken(); // :
				}
				else
				{
					throw this.parseException( ":", this.currentToken() );
				}

				if ( ( rhs = this.parseExpression( scope, null ) ) == null )
				{
					Location errorlocation = this.makeLocation( this.currentToken() );

					if ( !expressionSyntaxError )
					{
						this.error( errorlocation, "Value expected" );
					}
					expressionError = expressionSyntaxError = true;

					rhs = Value.BAD_VALUE.wrap( errorlocation );
				}

				if ( !expressionError && !oper.validCoercion( lhs.value.getType(), rhs.value.getType() ) )
				{
					this.error( this.makeLocation( lhs.location, rhs.location ), "Cannot choose between " + lhs.value + " (" + lhs.value.getType() + ") and " + rhs.value + " (" + rhs.value.getType() + ")" );
					expressionError = true;
				}

				lhs = new TernaryExpression( conditional.value, lhs.value, rhs.value )
					.wrap( this.makeLocation( conditional.location, rhs.location ) );
			}
			else
			{
				this.readToken(); //operator

				if ( ( rhs = this.parseExpression( scope, oper ) ) == null )
				{
					Location errorLocation = this.makeLocation( this.currentToken() );

					if ( !expressionSyntaxError )
					{
						this.error( errorLocation, "Value expected" );
					}
					expressionError = expressionSyntaxError = true;

					rhs = Value.BAD_VALUE.wrap( errorLocation );
				}


				Type ltype = lhs.value.getType();
				Type rtype = rhs.value.getType();

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
					if ( lhs.value instanceof Concatenate )
					{
						((Concatenate) lhs.value).addString( rhs.value );
						// re-wrap
						lhs = lhs.value.wrap( this.makeLocation( lhs.location, rhs.location ) );
					}
					else
					{
						lhs = new Concatenate( lhs.value, rhs.value )
							.wrap( this.makeLocation( lhs.location, rhs.location ) );
					}
				}
				else
				{
					Location operationLocation = this.makeLocation( lhs.location, rhs.location );

					rhs = this.autoCoerceValue( ltype, rhs, scope );
					if ( !expressionError && !oper.validCoercion( ltype, rhs.value.getType() ) )
					{
						this.error( operationLocation, "Cannot apply operator " + oper + " to " + lhs.value + " (" + lhs.value.getType() + ") and " + rhs.value + " (" + rhs.value.getType() + ")" );
						expressionError = true;
					}
					lhs = new Operation( lhs.value, rhs.value, oper )
						.wrap( operationLocation );
				}
			}
		}

		if ( !expressionError )
		{
			// should NOT happen
			this.error( this.currentToken(), "Internal error; got stuck while parsing expressions" );
		}

		return lhs;
	}

	private LocatedValue parseValue( final BasicScope scope )
		throws InterruptedException
	{
		if ( this.currentToken().equals( ";" ) )
		{
			return null;
		}

		Token valueStartToken = this.currentToken();

		LocatedValue result = null;

		// Parse parenthesized expressions
		if ( "(".equals( this.currentToken().value ) )
		{
			this.readToken(); // (

			result = this.parseExpression( scope );
			if ( this.currentToken().equals( ")" ) )
			{
				this.readToken(); // )
			}
			else
			{
				this.readToken(); // )
			}
			else
			{
				this.unexpectedTokenError( ")", this.currentToken() );
			}

			if ( expression != null )
			{
				result = expression.value.wrap( this.makeLocation( valueStartToken, this.peekPreviousToken() ) );
			}
		}

		// Parse constant values
		// true and false are reserved words

		else if ( "true".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken();
			result = DataTypes.TRUE_VALUE.wrap( this.makeLocation( this.peekLastToken() ) );
		}

		else if ( "false".equalsIgnoreCase( this.currentToken().value ) )
		{
			this.readToken();
			result = DataTypes.FALSE_VALUE.wrap( this.makeLocation( this.peekLastToken() ) );
		}

		else if ( "__FILE__".equals( this.currentToken().value ) )
		{
			this.readToken();
			result = new Value( String.valueOf( this.shortFileName ) ).wrap( this.makeLocation( this.peekLastToken() ) );
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
				if ( this.currentToken().equals( "{" ) )
				{
					result = this.parseAggregateLiteral( scope, (AggregateType) baseType.getBaseType() );
				}
				else
				{
					this.unexpectedTokenError( "{", this.currentToken() );
					// don't parse. We don't know if they just didn't put anything.

					result = Value.BAD_VALUE.wrap( this.makeLocation( this.currentToken() ) );
				}
			}
			else
			{
				if ( baseType != null )
				{
					// TODO find a way to spot these with lookaheads
					// otherwise it means we mistakenly added a reference to that type
					this.rewindBackTo( anchor );
				}
				if ( ( result = this.parseVariableReference( scope ) ) != null )
				{
				}
			}
		}

		while ( result != null && ( this.currentToken().equals( "." ) || this.currentToken().equals( "[" ) ) )
		{
			Variable current = new Variable( result.value.getType() );
			current.setExpression( result.value );

			result = this.parseVariableReference( scope, new VariableReference( current ) );
		}

		if ( result != null && result.value instanceof VariableReference )
		{
			LocatedValue refBackup = result;
			result = this.parseAssignment( scope, result );
			if ( result == null )
			{
				result = this.parsePostIncDec( refBackup );
			}
		}

		return result;
	}

	private LocatedValue parseNumber()
		throws InterruptedException
	{
		Token numberStartToken = this.currentToken();

		Value number;

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
			Token fraction = this.currentToken();

			if ( this.readIntegerToken( fraction.content ) )
			{
				this.readToken(); // integer
			}
			else
			{
				this.unexpectedTokenError( "numeric value", this.currentToken() );

				number = new Value( 0 );
			}

			return new Value( sign * StringUtilities.parseDouble( "0." + fraction ) );
		}

		Token integer = this.currentToken();
		if ( !this.readIntegerToken( integer.content ) )
		{
			return null;
		}

		this.readToken(); // integer

		if ( ".".equals( this.currentToken().value ) )
		{
			String fraction = this.nextToken();
			if ( !this.readIntegerToken( fraction ) )
			{
				return new Value( sign * StringUtilities.parseLong( integer.content ) );
			}

		if ( ".".equals( this.currentToken().value ) &&
		     this.readIntegerToken( fraction ) )
		{
			this.readToken(); // .
			this.readToken(); // fraction

			number = new Value( sign * StringUtilities.parseDouble( integer + "." + fraction ) );
		}
		else
		{
			number = new Value( sign * StringUtilities.parseLong( integer ) );
		}

		return new Value( sign * StringUtilities.parseLong( integer.content ) );
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

	private LocatedValue parseString( final BasicScope scope )
		throws InterruptedException
	{
		if ( !this.currentToken().equals( "\"" ) &&
		     !this.currentToken().equals( "'" ) &&
		     !this.currentToken().equals( "`" ) )
		{
			return null;
		}

		this.clearCurrentToken();

		// Directly work with currentLine - ignore any "tokens" you meet until
		// the string is closed

		char startCharacter = this.restOfLine().charAt( 0 );
		char stopCharacter = startCharacter;
		boolean template = startCharacter == '`';

		final Value result;
		Concatenate conc = null;
		StringBuilder resultString = new StringBuilder();
		for ( int i = 1; ; ++i )
		{
			final String line = this.restOfLine();

			if ( i == line.length() )
			{
				if ( i == 0 && this.currentIndex == this.currentLine.offset && this.currentLine.content != null )
				{
					// Empty lines are OK.
					this.currentLine = this.currentLine.nextLine;
					this.currentIndex = this.currentLine.offset;
					i = -1;
					continue;
				}

				// Plain strings can't span lines
				throw this.parseException( "No closing " + stopCharacter + " found" );
			}

			char ch = line.charAt( i );

			// Handle escape sequences
			if ( ch == '\\' )
			{
				i = this.parseEscapeSequence( resultString, i );
				continue;
			}

			// Handle template substitutions
			if ( template && ch == '{' )
			{
				// Move the current token to the expression
				this.currentToken = this.currentLine.makeToken( ++i );
				this.readToken(); // read the string so far, including the {

				LocatedValue rhs = this.parseExpression( scope );

				if ( rhs == null )
				{
					Location errorLocation = this.makeLocation( this.currentToken() );

					this.error( errorLocation, "Expression expected" );

					rhs = Value.BAD_VALUE.wrap( errorLocation );
				}

				if ( rhs == null )
				{
					throw this.parseException( "Expression expected" );
				}

				// Set i to -1 so that it is set to zero by the loop, as the
				// currentLine has been shortened.
				i = -1;

				// Skip comments before the next token, look at what it is, then
				// discard said token.
				if ( this.currentToken().equals( "}" ) )
				{
					// Increment manually to not skip whitespace after the curly brace.
					++i; // }
				}
				else
				{
					throw this.parseException( "}", this.currentToken() );
				}

				this.clearCurrentToken();

				Value lhs = new Value( resultString.toString() );
				if ( conc == null )
				{
					conc = new Concatenate( lhs, rhs.value );
				}
				else
				{
					conc.addString( lhs );
					conc.addString( rhs.value );
				}
				
				resultString.setLength( 0 );
				continue;
			}

			if ( ch == stopCharacter )
			{
				this.currentToken = this.currentLine.makeToken( i + 1 ); //+ 1 to get rid of stop character token
				this.readToken();

				Value newString = new Value( resultString.toString() );

				if ( conc == null )
				{
					result = newString;
				}
				else
				{
					conc.addString( newString );
					result = conc;
				}

				break;
			}
			resultString.append( ch );
		}

		return result.wrap( this.makeLocation( stringStartPosition ) );
	}

	private int parseEscapeSequence( final StringBuilder resultString, int i )
	{
		final int backslashIndex = i++;
		final String line = this.restOfLine();

		if ( ++i == line.length() )
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
				throw this.parseException( "Hexadecimal character escape requires 2 digits" );
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
				throw this.parseException( "Unicode character escape requires 4 digits" );
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
					throw this.parseException( "Octal character escape requires 3 digits" );
				}
			}
			resultString.append( ch );
		}

		return i;
	}

	private Value parseLiteral( final Type type, final String element, final Location location )
	{
		Value value = DataTypes.parseValue( type, element, false );
		if ( value == null )
		{
			if ( !type.isBad() )
			{
				this.error( location, "Bad " + type.toString() + " value: \"" + element + "\"" );
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
				if ( type.equals( DataTypes.ITEM_TYPE ) )
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
				else if ( type.equals( DataTypes.EFFECT_TYPE ) )
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
				else if ( type.equals( DataTypes.MONSTER_TYPE ) )
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
				else if ( type.equals( DataTypes.SKILL_TYPE ) )
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
					this.warning( location,
						"Multiple matches for \"" + s1 + "\"; using \"" + s2 + "\".",
						"Clarify by using one of:" +
							KoLConstants.LINE_BREAK +
							String.join( KoLConstants.LINE_BREAK, names ) );
				}
				else
				{
					this.warning( location, "Changing \"" + s1 + "\" to \"" + s2 + "\" would get rid of this message." );
				}
			}
		}

		return value;
	}

	private LocatedValue parseTypedConstant( final BasicScope scope )
		throws InterruptedException
	{
		if ( !this.currentToken().equals( "$" ) )
		{
			return null;
		}

		this.readToken(); // read $

		Token name = this.currentToken();
		Type type = scope.findType( name.content );
		boolean plurals = false;
		boolean typedConstantError = false, typedConstantSyntaxError = false;

		if ( this.parseIdentifier( name ) )
		{
			StringBuilder buf = new StringBuilder( name.content );
			int length = name.length();

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

		this.readToken();

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
			scope.addReference( type, this.makeLocation( typedConstantTypeToken ) );
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

		if ( this.currentToken().equals( "[" ) )
		{
			this.readToken(); // read [
		}
		else
		{
			if ( !typedConstantSyntaxError )
			{
				this.unexpectedTokenError( "[", this.currentToken() );
			}

			return Value.BAD_VALUE.wrap( this.makeLocation( typedConstantStartToken, this.peekPreviousToken() ) );
		}

		if ( plurals )
		{
			Value value = this.parsePluralConstant( scope, type );

			Location typedConstantLocation = this.makeLocation( typedConstantStartToken, this.peekPreviousToken() );

			if ( value != null )
			{
				return value.wrap( typedConstantLocation );	// explicit list of values
			}
			value = type.allValues();
			if ( value != null )
			{
				return value.wrap( typedConstantLocation );	// implicit enumeration
			}

			if ( !typedConstantSyntaxError )
			{
				this.error( typedConstantLocation, "Can't enumerate all " + name );
			}

			return Value.BAD_VALUE.wrap( typedConstantLocation );
		}

		StringBuilder resultString = new StringBuilder();
		final Value result;

		Position currentElementStartPosition = this.getCurrentPosition();

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

				Location currentElementLocation = this.makeLocation( currentElementStartPosition );

				if ( !typedConstantError )
				{
					this.error( currentElementLocation, "No closing ] found" );
				}

				String input = resultString.toString().trim();
				result = this.parseLiteral( type, input, currentElementLocation );
				break;
			}

			char c = line.charAt( i );
			if ( c == '\\' )
			{
				if ( ++i == line.length() )
				{
					throw this.parseException( "No closing ] found" );
				}

				resultString.append( line.charAt( i ) );
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

				if ( i > 0 )
				{
					this.currentLine.makeToken( i );
					this.currentIndex += i;
				}
				this.readToken(); // read ]
				String input = resultString.toString().trim();
				result = this.parseLiteral( type, input, currentElementLocation );
				break;
			}
			else
			{
				resultString.append( c );
			}
		}

		return result.wrap( this.makeLocation( typedConstantStartToken, this.peekPreviousToken() ) );
	}

	private PluralValue parsePluralConstant( final BasicScope scope, final Type type )
		throws InterruptedException
	{
		// Directly work with currentLine - ignore any "tokens" you meet until
		// the string is closed

		List<Value> list = new ArrayList<>();
		int level = 1;
		boolean slash = false;

		Position currentElementStartPosition = this.getCurrentPosition();

		StringBuilder resultString = new StringBuilder();
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

				if ( slash )
				{
					slash = false;
					resultString.append( '/' );
				}

				if ( this.currentLine.content == null )
				{
					throw this.parseException( "No closing ] found" );
				}

				this.currentLine = this.currentLine.nextLine;
				this.currentIndex = this.currentLine.offset;
				i = -1;
				continue;
			}

			char ch = line.charAt( i );

			// Handle escape sequences
			if ( ch == '\\' )
			{
				i = this.parseEscapeSequence( resultString, i );
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
				Position currentElementEndPosition =
					new Position(
						this.currentLine.lineNumber,
						this.currentIndex + i - 1 );
				Location currentElementLocation = this.makeLocation( new Range( currentElementStartPosition, currentElementEndPosition ) );
				currentElementStartPosition = currentElementEndPosition;

				list.add( this.parseLiteral( type, element, currentElementLocation ) );
			}

			if ( ch == ']' )
			{
				if ( i > 0 )
				{
					this.currentLine.makeToken( i );
					this.currentIndex += i;
				}
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
		if ( !this.isOperator( oper.content ) )
		{
			return null;
		}

		return new Operator( oper.content, this );
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

	private LocatedValue parseVariableReference( final BasicScope scope )
		throws InterruptedException
	{
		if ( !this.parseIdentifier( this.currentToken().content ) )
		{
			return null;
		}

		Token name = this.currentToken();
		Variable var = scope.findVariable( name.content, true );

		this.readToken(); // read name

		if ( var != null )
		{
			scope.addReference( var, variableLocation );
		}
		else
		{
			this.error( variableLocation, "Unknown variable '" + name + "'" );

		Position start = this.here();

		this.readToken(); // read name

		return this.parseVariableReference( scope, new VariableReference( var ) );
	}

	/**
	 * Look for an index/key, and return the corresponding data, expecting {@code var} to be a
	 * {@link AggregateType}/{@link RecordType}, e.g., {@code map.key}, {@code array[0]}.
	 *
	 * <p>May also return a {@link FunctionCall} if the chain ends with/is a function call,
	 * e.g., {@code var.function()}.
	 *
	 * <p>There may also be nothing, in which case the submitted variable reference is returned
	 * as is.
	 */
	private LocatedValue parseVariableReference( final BasicScope scope, final LocatedValue var )
		throws InterruptedException
	{
		if ( var == null || !( var.value instanceof VariableReference ) )
		{
			return null;
		}

		LocatedValue current = var;
		Type type = var.value.getType();
		List<Value> indices = new ArrayList<Value>();

		boolean parseAggregate = "[".equals( this.currentToken().value );
		boolean variableReferenceError = false, variableReferenceSyntaxError = false;

		while ( this.currentToken().equals( "[" ) ||
		        this.currentToken().equals( "." ) ||
		        parseAggregate && this.currentToken().equals( "," ) )
		{
			LocatedValue index;

			type = type.getBaseType();

			if ( "[".equals( this.currentToken().value ) || ",".equals( this.currentToken().value ) )
			{
				this.readToken(); // read [ or ,
				parseAggregate = true;

				if ( !( type instanceof AggregateType ) )
				{
					if ( !variableReferenceError && !type.isBad() )
					{
						Location location = this.makeLocation( current.location, this.peekPreviousToken() );
						String message;
						if ( indices.isEmpty() )
						{
							message = "Variable '" + ((VariableReference) var.value).getName() + "' cannot be indexed";
						}
						else
						{
							message = "Too many keys for '" + ((VariableReference) var.value).getName() + "'";
						}
						this.error( location, message );
						variableReferenceError = true;
					}

					type = new BadAggregateType();
				}

				AggregateType atype = (AggregateType) type;
				index = this.parseExpression( scope );
				if ( index == null )
				{
					throw this.parseException( "Index for '" + current.getName() + "' expected" );
				}

				if ( !variableReferenceError &&
				     !index.value.getType().getBaseType().equals( atype.getIndexType().getBaseType() ) &&
				     !index.value.getType().isBad() &&
				     !atype.getIndexType().isBad() )
				{
					throw this.parseException(
						"Index for '" + current.getName() + "' has wrong data type " + "(expected " + atype.getIndexType() + ", got " + index.getType() + ")" );
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
							this.error( current.location, "Record expected" );
						}
						variableReferenceError = variableReferenceSyntaxError = true;
					}

					type = new BadRecordType( null, null );
				}

				RecordType rtype = (RecordType) type;

				Token field = this.currentToken();
				if ( this.parseIdentifier( field.content ) )
				{
					this.readToken(); // read name
				}
				else
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
				}

				index = rtype.getFieldIndex( field.content );
				if ( index != null )
				{
					type = rtype.getDataType( index );
				}
				else
				{
					if ( !variableReferenceError )
					{
						this.error( fieldToken, "Invalid field name '" + field + "'" );
						variableReferenceError = true;
					}

					index = Value.BAD_VALUE.wrap( this.makeLocation( fieldToken ) );
					type = new BadType( null, null );
				}
				else
				{
					type = rtype.getDataType( index.value );
				}
			}

			indices.add( index.value );

			if ( parseAggregate && this.currentToken().equals( "]" ) )
			{
				this.readToken(); // read ]
				parseAggregate = false;
			}

			current = new CompositeReference( current.target, indices, this );
		}

		if ( parseAggregate && !variableReferenceSyntaxError )
		{
			throw this.parseException( "]", this.currentToken() );
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
		throws InterruptedException
	{
		if ( !this.currentToken().equalsIgnoreCase( directive ) )
		{
			return null;
		}

		Token directiveToken = this.currentToken();
		Token directiveValueToken = null;

		this.readToken(); //directive

		if ( this.atEndOfFile() )
		{
			throw this.parseException( "<", this.currentToken() );
		}

		// We called atEndOfFile(), which calls currentToken() to trim whitespace
		// and skip comments. Remove the resulting token.

		this.clearCurrentToken();

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
				throw this.parseException( "No closing " + ch + " found" );
			}

			resultString = line.substring( 1, endIndex );
			// +1 to include and get rid of '>', '\'' or '"' token
			this.currentToken = this.currentLine.makeToken( endIndex + 1 );
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

		if ( ";".equals( this.currentToken() ) )
		{
			this.readToken(); //read ;
		}

		return new Directive( resultString, this.makeRange( directiveToken, directiveValueToken ) );
	}

	private void parseScriptName()
		throws InterruptedException
	{
		Directive scriptDirective = this.parseDirective( "script" );
		if ( this.scriptName == null && scriptDirective != null )
		{
			this.scriptName = scriptDirective.value;
		}
	}

	private void parseNotify()
		throws InterruptedException
	{
		Directive notifyDirective = this.parseDirective( "notify" );
		if ( this.notifyRecipient == null && notifyDirective != null )
		{
			this.notifyRecipient = notifyDirective.value;
		}
	}

	private void parseSince()
		throws InterruptedException
	{
		Directive sinceDirective = this.parseDirective( "since" );
		if ( sinceDirective != null )
		{
			// enforce "since" directives RIGHT NOW at parse time
			this.enforceSince( sinceDirective.value, sinceDirective.range );
		}
	}

	private Directive parseImport()
		throws InterruptedException
	{
		return this.parseDirective( "import" );
	}

	// **************** Tokenizer *****************

	private static final char BOM = '\ufeff';

	/**
	 * Returns {@link #currentToken} if non-null. Otherwise, moves in front of
	 * the next non-comment token that we can find, before assigning it to
	 * {@link #currentToken} and returning it.
	 *
	 * <p>
	 * Never returns {@code null}.
	 */
	private Token currentToken()
		throws InterruptedException
	{
		if ( Thread.interrupted() )
		{
			throw new InterruptedException();
		}

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

			// "#" was "supposed" to start a whole-line comment, but a bad implementation made it
			// act just like "//"

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

	/**
	 * Calls {@link #currentToken()} to make sure we are currently in front of an unread
	 * token. Then, returns a string version of the next token that can be found after that.
	 *
	 * @return the content of the next token to come after the token we are currently in front of,
	 *         or {@code null} if we are at the end of the file.
	 */
	private String nextToken()
		throws InterruptedException
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

			// "#" was "supposed" to start a whole-line comment, but a bad implementation made it
			// act just like "//"

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
	 * Forget every token up to {@code destinationToken}, so that we can resume parsing from there.
	 */
	private void rewindBackTo( final Token destinationToken )
		throws InterruptedException
	{
		this.currentToken();

		while ( this.currentToken != destinationToken )
		{
			this.currentLine.tokens.removeLast();

			while ( this.currentLine.tokens.isEmpty() )
			{
				// Don't do null checks. If previousLine is null, it means we never saw the
				// destination token, meaning we'd want to throw an error anyway.
				this.currentLine = this.currentLine.previousLine;
			}

			this.currentToken = this.currentLine.tokens.getLast();
			this.currentIndex = this.currentToken.offset;
		}
	}

	/**
	 * If we are not at the end of the file, null out
	 * {@link #currentToken} (allowing a new one to be gathered
	 * next time we call {@link #currentToken()}), and move
	 * {@link #currentIndex} forward.
	 */
	private void readToken()
		throws InterruptedException
	{
		// at "end of file"
		if ( this.currentToken().getLine().content == null )
		{
			return;
		}

		this.currentIndex = this.currentToken.restOfLineStart;
		this.currentToken = null;
	}

	/**
	 * If we have an unread token saved in {@link #currentToken}, null the field,
	 * and delete it from its {@link Line#tokens}, effectively forgetting that we saw it.
	 * <p>
	 * This method is made for parsing methods that manipulate lines character-by-character,
	 * and need to create Tokens of custom lengths.
	 */
	private void clearCurrentToken()
	{
		if ( this.currentToken != null )
		{
			this.currentToken = null;
			this.currentLine.tokens.removeLast();
		}
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

	private boolean tokenChar( char ch )
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
		throws InterruptedException
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

	/**
	 * Returns the content of {@link #currentLine} starting at {@link #currentIndex}.
	 */
	private String restOfLine()
	{
		return this.currentLine.substring( this.currentIndex );
	}

	/**
	 * Calls {@link #currentToken()} in order to skip any
	 * comment or whitespace we would be in front of,
	 * then return whether or not we reached the end
	 * of the file.
	 */
	private boolean atEndOfFile()
	{
		this.currentToken();

		return this.currentLine.content == null;
	}

	public final class Line
	{
		private final String content;
		private final int lineNumber;
		private final int offset;

		private final Deque<Token> tokens = new LinkedList<>();

		private final Line previousLine;
		/* Not made final to avoid a possible StackOverflowError. Do not modify. */
		private Line nextLine = null;

		private Line( final LineNumberReader commandStream )
		{
			this( commandStream, null );
		}

		private Line( final LineNumberReader commandStream, final Line previousLine )
		{
			this.previousLine = previousLine;
			if ( previousLine != null )
			{
				previousLine.nextLine = this;
			}

			int offset = 0;
			String line;

			try
			{
				line = commandStream.readLine();
			}
			catch ( IOException e )
			{
				// This should not happen. Therefore, print a stack trace for debug purposes.
				StaticEntity.printStackTrace( e );
				line = null;
			}

			if ( line == null )
			{
				// We are the "end of file" (or there was an IOException when reading)
				this.content = null;
				this.lineNumber = this.previousLine != null ? this.previousLine.lineNumber : 0;
				this.offset = this.previousLine != null ? this.previousLine.offset : 0;
				return;
			}

			// If the line starts with a Unicode BOM, remove it.
			if ( line.length() > 0 &&
			     line.charAt( 0 ) == Parser.BOM )
			{
				line = line.substring( 1 );
				offset += 1;
			}

			// Remove whitespace at front and end
			final String trimmed = line.trim();

			if ( !trimmed.isEmpty() )
			{
				// While the more "obvious" solution would be to use line.indexOf( trimmed ), we
				// know that the only difference between these strings is leading/trailing
				// whitespace.
				//
				// There are two cases:
				//  1. `trimmed` is empty, in which case `line` was entirely composed of whitespace.
				//  2. `trimmed` is non-empty. The first non-whitespace character in `line`
				//     indicates the start of `trimmed`.
				//
				// This is more efficient in that we don't need to confirm that the rest of
				// `trimmed` is present in `line`.

				final int ltrim = line.indexOf( trimmed.charAt( 0 ) );
				offset += ltrim;
			}
			else
			{
				// We are the "end of file"
				// (or there was an IOException when reading)
				this.lineNumber = this.previousLine != null ? this.previousLine.lineNumber : 0;
				this.offset = this.previousLine != null ? this.previousLine.offset : 0;

			line = trimmed;

			this.content = line;
			this.lineNumber = commandStream.getLineNumber();
			this.offset = offset;
		}

		private String substring( final int beginIndex )
		{
			if ( this.content == null )
			{
				return "";
			}

			// subtract "offset" from beginIndex, since we already removed it
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

		@Override
		public String toString()
		{
			return this.content;
		}

		public class Token
		{
			final int offset;
			final String content;
			final String followingWhitespace;
			final int restOfLineStart;

			private Token( final int tokenLength )
			{
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
					this.content = ";";
					// Going forward, we can just assume lineRemainder is an
					// empty string.
					lineRemainder = "";
				}
				else
				{
					final String lineRemainderWithToken = Line.this.substring( offset );

					this.content = lineRemainderWithToken.substring( 0, tokenLength );
					lineRemainder = lineRemainderWithToken.substring( tokenLength );
				}

				// As in Line(), this is more efficient than lineRemainder.indexOf( lineRemainder.trim() ).
				String trimmed = lineRemainder.trim();
				final int lTrim = trimmed.isEmpty() ? 0 : lineRemainder.indexOf( trimmed.charAt( 0 ) );

				this.followingWhitespace = lineRemainder.substring( 0, lTrim );

				this.restOfLineStart = offset + tokenLength + lTrim;
			}

			/** The Line in which this token exists */
			final Line getLine()
			{
				return Line.this;
			}

			public boolean equals( final String s )
			{
				return this.content.equals( s );
			}

			public boolean equalsIgnoreCase( final String s )
			{
				return this.content.equalsIgnoreCase( s );
			}

			public int length()
			{
				return this.content.length();
			}

			public String substring( final int beginIndex )
			{
				return this.content.substring( beginIndex );
			}

			public String substring( final int beginIndex, final int endIndex )
			{
				return this.content.substring( beginIndex, endIndex );
			}

			public boolean endsWith( final String suffix )
			{
				return this.content.endsWith( suffix );
			}

			@Override
			public String toString()
			{
				return this.content;
			}
		}

		private class Comment
			extends Token
		{
			private Comment( final int commentLength )
			{
				super( commentLength );
			}
		}
	}

	public List<Token> getTokens()
	{
		final List<Token> result = new LinkedList<>();

		Line line = this.currentLine;

		// Go back to the start
		while ( line != null &&
		        line.previousLine != null )
		{
			line = line.previousLine;
		}

		while ( line != null &&
		        line.content != null )
		{
			for ( final Token token : line.tokens )
			{
				result.add( token );
			}

			line = line.nextLine;
		}

		return result;
	}

	public List<String> getTokensContent()
	{
		return this.getTokens()
			.stream().map( token -> token.content ).collect( Collectors.toList() );
	}

	// **************** Parse errors *****************

	private ScriptException parseException( final String expected, final Token found )
	{
		String foundString = found.content;

		if ( found.getLine().content == null )
		{
			foundString = "end of file";
		}

		return this.parseException( "Expected " + expected + ", found " + foundString );
	}

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
		this.error( this.getCurrentPosition(), msg1, msg2 );
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
		this.warning( this.getCurrentPosition(), msg1, msg2 );
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
		this.warning( this.makeRange( start, end ), msg1, msg2 );
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

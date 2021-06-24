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

import net.sourceforge.kolmafia.textui.parsetree.AggregateType;
import net.sourceforge.kolmafia.textui.parsetree.ArrayLiteral;
import net.sourceforge.kolmafia.textui.parsetree.Assignment;
import net.sourceforge.kolmafia.textui.parsetree.BasicScope;
import net.sourceforge.kolmafia.textui.parsetree.BasicScript;
import net.sourceforge.kolmafia.textui.parsetree.Catch;
import net.sourceforge.kolmafia.textui.parsetree.CompositeReference;
import net.sourceforge.kolmafia.textui.parsetree.Concatenate;
import net.sourceforge.kolmafia.textui.parsetree.Conditional;
import net.sourceforge.kolmafia.textui.parsetree.Else;
import net.sourceforge.kolmafia.textui.parsetree.ElseIf;
import net.sourceforge.kolmafia.textui.parsetree.ForEachLoop;
import net.sourceforge.kolmafia.textui.parsetree.ForLoop;
import net.sourceforge.kolmafia.textui.parsetree.Function;
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
import net.sourceforge.kolmafia.textui.parsetree.ParseTreeNode;
import net.sourceforge.kolmafia.textui.parsetree.PluralValue;
import net.sourceforge.kolmafia.textui.parsetree.RecordType;
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
import net.sourceforge.kolmafia.textui.parsetree.TypeDef;
import net.sourceforge.kolmafia.textui.parsetree.UserDefinedFunction;
import net.sourceforge.kolmafia.textui.parsetree.Value;
import net.sourceforge.kolmafia.textui.parsetree.VarArgType;
import net.sourceforge.kolmafia.textui.parsetree.Variable;
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

	private LineNumberReader commandStream;
	private Line previousLine;
	private Line currentLine;
	private Line nextLine;
	private String currentToken;

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
			this.commandStream = new LineNumberReader( new InputStreamReader( this.istream, StandardCharsets.UTF_8 ) );
			this.currentLine = this.getNextLine();
			this.nextLine = this.getNextLine();
		}
		catch ( Exception e )
		{
			// If any part of the initialization fails,
			// then throw an exception.

			throw this.parseException( this.fileName + " could not be accessed" );
		}
	}

	private void disconnect()
	{
		try
		{
			this.commandStream = null;
			this.istream.close();
		}
		catch ( IOException e )
		{
		}
	}

	public Scope parse()
	{
		try
		{
			Scope scope = this.parseScope( null, null, null, Parser.getExistingFunctionScope(), false, false );

			if ( this.currentLine != null )
			{
				throw this.parseException( "Script parsing error; thought we reached the end of the file" );
			}

			return scope;
		}
		finally
		{
			this.disconnect();
		}
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
		if ( this.currentLine == null )
		{
			if ( this.previousLine == null )
			{
				return 0;
			}

			return this.previousLine.lineNumber;
		}

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
			throw this.parseException( "too many matches for " + fileName + ": " + s );
		}
		if ( matches.size() == 0 )
		{
			throw this.parseException( fileName + " could not be found" );
		}

		File scriptFile = matches.get( 0 );

		if ( this.imports.containsKey( scriptFile ) )
		{
			return scope;
		}

		this.imports.put( scriptFile, scriptFile.lastModified() );

		Scope result = scope;
		Parser parser = null;

		try
		{
			parser = new Parser( scriptFile, null, this.imports );
			result = parser.parseScope( scope, null, null, scope.getParentScope(), false, false );
			if ( parser.currentLine != null )
			{
				throw this.parseException( "Script parsing error" );
			}
		}
		finally
		{
			if ( parser != null )
			{
				parser.disconnect();
			}
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
		Type t = this.parseType( result, true );

		// If there is no data type, it's a command of some sort
		if ( t == null )
		{
			ParseTreeNode c = this.parseCommand( expectedType, result, false, false, false );
			if ( c == null )
			{
				throw this.parseException( "command or declaration required" );
			}

			result.addCommand( c, this );
			return result;
		}

		if ( this.parseVariables( t, result ) )
		{
			if ( !";".equals( this.currentToken() ) )
			{
				throw this.parseException( ";", this.currentToken() );
			}

			this.readToken(); //read ;
			return result;
		}

		//Found a type but no function or variable to tie it to
		throw this.parseException( "Type given but not used to declare anything" );
	}

	private Scope parseScope( final Scope startScope,
	                          final Type expectedType,
	                          final VariableList variables,
	                          final BasicScope parentScope,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
	{
		Scope result = startScope == null ? new Scope( variables, parentScope ) : startScope;
		return this.parseScope( result, expectedType, parentScope, allowBreak, allowContinue );
	}

	private Scope parseScope( Scope result,
	                          final Type expectedType,
	                          final BasicScope parentScope,
	                          final boolean allowBreak,
	                          final boolean allowContinue )
	{
		Directive importDirective;

		this.parseScriptName();
		this.parseNotify();
		this.parseSince();

		while ( ( importDirective = this.parseImport() ) != null )
		{
			result = this.importFile( importDirective.value, result );
		}

		while ( true )
		{
			if ( this.parseTypedef( result ) )
			{
				if ( !";".equals( this.currentToken() ) )
				{
					throw this.parseException( ";", this.currentToken() );
				}

				this.readToken(); //read ;
				continue;
			}

			Type t = this.parseType( result, true );

			// If there is no data type, it's a command of some sort
			if ( t == null )
			{
				// See if it's a regular command
				ParseTreeNode c = this.parseCommand( expectedType, result, false, allowBreak, allowContinue );
				if ( c != null )
				{
					result.addCommand( c, this );
					continue;
				}

				// No type and no command -> done.
				break;
			}

			// If this is a new record definition, enter it
			if ( t.getType() == DataTypes.TYPE_RECORD && ";".equals( this.currentToken() ) )
			{
				this.readToken(); // read ;
				continue;
			}

			Function f = this.parseFunction( t, result );
			if ( f != null )
			{
				if ( "main".equalsIgnoreCase( f.getName() ) )
				{
					if ( parentScope.getParentScope() != null )
					{
						throw this.parseException( "main method must appear at top level" );
					}
					this.mainMethod = f;
				}

				continue;
			}

			if ( this.parseVariables( t, result ) )
			{
				if ( !";".equals( this.currentToken() ) )
				{
					throw this.parseException( ";", this.currentToken() );
				}

				this.readToken(); //read ;
				continue;
			}

			if ( ( t.getBaseType() instanceof AggregateType ) && "{".equals( this.currentToken() ) )
			{
				this.readToken(); // read {
				result.addCommand( this.parseAggregateLiteral( result, (AggregateType) t ), this );
			}
			else {
				//Found a type but no function or variable to tie it to
				throw this.parseException( "Type given but not used to declare anything" );
			}
		}

		return result;
	}

	private Type parseRecord( final BasicScope parentScope )
	{
		if ( !"record".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // read record

		if ( ";".equals( this.currentToken() ) )
		{
			throw this.parseException( "Record name expected" );
		}

		// Allow anonymous records
		String recordName = null;
		Location recordDefinition = null;
		Position recordStart = this.here();

		if ( !"{".equals( this.currentToken() ) )
		{
			// Named record
			recordName = this.currentToken();

			if ( !this.parseIdentifier( recordName ) )
			{
				throw this.parseException( "Invalid record name '" + recordName + "'" );
			}

			if ( Parser.isReservedWord( recordName ) )
			{
				throw this.parseException( "Reserved word '" + recordName + "' cannot be a record name" );
			}

			if ( parentScope.findType( recordName ) != null )
			{
				throw this.parseException( "Record name '" + recordName + "' is already defined" );
			}

			this.readToken(); // read name

			recordDefinition = this.makeLocation( recordStart );
		}

		if ( !"{".equals ( this.currentToken() ) )
		{
			throw this.parseException( "{", this.currentToken() );
		}

		this.readToken(); // read {

		// Loop collecting fields
		List<Type> fieldTypes = new ArrayList<Type>();
		List<String> fieldNames = new ArrayList<String>();

		while ( true )
		{
			// Get the field type
			Type fieldType = this.parseType( parentScope, true );
			if ( fieldType == null )
			{
				throw this.parseException( "Type name expected" );
			}

			// Get the field name
			String fieldName = this.currentToken();
			if ( ";".equals( fieldName ) )
			{
				throw this.parseException( "Field name expected" );
			}

			if ( !this.parseIdentifier( fieldName ) )
			{
				throw this.parseException( "Invalid field name '" + fieldName + "'" );
			}

			if ( Parser.isReservedWord( fieldName ) )
			{
				throw this.parseException( "Reserved word '" + fieldName + "' cannot be used as a field name" );
			}

			if ( fieldNames.contains( fieldName ) )
			{
				throw this.parseException( "Field name '" + fieldName + "' is already defined" );
			}

			this.readToken(); // read name

			if ( !";".equals( this.currentToken() ) )
			{
				throw this.parseException( ";", this.currentToken() );
			}

			this.readToken(); // read ;

			fieldTypes.add( fieldType );
			fieldNames.add( fieldName.toLowerCase() );

			if ( this.atEndOfFile() )
			{
				throw this.parseException( "}", "EOF" );
			}

			if ( "}".equals( this.currentToken() ) )
			{
				break;
			}
		}

		this.readToken(); // read }

		if ( recordDefinition == null )
		{
			recordDefinition = this.makeLocation( recordStart );
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
	{
		if ( !this.parseIdentifier( this.currentToken() ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		String functionName = this.currentToken();

		Position start = this.here();

		if ( Parser.isReservedWord( functionName ) )
		{
			throw this.parseException( "Reserved word '" + functionName + "' cannot be used as a function name" );
		}

		this.readToken(); //read Function name
		this.readToken(); //read (

		VariableList paramList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		boolean vararg = false;

		while ( !")".equals( this.currentToken() ) )
		{
			Position varargStart = this.here();

			Type paramType = this.parseType( parentScope, false );
			if ( paramType == null )
			{
				throw this.parseException( ")", this.currentToken() );
			}

			if ( "...".equals( this.currentToken() ) )
			{
				// We can only have a single vararg parameter
				if ( vararg )
				{
					throw this.parseException( "Only one vararg parameter is allowed" );
				}
				// Make an vararg type out of the previously parsed type.
				paramType = new VarArgType( paramType );

				this.readToken(); //read ...

				paramType.getReferenceLocations().add( this.makeLocation( varargStart ) );

				// Only one vararg is allowed
				vararg = true;
			}

			Variable param = this.parseVariable( paramType, null );
			if ( param == null )
			{
				throw this.parseException( "identifier", this.currentToken() );
			}

			if ( !paramList.add( param ) )
			{
				throw this.parseException( "Variable " + param.getName() + " is already defined" );
			}

			if ( !")".equals( this.currentToken() ) )
			{
				// The single vararg parameter must be the last one
				if ( vararg )
				{
					throw this.parseException( "The vararg parameter must be the last one" );
				}

				if ( !",".equals( this.currentToken() ) )
				{
					throw this.parseException( ",", this.currentToken() );
				}

				this.readToken(); //read comma
			}

			variableReferences.add( new VariableReference( param ) );
		}

		this.readToken(); //read )

		// Add the function to the parent scope before we parse the
		// function scope to allow recursion.

		UserDefinedFunction f = new UserDefinedFunction( functionName, functionType, this.makeLocation( start ), variableReferences );

		if ( f.overridesLibraryFunction() )
		{
			throw this.overridesLibraryFunctionException( f );
		}

		UserDefinedFunction existing = parentScope.findFunction( f );

		if ( existing != null && existing.getScope() != null )
		{
			throw this.multiplyDefinedFunctionException( f );
		}

		if ( vararg )
		{
			Function clash = parentScope.findVarargClash( f );

			if ( clash != null )
			{
				throw this.varargClashException( f, clash );
			}
		}

		// Add new function or replace existing forward reference

		UserDefinedFunction result = parentScope.replaceFunction( existing, f );

		if ( ";".equals( this.currentToken() ) )
		{
			// Return forward reference
			this.readToken(); // ;
			return result;
		}

		Scope scope = this.parseBlockOrSingleCommand( functionType, paramList, parentScope, false, false, false );

		result.setScope( scope );
		if ( !result.assertBarrier() && !functionType.equals( DataTypes.TYPE_VOID ) )
		{
			throw this.parseException( "Missing return value" );
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

			if ( ",".equals( this.currentToken() ) )
			{
				this.readToken(); //read ,
				continue;
			}

			return true;
		}
	}

	private Variable parseVariable( final Type t, final BasicScope scope )
	{
		if ( !this.parseIdentifier( this.currentToken() ) )
		{
			return null;
		}

		String variableName = this.currentToken();
		if ( Parser.isReservedWord( variableName ) )
		{
			throw this.parseException( "Reserved word '" + variableName + "' cannot be a variable name" );
		}

		if ( scope != null && scope.findVariable( variableName ) != null )
		{
			throw this.parseException( "Variable " + variableName + " is already defined" );
		}

		Position start = this.here();

		this.readToken(); // If parsing of Identifier succeeded, go to next token.
		// If we are parsing a parameter declaration, we are done

		Variable result = new Variable( variableName, t, this.makeLocation( start ) );

		if ( scope == null )
		{
			if ( "=".equals( this.currentToken() ) )
			{
				throw this.parseException( "Cannot initialize parameter " + variableName );
			}
			return result;
		}

		// Otherwise, we must initialize the variable.

		Value rhs;

		Type ltype = t.getBaseType();
		if ( "=".equals( this.currentToken() ) )
		{
			this.readToken(); // read =

			if ( "{".equals( this.currentToken() ) )
			{
				if ( !( ltype instanceof AggregateType ) )
				{
					throw this.parseException(
						"Cannot initialize " + variableName + " of type " + t + " with an aggregate literal" );
				}
				this.readToken(); // read {
				rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
			}
			else
			{
				rhs = this.parseExpression( scope );
			}

			if ( rhs == null )
			{
				throw this.parseException( "Expression expected" );
			}

			rhs = this.autoCoerceValue( t, rhs, scope );
			if ( !Operator.validCoercion( ltype, rhs.getType(), "assign" ) )
			{
				throw this.parseException( "Cannot store " + rhs.getType() + " in " + variableName + " of type " + ltype );
			}
		}
		else if ( "{".equals( this.currentToken() ) && ltype instanceof AggregateType )
		{
			this.readToken(); // read {
			rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
		}
		else
		{
			rhs = null;
		}

		scope.addVariable( result );
		VariableReference lhs = new VariableReference( variableName, scope );
		scope.addCommand( new Assignment( lhs, rhs ), this );

		return result;
	}

	private Value autoCoerceValue( final Type ltype, final Value rhs, final BasicScope scope )
	{
		// DataTypes.TYPE_ANY has no name
		if ( ltype == null || ltype.getName() == null )
		{
			return rhs;
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
				target.addReference( this.make0WidthLocation() );

				return new FunctionCall( target, params, this );
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
				target.addReference( this.make0WidthLocation() );

				return new FunctionCall( target, params, this );
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
		if ( !"typedef".equalsIgnoreCase( this.currentToken() ) )
		{
			return false;
		}

		this.readToken(); // read typedef

		Type t = this.parseType( parentScope, true );
		if ( t == null )
		{
			throw this.parseException( "Missing data type for typedef" );
		}

		String typeName = this.currentToken();
		Position start = this.here();

		if ( ";".equals( typeName ) )
		{
			throw this.parseException( "Type name expected" );
		}

		if ( !this.parseIdentifier( typeName ) )
		{
			throw this.parseException( "Invalid type name '" + typeName + "'" );
		}

		if ( Parser.isReservedWord( typeName ) )
		{
			throw this.parseException( "Reserved word '" + typeName + "' cannot be a type name" );
		}

		this.readToken(); // read name

		Type existingType = parentScope.findType( typeName );
		if ( existingType != null )
		{
			if ( existingType.getBaseType().equals( t ) )
			{
				// It is OK to redefine a typedef with an equivalent type
				return true;
			}
				
			throw this.parseException( "Type name '" + typeName + "' is already defined" );
		}

		// Add the type to the type table
		TypeDef type = new TypeDef( typeName, t, this.makeLocation( start ) );
		parentScope.addType( type );

		return true;
	}

	private ParseTreeNode parseCommand( final Type functionType,
	                                    final BasicScope scope,
	                                    final boolean noElse,
	                                    final boolean allowBreak,
	                                    final boolean allowContinue )
	{
		ParseTreeNode result;

		if ( "break".equalsIgnoreCase( this.currentToken() ) )
		{
			if ( !allowBreak )
			{
				throw this.parseException( "Encountered 'break' outside of loop" );
			}

			result = new LoopBreak();
			this.readToken(); //break
		}

		else if ( "continue".equalsIgnoreCase( this.currentToken() ) )
		{
			if ( !allowContinue )
			{
				throw this.parseException( "Encountered 'continue' outside of loop" );
			}

			result = new LoopContinue();
			this.readToken(); //continue
		}

		else if ( "exit".equalsIgnoreCase( this.currentToken() ) )
		{
			result = new ScriptExit();
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

		if ( !";".equals( this.currentToken() ) )
		{
			throw this.parseException( ";", this.currentToken() );
		}

		this.readToken(); // ;
		return result;
	}

	private Type parseType( final BasicScope scope, final boolean records )
	{
		if ( ";".equals( this.currentToken() ) )
		{
			return null;
		}

		Position typeStart = this.here();

		Type valType;

		if ( ( valType = this.parseRecord( scope ) ) != null )
		{
			if ( !records )
			{
				throw this.parseException( "Record creation is not allowed here" );
			}
		}
		else if ( ( valType = scope.findType( this.currentToken() ) ) != null )
		{
			this.readToken();
		}
		else
		{
			return null;
		}

		if ( "[".equals( this.currentToken() ) )
		{
			valType = this.parseAggregateType( valType, scope );
		}

		if ( valType != null )
		{
			valType.addReference( this.makeLocation( typeStart ) );
		}

		return valType;
	}

	private Value parseAggregateLiteral( final BasicScope scope, final AggregateType aggr )
	{
		Type index = aggr.getIndexType();
		Type data = aggr.getDataType();

		List<Value> keys = new ArrayList<Value>();
		List<Value> values = new ArrayList<Value>();

		// If index type is an int, it could be an array or a map
		boolean arrayAllowed = index.equals( DataTypes.INT_TYPE );

		// Assume it is a map.
		boolean isArray = false;

		while ( !"}".equals( this.currentToken() ) )
		{
			Value lhs;

			// If we know we are reading an ArrayLiteral or haven't
			// yet ensured we are reading a MapLiteral, allow any
			// type of Value as the "key"
			Type dataType = data.getBaseType();

			if ( "{".equals( this.currentToken() ) )
			{
				this.readToken(); // read {

				if ( !isArray && !arrayAllowed )
				{
					// We know this is a map, but they placed an
					// aggregate literal as a key
					throw this.parseException( "a key of type " + index.toString(), "an aggregate" );
				}

				if ( !( dataType instanceof AggregateType ) )
				{
					throw this.parseException( "an element of type " + dataType.toString(), "an aggregate" );
				}

				lhs = parseAggregateLiteral( scope, (AggregateType) dataType );
			}
			else
			{
				lhs = this.parseExpression( scope );
			}

			if ( lhs == null )
			{
				throw this.parseException( "Script parsing error" );
			}

			String delim = this.currentToken();

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

			// If parsing an ArrayLiteral, accumulate only values
			if ( isArray )
			{
				// The value must have the correct data type
				lhs = this.autoCoerceValue( data, lhs, scope );
				if ( !Operator.validCoercion( dataType, lhs.getType(), "assign" ) )
				{
					throw this.parseException( "Invalid array literal" );
				}

				values.add( lhs );

				// If there is not another value, done parsing values
				if ( !",".equals( delim ) )
				{
					break;
				}

				// Otherwise, move on to the next value
				this.readToken(); // read ,
				continue;
			}

			// We are parsing a MapLiteral
			if ( !":".equals( delim ) )
			{
				throw this.parseException( ":", this.currentToken() );
			}

			this.readToken(); // read :

			Value rhs;

			if ( "{".equals( this.currentToken() ) )
			{
				this.readToken(); // read {

				if ( !( dataType instanceof AggregateType ) )
				{
					throw this.parseException( "a value of type " + dataType.toString(), "an aggregate" );
				}

				rhs = parseAggregateLiteral( scope, (AggregateType) dataType );
			}
			else
			{
				rhs = this.parseExpression( scope );
			}

			if ( rhs == null )
			{
				throw this.parseException( "Script parsing error" );
			}

			// Check that each type is valid via validCoercion
			lhs = this.autoCoerceValue( index, lhs, scope );
			rhs = this.autoCoerceValue( data, rhs, scope );
			if ( !Operator.validCoercion( index, lhs.getType(), "assign" ) ||
			     !Operator.validCoercion( data, rhs.getType(), "assign" ) )
			{
				throw this.parseException( "Invalid map literal" );
			}

			keys.add( lhs );
			values.add( rhs );

			if ( !",".equals( this.currentToken() ) )
			{
				break;
			}

			this.readToken();
		}

		if ( !"}".equals( this.currentToken() ) )
		{
			throw this.parseException( "}", this.currentToken() );
		}

		this.readToken(); // "}"

		if ( isArray )
		{
			int size = aggr.getSize();
			if ( size > 0 && size < values.size() )
			{
				throw this.parseException( "Array has " + size + " elements but " + values.size() + " initializers." );
			}
		}

		return isArray ? new ArrayLiteral( aggr, values ) :  new MapLiteral( aggr, keys, values );
	}

	private Type parseAggregateType( Type dataType, final BasicScope scope )
	{
		this.readToken(); // [ or ,
		if ( ";".equals( this.currentToken() ) )
		{
			throw this.parseException( "Missing index token" );
		}

		if ( "]".equals( this.currentToken() ) )
		{
			this.readToken(); // ]

			if ( "[".equals( this.currentToken() ) )
			{
				return new AggregateType( this.parseAggregateType( dataType, scope ), 0 );
			}

			return new AggregateType( dataType, 0 );
		}

		if ( this.readIntegerToken( this.currentToken() ) )
		{
			int size = StringUtilities.parseInt( this.currentToken() );
			this.readToken(); // integer

			if ( ";".equals( this.currentToken() ) )
			{
				throw this.parseException( "]", this.currentToken() );
			}

			if ( "]".equals( this.currentToken() ) )
			{
				this.readToken(); // ]

				if ( "[".equals( this.currentToken() ) )
				{
					return new AggregateType( this.parseAggregateType( dataType, scope ), size );
				}

				return new AggregateType( dataType, size );
			}

			if ( ",".equals( this.currentToken() ) )
			{
				return new AggregateType( this.parseAggregateType( dataType, scope ), size );
			}

			throw this.parseException( "]", this.currentToken() );
		}

		Type indexType = scope.findType( this.currentToken() );
		if ( indexType == null )
		{
			throw this.parseException( "Invalid type name '" + this.currentToken() + "'" );
		}

		if ( !indexType.isPrimitive() )
		{
			throw this.parseException( "Index type '" + this.currentToken() + "' is not a primitive type" );
		}

		this.readToken(); // type name
		if ( ";".equals( this.currentToken() ) )
		{
			throw this.parseException( "]", this.currentToken() );
		}

		if ( "]".equals( this.currentToken() ) )
		{
			this.readToken(); // ]

			if ( "[".equals( this.currentToken() ) )
			{
				return new AggregateType( this.parseAggregateType( dataType, scope ), indexType );
			}

			return new AggregateType( dataType, indexType );
		}

		if ( ",".equals( this.currentToken() ) )
		{
			return new AggregateType( this.parseAggregateType( dataType, scope ), indexType );
		}

		throw this.parseException( ", or ]", this.currentToken() );
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
			if ( !Character.isLetterOrDigit( identifier.charAt( i ) ) && identifier.charAt( i ) != '_'  && identifier.charAt( i ) != '@' )
			{
				return false;
			}
		}

		return true;
	}

	private FunctionReturn parseReturn( final Type expectedType, final BasicScope parentScope )
	{
		if ( !"return".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); //return

		if ( ";".equals( this.currentToken() ) )
		{
			if ( expectedType != null && expectedType.equals( DataTypes.TYPE_VOID ) )
			{
				return new FunctionReturn( null, DataTypes.VOID_TYPE );
			}

			throw this.parseException( "Return needs " + expectedType + " value" );
		}

		if ( expectedType != null && expectedType.equals( DataTypes.TYPE_VOID ) )
		{
			throw this.parseException( "Cannot return a value from a void function" );
		}

		Value value = this.parseExpression( parentScope );

		if ( value == null )
		{
			throw this.parseException( "Expression expected" );
		}

		value = this.autoCoerceValue( expectedType, value, parentScope );
		if ( expectedType != null && !Operator.validCoercion( expectedType, value.getType(), "return" ) )
		{
			throw this.parseException( "Cannot return " + value.getType() + " value from " + expectedType + " function" );
		}

		return new FunctionReturn( value, expectedType );
	}

	private Scope parseSingleCommandScope( final Type functionType,
	                                       final BasicScope parentScope,
	                                       final boolean noElse,
	                                       final boolean allowBreak,
	                                       final boolean allowContinue )
	{
		ParseTreeNode command = this.parseCommand( functionType, parentScope, noElse, allowBreak, allowContinue );
		if ( command == null )
		{
			if ( !";".equals( this.currentToken() ) )
			{
				throw this.parseException( ";", this.currentToken() );
			}

			this.readToken(); // ;
			return new Scope( parentScope );
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
		if ( !"{".equals( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // {

		Scope scope = this.parseScope( null, functionType, variables, parentScope, allowBreak, allowContinue );

		if ( !"}".equals( this.currentToken() ) )
		{
			throw this.parseException( "}", this.currentToken() );
		}

		this.readToken(); //read }

		return scope;
	}

	private Conditional parseConditional( final Type functionType,
	                                      final BasicScope parentScope,
	                                      final boolean noElse,
	                                      final boolean allowBreak,
	                                      final boolean allowContinue )
	{
		if ( !"if".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			throw this.parseException( "(", this.nextToken() );
		}

		this.readToken(); // if
		this.readToken(); // (

		Value condition = this.parseExpression( parentScope );
		if ( !")".equals( this.currentToken() ) )
		{
			throw this.parseException( ")", this.currentToken() );
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			throw this.parseException( "\"if\" requires a boolean conditional expression" );
		}

		this.readToken(); // )

		If result = null;
		boolean elseFound = false;
		boolean finalElse = false;

		do
		{
			Scope scope = parseBlockOrSingleCommand( functionType, null, parentScope, !elseFound, allowBreak, allowContinue );

			if ( result == null )
			{
				result = new If( scope, condition );
			}
			else if ( finalElse )
			{
				result.addElseLoop( new Else( scope, condition ) );
			}
			else
			{
				result.addElseLoop( new ElseIf( scope, condition ) );
			}

			if ( !noElse && "else".equalsIgnoreCase( this.currentToken() ) )
			{
				if ( finalElse )
				{
					throw this.parseException( "Else without if" );
				}

				if ( "if".equalsIgnoreCase( this.nextToken() ) )
				{
					this.readToken(); //else
					this.readToken(); //if

					if ( !"(".equals( this.currentToken() ) )
					{
						throw this.parseException( "(", this.currentToken() );
					}

					this.readToken(); //(
					condition = this.parseExpression( parentScope );

					if ( !")".equals( this.currentToken() ) )
					{
						throw this.parseException( ")", this.currentToken() );
					}

					if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
					{
						throw this.parseException( "\"if\" requires a boolean conditional expression" );
					}

					this.readToken(); // )
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
		if ( !"cli_execute".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		if ( !"{".equals( this.nextToken() ) )
		{
			return null;
		}

		this.readToken(); // cli_execute
		this.readToken(); // {

		ByteArrayStream ostream = new ByteArrayStream();

		while ( !"}".equals( this.currentToken() ) )
		{
			if ( this.atEndOfFile() )
			{
				throw this.parseException( "}", this.currentToken() );
			}

			try
			{
				ostream.write( this.currentLine.line.getBytes() );
				ostream.write( KoLConstants.LINE_BREAK.getBytes() );
			}
			catch ( Exception e )
			{
				// Byte array output streams do not throw errors,
				// other than out of memory errors.

				StaticEntity.printStackTrace( e );
			}

			this.currentLine = this.currentLine.clear();
			this.fixLines();
		}

		this.readToken(); // }

		return new BasicScript( ostream );
	}

	private Loop parseWhile( final Type functionType, final BasicScope parentScope )
	{
		if ( !"while".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			throw this.parseException( "(", this.nextToken() );
		}

		this.readToken(); // while
		this.readToken(); // (

		Value condition = this.parseExpression( parentScope );
		if ( !")".equals( this.currentToken() ) )
		{
			throw this.parseException( ")", this.currentToken() );
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			throw this.parseException( "\"while\" requires a boolean conditional expression" );
		}

		this.readToken(); // )

		Scope scope = this.parseLoopScope( functionType, null, parentScope );

		return new WhileLoop( scope, condition );
	}

	private Loop parseRepeat( final Type functionType, final BasicScope parentScope )
	{
		if ( !"repeat".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // repeat

		Scope scope = this.parseLoopScope( functionType, null, parentScope );
		if ( !"until".equalsIgnoreCase( this.currentToken() ) )
		{
			throw this.parseException( "until", this.currentToken() );
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			throw this.parseException( "(", this.nextToken() );
		}

		this.readToken(); // until
		this.readToken(); // (

		Value condition = this.parseExpression( parentScope );
		if ( !")".equals( this.currentToken() ) )
		{
			throw this.parseException( ")", this.currentToken() );
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			throw this.parseException( "\"repeat\" requires a boolean conditional expression" );
		}

		this.readToken(); // )

		return new RepeatUntilLoop( scope, condition );
	}

	private Switch parseSwitch( final Type functionType, final BasicScope parentScope, final boolean allowContinue )
	{
		if ( !"switch".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) && !"{".equals( this.nextToken() ) )
		{
			throw this.parseException( "( or {", this.nextToken() );
		}

		this.readToken(); // switch

		Value condition = DataTypes.TRUE_VALUE;
		if ( "(".equals( this.currentToken() ) )
		{
			this.readToken(); // (

			condition = this.parseExpression( parentScope );
			if ( !")".equals( this.currentToken() ) )
			{
				throw this.parseException( ")", this.currentToken() );
			}

			this.readToken(); // )

			if ( condition == null )
			{
				throw this.parseException( "\"switch ()\" requires an expression" );
			}
		}

		Type type = condition.getType();

		if ( !"{".equals( this.currentToken() ) )
		{
			throw this.parseException( "{", this.currentToken() );
		}

		this.readToken(); // {

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
			if ( "case".equalsIgnoreCase( this.currentToken() ) )
			{
				this.readToken(); // case

				Value test = this.parseExpression( parentScope );

				if ( !":".equals( this.currentToken() ) )
				{
					throw this.parseException( ":", this.currentToken() );
				}

				if ( !test.getType().equals( type ) )
				{
					throw this.parseException( "Switch conditional has type " + type + " but label expression has type " + test.getType() );
				}

				this.readToken(); // :

				if ( currentInteger == null )
				{
					currentInteger = IntegerPool.get( currentIndex );
				}

				if ( test.getClass() == Value.class )
				{
					if ( labels.get( test ) != null )
					{
						throw this.parseException( "Duplicate case label: " + test );
					}
					labels.put( test, currentInteger );
				}
				else
				{
					constantLabels = false;
				}


				tests.add( test );
				indices.add( currentInteger );
				scope.resetBarrier();

				continue;
			}

			if ( "default".equalsIgnoreCase( this.currentToken() ) )
			{
				this.readToken(); // default

				if ( !":".equals( this.currentToken() ) )
				{
					throw this.parseException( ":", this.currentToken() );
				}

				if ( defaultIndex != -1 )
				{
					throw this.parseException( "Only one default label allowed in a switch statement" );
				}

				this.readToken(); // :

				defaultIndex = currentIndex;
				scope.resetBarrier();

				continue;
			}

			Type t = this.parseType( scope, true );

			// If there is no data type, it's a command of some sort
			if ( t == null )
			{
				// See if it's a regular command
				ParseTreeNode c = this.parseCommand( functionType, scope, false, true, allowContinue );
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

			if ( this.parseVariables( t, scope ) )
			{
				if ( !";".equals( this.currentToken() ) )
				{
					throw this.parseException( ";", this.currentToken() );
				}

				this.readToken(); //read ;
				currentIndex = scope.commandCount();
				currentInteger = null;
				continue;
			}

			//Found a type but no function or variable to tie it to
			throw this.parseException( "Type given but not used to declare anything" );
		}

		if ( !"}".equals( this.currentToken() ) )
		{
			throw this.parseException( "}", this.currentToken() );
		}

		this.readToken(); // }

		return new Switch( condition, tests, indices, defaultIndex, scope,
		                   constantLabels ? labels : null );
	}

	private Try parseTry( final Type functionType, final BasicScope parentScope,
	                      final boolean allowBreak, final boolean allowContinue )
	{
		if ( !"try".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // try

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		// catch clauses would be parsed here

		if ( !"finally".equalsIgnoreCase( this.currentToken() ) )
		{
			// this would not be an error if at least one catch was present
			throw this.parseException( "\"try\" without \"finally\" is pointless" );
		}
		this.readToken(); // finally

		Scope finalClause = this.parseBlockOrSingleCommand( functionType, null, body, false, allowBreak, allowContinue );

		return new Try( body, finalClause );
	}

	private Catch parseCatch( final Type functionType, final BasicScope parentScope,
	                          final boolean allowBreak, final boolean allowContinue )
	{
		if ( !"catch".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // catch

		Scope body = this.parseBlockOrSingleCommand( functionType, null, parentScope, false, allowBreak, allowContinue );

		return new Catch( body );
	}

	private Catch parseCatchValue( final BasicScope parentScope )
	{
		if ( !"catch".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // catch

		Scope body = this.parseBlock( null, null, parentScope, true, false, false );
		if ( body != null )
		{
			return new Catch( body );
		}

		Value value = this.parseExpression( parentScope );
		if ( value != null )
		{
			return new Catch( value );
		}

		throw this.parseException( "\"catch\" requires a block or an expression" );
	}

	private Scope parseStatic( final Type functionType, final BasicScope parentScope )
	{
		if ( !"static".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // final

		Scope result = new StaticScope( parentScope );

		if ( !"{".equals( this.currentToken() ) )	// body is a single call
		{
			return this.parseCommandOrDeclaration( result, functionType );
		}

		this.readToken(); //read {

		this.parseScope( result, functionType, parentScope, false, false );

		if ( !"}".equals( this.currentToken() ) )
		{
			throw this.parseException( "}", this.currentToken() );
		}

		this.readToken(); //read }

		return result;
	}

	private SortBy parseSort( final BasicScope parentScope )
	{
		// sort aggregate by expr

		if ( !"sort".equalsIgnoreCase( this.currentToken() ) )
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

		this.readToken(); // sort

		// Get an aggregate reference
		Value aggregate = this.parseVariableReference( parentScope );

		if ( !( aggregate instanceof VariableReference ) ||
		     !( aggregate.getType().getBaseType() instanceof AggregateType ) )
		{
			throw this.parseException( "Aggregate reference expected" );
		}

		if ( !"by".equalsIgnoreCase( this.currentToken() ) )
		{
			throw this.parseException( "by", this.currentToken() );
		}
		this.readToken();	// by

		// Define key variables of appropriate type
		VariableList varList = new VariableList();
		AggregateType type = (AggregateType) aggregate.getType().getBaseType();
		Variable valuevar = new Variable( "value", type.getDataType(), null );
		varList.add( valuevar );
		Variable indexvar = new Variable( "index", type.getIndexType(), null );
		varList.add( indexvar );

		// Parse the key expression in a new scope containing 'index' and 'value'
		Scope scope = new Scope( varList, parentScope );
		Value expr = this.parseExpression( scope );

		return new SortBy( (VariableReference) aggregate, indexvar, valuevar, expr, this );
	}

	private Loop parseForeach( final Type functionType, final BasicScope parentScope )
	{
		// foreach key [, key ... ] in aggregate { scope }

		if ( !"foreach".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // foreach

		List<String> names = new ArrayList<String>();
		List<Position> positions = new ArrayList<>();

		while ( true )
		{
			String name = this.currentToken();

			if ( !this.parseIdentifier( name ) )
			{
				throw this.parseException( "Key variable name expected" );
			}

			if ( Parser.isReservedWord( name ) )
			{
				throw this.parseException( "Reserved word '" + name + "' cannot be a key variable name" );
			}

			if ( names.contains( name ) )
			{
				throw this.parseException( "Key variable '" + name + "' is already defined" );
			}

			names.add( name );
			positions.add( this.here() );
			this.readToken(); // name

			if ( ",".equals( this.currentToken() ) )
			{
				this.readToken(); // ,
				continue;
			}

			if ( "in".equalsIgnoreCase( this.currentToken() ) )
			{
				this.readToken(); // in
				break;
			}

			throw this.parseException( "in", this.currentToken() );
		}

		// Get an aggregate reference
		Value aggregate = this.parseValue( parentScope );

		if ( aggregate == null || !( aggregate.getType().getBaseType() instanceof AggregateType ) )
		{
			throw this.parseException( "Aggregate reference expected" );
		}

		// Define key variables of appropriate type
		VariableList varList = new VariableList();
		List<VariableReference> variableReferences = new ArrayList<VariableReference>();
		Type type = aggregate.getType().getBaseType();

		for ( int i = 0; i < names.size(); i++ )
		{
			String name = names.get( i );
			Position start = positions.get( i );

			Type itype;
			if ( type == null )
			{
				throw this.parseException( "Too many key variables specified" );
			}
			else if ( !( type instanceof AggregateType ) )
			{	// Variable after all key vars holds the value instead
				itype = type;
				type = null;
			}
			else
			{
				itype = ( (AggregateType) type ).getIndexType();
				type = ( (AggregateType) type ).getDataType();
			}

			Variable keyvar = new Variable( name, itype, makeLocation( start ) );
			varList.add( keyvar );
			variableReferences.add( new VariableReference( keyvar ) );
		}

		// Parse the scope with the list of keyVars
		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		// Add the foreach node with the list of varRefs
		return new ForEachLoop( scope, variableReferences, aggregate, this );
	}

	private Loop parseFor( final Type functionType, final BasicScope parentScope )
	{
		// for identifier from X [upto|downto|to|] Y [by Z]? {scope }

		if ( !"for".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		String name = this.nextToken();

		if ( !this.parseIdentifier( name ) )
		{
			return null;
		}

		if ( Parser.isReservedWord( name ) )
		{
			throw this.parseException( "Reserved word '" + name + "' cannot be an index variable name" );
		}

		if ( parentScope.findVariable( name ) != null )
		{
			throw this.parseException( "Index variable '" + name + "' is already defined" );
		}

		this.readToken(); // for

		Position start = this.here();

		this.readToken(); // name

		if ( !"from".equalsIgnoreCase( this.currentToken() ) )
		{
			throw this.parseException( "from", this.currentToken() );
		}

		this.readToken(); // from

		Value initial = this.parseExpression( parentScope );

		int direction = 0;

		if ( "upto".equalsIgnoreCase( this.currentToken() ) )
		{
			direction = 1;
		}
		else if ( "downto".equalsIgnoreCase( this.currentToken() ) )
		{
			direction = -1;
		}
		else if ( "to".equalsIgnoreCase( this.currentToken() ) )
		{
			direction = 0;
		}
		else
		{
			throw this.parseException( "to, upto, or downto", this.currentToken() );
		}

		this.readToken(); // upto/downto

		Value last = this.parseExpression( parentScope );

		Value increment = DataTypes.ONE_VALUE;
		if ( "by".equalsIgnoreCase( this.currentToken() ) )
		{
			this.readToken(); // by
			increment = this.parseExpression( parentScope );
		}

		// Create integer index variable
		Variable indexvar = new Variable( name, DataTypes.INT_TYPE, this.makeLocation( start ) );

		// Put index variable onto a list
		VariableList varList = new VariableList();
		varList.add( indexvar );

		Scope scope = this.parseLoopScope( functionType, varList, parentScope );

		return new ForLoop( scope, new VariableReference( indexvar ), initial, last, increment, direction, this );
	}

	private Loop parseJavaFor( final Type functionType, final BasicScope parentScope )
	{
		if ( !"for".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		this.readToken(); // for
		this.readToken(); // (

		// Parse variables and initializers

		Scope scope = new Scope( parentScope );
		List<Assignment> initializers = new ArrayList<Assignment>();

		// Parse each initializer in the context of scope, adding
		// variable to variable list in the scope, and saving
		// initialization expressions in initializers.

		while ( !this.atEndOfFile() && !";".equals( this.currentToken() ) )
		{
			Type t = this.parseType( scope, true );

			String name = this.currentToken();
			Position start = this.here();
			Variable variable;

			if ( !this.parseIdentifier( name ) || Parser.isReservedWord( name ) )
			{
				throw this.parseException( "Identifier required" );
			}

			this.readToken(); // name

			// If there is no data type, it is using an existing variable
			if ( t == null )
			{
				variable = parentScope.findVariable( name );
				if ( variable == null )
				{
					throw this.parseException( "Unknown variable '" + name + "'" );
				}
				t = variable.getType();
			}
			else
			{
				if ( scope.findVariable( name, true ) != null )
				{
					throw this.parseException( "Variable '" + name + "' already defined" );
				}

				// Create variable and add it to the scope
				variable = new Variable( name, t, this.makeLocation( start ) );
				scope.addVariable( variable );
			}

			VariableReference lhs = new VariableReference( name, scope );
			Value rhs = null;

			if ( "=".equals( this.currentToken() ) )
			{
				this.readToken(); // =

				rhs = this.parseExpression( scope );

				if ( rhs == null )
				{
					throw this.parseException( "Expression expected" );
				}

				Type ltype = t.getBaseType();
				rhs = this.autoCoerceValue( t, rhs, scope );
				Type rtype = rhs.getType();

				if ( !Operator.validCoercion( ltype, rtype, "assign" ) )
				{
					throw this.parseException( "Cannot store " + rtype + " in " + name + " of type " + ltype );
				}

			}

			Assignment initializer = new Assignment( lhs, rhs );

			initializers.add( initializer);

			if ( ",".equals( this.currentToken() ) )
			{
				this.readToken(); // ,

				if ( ";".equals( this.currentToken() ) )
				{
					throw this.parseException( "Identifier expected" );
				}
			}
		}

		if ( !";".equals( this.currentToken() ) )
		{
			throw this.parseException( ";", this.currentToken() );
		}

		this.readToken(); // ;

		// Parse condition in context of scope

		Value condition =
			( ";".equals( this.currentToken() ) ) ?
			DataTypes.TRUE_VALUE : this.parseExpression( scope );

		if ( !";".equals( this.currentToken() ) )
		{
			throw this.parseException( ";", this.currentToken() );
		}

		if ( condition == null || condition.getType() != DataTypes.BOOLEAN_TYPE )
		{
			throw this.parseException( "\"for\" requires a boolean conditional expression" );
		}

		this.readToken(); // ;

		// Parse incrementers in context of scope

		List<ParseTreeNode> incrementers = new ArrayList<ParseTreeNode>();

		while ( !this.atEndOfFile() && !")".equals( this.currentToken() ) )
		{
			Value value = parsePreIncDec( scope );
			if ( value != null )
			{
				incrementers.add( value );
			}
			else
			{
				value = this.parseVariableReference( scope );
				if ( !( value instanceof VariableReference ) )
				{
					throw this.parseException( "Variable reference expected" );
				}

				VariableReference ref = (VariableReference) value;
				Value lhs = this.parsePostIncDec( ref );

				if ( lhs == ref )
				{
					Assignment incrementer = parseAssignment( scope, ref );

					if ( incrementer == null )
					{
						throw this.parseException( "Variable '" + ref.getName() + "' not incremented" );
					}

					incrementers.add( incrementer );
				}
				else 
				{
					incrementers.add( lhs );
				}
			}

			if ( ",".equals( this.currentToken() ) )
			{
				this.readToken(); // ,

				if ( this.atEndOfFile() || ")".equals( this.currentToken() ) )
				{
					throw this.parseException( "Identifier expected" );
				}
			}
		}

		if ( !")".equals( this.currentToken() ) )
		{
			throw this.parseException( ")", this.currentToken() );
		}

		this.readToken(); // )

		// Parse scope body
		this.parseLoopScope( scope, functionType, parentScope );

		return new JavaForLoop( scope, initializers, condition, incrementers );
	}

	private Scope parseLoopScope( final Type functionType, final VariableList varList, final BasicScope parentScope )
	{
		return this.parseLoopScope( new Scope( varList, parentScope ), functionType, parentScope );
	}

	private Scope parseLoopScope( final Scope result, final Type functionType, final BasicScope parentScope )
	{
		if ( "{".equals( this.currentToken() ) )
		{
			// Scope is a block

			this.readToken(); // {

			this.parseScope( result, functionType, parentScope, true, true );
			if ( !"}".equals( this.currentToken() ) )
			{
				throw this.parseException( "}", this.currentToken() );
			}

			this.readToken(); // }
		}
		else
		{
			// Scope is a single command
			ParseTreeNode command = this.parseCommand( functionType, result, false, true, true );
			if ( command == null )
			{
				if ( !";".equals( this.currentToken() ) )
				{
					throw this.parseException( ";", this.currentToken() );
				}

				this.readToken(); // ;
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
		if ( !this.parseIdentifier( this.currentToken() ) )
		{
			return null;
		}

		String name = this.currentToken();
		Type type = scope.findType( name );

		Position start = this.here();

		this.readToken(); //name

		if ( type != null )
		{
			type.addReference( this.makeLocation( start ) );
		}

		if ( !( type instanceof RecordType ) )
		{
			throw this.parseException( "'" + name + "' is not a record type" );
		}

		RecordType target = (RecordType) type;

		List<Value> params = new ArrayList<>();
		String [] names = target.getFieldNames();
		Type [] types = target.getFieldTypes();
		int param = 0;

		if ( "(".equals( this.currentToken() ) )
		{
			this.readToken(); //(

			while ( !")".equals( this.currentToken() ) )
			{
				if ( this.atEndOfFile() )
				{
					throw this.parseException( ")", this.currentToken() );
				}

				Type expected = types[param].getBaseType();
				Value val;

				if ( ",".equals( this.currentToken() ) )
				{
					val = DataTypes.VOID_VALUE;
				}
				else if ( "{".equals( this.currentToken() ) && expected instanceof AggregateType )
				{
					this.readToken(); // read {
					val = this.parseAggregateLiteral( scope, (AggregateType) expected );
				}
				else
				{
					val = this.parseExpression( scope );
				}

				if ( val == null )
				{
					throw this.parseException( "Expression expected for field #" + ( param + 1 ) + " (" + names[param] + ")" );
				}

				if ( val != DataTypes.VOID_VALUE )
				{
					val = this.autoCoerceValue( types[param], val, scope );
					Type given = val.getType();
					if ( !Operator.validCoercion( expected, given, "assign" ) )
					{
						throw this.parseException( given + " found when " + expected + " expected for field #" + ( param + 1 ) + " (" + names[param] + ")" );
					}
				}

				params.add( val );
				param++;

				if ( ",".equals( this.currentToken() ) )
				{
					if ( param == names.length )
					{
						throw this.parseException( "Too many field initializers for record " + name );
					}

					this.readToken(); // ,
				}
			}

			this.readToken(); // )
		}

		return target.initialValueExpression( params );
	}

	private Value parseCall( final BasicScope scope )
	{
		return this.parseCall( scope, null );
	}

	private Value parseCall( final BasicScope scope, final Value firstParam )
	{
		if ( !"(".equals( this.nextToken() ) )
		{
			return null;
		}

		if ( !this.parseScopedIdentifier( this.currentToken() ) )
		{
			return null;
		}

		String name = this.currentToken();
		Position nameStart = this.here();
		this.readToken(); //name

		Location nameLocation = this.makeLocation( nameStart );

		List<Value> params = this.parseParameters( scope, firstParam );
		Function target = scope.findFunction( name, params );

		if ( target == null )
		{
			throw this.undefinedFunctionException( name, params );
		}

		target.addReference( nameLocation );

		params = this.autoCoerceParameters( target, params, scope );
		FunctionCall call = new FunctionCall( target, params, this );

		return parsePostCall( scope, call );
	}

	private List<Value> parseParameters( final BasicScope scope, final Value firstParam )
	{
		if ( !"(".equals( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); //(

		List<Value> params = new ArrayList<Value>();
		if ( firstParam != null )
		{
			params.add( firstParam );
		}

		while ( !")".equals( this.currentToken() ) )
		{
			if ( this.atEndOfFile() )
			{
				throw this.parseException( ")", "end of file" );
			}

			Value val = this.parseExpression( scope );
			if ( val != null )
			{
				params.add( val );
			}

			if ( this.atEndOfFile() )
			{
				throw this.parseException( ")", "end of file" );
			}

			if ( !",".equals( this.currentToken() ) )
			{
				if ( !")".equals( this.currentToken() ) )
				{
					throw this.parseException( ")", this.currentToken() );
				}
				continue;
			}

			this.readToken(); // ,

			if ( this.atEndOfFile() )
			{
				throw this.parseException( ")", "end of file" );
			}

			if ( ")".equals( this.currentToken() ) )
			{
				throw this.parseException( "parameter", this.currentToken() );
			}
		}

		if ( !")".equals( this.currentToken() ) )
		{
			throw this.parseException( ")", this.currentToken() );
		}

		this.readToken(); // )

		return params;
	}

	private Value parsePostCall( final BasicScope scope, final FunctionCall call )
	{
		Value result = call;
		while ( result != null && ".".equals( this.currentToken() ) )
		{
			Variable current = new Variable( result.getType() );
			current.setExpression( result );

			result = this.parseVariableReference( scope, current );
		}

		return result;
	}

	private Value parseInvoke( final BasicScope scope )
	{
		if ( !"call".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		this.readToken(); // call

		Type type = this.parseType( scope, false );

		// You can omit the type, but then this function invocation
		// cannot be used in an expression

		if ( type == null )
		{
			type = DataTypes.VOID_TYPE;
		}

		String current = this.currentToken();
		Value name = null;

		if ( "(".equals( current ) )
		{
			name = this.parseExpression( scope );
			if ( name == null || !name.getType().equals( DataTypes.STRING_TYPE ) )
			{
				throw this.parseException( "String expression expected for function name" );
			}
		}
		else
		{
			if ( !this.parseIdentifier( current ) )
			{
				throw this.parseException( "Variable reference expected for function name" );
			}

			name = this.parseVariableReference( scope );

			if ( !( name instanceof VariableReference ) )
			{
				throw this.parseException( "Variable reference expected for function name" );
			}
		}

		List<Value> params = parseParameters( scope, null );

		FunctionInvocation call = new FunctionInvocation( scope, type, name, params, this );

		return parsePostCall( scope, call );
	}

	private Assignment parseAssignment( final BasicScope scope, final VariableReference lhs )
	{
		String operStr = this.currentToken();
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

		if ( isAggregate && !"=".equals( operStr ) )
		{
			throw this.parseException( "Cannot use '" + operStr + "' on an aggregate" );
		}

		Operator oper = new Operator( operStr, this );
		this.readToken(); // oper

		Value rhs;

		if ( "{".equals( this.currentToken() ) )
		{
			if ( !isAggregate )
			{
				throw this.parseException( "Cannot use an aggregate literal for type " + lhs.getType() );
			}
			this.readToken(); // read {
			rhs = this.parseAggregateLiteral( scope, (AggregateType) ltype );
		}
		else
		{
			rhs = this.parseExpression( scope );
		}

		if ( rhs == null )
		{
			throw this.parseException( "Internal error" );
		}

		rhs = this.autoCoerceValue( lhs.getRawType(), rhs, scope );
		if ( !oper.validCoercion( lhs.getType(), rhs.getType() ) )
		{
			String error =
				oper.isLogical() ?
				( oper + " requires an integer or boolean expression and an integer or boolean variable reference" ) :
				oper.isInteger() ?
				( oper + " requires an integer expression and an integer variable reference" ) :
				( "Cannot store " + rhs.getType() + " in " + lhs + " of type " + lhs.getType() );
			throw this.parseException( error );
		}

		Operator op = "=".equals( operStr ) ? null : new Operator( operStr.substring( 0, operStr.length() - 1 ), this );

		return new Assignment( lhs, rhs, op );
	}

	private Value parseRemove( final BasicScope scope )
	{
		if ( !"remove".equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		Value lhs = this.parseExpression( scope );

		if ( lhs == null )
		{
			throw this.parseException( "Bad 'remove' statement" );
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

		if ( !"++".equals( this.currentToken() ) &&
		     !"--".equals( this.currentToken() ) )
		{
			return null;
		}

		String operStr = "++".equals( this.currentToken() ) ? Parser.PRE_INCREMENT : Parser.PRE_DECREMENT;

		this.readToken(); // oper

		Value lhs = this.parseVariableReference( scope );
		if ( lhs == null )
		{
			throw this.parseException( "Variable reference expected" );
		}

		int ltype = lhs.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT )
		{
			throw this.parseException( operStr + " requires a numeric variable reference" );
		}

		Operator oper = new Operator( operStr, this );

		return new IncDec( (VariableReference) lhs, oper );
	}

	private Value parsePostIncDec( final VariableReference lhs )
	{
		// [VariableReference]++
		// [VariableReference]--

		if ( !"++".equals( this.currentToken() ) &&
		     !"--".equals( this.currentToken() ) )
		{
			return lhs;
		}

		String operStr = "++".equals( this.currentToken() ) ? Parser.POST_INCREMENT : Parser.POST_DECREMENT;

		int ltype = lhs.getType().getType();
		if ( ltype != DataTypes.TYPE_INT && ltype != DataTypes.TYPE_FLOAT )
		{
			throw this.parseException( operStr + " requires a numeric variable reference" );
		}

		this.readToken(); // oper

		Operator oper = new Operator( operStr, this );

		return new IncDec( lhs, oper );
	}

	private Value parseExpression( final BasicScope scope )
	{
		return this.parseExpression( scope, null );
	}

	private Value parseExpression( final BasicScope scope, final Operator previousOper )
	{
		if ( ";".equals( this.currentToken() ) )
		{
			return null;
		}

		Value lhs = null;
		Value rhs = null;
		Operator oper = null;

		if ( "!".equals( this.currentToken() ) )
		{
			String operator = this.currentToken();
			this.readToken(); // !
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				this.error( "Value expected" );

				lhs = Value.BAD_VALUE;
			}

			lhs = this.autoCoerceValue( DataTypes.BOOLEAN_TYPE, lhs, scope );
			lhs = new Operation( lhs, new Operator( operator, this ) );
			if ( lhs.getType() != DataTypes.BOOLEAN_TYPE && lhs.getType() != Type.BAD_TYPE )
			{
				this.error( "\"!\" operator requires a boolean value" );
			}
		}
		else if ( "~".equals( this.currentToken() ) )
		{
			String operator = this.currentToken();
			this.readToken(); // ~
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				this.error( "Value expected" );

				lhs = Value.BAD_VALUE;
			}

			lhs = new Operation( lhs, new Operator( operator, this ) );
			if ( lhs.getType() != DataTypes.INT_TYPE && lhs.getType() != DataTypes.BOOLEAN_TYPE && lhs.getType() != Type.BAD_TYPE )
			{
				this.error( "\"~\" operator requires an integer or boolean value" );
			}
		}
		else if ( "-".equals( this.currentToken() ) )
		{
			// See if it's a negative numeric constant
			if ( ( lhs = this.parseValue( scope ) ) == null )
			{
				// Nope. Unary minus.
				String operator = this.currentToken();
				this.readToken(); // -
				if ( ( lhs = this.parseValue( scope ) ) == null )
				{
					this.error( "Value expected" );

					lhs = Value.BAD_VALUE;
				}

				lhs = new Operation( lhs, new Operator( operator, this ) );
			}
		}
		else if ( "remove".equals( this.currentToken() ) )
		{
			String operator = this.currentToken();
			this.readToken(); // remove

			lhs = this.parseVariableReference( scope );
			if ( lhs == null || !( lhs instanceof CompositeReference ) && lhs.getType().simpleType() != Type.BAD_TYPE )
			{
				this.error( "Aggregate reference expected" );

				lhs = VariableReference.BAD_VARIABLE_REFERENCE;
			}

			lhs = new Operation( lhs, new Operator( operator, this ) );
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

			if ( ":".equals( this.currentToken() ) )
			{
				return lhs;
			}

			if ( "?".equals( this.currentToken() ) )
			{
				this.readToken(); // ?

				Value conditional = lhs;

				if ( conditional.getType() != DataTypes.BOOLEAN_TYPE &&
				     conditional.getType() != Type.BAD_TYPE && !expressionError )
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

				if ( ":".equals( this.currentToken() ) )
				{
					this.readToken(); // :
				}
				else
				{
					if ( !expressionSyntaxError )
					{
						this.parseException( ":", this.currentToken() );
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
		if ( ";".equals( this.currentToken() ) )
		{
			return null;
		}

		Value result = null;

		// Parse parenthesized expressions
		if ( "(".equals( this.currentToken() ) )
		{
			this.readToken(); // (

			result = this.parseExpression( scope );

			if ( ")".equals( this.currentToken() ) )
			{
				this.readToken(); // )
			}
			else
			{
				this.parseException( ")", this.currentToken() );
			}
		}

		// Parse constant values
		// true and false are reserved words

		else if ( "true".equalsIgnoreCase( this.currentToken() ) )
		{
			this.readToken();
			result = DataTypes.TRUE_VALUE;
		}

		else if ( "false".equalsIgnoreCase( this.currentToken() ) )
		{
			this.readToken();
			result = DataTypes.FALSE_VALUE;
		}

		else if ( "__FILE__".equals( this.currentToken() ) )
		{
			this.readToken();
			result = new Value( String.valueOf( this.shortFileName ) );
		}

		// numbers
		else if ( ( result = this.parseNumber() ) != null )
		{
		}

		else if ( "\"".equals( this.currentToken() ) || "'".equals( this.currentToken() ) || "`".equals( this.currentToken() ) )
		{
			result = this.parseString( scope, null );
		}

		else if ( "$".equals( this.currentToken() ) )
		{
			result = this.parseTypedConstant( scope );
		}

		else if ( "new".equalsIgnoreCase( this.currentToken() ) )
		{
			this.readToken();
			result = this.parseNewRecord( scope );
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
			Type baseType = this.parseType( scope, false );
			if ( baseType != null && baseType.getBaseType() instanceof AggregateType )
			{
				if ( "{".equals( this.currentToken() ) )
				{
					this.readToken(); // {
					result = this.parseAggregateLiteral( scope, (AggregateType) baseType.getBaseType() );
				}
				else
				{
					this.parseException( "{", this.currentToken() );
					// don't parse. We don't know if they just didn't put anything.

					result = Value.BAD_VALUE;
				}
			}
			else
			{
				if ( baseType != null )
				{
					this.replaceToken( baseType.name );
				}
				if ( ( result = this.parseVariableReference( scope ) ) != null )
				{
				}
			}
		}

		while ( result != null && ( ".".equals( this.currentToken() ) || "[".equals( this.currentToken() ) ) )
		{
			Variable current = new Variable( result.getType() );
			current.setExpression( result );

			result = this.parseVariableReference( scope, current );
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

		if ( "-".equals( this.currentToken() ) )
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

		if ( ".".equals( this.currentToken() ) )
		{
			this.readToken();
			String fraction = this.currentToken();

			if ( !this.readIntegerToken( fraction ) )
			{
				this.parseException( "numeric value", fraction );

				return new Value( (double) 0 );
			}

			this.readToken(); // integer
			return new Value( sign * StringUtilities.parseDouble( "0." + fraction ) );
		}

		String integer = this.currentToken();
		if ( !this.readIntegerToken( integer ) )
		{
			return null;
		}

		this.readToken(); // integer

		if ( ".".equals( this.currentToken() ) )
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

	private Value parseString( final BasicScope scope, final Type type )
	{
		// Directly work with currentLine - ignore any "tokens" you meet until
		// the string is closed

		char startCharacter = this.currentToken().charAt( 0 );
		this.readToken();

		char stopCharacter = startCharacter;
		boolean template = startCharacter == '`';
		boolean allowComments = false;

		List<Value> list = null;

		if ( type != null )
		{
			// Typed plural constant - handled by same code as plain strings
			// so that they can share escape character processing
			stopCharacter = ']';
			allowComments = true;
			list = new ArrayList<Value>();
		}

		int level = 1;
		boolean slash = false;

		Concatenate conc = null;
		StringBuilder resultString = new StringBuilder();
		for ( int i = 0; ; ++i )
		{
			if ( i == this.currentLine.line.length() )
			{
				this.currentLine = this.currentLine.clear();
				this.currentToken = null;

				// Plain strings can't span lines
				if ( type == null )
				{
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

				this.fixLines();
				i = 0;
				if ( this.currentLine == null )
				{
					this.error( "No closing " + stopCharacter + " found" );

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
			}

			char ch = this.currentLine.line.charAt( i );

			// Handle escape sequences
			if ( ch == '\\' )
			{
				if ( i == this.currentLine.line.length() - 1 )
				{
					i = -1;
					ch = '\n';
					this.previousLine = this.currentLine;
					this.currentLine = this.nextLine;
					this.nextLine = this.getNextLine();
				}
				else
				{
					ch = this.currentLine.line.charAt( ++i );
				}

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

				case ',':
					resultString.append( ',' );
					break;

				case 'x':
					try
					{
						int hex08 = Integer.parseInt( this.currentLine.line.substring( i + 1, i + 3 ), 16 );
						resultString.append( (char) hex08 );
						i += 2;
					}
					catch ( NumberFormatException e )
					{
						this.error( "Hexadecimal character escape requires 2 digits" );

						resultString.append( ch );
					}
					break;

				case 'u':
					try
					{
						int hex16 = Integer.parseInt( this.currentLine.line.substring( i + 1, i + 5 ), 16 );
						resultString.append( (char) hex16 );
						i += 4;
					}
					catch ( NumberFormatException e )
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
							int octal = Integer.parseInt( this.currentLine.line.substring( i, i + 3 ), 8 );
							resultString.append( (char) octal );
							i += 2;
							break;
						}
						catch ( NumberFormatException e )
						{
							this.error( "Octal character escape requires 3 digits" );
						}
					}
					resultString.append( ch );
				}
				continue;
			}

			// Handle template substitutions
			if ( template && ch == '{' )
			{
				// Move the current token to the expression
				this.currentLine = this.currentLine.substring( i + 1 );

				Value rhs = this.parseExpression( scope );

				if ( "}".equals( this.currentToken() ) )
				{
					this.readToken(); // }
				}
				else
				{
					this.parseException( "}", this.currentToken() );
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

			// Potentially handle comments
			if ( allowComments )
			{
				// If we've already seen a slash
				if ( slash )
				{
					slash = false;
					if ( ch == '/' )
					{
						// Throw away the rest of the line
						i = this.currentLine.line.length() - 1;
						continue;
					}
					resultString.append( '/' );
				}
				else if ( ch == '/' )
				{
					slash = true;
					continue;
				}
			}

			// Handle plain strings
			if ( type == null )
			{
				if ( ch == stopCharacter )
				{
					this.currentLine = this.currentLine.substring( i + 1 ); //+ 1 to get rid of stop character token
					this.currentToken = null;

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
				continue;
			}

			// Handle typed constants
			// Allow start char without escaping
			if ( ch == startCharacter )
			{
				level++;
				resultString.append( ch );
				continue;
			}

			// Match non-initial start char
			if ( ch == stopCharacter && --level > 0 )
			{
				resultString.append( ch );
				continue;
			}

			if ( ch != stopCharacter && ch != ',' )
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

			if ( ch == stopCharacter )
			{
				this.currentLine = this.currentLine.substring( i + 1 );
				this.currentToken = null;
				if ( list.size() == 0 )
				{
					// Empty list - caller will interpret this specially
					return null;
				}
				return new PluralValue( type, list );
			}
		}
	}

	private Value parseLiteral( final Type type, final String element )
	{
		Value value = DataTypes.parseValue( type, element, false );
		if ( value == null )
		{
			this.error( "Bad " + type.toString() + " value: \"" + element + "\"" );

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
		if ( !"$".equals( this.currentToken() ) )
		{
			return null;
		}

		Position typedConstantStart = this.here();

		this.readToken(); // read $

		String name = this.currentToken();
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
				this.error( typedConstantStart, "Unknown type " + name );
			}
			typedConstantError = typedConstantSyntaxError = true;

			type = Type.BAD_TYPE;
		}
		else
		{
			type.addReference( this.makeLocation( typedConstantStart ) );
		}

		if ( !type.isPrimitive() && type != Type.BAD_TYPE )
		{
			if ( !typedConstantError )
			{
				this.error( typedConstantStart, "Non-primitive type " + name );
				typedConstantError = true;
			}

			type = Type.BAD_TYPE;
		}

		if ( !"[".equals( this.currentToken() ) )
		{
			if ( !typedConstantSyntaxError )
			{
				this.parseException( "[", this.currentToken() );
			}

			return Value.BAD_VALUE;
		}

		if ( plurals )
		{
			Value value = this.parseString( scope, type );
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
		for ( int i = 1;; ++i )
		{
			if ( i == this.currentLine.line.length() )
			{
				this.currentLine = this.currentLine.clear();
				this.currentToken = null;

				if ( !typedConstantError )
				{
					this.error( "No closing ] found" );
				}

				String input = resultString.toString().trim();
				return this.parseLiteral( type, input );
			}

			char c = this.currentLine.line.charAt( i );
			if ( c == '\\' )
			{
				resultString.append( this.currentLine.line.charAt( ++i ) );
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
				this.currentLine = this.currentLine.substring( i + 1 ); //+1 to get rid of ']' token
				this.currentToken = null;
				String input = resultString.toString().trim();
				return this.parseLiteral( type, input );
			}
			else
			{
				resultString.append( c );
			}
		}
	}

	private Operator parseOperator( final String oper )
	{
		if ( !this.isOperator( oper ) )
		{
			return null;
		}

		return new Operator( oper, this );
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
		if ( !this.parseIdentifier( this.currentToken() ) )
		{
			return null;
		}

		Position variableStart = this.here();

		String name = this.currentToken();
		Variable var = scope.findVariable( name, true );

		this.readToken(); // read name

		if ( var != null )
		{
			var.addReference( this.makeLocation( variableStart ) );
		}
		else
		{
			this.error( variableStart, "Unknown variable '" + name + "'" );

			var = Variable.BAD_VARIABLE;
		}

		if ( !"[".equals( this.currentToken() ) && !".".equals( this.currentToken() ) )
		{
			return new VariableReference( var );
		}

		return this.parseVariableReference( scope, var );
	}

	private Value parseVariableReference( final BasicScope scope, final Variable var )
	{
		Type type = var.getType();
		List<Value> indices = new ArrayList<Value>();

		boolean parseAggregate = "[".equals( this.currentToken() );
		boolean variableReferenceError = false, variableReferenceSyntaxError = false;

		while ( "[".equals( this.currentToken() ) ||
		        ".".equals( this.currentToken() ) ||
		        parseAggregate && ",".equals( this.currentToken() ) )
		{
			Value index;

			type = type.getBaseType();

			if ( "[".equals( this.currentToken() ) || ",".equals( this.currentToken() ) )
			{
				this.readToken(); // read [ or ,
				parseAggregate = true;

				if ( !( type instanceof AggregateType ) )
				{
					if ( !variableReferenceError && type != Type.BAD_TYPE )
					{
						String message;
						if ( indices.isEmpty() )
						{
							message = "Variable '" + var.getName() + "' cannot be indexed";
						}
						else
						{
							message = "Too many keys for '" + var.getName() + "'";
						}
						this.error( message );
						variableReferenceError = true;
					}

					type = AggregateType.BAD_AGGREGATE;
				}

				AggregateType atype = (AggregateType) type;
				index = this.parseExpression( scope );
				if ( index == null )
				{
					if ( !variableReferenceSyntaxError )
					{
						this.error( "Index for '" + var.getName() + "' expected" );
					}
					variableReferenceError = variableReferenceSyntaxError = true;

					index = Value.BAD_VALUE;
				}

				if ( !variableReferenceError &&
				     !index.getType().getBaseType().equals( atype.getIndexType().getBaseType() ) &&
				     index.getType().getBaseType() != Type.BAD_TYPE &&
				     atype.getIndexType().getBaseType() != Type.BAD_TYPE )
				{
					this.error( "Index for '" + var.getName() + "' has wrong data type " +
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
					return this.parseCall(
						scope, indices.isEmpty() ? new VariableReference( var ) : new CompositeReference( var, indices, this ) );
				}

				type = type.asProxy();
				if ( !( type instanceof RecordType ) )
				{
					if ( type != Type.BAD_TYPE )
					{
						// See this as a syntax error, since we don't know yet
						// if what follows is even an identifier.
						if ( !variableReferenceSyntaxError )
						{
							this.error( "Record expected" );
						}
						variableReferenceError = variableReferenceSyntaxError = true;
					}

					type = RecordType.BAD_RECORD;
				}

				RecordType rtype = (RecordType) type;

				String field = this.currentToken();
				if ( this.parseIdentifier( field ) )
				{
					this.readToken(); // read name
				}
				else
				{
					if ( !variableReferenceSyntaxError )
					{
						this.error( "Field name expected" );
					}
					variableReferenceError = variableReferenceSyntaxError = true;
				}

				index = rtype.getFieldIndex( field );
				if ( index == null )
				{
					if ( !variableReferenceError )
					{
						this.error( "Invalid field name '" + field + "'" );
						variableReferenceError = true;
					}

					index = Value.BAD_VALUE;
					type = Type.BAD_TYPE;
				}
				else
				{
					type = rtype.getDataType( index );
				}
			}

			indices.add( index );

			if ( parseAggregate && "]".equals( this.currentToken() ) )
			{
				this.readToken(); // read ]
				parseAggregate = false;
			}
		}

		if ( parseAggregate && !variableReferenceSyntaxError )
		{
			this.parseException( "]", this.currentToken() );
		}

		return new CompositeReference( var, indices, this );
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
		if ( !directive.equalsIgnoreCase( this.currentToken() ) )
		{
			return null;
		}

		Position directiveStart = this.here();

		this.readToken(); //directive

		this.currentToken(); // we don't care what it is, just trigger the method.

		if ( this.atEndOfFile() )
		{
			this.parseException( "<", "end of file" );

			return null;
		}

		String resultString = null;
		int endIndex = -1;
		char firstChar = this.currentLine.line.charAt( 0 );

		for ( char ch : new char[] { '<', '\'', '"' } )
		{
			if ( ch != firstChar )
			{
				continue;
			}

			this.currentLine = this.currentLine.substring( 1 );

			if ( ch == '<' )
			{
				ch = '>';
			}

			endIndex = this.currentLine.line.indexOf( ch );

			if ( endIndex == -1 )
			{
				this.error( "No closing " + ch + " found" );

				break;
			}

			resultString = this.currentLine.line.substring( 0, endIndex );
			this.currentLine = this.currentLine.substring( endIndex + 1 ); //get rid of '>', '\'' or '"' token

			break;
		}

		if ( endIndex == -1 )
		{
			endIndex = this.currentLine.line.indexOf( ";" );

			if ( endIndex == -1 )
			{
				endIndex = this.currentLine.line.length();
			}

			resultString = this.currentLine.line.substring( 0, endIndex );
			this.currentLine = this.currentLine.substring( endIndex );
		}

		this.currentToken = null;

		if ( ";".equals( this.currentToken() ) )
		{
			this.readToken(); //read ;
		}

		return new Directive( resultString, this.rangeToHere( directiveStart ) );
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
	private Line getNextLine()
	{
		try
		{
			Line fullLine;
			do
			{
				// Read a line from input, and break out of the
				// do-while loop when you've read a valid line

				fullLine = new Line( this.commandStream.readLine(), this.commandStream.getLineNumber() );

				// Return null at end of file
				if ( fullLine.line == null )
				{
					return null;
				}

				if ( fullLine.line.length() == 0 )
				{
					continue;
				}

				// If the line starts with a Unicode BOM, remove it.
				if ( fullLine.line.charAt( 0 ) == Parser.BOM )
				{
					fullLine = fullLine.substring( 1 );
				}

				// Remove whitespace at front and end
				fullLine = fullLine.trim();
			}
			while ( fullLine.line.length() == 0 );

			// Found valid currentLine - return it

			return fullLine;
		}
		catch ( IOException e )
		{
			// This should not happen.  Therefore, print
			// a stack trace for debug purposes.

			StaticEntity.printStackTrace( e );
			return null;
		}
	}

	private String currentToken()
	{
		// Repeat until we get a token
		while ( true )
		{
			// If we've already parsed a token, return it
			if ( this.currentToken != null )
			{
				return this.currentToken;
			}

			// Locate next token
			this.fixLines();
			if ( this.currentLine == null )
			{
				return ";";
			}

			// "#" starts a whole-line comment
			if ( this.currentLine.line.startsWith( "#" ) )
			{
				// Skip the comment
				this.currentLine = this.currentLine.clear();
				continue;
			}

			// Get the next token for consideration
			this.currentToken = this.currentLine.line.substring( 0, this.tokenLength( this.currentLine.line ) );

			// "//" starts a comment which consumes the rest of the line
			if ( "//".equals( this.currentToken ) )
			{
				// Skip the comment
				this.currentToken = null;
				this.currentLine = this.currentLine.clear();
				continue;
			}

			// "/*" starts a comment which is terminated by "*/"
			if ( !"/*".equals( this.currentToken ) )
			{
				return this.currentToken;
			}

			while ( this.currentLine != null )
			{
				int end = this.currentLine.line.indexOf( "*/" );
				if ( end == -1 )
				{
					// Skip entire line
					this.currentLine = this.currentLine.clear();
					this.fixLines();
					continue;
				}

				this.currentLine = this.currentLine.substring( end + 2 );
				this.currentToken = null;
				break;
			}
		}
	}

	private String nextToken()
	{
		this.fixLines();

		if ( this.currentLine == null )
		{
			return null;
		}

		if ( this.tokenLength( this.currentLine.line ) >= this.currentLine.line.length() )
		{
			if ( this.nextLine == null )
			{
				return null;
			}

			return this.nextLine.line.substring( 0, this.tokenLength( this.nextLine.line ) ).trim();
		}

		String result = this.currentLine.line.substring( this.tokenLength( this.currentLine.line ) ).trim();

		if ( "".equals( result ) )
		{
			if ( this.nextLine == null )
			{
				return null;
			}

			return this.nextLine.line.substring( 0, this.tokenLength( this.nextLine.line ) );
		}

		return result.substring( 0, this.tokenLength( result ) );
	}

	// Put a token back, so it can be parsed again later.
	private void replaceToken( final String s )
	{
		this.currentLine = this.currentLine.replaceToken( s );
		this.currentToken = null;
	}

	private void readToken()
	{
		this.fixLines();

		if ( this.currentLine == null )
		{
			return;
		}

		this.currentLine = this.currentLine.substring( this.tokenLength( this.currentLine.line ) );
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

	private void fixLines()
	{
		this.currentToken = null;
		if ( this.currentLine == null )
		{
			return;
		}

		while ( "".equals( this.currentLine.line ) )
		{
			this.previousLine = this.currentLine;
			this.currentLine = this.nextLine;
			this.nextLine = this.getNextLine();

			if ( this.currentLine == null )
			{
				return;
			}
		}

		this.currentLine = this.currentLine.trim();

		if ( this.nextLine == null )
		{
			return;
		}

		while ( "".equals( this.nextLine.line ) )
		{
			this.nextLine = this.getNextLine();
			if ( this.nextLine == null )
			{
				return;
			}
		}

		this.nextLine = this.nextLine.trim();
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

	private boolean atEndOfFile()
	{
		this.currentToken();

		return this.currentLine == null;
	}

	private boolean madeProgress( final Position previousPosition, final Position currentPosition )
	{
		return previousPosition == null ||
		       previousPosition.getLine() < currentPosition.getLine() ||
		       previousPosition.getCharacter() < currentPosition.getCharacter();
	}

	/**
	 * A container for a String as well as its position
	 * in a file.
	 * Would be simpler if we could just extend {@link String},
	 * but we can't, since it's a final class...
	 */
	class Line
	{
		final String line;
		final int lineNumber;
		final int offset;

		Line( final String line, final int lineNumber )
		{
			this( line, lineNumber, 0 );
		}

		Line( final String line, final int lineNumber, final int offset )
		{
			this.line = line;
			this.lineNumber = lineNumber;
			this.offset = offset;
		}

		Line trim()
		{
			String trimmed = this.line.trim();

			if ( trimmed.equals( this.line ) )
			{
				return this;
			}

			int lTrim = this.line.indexOf( trimmed );
			if ( lTrim == -1 )
			{
				lTrim = this.line.length();
			}

			return new Line( trimmed, this.lineNumber, this.offset + lTrim );
		}

		Line substring( final int beginIndex )
		{
			return new Line( this.line.substring( beginIndex ), this.lineNumber, this.offset + beginIndex );
		}

		Line clear()
		{
			return this.substring( this.line.length() );
		}

		Line replaceToken( final String s )
		{
			return new Line( s + this.line, this.lineNumber, this.offset - s.length() );
		}
	}

	private Position here() // FIXME temporarily made short. Rename to getCurrentPosition()
	{
		Line line = this.currentLine != null ? this.currentLine : this.previousLine;

		// 0-indexed
		int lineNumber = line != null ? line.lineNumber - 1 : 0;
		int character = line != null ? line.offset : 0;
		return new Position( lineNumber, character );
	}

	private Range rangeToHere( final Position start )
	{
		return new Range( start != null ? start : this.here(), this.here() );
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

	private void parseException( final String expected, final String actual )
	{
		this.error( this.make0WidthLocation(), "Expected " + expected + ", found " + actual );
	}

	private void parseException( final Position start, final String expected, final String actual )
	{
		this.error( start, "Expected " + expected + ", found " + actual );
	}

	private void multiplyDefinedFunctionError( final Position start, final Function f )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' defined multiple times.";
		this.error( start, buffer );
	}

	private void overridesLibraryFunctionError( final Position start, final Function f )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' overrides a library function.";
		this.error( start, buffer );
	}

	private void varargClashError( final Position start, final Function f, final Function clash )
	{
		String buffer = "Function '" +
				f.getSignature() +
				"' clashes with existing function '" +
				clash.getSignature() +
				"'.";
		this.error( start, buffer );
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
					this.error( "invalid 'since' format" );
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
			this.error( "invalid 'since' format" );
		}
	}

	public final void error( final String msg )
	{
		this.error( msg, "" );
	}

	public final void error( final String msg1, final String msg2 )
	{
		this.error( this.make0WidthLocation(), msg1, msg2 );
	}

	public final void error( final Position start, final String msg )
	{
		this.error( start, msg, "" );
	}

	public final void error( final Position start, final String msg1, final String msg2 )
	{
		this.error( this.makeLocation( start ), msg1, msg2 );
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
		this.warning( this.make0WidthLocation(), msg1, msg2 );
	}

	public final void warning( final Location location, final String msg )
	{
		this.warning( location, msg, "" );
	}

	public final void warning( final Location location, final String msg1, final String msg2 )
	{
		this.diagnostics.add( new AshDiagnostic( location, Warning, msg1, msg2 ) );
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

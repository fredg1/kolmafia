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

package net.sourceforge.kolmafia.textui.langserver.textdocumentservice;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;

/**
 * Semantic tokens, i.e. "telling the client what everything 'is' so
 * that it can slap color onto it accordingly".
 */
class SemanticTokensHandler
{
	static final List<String> TYPES =
		Collections.unmodifiableList(
			Arrays.asList(
				SemanticTokenTypes.Type,
				SemanticTokenTypes.Enum, // for plural typed constants..?
				SemanticTokenTypes.Struct, // We call those "record"s
				SemanticTokenTypes.Variable,
				SemanticTokenTypes.Property,
				SemanticTokenTypes.EnumMember, // for typed constants..?
				SemanticTokenTypes.Function,
				SemanticTokenTypes.Macro, // for cli_execute {}
				SemanticTokenTypes.Keyword,
				SemanticTokenTypes.Comment,
				SemanticTokenTypes.String,
				SemanticTokenTypes.Number,
				SemanticTokenTypes.Operator

				/** Unused:
				 * Namespace
				 * Class
				 * Interface
				 * TypeParameter
				 * Parameter (we don't keep track of which variable is a parameter)
				 * Event
				 * Method
				 * Modifier
				 * Regexp
				 */
			) );

	static final Map<String, Integer> TYPES_INVERTED = 
		Collections.unmodifiableMap(
			new HashMap<>()
			{{
				for ( int i = 0; i < TYPES.size(); ++i )
				{
					this.put( TYPES.get( i ), i );
				}
			}}
		);

	static final List<String> MODIFIERS =
		Collections.unmodifiableList(
			Arrays.asList(
				SemanticTokenModifiers.Declaration, // we allow this with functions. Also for record fields
				SemanticTokenModifiers.Definition, // not for variables
				SemanticTokenModifiers.Readonly,
				SemanticTokenModifiers.Modification,
				SemanticTokenModifiers.DefaultLibrary

				/** Unused:
				 * Static
				 * Deprecated
				 * Abstract
				 * Async
				 * Documentation (we currently don't have such thing as "documentation")
				 */
			) );

	static final Map<String, Integer> MODIFIERS_INVERTED = 
		Collections.unmodifiableMap(
			new HashMap<>()
			{{
				for ( int i = 0; i < MODIFIERS.size(); ++i )
				{
					this.put( MODIFIERS.get( i ), i );
				}
			}}
		);

	static int getType( final String type )
	{
		if ( !SemanticTokensHandler.TYPES_INVERTED.containsKey( type ) )
		{
			throw new RuntimeException( "Unknown semantic token type" );
		}

		return SemanticTokensHandler.TYPES_INVERTED.get( type );
	}

	static int getModifier( final String modifier )
	{
		if ( !SemanticTokensHandler.MODIFIERS_INVERTED.containsKey( modifier ) )
		{
			throw new RuntimeException( "Unknown semantic token modifier" );
		}

		return SemanticTokensHandler.MODIFIERS_INVERTED.get( modifier );
	}
}

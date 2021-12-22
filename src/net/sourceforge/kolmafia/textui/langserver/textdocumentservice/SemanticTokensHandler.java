package net.sourceforge.kolmafia.textui.langserver.textdocumentservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.kolmafia.textui.Line.Token;
import net.sourceforge.kolmafia.textui.Parser;
import net.sourceforge.kolmafia.textui.langserver.AshLanguageServer;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokenTypes;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensCapabilities;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensServerFull;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TokenFormat;

/**
 * Semantic tokens, i.e. "telling the client what everything 'is' so that it can slap color onto it
 * accordingly".
 */
class SemanticTokensHandler {
  private static final List<String> TYPES =
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

              /**
               * Unused: Namespace Class Interface TypeParameter Parameter (we don't keep track of
               * which variable is a parameter) Event Method Modifier Regexp
               */
              ));

  private static final Map<String, Integer> TYPES_INVERTED =
      Collections.unmodifiableMap(
          new HashMap<String, Integer>() {
            {
              for (int i = 0; i < TYPES.size(); ++i) {
                this.put(TYPES.get(i), i);
              }
            }
          });

  private static final List<String> MODIFIERS =
      Collections.unmodifiableList(
          Arrays.asList(
              SemanticTokenModifiers
                  .Declaration, // we allow this with functions. Also for record fields
              SemanticTokenModifiers.Definition, // not for variables
              SemanticTokenModifiers.Readonly,
              SemanticTokenModifiers.Modification,
              SemanticTokenModifiers.DefaultLibrary

              /**
               * Unused: Static Deprecated Abstract Async Documentation (we currently don't have
               * such thing as "documentation")
               */
              ));

  private static final Map<String, Integer> MODIFIERS_INVERTED =
      Collections.unmodifiableMap(
          new HashMap<String, Integer>() {
            {
              for (int i = 0; i < MODIFIERS.size(); ++i) {
                this.put(MODIFIERS.get(i), i);
              }
            }
          });

  private static int getType(final String type) {
    if (!SemanticTokensHandler.TYPES_INVERTED.containsKey(type)) {
      throw new RuntimeException("Unknown semantic token type");
    }

    return SemanticTokensHandler.TYPES_INVERTED.get(type);
  }

  private static int getModifier(final String modifier) {
    if (!SemanticTokensHandler.MODIFIERS_INVERTED.containsKey(modifier)) {
      throw new RuntimeException("Unknown semantic token modifier");
    }

    return SemanticTokensHandler.MODIFIERS_INVERTED.get(modifier);
  }

  final AshLanguageServer parent;

  private SemanticTokensCapabilities clientCapabilities;
  private boolean relativeFormat = false;

  SemanticTokensHandler(final AshLanguageServer parent) {
    this.parent = parent;
  }

  final void setCapabilities(final ServerCapabilities capabilities) {
    if (this.parent.clientCapabilities != null
        && this.parent.clientCapabilities.getTextDocument() != null) {
      this.clientCapabilities =
          this.parent.clientCapabilities.getTextDocument().getSemanticTokens();
    }

    if (this.clientCapabilities != null) {
      this.relativeFormat = this.clientCapabilities.getFormats().contains(TokenFormat.Relative);
    }

    final SemanticTokensWithRegistrationOptions registrationOptions =
        new SemanticTokensWithRegistrationOptions(
            new SemanticTokensLegend(SemanticTokensHandler.TYPES, SemanticTokensHandler.MODIFIERS));

    if (this.clientCapabilities != null
        && this.clientCapabilities.getRequests() != null
        && this.clientCapabilities.getRequests().getFull() != null
        && this.clientCapabilities.getRequests().getFull().getRight() != null) {
      registrationOptions.setFull(new SemanticTokensServerFull(false));
    } else {
      registrationOptions.setFull(true);
    }

    registrationOptions.setRange(true);

    capabilities.setSemanticTokensProvider(registrationOptions);
  }

  SemanticTokens getSemanticTokens(final File file, final Range range) {
    if (file == null) {
      return new SemanticTokens(Collections.emptyList());
    }

    final List<Token> tokens;
    final Parser parser = this.parent.monitor.findOrMakeHandler(file).get(0).getParser();
    if (parser != null) {
      tokens = parser.getTokens(range);
    } else {
      tokens = Collections.emptyList();
    }

    final List<Integer> data = new ArrayList<>(tokens.size() * 5);

    Position previousTokenStart = new Position(0, 0);
    for (final Token token : tokens) {
      final Integer[] group = new Integer[5];

      // 0 = line
      // 1 = start character
      if (!this.relativeFormat) {
        group[0] = token.getStart().getLine();
        group[1] = token.getStart().getCharacter();
      } else if (previousTokenStart.getLine() == token.getStart().getLine()) {
        group[0] = 0;
        group[1] = token.getStart().getCharacter() - previousTokenStart.getCharacter();
      } else {
        group[0] = token.getStart().getLine() - previousTokenStart.getLine();
        group[1] = token.getStart().getCharacter();
      }

      // 2 = length
      group[2] = token.getEnd().getCharacter() - token.getStart().getCharacter();

      try {
        // 3 = type
        String type = token.getSemanticType();

        if (this.clientCapabilities != null
            && this.clientCapabilities.getTokenTypes() != null
            && !this.clientCapabilities.getTokenTypes().contains(type)) {
          if (SemanticTokenTypes.Enum.equals(type) || SemanticTokenTypes.Struct.equals(type)) {
            type = SemanticTokenTypes.Type;
          } else if (SemanticTokenTypes.Property.equals(type)
              || SemanticTokenTypes.EnumMember.equals(type)) {
            type = SemanticTokenTypes.Variable;
          } else if (SemanticTokenTypes.Macro.equals(type)) {
            type = SemanticTokenTypes.String;
          } else {
            // *shrug*
          }
        }

        group[3] = SemanticTokensHandler.getType(type);

        // 4 = modifiers
        int mask = 0;

        for (final String modifier : token.getSemanticModifiers()) {
          mask |= 1 << SemanticTokensHandler.getModifier(modifier);
        }

        group[4] = mask;
      } catch (RuntimeException e) {
        // someone put an unregistered (i.e. not in the maps above)
        // token type/modifier in Parser.java
        // (just add it to the corresponding map to fix this)
        continue;
      }

      previousTokenStart = token.getStart();
      data.addAll(Arrays.asList(group));
    }

    return new SemanticTokens(data);
  }
}

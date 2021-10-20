package net.sourceforge.kolmafia.textui;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.sourceforge.kolmafia.StaticEntity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokenTypes;

public final class Line {
  private static final char BOM = '\ufeff';

  final String content;
  final int lineNumber;
  final int offset;

  final Deque<Token> tokens = new LinkedList<>();

  final Line previousLine;
  /* Not made final to avoid a possible StackOverflowError. Do not modify. */
  Line nextLine = null;

  Line(final LineNumberReader commandStream) {
    this(commandStream, null);
  }

  Line(final LineNumberReader commandStream, final Line previousLine) {
    this.previousLine = previousLine;
    if (previousLine != null) {
      previousLine.nextLine = this;
    }

    int offset = 0;
    String line;

    try {
      line = commandStream.readLine();
    } catch (IOException e) {
      // This should not happen. Therefore, print a stack trace for debug purposes.
      StaticEntity.printStackTrace(e);
      line = null;
    }

    if (line == null) {
      // We are the "end of file" (or there was an IOException when reading)
      this.content = null;
      this.lineNumber = this.previousLine != null ? this.previousLine.lineNumber : 0;
      this.offset = this.previousLine != null ? this.previousLine.offset : 0;
      return;
    }

    // If the line starts with a Unicode BOM, remove it.
    if (line.length() > 0 && line.charAt(0) == Line.BOM) {
      line = line.substring(1);
      offset += 1;
    }

    // Remove whitespace at front and end
    final String trimmed = line.trim();

    if (!trimmed.isEmpty()) {
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

      final int ltrim = line.indexOf(trimmed.charAt(0));
      offset += ltrim;
    }

    line = trimmed;

    this.content = line;
    this.lineNumber = commandStream.getLineNumber();
    this.offset = offset;
  }

  String substring(final int beginIndex) {
    if (this.content == null) {
      return "";
    }

    // subtract "offset" from beginIndex, since we already removed it
    return this.content.substring(beginIndex - this.offset);
  }

  Token makeToken(final int tokenLength) {
    final Token newToken = new Token(tokenLength);
    this.tokens.addLast(newToken);
    return newToken;
  }

  Token makeComment(final int commentLength) {
    final Token newToken = new Comment(commentLength);
    newToken.setType(SemanticTokenTypes.Comment);
    this.tokens.addLast(newToken);
    return newToken;
  }

  Token peekLastToken() {
    Line line = this;

    while (line != null) {
      final Iterator<Token> iter = line.tokens.descendingIterator();
      while (iter.hasNext()) {
        final Token token = iter.next();

        if (!(token instanceof Comment)) {
          return token;
        }
      }

      line = this.previousLine;
    }

    return null;
  }

  @Override
  public String toString() {
    return this.content;
  }

  public class Token extends Range {
    final String content;
    final String followingWhitespace;
    final int restOfLineStart;

    private String semanticType;
    private List<String> semanticModifiers = new ArrayList<>();

    private Token(final int tokenLength) {
      final int offset;

      if (!Line.this.tokens.isEmpty()) {
        offset = Line.this.tokens.getLast().restOfLineStart;
      } else {
        offset = Line.this.offset;
      }

      final String lineRemainder;

      if (Line.this.content == null) {
        // At end of file
        this.content = ";";
        // Going forward, we can just assume lineRemainder is an
        // empty string.
        lineRemainder = "";
      } else {
        final String lineRemainderWithToken = Line.this.substring(offset);

        this.content = lineRemainderWithToken.substring(0, tokenLength);
        lineRemainder = lineRemainderWithToken.substring(tokenLength);
      }

      // 0-indexed line
      final int lineNumber = Math.max(0, Line.this.lineNumber - 1);
      this.setStart(new Position(lineNumber, offset));
      this.setEnd(new Position(lineNumber, offset + tokenLength));

      // As in Line(), this is more efficient than lineRemainder.indexOf( lineRemainder.trim() ).
      String trimmed = lineRemainder.trim();
      final int lTrim = trimmed.isEmpty() ? 0 : lineRemainder.indexOf(trimmed.charAt(0));

      this.followingWhitespace = lineRemainder.substring(0, lTrim);

      this.restOfLineStart = offset + tokenLength + lTrim;
    }

    public String getSemanticType() {
      return this.semanticType;
    }

    void setType(final String semanticType) {
      this.semanticType = semanticType;
    }

    public List<String> getSemanticModifiers() {
      return this.semanticModifiers;
    }

    void addModifier(final String semanticModifier) {
      this.semanticModifiers.add(semanticModifier);
    }

    /** The Line in which this token exists */
    final Line getLine() {
      return Line.this;
    }

    public boolean equals(final String s) {
      return this.content.equals(s);
    }

    public boolean equalsIgnoreCase(final String s) {
      return this.content.equalsIgnoreCase(s);
    }

    public int length() {
      return this.content.length();
    }

    public String substring(final int beginIndex) {
      return this.content.substring(beginIndex);
    }

    public String substring(final int beginIndex, final int endIndex) {
      return this.content.substring(beginIndex, endIndex);
    }

    public boolean endsWith(final String suffix) {
      return this.content.endsWith(suffix);
    }

    @Override
    public String toString() {
      return this.content;
    }
  }

  private class Comment extends Token {
    private Comment(final int commentLength) {
      super(commentLength);
    }
  }
}

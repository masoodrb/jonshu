package jonshu.syntax;

import java.util.Objects;

// import Microsoft.PowerFx.Core.Localization.Span;

/**
 * Base class for all lexing tokens.
 */
public abstract class Token {
    private final TokKind kind;
    private final Span span;

    Token(TokKind kind, Span span) {
        this.kind = kind;
        this.span = span;
    }

    /**
     * Kind of the token.
     */
    public TokKind getKind() {
        return kind;
    }

    /**
     * Span of the token in the formula.
     */
    public Span getSpan() {
        return span;
    }

    protected boolean isDottedNamePunctuator() {
        return false;
    }

    protected abstract Token clone(Span span);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Token token = (Token) obj;
        return kind == token.kind && Objects.equals(span, token.span);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, span);
    }
}
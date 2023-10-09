package jonshu.syntax;

/**
 * Kinds of tokens.
 */
public enum TokKind {
    None, // TODO: Don't know what this is

    // Miscellaneous

    /**
     * End of file.
     */
    Eof,

    /**
     * Unknown/lexing error.
     */
    Error,

    // Identifiers and literals

    /**
     * Identifier/name.
     */
    Ident,

    /**
     * Numeric literal.
     * <code>3.14</code>
     */
    NumLit,

    /**
     * Decimal literal.
     * <code>12.34</code>
     */
    DecLit,

    /**
     * String literal.
     * <code>"Hello world"</code>
     */
    StrLit,

    /**
     * Comment.
     */
    Comment,

    /**
     * Whitespace.
     */
    Whitespace,

    // Punctuators

    /**
     * Addition.
     * <code>+</code>
     */
    Add,

    /**
     * Subtraction.
     * <code>-</code>
     */
    Sub,

    /**
     * Multiplication.
     * <code>*</code>
     */
    Mul,

    /**
     * Division.
     * <code>/</code>
     */
    Div,

    /**
     * Power/exponentiation.
     * <code>^</code>
     */
    Caret,

    /**
     * Open parenthesis.
     * <code>(</code>
     */
    ParenOpen,

    /**
     * Closed parenthesis.
     * <code>)</code>
     */
    ParenClose,

    /**
     * Open curly brace.
     * <code>{</code>
     */
    CurlyOpen,

    /**
     * Closed curly brace.
     * <code>}</code>
     */
    CurlyClose,

    /**
     * Open bracket.
     * <code>[</code>
     */
    BracketOpen,

    /**
     * Closed bracket.
     * <code>]</code>
     */
    BracketClose,

    /**
     * Equals.
     * <code>=</code>
     */
    Equ,

    /**
     * Less than.
     * <code>&lt;</code>
     */
    Lss,

    /**
     * Less than or equal.
     * <code>&lt;=</code>
     */
    LssEqu,

    /**
     * Greater than.
     * <code>&gt;</code>
     */
    Grt,

    /**
     * Greater than or equal.
     * <code>&gt;=</code>
     */
    GrtEqu,

    /**
     * Less than or greater than.
     * <code>&lt;&gt;</code>
     */
    LssGrt,

    /**
     * Comma.
     * <code>,</code>
     */
    Comma,

    /**
     * Dot.
     * <code>.</code>
     */
    Dot,

    /**
     * Colon.
     * <code>:</code>
     */
    Colon,

    /**
     * Ampersand (concatenation).
     * <code>&amp;</code>
     */
    Ampersand,

    /**
     * Percent sign.
     * <code>%</code>
     */
    PercentSign,

    /**
     * Semicolon.
     * <code>;</code>
     */
    Semicolon,

    /**
     * At symbol.
     * <code>@</code>
     */
    At,

    // Keywords

    /**
     * Or operator.
     * <code>||</code>
     */
    Or,

    /**
     * And operator
     * <code>&amp;&amp;</code>.
     */
    And,

    /**
     * Bang (not).
     * <code>!</code>
     */
    Bang,

    /**
     * Boolean true constant.
     */
    True,

    /**
     * Boolean false constant.
     */
    False,

    /**
     * In keyword.
     */
    In,

    /**
     * Exact in keyword.
     * <code>exactin</code>
     */
    Exactin,

    /**
     * Self identifier.
     */
    Self,

    /**
     * Parent identifier.
     */
    Parent,

    /**
     * Or keyword.
     */
    KeyOr,

    /**
     * And keyword.
     */
    KeyAnd,

    /**
     * Not keyword.
     */
    KeyNot,

    /**
     * As keyword.
     */
    As,

    // Interpolation

    /**
     * Start of the string interpolation.
     * <code>$"</code>
     */
    StrInterpStart,

    /**
     * End of the string interpolation.
     */
    StrInterpEnd,

    /**
     * Start of the string interpolation part (island).
     * <code>{</code>
     */
    IslandStart,

    /**
     * End of the string interpolation part (island).
     * <code>}</code>
     */
    IslandEnd,

    /**
     * Start of body for user defined functions.
     * <code>=></code>
     */
    DoubleBarrelArrow,
}

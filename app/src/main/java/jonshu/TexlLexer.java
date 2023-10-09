package jonshu;

import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jonshu.Syntax.*;
import jonshu.Utils.StringBuilderCache;
import jonshu.Runtime.FlagsAttribute;

public class TexlLexer {

    @FlagsAttribute
    public enum Flags {
        None(0),

        // When specified, literal numbers are treated as floats. By default, literal
        // numbers are decimals.
        NumberIsFloat(1 << 0),

        // Enable the use of reserved keywords as identifiers, for Canvas short term.
        DisableReservedKeywords(2 << 0);

        private final int value;

        Flags(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static final String KeywordTrue = "true";
    public static final String KeywordFalse = "false";
    public static final String KeywordIn = "in";
    public static final String KeywordExactin = "exactin";
    public static final String KeywordSelf = "Self";
    public static final String KeywordParent = "Parent";
    public static final String KeywordAnd = "And";
    public static final String KeywordOr = "Or";
    public static final String KeywordNot = "Not";
    public static final String KeywordAs = "As";
    public static final String PunctuatorDecimalSeparatorInvariant = ".";
    public static final String PunctuatorCommaInvariant = ",";
    public static final String PunctuatorSemicolonInvariant = ";";
    public static final String PunctuatorAnd = "&&";
    public static final String PunctuatorOr = "||";
    public static final String PunctuatorDot = ".";
    public static final String PunctuatorBang = "!";
    public static final String PunctuatorAdd = "+";
    public static final String PunctuatorSub = "-";
    public static final String PunctuatorMul = "*";
    public static final String PunctuatorDiv = "/";
    public static final String PunctuatorCaret = "^";
    public static final String PunctuatorAmpersand = "&";
    public static final String PunctuatorPercent = "%";
    public static final String PunctuatorEqual = "=";
    public static final String PunctuatorNotEqual = "<>";
    public static final String PunctuatorGreater = ">";
    public static final String PunctuatorGreaterOrEqual = ">=";
    public static final String PunctuatorLess = "<";
    public static final String PunctuatorLessOrEqual = "<=";
    public static final String PunctuatorParenOpen = "(";
    public static final String PunctuatorParenClose = ")";
    public static final String PunctuatorCurlyOpen = "{";
    public static final String PunctuatorCurlyClose = "}";
    public static final String PunctuatorBracketOpen = "[";
    public static final String PunctuatorBracketClose = "]";
    public static final String PunctuatorColon = ":";
    public static final String PunctuatorAt = "@";
    public static final char IdentifierDelimiter = '\'';
    public static final String PunctuatorDoubleBarrelArrow = "=>";

    // These puntuators are related to commenting in the formula bar
    public static final String PunctuatorBlockComment = "/*";
    public static final String PunctuatorLineComment = "//";

    // Defaults and options for disambiguation
    private static final String PunctuatorSemicolonDefault = PunctuatorSemicolonInvariant;
    private static final String PunctuatorSemicolonAlt1 = ";;";

    // Pretty Print defaults
    public static final String FourSpaces = "    ";
    public static final String LineBreakAndfourSpaces = "\n    ";

    // Reserved but currently unused keywords
    public static final String ReservedBlank = "blank";
    public static final String ReservedNull = "null";
    public static final String ReservedEmpty = "empty";
    public static final String ReservedNone = "none";
    public static final String ReservedNothing = "nothing";
    public static final String ReservedUndefined = "undefined";
    public static final String ReservedThis = "This";
    public static final String ReservedIs = "Is";
    public static final String ReservedChild = "Child";
    public static final String ReservedChildren = "Children";
    public static final String ReservedSiblings = "Siblings";

    private static final Map<String, TokKind> _keywords;
    private static final Set<String> _reservedKeywords;

    static {
        Map<String, TokKind> keywords = new HashMap<>();
        keywords.put(KeywordTrue, TokKind.True);
        keywords.put(KeywordFalse, TokKind.False);
        keywords.put(KeywordIn, TokKind.In);
        keywords.put(KeywordExactin, TokKind.Exactin);
        keywords.put(KeywordSelf, TokKind.Self);
        keywords.put(KeywordParent, TokKind.Parent);
        keywords.put(KeywordAnd, TokKind.KeyAnd);
        keywords.put(KeywordOr, TokKind.KeyOr);
        keywords.put(KeywordNot, TokKind.KeyNot);
        keywords.put(KeywordAs, TokKind.As);
        _keywords = Collections.unmodifiableMap(keywords);

        Set<String> reservedKeywords = new HashSet<>();
        reservedKeywords.add(ReservedBlank);
        reservedKeywords.add(ReservedNull);
        reservedKeywords.add(ReservedEmpty);
        reservedKeywords.add(ReservedNone);
        reservedKeywords.add(ReservedNothing);
        reservedKeywords.add(ReservedUndefined);
        reservedKeywords.add(ReservedIs);
        reservedKeywords.add(ReservedThis);
        reservedKeywords.add(ReservedChild);
        reservedKeywords.add(ReservedChildren);
        reservedKeywords.add(ReservedSiblings);
        _reservedKeywords = Collections.unmodifiableSet(reservedKeywords);
    }

    private static final int _desiredStringBuilderSize = 128;

    public static final TexlLexer InvariantLexer = new TexlLexer(PunctuatorDecimalSeparatorInvariant);
    public static final TexlLexer CommaDecimalSeparatorLexer = new TexlLexer(PunctuatorCommaInvariant);

    private static final List<String> _unaryOperatorKeywords;
    private static final List<String> _binaryOperatorKeywords;
    private static final List<String> _operatorKeywordsPrimitive;
    private static final List<String> _operatorKeywordsAggregate;
    private static final List<String> _constantKeywordsDefault;
    private static final List<String> _constantKeywordsGetParent;

    private final Map<String, TokKind> _punctuators;
    private final char _decimalSeparator;
    private final Map<String, String> _punctuatorsAndInvariants;
    private final NumberFormat _numberFormat;
    private final String LocalizedPunctuatorDecimalSeparator;
    private final String LocalizedPunctuatorListSeparator;
    private final String LocalizedPunctuatorChainingSeparator;

    static {
        StringBuilderCache.SetMaxBuilderSize(_desiredStringBuilderSize);

        _unaryOperatorKeywords = Arrays.asList(KeywordNot, PunctuatorBang);

        _binaryOperatorKeywords = Arrays.asList(
                PunctuatorAmpersand,
                PunctuatorAnd,
                PunctuatorOr,
                PunctuatorAdd,
                PunctuatorSub,
                PunctuatorMul,
                PunctuatorDiv,
                PunctuatorEqual,
                PunctuatorLess,
                PunctuatorLessOrEqual,
                PunctuatorGreater,
                PunctuatorGreaterOrEqual,
                PunctuatorNotEqual,
                PunctuatorCaret,

                KeywordAnd,
                KeywordOr,
                KeywordIn,
                KeywordExactin,
                KeywordAs);

        _operatorKeywordsPrimitive = Arrays.asList(
                PunctuatorAmpersand,
                PunctuatorEqual,
                PunctuatorNotEqual,
                PunctuatorAdd,
                PunctuatorSub,
                PunctuatorMul,
                PunctuatorDiv,
                PunctuatorCaret,
                PunctuatorAnd,
                PunctuatorOr,
                PunctuatorLess,
                PunctuatorLessOrEqual,
                PunctuatorGreater,
                PunctuatorGreaterOrEqual,

                KeywordAnd,
                KeywordOr,
                KeywordIn,
                KeywordExactin,
                KeywordAs);

        _operatorKeywordsAggregate = Arrays.asList(KeywordIn, KeywordExactin, KeywordAs);

        _constantKeywordsDefault = Arrays.asList(KeywordFalse, KeywordTrue, KeywordSelf);

        _constantKeywordsGetParent = Arrays.asList(KeywordFalse, KeywordTrue, KeywordParent, KeywordSelf);

    }

    public static TexlLexer getLocalizedInstance(Locale locale) {
        // this is a safe default value as we only use this value for determining the
        // decimal separator at next line
        locale = locale != null ? locale : Locale.getDefault();

        // Number decimal separator can be a dot (.), comma (,), arabic comma (Unicode
        // 0x66B)
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        return symbols.getDecimalSeparator() == PunctuatorDecimalSeparatorInvariant.charAt(0)
                ? InvariantLexer
                : CommaDecimalSeparatorLexer;
    }

    public static List<String> getKeywords() {
        return Collections.unmodifiableList(new ArrayList<>(_keywords.keySet()));
    }

    private TexlLexer(String decimalSeparator) {
        _punctuators = new HashMap<>();
        _punctuatorsAndInvariants = new HashMap<>();
        _decimalSeparator = decimalSeparator.charAt(0);

        _numberFormat = NumberFormat.getNumberInstance();
        _numberFormat.setGroupingUsed(false);
        _numberFormat.setMinimumFractionDigits(0);
        _numberFormat.setMaximumFractionDigits(Integer.MAX_VALUE);
        _numberFormat.setRoundingMode(RoundingMode.HALF_UP);

        LocalizedPunctuatorDecimalSeparator = _numberFormat.format(0.1).substring(1, 2);
        LocalizedPunctuatorListSeparator = _numberFormat.format(1.1).substring(1, 2);
        LocalizedPunctuatorChainingSeparator = _numberFormat.format(1.1).substring(1, 2);

        // List and decimal separators.
        // These are the default global settings. If there is a collision between the
        // two,
        // the list separator automatically becomes ;.
        LocalizedPunctuatorDecimalSeparator = ChooseDecimalSeparator(preferredDecimalSeparator);
        LocalizedPunctuatorListSeparator = ChooseListSeparatorPunctuator(LocalizedPunctuatorDecimalSeparator);

        // The chaining operator has to be disambiguated accordingly.
        LocalizedPunctuatorChainingSeparator = ChooseChainingPunctuator(LocalizedPunctuatorListSeparator,
                LocalizedPunctuatorDecimalSeparator);

        _punctuatorsAndInvariants = new HashMap<String, String>();
        _punctuatorsAndInvariants.put(LocalizedPunctuatorDecimalSeparator, ".");
        _punctuatorsAndInvariants.put(LocalizedPunctuatorListSeparator, ",");
        _punctuatorsAndInvariants.put(LocalizedPunctuatorChainingSeparator, ";");

        _numberFormatInfo = new NumberFormatInfo();
        _numberFormatInfo.NumberDecimalSeparator = LocalizedPunctuatorDecimalSeparator;
        _decimalSeparator = LocalizedPunctuatorDecimalSeparator.charAt(0);

        Map<String, TokKind> punctuators = new HashMap<String, TokKind>();

        // Invariant punctuators
        addPunctuator(punctuators, PunctuatorOr, TokKind.Or);
        addPunctuator(punctuators, PunctuatorAnd, TokKind.And);
        addPunctuator(punctuators, PunctuatorBang, TokKind.Bang);
        addPunctuator(punctuators, PunctuatorAdd, TokKind.Add);
        addPunctuator(punctuators, PunctuatorSub, TokKind.Sub);
        addPunctuator(punctuators, PunctuatorMul, TokKind.Mul);
        addPunctuator(punctuators, PunctuatorDiv, TokKind.Div);
        addPunctuator(punctuators, PunctuatorCaret, TokKind.Caret);
        addPunctuator(punctuators, PunctuatorParenOpen, TokKind.ParenOpen);
        addPunctuator(punctuators, PunctuatorParenClose, TokKind.ParenClose);
        addPunctuator(punctuators, PunctuatorEqual, TokKind.Equ);
        addPunctuator(punctuators, PunctuatorLess, TokKind.Lss);
        addPunctuator(punctuators, PunctuatorLessOrEqual, TokKind.LssEqu);
        addPunctuator(punctuators, PunctuatorGreater, TokKind.Grt);
        addPunctuator(punctuators, PunctuatorGreaterOrEqual, TokKind.GrtEqu);
        addPunctuator(punctuators, PunctuatorNotEqual, TokKind.LssGrt);
        addPunctuator(punctuators, PunctuatorDot, TokKind.Dot);
        addPunctuator(punctuators, PunctuatorColon, TokKind.Colon);
        addPunctuator(punctuators, PunctuatorCurlyOpen, TokKind.CurlyOpen);
        addPunctuator(punctuators, PunctuatorCurlyClose, TokKind.CurlyClose);
        addPunctuator(punctuators, PunctuatorBracketOpen, TokKind.BracketOpen);
        addPunctuator(punctuators, PunctuatorBracketClose, TokKind.BracketClose);
        addPunctuator(punctuators, PunctuatorAmpersand, TokKind.Ampersand);
        addPunctuator(punctuators, PunctuatorPercent, TokKind.PercentSign);
        addPunctuator(punctuators, PunctuatorAt, TokKind.At);
        addPunctuator(punctuators, PunctuatorDoubleBarrelArrow, TokKind.DoubleBarrelArrow);

        // Commenting punctuators
        addPunctuator(punctuators, PunctuatorBlockComment, TokKind.Comment);
        addPunctuator(punctuators, PunctuatorLineComment, TokKind.Comment);

        // Localized
        addPunctuator(punctuators, LocalizedPunctuatorListSeparator, TokKind.Comma);
        addPunctuator(punctuators, LocalizedPunctuatorChainingSeparator, TokKind.Semicolon);

        _punctuators = punctuators;
    }

    private static boolean addPunctuator(Map<String, TokKind> punctuators, String str, TokKind tid) {
        assert str != null && !str.isEmpty();

        TokKind tidCur = punctuators.getOrDefault(str, TokKind.None);
        if (tidCur == tid) {
            return true;
        }
        if (tidCur != TokKind.None) {
            return false;
        }

        for (int ich = 1; ich < str.length(); ich++) {
            String strTmp = str.substring(0, ich);
            if (!punctuators.containsKey(strTmp)) {
                punctuators.put(strTmp, TokKind.None);
            }
        }

        punctuators.put(str, tid);
        return true;
    }

    // private Token LexInterpolatedStringBody() {
    // _sb.setLength(0);

    // do {
    // char ch = CurrentChar;

    // if (IsStringDelimiter(ch)) {
    // char nextCh;
    // if (Eof || CharacterUtils.IsLineTerm(nextCh = PeekChar(1)) ||
    // !IsStringDelimiter(nextCh)) {
    // // Interpolated string end, do not call NextChar()
    // if (Eof) {
    // return new ErrorToken(GetTextSpan());
    // }

    // return new StrLitToken(_sb.toString(), GetTextSpan());
    // }

    // // If we are here, we are seeing a double quote followed immediately by
    // another
    // // double quote. That is an escape sequence for double quote characters.
    // _sb.append(ch);
    // NextChar();
    // } else if (IsCurlyOpen(ch)) {
    // char nextCh;
    // if (Eof || CharacterUtils.IsLineTerm(nextCh = PeekChar(1)) ||
    // !IsCurlyOpen(nextCh)) {
    // // Island start, do not call NextChar()
    // return new IslandStartToken(GetTextSpan());
    // }

    // _sb.append(ch);
    // NextChar();
    // } else if (IsCurlyClose(ch)) {
    // char nextCh;
    // if (Eof || CharacterUtils.IsLineTerm(nextCh = PeekChar(1)) ||
    // !IsCurlyClose(nextCh)) {
    // // If we are here, we are seeing a close curly followed immediately by
    // another
    // // close curly. That is an escape sequence for close curly characters.
    // _sb.append(ch);
    // NextChar();
    // } else {
    // // Interpolated expression start, do not call NextChar()
    // return new InterpolatedExprStartToken(GetTextSpan());
    // }
    // } else if (!CharacterUtils.IsFormatCh(ch)) {
    // _sb.append(ch);
    // }

    // NextChar();
    // } while (!Eof);

    // return new ErrorToken(GetTextSpan());
    // }
}

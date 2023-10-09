package jonshu.Syntax;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Span in the text formula.
 */
public final class Span {
    /**
     * Start index of this span.
     */
    public final int min;

    /**
     * End index of this span.
     */
    public final int lim;

    public Span(int min, int lim) {
        if (min < 0) {
            throw new IllegalArgumentException("min");
        }
        if (lim < min) {
            throw new IllegalArgumentException("lim");
        }

        this.min = min;
        this.lim = lim;
    }

    public Span(Span span) {
        if (span.min < 0) {
            throw new IllegalArgumentException("min");
        }
        if (span.lim < span.min) {
            throw new IllegalArgumentException("lim");
        }

        this.min = span.min;
        this.lim = span.lim;
    }

    /**
     * Get fragment of the text denoted by this span.
     *
     * @param script the text to extract the fragment from
     * @return the fragment of the text denoted by this span
     */
    public String getFragment(String script) {
        if (script == null) {
            throw new IllegalArgumentException("script");
        }
        if (lim > script.length()) {
            throw new IllegalArgumentException("lim");
        }

        return script.substring(min, lim);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("(%d,%d)", min, lim);
    }

    boolean startsWith(String script, String match) {
        if (script == null) {
            throw new IllegalArgumentException("script");
        }
        if (min > script.length()) {
            throw new IllegalArgumentException("min");
        }
        if (match == null) {
            throw new IllegalArgumentException("match");
        }

        return min + match.length() <= script.length() && script.substring(min, min + match.length()).equals(match);
    }

    static String replaceSpans(String script, List<Map.Entry<Span, String>> worklist) {
        if (script == null) {
            throw new IllegalArgumentException("script");
        }
        if (worklist == null) {
            throw new IllegalArgumentException("worklist");
        }

        StringBuilder sb = new StringBuilder(script.length());
        int index = 0;

        for (Map.Entry<Span, String> pair : worklist.stream().sorted(Comparator.comparingInt(kvp -> kvp.getKey().min)).collect(Collectors.toList())) {
            sb.append(script, index, pair.getKey().min);
            sb.append(pair.getValue());
            index = pair.getKey().lim;
        }

        if (index < script.length()) {
            sb.append(script, index, script.length());
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Span)) {
            return false;
        }

        Span span = (Span) obj;
        return min == span.min && lim == span.lim;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + min;
        result = 31 * result + lim;
        return result;
    }
}
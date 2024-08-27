package org.dice_research.java.stream;

public class PredicateHelper {

    /**
     * This method returns true if the given String starts with any of the given
     * prefixes.
     * 
     * @param s        the string that should be checked
     * @param prefixes the array of prefixes
     * @return {@code true} if s.startsWith(prefix) is true for any of the given
     *         prefixes
     */
    public static boolean startsWithAny(String s, String... prefixes) {
        for (int i = 0; i < prefixes.length; ++i) {
            if (s.startsWith(prefixes[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns {@code true} if the given String contains any of the
     * given patterns.
     * 
     * @param s        the string that should be checked
     * @param patterns the array of patterns
     * @return {@code true} if s.contains(pattern) is {@code true} for any of the
     *         given patterns
     */
    public static boolean containsAny(String s, String... patterns) {
        for (int i = 0; i < patterns.length; ++i) {
            if (s.contains(patterns[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns {@code true} if the given String contains all of the
     * given patterns.
     * 
     * @param s        the string that should be checked
     * @param patterns the array of patterns
     * @return {@code true} if s.contains(pattern) is {@code true} for all of the
     *         given patterns
     */
    public static boolean containsAll(String s, String... patterns) {
        for (int i = 0; i < patterns.length; ++i) {
            if (!s.contains(patterns[i])) {
                return false;
            }
        }
        return true;
    }
}

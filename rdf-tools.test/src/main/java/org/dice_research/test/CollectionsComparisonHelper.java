package org.dice_research.test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.function.IntFunction;

import org.junit.Assert;

/**
 * A simple helper class which eases the comparison of collections in JUnit
 * tests.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class CollectionsComparisonHelper {

    /**
     * Compares the given sets by transforming them into arrays, sorting the arrays
     * and comparing them using
     * {@link Assert#assertArrayEquals(Object[], Object[])}.
     * 
     * @param <T>        class of the elements within the given sets
     * @param expected   the expected set
     * @param result     the set that should be compared to the expected set
     * @param clazz      the class T which will be used to generate T[] arrays
     * @param comparator a comparator for comparing the T objects while sorting
     */
    public static <T extends Comparable<T>> void assertSetsEqual(Set<T> expected, Set<T> result, Class<T> clazz) {
        assertSetsEqual(expected, result, clazz, null);
    }

    /**
     * Compares the given sets by transforming them into arrays, sorting the arrays
     * and comparing them using
     * {@link Assert#assertArrayEquals(Object[], Object[])}.
     * 
     * @param <T>            class of the elements within the given sets
     * @param expected       the expected set
     * @param result         the set that should be compared to the expected set
     * @param arrayGenerator a generator of T[] arrays
     * @param comparator     a comparator for comparing the T objects while sorting
     */
    public static <T extends Comparable<T>> void assertSetsEqual(Set<T> expected, Set<T> result,
            IntFunction<T[]> arrayGenerator) {
        assertSetsEqual(expected, result, arrayGenerator, null);
    }

    /**
     * Compares the given sets by transforming them into arrays, sorting the arrays
     * and comparing them using
     * {@link Assert#assertArrayEquals(Object[], Object[])}.
     * 
     * @param <T>        class of the elements within the given sets
     * @param expected   the expected set
     * @param result     the set that should be compared to the expected set
     * @param clazz      the class T which will be used to generate T[] arrays
     * @param comparator a comparator for comparing the T objects while sorting
     */
    public static <T> void assertSetsEqual(Set<T> expected, Set<T> result, Class<T> clazz, Comparator<T> comparator) {
        @SuppressWarnings("unchecked")
        IntFunction<T[]> arrayGenerator = (int i) -> (T[]) Array.newInstance(clazz, i);
        assertSetsEqual(expected, result, arrayGenerator, comparator);
    }

    /**
     * Compares the given sets by transforming them into arrays, sorting the arrays
     * and comparing them using
     * {@link Assert#assertArrayEquals(Object[], Object[])}.
     * 
     * @param <T>            class of the elements within the given sets
     * @param expected       the expected set
     * @param result         the set that should be compared to the expected set
     * @param arrayGenerator a generator of T[] arrays
     * @param comparator     a comparator for comparing the T objects while sorting
     */
    public static <T> void assertSetsEqual(Set<T> expected, Set<T> result, IntFunction<T[]> arrayGenerator,
            Comparator<T> comparator) {
        T expectedArray[] = expected.toArray(arrayGenerator);
        T resultArray[] = result.toArray(arrayGenerator);
        if (comparator != null) {
            Arrays.sort(expectedArray, comparator);
            Arrays.sort(resultArray, comparator);
        } else {
            Arrays.sort(expectedArray);
            Arrays.sort(resultArray);
        }
        Assert.assertArrayEquals(expectedArray, resultArray);
    }
}

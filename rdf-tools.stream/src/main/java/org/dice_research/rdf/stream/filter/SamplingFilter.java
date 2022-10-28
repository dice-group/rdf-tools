package org.dice_research.rdf.stream.filter;

import java.util.Random;
import java.util.function.Predicate;

/**
 * A Predicate implementation that returns true or false based on the given
 * probability. This can be used to sample elements from a stream. In a larger
 * stream, the elements that receive a {@code true} should be close to the
 * overall number of elements in the stream multiplied with the given
 * {@code probability}. Hence, the number of elements that receive a
 * {@code false} should be close to the overall number of elements in the stream
 * multiplied with {@code (1 - probability)}. <b>Note</b> that the probability
 * has to be in the range [0,1].
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 * @param <T>
 */
public class SamplingFilter<T> implements Predicate<T> {

    /**
     * The probability that a triple that has one of the given properties as
     * predicate is chosen.
     */
    protected double probability;
    /**
     * Maximum number of chosen triples (optional).
     */
    protected int maxNumberOfChosen;
    /**
     * Random number generator used to sample triples.
     */
    protected Random rng;
    /**
     * Number of chosen triples.
     */
    protected long numberOfChoseTriples = 0;

    /**
     * Constructor. {@link System#currentTimeMillis()} will be used as seed for the
     * random number generator.
     * 
     * @param probability The probability that a triple that has one of the given
     *                    properties as predicate is chosen.
     */
    public SamplingFilter(double probability) {
        this(probability, System.currentTimeMillis());
    }

    /**
     * Constructor.
     * 
     * @param probability       The probability that a triple that has one of the
     *                          given properties as predicate is chosen.
     * @param maxNumberOfChosen Maximum number of chosen triples.
     * @param seed              The seed for the random number generator used for
     *                          sampling.
     */
    public SamplingFilter(double probability, int maxNumberOfChosen, long seed) {
        if (probability < 0) {
            throw new IllegalArgumentException(
                    "The given probability (" + probability + ") is smaller than 0. This is not allowed.");
        }
        if (probability > 1.0) {
            throw new IllegalArgumentException(
                    "The given probability (" + probability + ") is larger than 1.0. This is not allowed.");
        }
        this.probability = probability;
        rng = new Random(seed);
        this.maxNumberOfChosen = maxNumberOfChosen;
    }

    /**
     * Constructor.
     * 
     * @param probability The probability that a triple that has one of the given
     *                    properties as predicate is chosen.
     * @param seed        The seed for the random number generator used for
     *                    sampling.
     */
    public SamplingFilter(double probability, long seed) {
        if (probability < 0) {
            throw new IllegalArgumentException(
                    "The given probability (" + probability + ") is smaller than 0. This is not allowed.");
        }
        if (probability > 1.0) {
            throw new IllegalArgumentException(
                    "The given probability (" + probability + ") is larger than 1.0. This is not allowed.");
        }
        this.probability = probability;
        rng = new Random(seed);
    }

    @Override
    public boolean test(T t) {
        if (rng.nextDouble() < probability) {
            if (maxNumberOfChosen <= 0) {
                return true;
            }
            // We have to check how many triples have been chosen
            if (numberOfChoseTriples < maxNumberOfChosen) {
                // We have to synchronize and check it again
                // (in case the class is used in parallel)
                synchronized (this) {
                    if (numberOfChoseTriples < maxNumberOfChosen) {
                        ++numberOfChoseTriples;
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

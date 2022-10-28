package org.dice_research.java.stream;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Combines a {@link Predicate} and a {@link Consumer}. In this way, data can be
 * "pushed" to a consumer by filtering it with the given predicate.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 * @param <T> The type that the given {@link Predicate} and {@link Consumer} can handle. 
 */
public class PredicateConsumerCombiner<T> implements Consumer<T> {

    private Predicate<T> predicate;
    private Consumer<T> consumer;

    public PredicateConsumerCombiner(Predicate<T> predicate, Consumer<T> consumer) {
        super();
        this.predicate = predicate;
        this.consumer = consumer;
    }

    @Override
    public void accept(T data) {
        if (predicate.test(data)) {
            consumer.accept(data);
        }
    }

    /**
     * @return the predicate
     */
    public Predicate<T> getPredicate() {
        return predicate;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the consumer
     */
    public Consumer<T> getConsumer() {
        return consumer;
    }

    /**
     * @param consumer the consumer to set
     */
    public void setConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
    }

}

package org.dice_research.java.stream;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Combines a {@link Function} and a {@link Consumer}. In this way, data can be
 * "pushed" through the function into the consumer. For each
 * {@link #accept(Object)} call, the given object is first given to the
 * {@link #function}. The result of the function is given to the
 * {@link #consumer}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 * @param <S> The input type of the {@link Function}.
 * @param <T> The result type of the {@link Function} and the input type of the {@link Consumer}.
 */
public class FunctionConsumerCombiner<S, T> implements Consumer<S> {

    private Function<S, T> function;
    private Consumer<T> consumer;

    public FunctionConsumerCombiner(Function<S, T> function, Consumer<T> consumer) {
        super();
        this.function = function;
        this.consumer = consumer;
    }

    @Override
    public void accept(S data) {
        consumer.accept(function.apply(data));
    }

    /**
     * @return the function
     */
    public Function<S, T> getFunction() {
        return function;
    }

    /**
     * @param function the function to set
     */
    public void setFunction(Function<S, T> function) {
        this.function = function;
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

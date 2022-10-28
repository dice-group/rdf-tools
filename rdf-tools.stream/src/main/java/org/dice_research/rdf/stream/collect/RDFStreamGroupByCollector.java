package org.dice_research.rdf.stream.collect;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFBase;

/**
 * This class collects elements from the given RDF stream by grouping them based
 * on a key. The values that share the same key are stored in a
 * {@link Collection} which is created with the given {@link Supplier}.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 * @param <K> the class of the key
 * @param <V> the class of the value
 * @param <C> the class of the collection that is used to store values that
 *            share the same key
 */
public class RDFStreamGroupByCollector<K, V, C extends Collection<V>> extends StreamRDFBase {

    /**
     * The function which is used to get the key from the given triple.
     */
    protected Function<Triple, K> keyFunction;
    /**
     * The function which is used to get the value from the given triple.
     */
    protected Function<Triple, V> valueFunction;
    /**
     * The supplier which is used to generate new {@link Collection} instances.
     */
    protected Supplier<C> collectionSupplier;
    /**
     * The map that is used to store the grouped data.
     */
    protected Map<K, C> collectedData;

    /**
     * Constructor. The class uses a {@link HashMap} to collect the data.
     * 
     * @param keyFunction        the function which is used to get the key from the
     *                           given triple.
     * @param valueFunction      the function which is used to get the value from
     *                           the given triple.
     * @param collectionSupplier the supplier which is used to generate new
     *                           {@link Collection} instances.
     */
    public RDFStreamGroupByCollector(Function<Triple, K> keyFunction, Function<Triple, V> valueFunction,
            Supplier<C> collectionSupplier) {
        this(keyFunction, valueFunction, collectionSupplier, new HashMap<>());
    }

    /**
     * Constructor.
     * 
     * @param keyFunction        the function which is used to get the key from the
     *                           given triple.
     * @param valueFunction      the function which is used to get the value from
     *                           the given triple.
     * @param collectionSupplier the supplier which is used to generate new
     *                           {@link Collection} instances.
     * @param collectedData      the map that is used to store the grouped data.
     */
    public RDFStreamGroupByCollector(Function<Triple, K> keyFunction, Function<Triple, V> valueFunction,
            Supplier<C> collectionSupplier, Map<K, C> collectedData) {
        super();
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction;
        this.collectionSupplier = collectionSupplier;
        this.collectedData = collectedData;
    }

    @Override
    public void triple(Triple triple) {
        K key = keyFunction.apply(triple);
        V value = valueFunction.apply(triple);
        C collection;
        if (collectedData.containsKey(key)) {
            collection = collectedData.get(key);
        } else {
            collection = collectionSupplier.get();
            collectedData.put(key, collection);
        }
        collection.add(value);
    }

    /**
     * @return the keyFunction
     */
    public Function<Triple, K> getKeyFunction() {
        return keyFunction;
    }

    /**
     * @param keyFunction the keyFunction to set
     */
    public void setKeyFunction(Function<Triple, K> keyFunction) {
        this.keyFunction = keyFunction;
    }

    /**
     * @return the valueFunction
     */
    public Function<Triple, V> getValueFunction() {
        return valueFunction;
    }

    /**
     * @param valueFunction the valueFunction to set
     */
    public void setValueFunction(Function<Triple, V> valueFunction) {
        this.valueFunction = valueFunction;
    }

    /**
     * @return the collectionSupplier
     */
    public Supplier<C> getCollectionSupplier() {
        return collectionSupplier;
    }

    /**
     * @param collectionSupplier the collectionSupplier to set
     */
    public void setCollectionSupplier(Supplier<C> collectionSupplier) {
        this.collectionSupplier = collectionSupplier;
    }

    /**
     * @return the collected data
     */
    public Map<K, C> getCollectedData() {
        return collectedData;
    }

    /**
     * @param collectedData the collectedData to set
     */
    public void setCollectedData(Map<K, C> collectedData) {
        this.collectedData = collectedData;
    }
}

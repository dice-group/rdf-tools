package org.dice_research.rdf.stream.collect;

import java.util.Collection;
import java.util.function.Function;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFBase;

/**
 * This class collects elements from the given RDF stream and stores them in a
 * given collection. It uses the given {@link #transformFunction} to transform a
 * given triple into the value that should be stored.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 * @param <T> the class of the value that should be stored in the collection
 */
public class RDFStreamCollector<T> extends StreamRDFBase {

    /**
     * The function which is used to transform a triple into the collected data.
     */
    protected Function<Triple, T> transformFunction;
    /**
     * The collection that is used to store the collected data.
     */
    protected Collection<T> collectedData;

    /**
     * Constructor.
     * 
     * @param transformFunction The function which is used to transform a triple
     *                          into the collected data.
     * @param collectedData     The collection that is used to store the collected
     *                          data.
     */
    public RDFStreamCollector(Function<Triple, T> transformFunction, Collection<T> collectedData) {
        super();
        this.transformFunction = transformFunction;
        this.collectedData = collectedData;
    }

    @Override
    public void triple(Triple triple) {
        collectedData.add(transformFunction.apply(triple));
    }

    /**
     * @return the transformFunction
     */
    public Function<Triple, T> getTransformFunction() {
        return transformFunction;
    }

    /**
     * @param transformFunction the transformFunction to set
     */
    public void setTransformFunction(Function<Triple, T> transformFunction) {
        this.transformFunction = transformFunction;
    }

    /**
     * @return the collectedData
     */
    public Collection<T> getCollectedData() {
        return collectedData;
    }

    /**
     * @param collectedData the collectedData to set
     */
    public void setCollectedData(Collection<T> collectedData) {
        this.collectedData = collectedData;
    }

}

package org.dice_research.rdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * This class comprises utility methods to change an existing model.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class ModelHelper {

    /**
     * Replace the given old resource with the given new resource in the subject and
     * object position in the given model.
     * 
     * @param model       the model that should be updated
     * @param oldResource the old resource that should be replaced
     * @param newResource the new resource that replaces the old resource
     */
    public static void replaceResource(Model model, Resource oldResource, Resource newResource) {
        replaceSubject(model, oldResource, newResource);
        replaceObject(model, oldResource, newResource);
    }

    /**
     * Replace the given old resource with the given new resource in the subject
     * position in the given model.
     * 
     * @param model       the model that should be updated
     * @param oldResource the old resource that should be replaced
     * @param newResource the new resource that replaces the old resource
     */
    public static void replaceSubject(Model model, Resource oldResource, Resource newResource) {
        // Iterate over all triples that need a new subject
        List<Statement> toRemove = new ArrayList<>();
        Statement oldStatement;
        StmtIterator stmtIter = model.listStatements(oldResource, null, (RDFNode) null);
        while (stmtIter.hasNext()) {
            oldStatement = stmtIter.next();
            // Add new triple
            model.add(newResource, oldStatement.getPredicate(), oldStatement.getObject());
            // Add old triple to the list of triples that need to be removed
            toRemove.add(oldStatement);
        }
        // Remove the old statements
        model.remove(toRemove);
    }

    /**
     * Replace the given old object with the given new object in the object
     * positions in the given model.
     * 
     * @param model     the model that should be updated
     * @param oldObject the old object that should be replaced
     * @param newObject the new object that replaces the old object
     */
    public static void replaceObject(Model model, RDFNode oldObject, RDFNode newObject) {
        // Iterate over all triples that need a new subject
        List<Statement> toRemove = new ArrayList<>();
        Statement oldStatement;
        StmtIterator stmtIter = model.listStatements(null, null, oldObject);
        while (stmtIter.hasNext()) {
            oldStatement = stmtIter.next();
            // Add new triple
            model.add(oldStatement.getSubject(), oldStatement.getPredicate(), newObject);
            // Add old triple to the list of triples that need to be removed
            toRemove.add(oldStatement);
        }
        // Remove the old statements
        model.remove(toRemove);
    }
}

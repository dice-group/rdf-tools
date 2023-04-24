package org.dice_research.test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;

/**
 * This is a simple utilities class that should ease the usage of maven
 * resources for JUnit tests.
 * 
 * @author Michael R&ouml;der (michael.roeder@uni-paderborn.de)
 *
 */
public class FileResourceUtils {

    /**
     * This method will try to load the file as resource using the
     * {@link ClassLoader} of the given class.
     * 
     * @param resourceName the name of the resource that should be loaded
     * @param clazz        the class that will be used to access the
     *                     {@link ClassLoader} which is used to get the resource's
     *                     URL
     * @return the resource location as {@link File} object
     * @throws IllegalArgumentException if the resource with the given name cannot
     *                                  be found
     */
    public static File getFile(String resourceName, Class<?> clazz) {
        return getFile(resourceName, clazz.getClassLoader());
    }

    /**
     * This method will try to load the file as resource using the given
     * {@link ClassLoader}.
     * 
     * @param resourceName the name of the resource that should be loaded
     * @param loader       the {@link ClassLoader} which is used to get the
     *                     resource's URL
     * @return the resource location as {@link File} object
     * @throws IllegalArgumentException if the resource with the given name cannot
     *                                  be found
     */
    public static File getFile(String resourceName, ClassLoader loader) {
        URL url = loader.getResource(resourceName);
        Assert.assertNotNull(url);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Got an unexpected error while processing the given resource name.", e);
        }
    }
}

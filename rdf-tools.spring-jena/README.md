# rdf-tools.jena2spring

This library creates a bridge between the Jena and Spring libraries.

The current implementation supports the serialization and de-serialization of instances of classes that support the Model interface.

## Usage in a project

1. Include the library, e.g., within your Maven pom file.
```xml
    <repositories>
        <!-- AKSW repository for rdf-tool libs -->
        <repository>
            <id>maven.aksw.internal</id>
            <name>University Leipzig, AKSW Maven2 Repository</name>
            <url>https://maven.aksw.org/repository/internal</url>
        </repository>
        <repository>
            <id>maven.aksw.snapshots</id>
            <name>University Leipzig, AKSW Maven2 Repository</name>
            <url>https://maven.aksw.org/repository/snapshots</url>
        </repository>
    </repositories>
    
    <dependencies>
        <!-- Spring Jena -->
        <dependency>
            <groupId>org.dice-research</groupId>
            <artifactId>rdf-tools.spring-jena</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
2. Include the creation of `HttpMessageConverter` instances as beans. For Spring Boot, this can be done as follows (the example creates two converters: one for `text/` and one for `application/` MIME types that use Turtle and JSON-LD, respectively):
```java
@Configuration
public class MyConfiguration {

    @Bean
    public HttpMessageConverter<?> getTextConverters() {
        return JenaModelHttpMessageConverter.createForTextMediaTypes(Lang.TURTLE);
    }

    @Bean
    public HttpMessageConverter<?> getApplicationConverters() {
        return JenaModelHttpMessageConverter.createForApplicationMediaTypes(Lang.JSONLD);
    }
}
```
3. Annotate your web methods to consume and/or produce the MIME types that you would like to support:
```java
@PostMapping(value = "/my-method", consumes = { "text/turtle" }, produces = { "text/turtle" })
    public ResponseEntity<Model> myMethod(@RequestBody Model request) {
        Model result = null;
        // Create a result model based on the data of the request
        return result;
    }
```
You can even use everything, that Jena supports:
```java
    @PostMapping(value = "/my-method", consumes = { WebContent.contentTypeJSONLD, WebContent.contentTypeTurtle,
            WebContent.contentTypeTurtleAlt1, WebContent.contentTypeRDFXML, WebContent.contentTypeRDFJSON,
            WebContent.contentTypeTextPlain, WebContent.contentTypeNTriples, WebContent.contentTypeNTriplesAlt,
            WebContent.contentTypeXML, WebContent.contentTypeXMLAlt, WebContent.contentTypeTriG,
            WebContent.contentTypeNQuads, WebContent.contentTypeTriGAlt1, WebContent.contentTypeRDFProto,
            WebContent.contentTypeRDFThrift, WebContent.contentTypeNQuadsAlt1, WebContent.contentTypeTriX,
            WebContent.contentTypeTriXxml, WebContent.contentTypeN3, WebContent.contentTypeN3Alt1,
            WebContent.contentTypeN3Alt2 }, produces = { WebContent.contentTypeJSONLD, WebContent.contentTypeTurtle,
                    WebContent.contentTypeTurtleAlt1, WebContent.contentTypeRDFXML, WebContent.contentTypeRDFJSON,
                    WebContent.contentTypeTextPlain, WebContent.contentTypeNTriples, WebContent.contentTypeNTriplesAlt,
                    WebContent.contentTypeXML, WebContent.contentTypeXMLAlt, WebContent.contentTypeTriG,
                    WebContent.contentTypeNQuads, WebContent.contentTypeTriGAlt1, WebContent.contentTypeRDFProto,
                    WebContent.contentTypeRDFThrift, WebContent.contentTypeNQuadsAlt1, WebContent.contentTypeTriX,
                    WebContent.contentTypeTriXxml, WebContent.contentTypeN3, WebContent.contentTypeN3Alt1,
                    WebContent.contentTypeN3Alt2 })
```

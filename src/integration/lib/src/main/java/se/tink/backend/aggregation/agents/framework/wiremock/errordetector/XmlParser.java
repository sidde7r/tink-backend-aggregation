package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

public class XmlParser {

    public String normalizeXmlData(String rawXmlData) {
        try {
            ByteArrayInputStream input =
                    new ByteArrayInputStream(rawXmlData.getBytes(StandardCharsets.UTF_8));
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance(
                            "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                            null);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(input);
            TransformerFactory transformerFactory =
                    TransformerFactory.newInstance(
                            "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                            null);
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not normalize XML data", e);
        }
    }
}

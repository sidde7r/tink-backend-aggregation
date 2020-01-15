package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Slf4j
public class OtmlParser {

    private static final String ATTRIBUTE_VAL = "val";

    private static final String XPATH_PIN_KEYBOARD_IMAGES =
            "/datasources/datasource[@key='response']/element[@key='images']/element";
    private static final String XPATH_PIN_POSITIONS =
            "/datasources/datasource[@key='response']/element[@key='positions']/element";

    private NodeList evaluateXPathExpresion(String document, String expression) {
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression xpathExpression = xPath.compile(expression);
            return (NodeList)
                    xpathExpression.evaluate(parseDocument(document), XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            log.error("There was an error while processing xPath expression.");
            throw new IllegalArgumentException("OTML Parsing Error", e);
        }
    }

    private Document parseDocument(String document) {
        try (InputStream inputStream =
                new ByteArrayInputStream(document.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            return documentBuilder.parse(inputStream);
        } catch (SAXException | IOException e) {
            log.error("There was an error while parsing OTML document.");
            throw new IllegalArgumentException("OTML Parsing Error", e);
        }
    }

    private DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setNamespaceAware(false);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            log.error("There was an error while instancing document builder.");
            throw new IllegalArgumentException("OTML Parsing Error", e);
        }
    }

    List<String> getPinKeyboardImages(String document) {
        NodeList xpathNodeList = evaluateXPathExpresion(document, XPATH_PIN_KEYBOARD_IMAGES);
        return IntStream.range(0, xpathNodeList.getLength())
                .mapToObj(xpathNodeList::item)
                .map(Node::getAttributes)
                .map(attr -> attr.getNamedItem(ATTRIBUTE_VAL))
                .map(Node::getNodeValue)
                .collect(Collectors.toList());
    }

    List<Integer> getPinPositions(String document) {
        NodeList xpathNodeList = evaluateXPathExpresion(document, XPATH_PIN_POSITIONS);
        return IntStream.range(0, xpathNodeList.getLength())
                .mapToObj(xpathNodeList::item)
                .map(Node::getAttributes)
                .map(attr -> attr.getNamedItem(ATTRIBUTE_VAL))
                .map(Node::getNodeValue)
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}

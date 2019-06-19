package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.FormValues;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.amount.Amount;

public class HtmlResponse {
    protected final String body;
    protected final Document document;
    private static final XPathFactory xpathFactory = XPathFactory.newInstance();
    private static final NumberFormat amountFormat =
            DecimalFormat.getInstance(Locale.forLanguageTag("es"));

    protected static Document parseHTML(String body) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setCoalescing(false);
        factory.setXIncludeAware(false);

        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature(
                    "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            final InputStream inputStream = IOUtils.toInputStream(body, StandardCharsets.UTF_8);
            return factory.newDocumentBuilder().parse(inputStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    public HtmlResponse(HttpResponse response) {
        this.body = response.getBody(String.class);
        this.document = parseHTML(this.body);
    }

    protected Amount parseAmount(String amountString) {
        // amount uses "." as thousands separator and "," as decimal separator
        if (!amountString.endsWith("€")) {
            throw new IllegalStateException("Unknown account currency for " + amountString);
        }

        try {
            if (amountString.startsWith("+")) {
                return Amount.inEUR(amountFormat.parse(amountString.substring(1)));
            } else {
                return Amount.inEUR(amountFormat.parse(amountString));
            }
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    private static QName returnTypeForClass(Class cls) {
        if (cls == String.class) {
            return XPathConstants.STRING;
        } else if (cls == Node.class || cls == Element.class) {
            return XPathConstants.NODE;
        } else if (cls == NodeList.class) {
            return XPathConstants.NODESET;
        } else {
            throw new NotImplementedException("No return type for " + cls.getCanonicalName());
        }
    }

    protected static <T> T evaluateXPath(Node node, String xPathExpression, Class<T> returnClass) {
        final XPath xPath = xpathFactory.newXPath();
        final XPathExpression expr;
        try {
            expr = xPath.compile(xPathExpression);
            return (T) expr.evaluate(node, returnTypeForClass(returnClass));
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }
    }

    protected <T> T evaluateXPath(String xPathExpression, Class<T> returnClass) {
        return evaluateXPath(this.document, xPathExpression, returnClass);
    }

    protected @Nullable Element getElementById(String id) {
        // because we're parsing without a DTD, document.getElementById doesn't work
        return (Element) evaluateXPath(String.format("//*[@id = '%s']", id), Node.class);
    }

    public String getViewState(String formId) {
        final String expr =
                String.format("//form[@id='%s']//input[@name='javax.faces.ViewState']", formId);
        return evaluateXPath(expr, Node.class).getAttributes().getNamedItem("value").getNodeValue();
    }

    // find the javascript for calling JSF to update
    public Form getJsfUpdateForm(String formId, String update) {
        // find submit link
        final String xPathExpression =
                String.format(
                        "//a[contains(@onclick,\"formId:'%1$s'\") and contains(@onclick,\"update:'%2$s'\")]",
                        formId, update);
        final NodeList links = evaluateXPath(xPathExpression, NodeList.class);
        if (links.getLength() != 1) {
            throw new IllegalStateException("Found too many forms");
        }
        final Node linkNode = links.item(0);
        final String jsfSource = linkNode.getAttributes().getNamedItem("id").getNodeValue();

        // build form
        final Element formElement = getElementById(formId);
        final NodeList formInputs = formElement.getElementsByTagName("input");
        final Form.Builder formBuilder = Form.builder();

        // add viewState and SUBMIT elements
        for (int i = 0; i < formInputs.getLength(); i++) {
            final NamedNodeMap inputAttributes = formInputs.item(i).getAttributes();
            final String key = inputAttributes.getNamedItem("name").getNodeValue();
            final String value = inputAttributes.getNamedItem("value").getNodeValue();
            formBuilder.put(key, value);
        }

        formBuilder.put(FormKeys.JSF_PARTIAL_AJAX, FormValues.TRUE);
        formBuilder.put(FormKeys.JSF_SOURCE, jsfSource);
        formBuilder.put(FormKeys.JSF_PARTIAL_EXECUTE, FormValues.JSF_EXECUTE_ALL);
        formBuilder.put(FormKeys.JSF_PARTIAL_RENDER, update);
        formBuilder.put(jsfSource, jsfSource);

        return formBuilder.build();
    }
}

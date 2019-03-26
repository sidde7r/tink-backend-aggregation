package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaTestData;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.OtmlResponse;

public class OtmlParsingTest {

    @Test
    public void shouldBeAbleToParseGenericOtml() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OtmlResponse otml =
                objectMapper.readValue(
                        BankAustriaTestData.OTML_ERROR_LOGIN_WRONG_FORMAT, OtmlResponse.class);
        Assert.assertEquals(
                "The field contains non numeric characters", otml.getParams().getUserIdError());
    }

    @Test
    public void shouldBeAbleToParseFailedLogin() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OtmlResponse otml =
                objectMapper.readValue(
                        BankAustriaTestData.OTML_ERROR_LOGIN_WRONG_CREDENTIALS, OtmlResponse.class);
        Assert.assertNull(otml.getParams().getUserIdError());
    }

    @Test
    public void shouldBeAbleToParseSuccessfulLogin() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        OtmlResponse otml =
                objectMapper.readValue(
                        BankAustriaTestData.OTML_SUCCESSFUL_LOGIN, OtmlResponse.class);
        Assert.assertNull(otml.getParams().getUserIdError());
        Assert.assertNotNull(otml.getXml());
    }

    @Test
    public void shouldParseAccountsFromSettings()
            throws ParserConfigurationException, XPathExpressionException, IOException,
                    SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document doc =
                documentBuilder.parse(
                        new ByteArrayInputStream(
                                BankAustriaTestData.SETTINGS_DATA_SOURCES.getBytes("UTF-8")));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expression =
                xpath.compile(
                        "/datasources/datasource[@key='response']/element[@key='customizedAccountMetaModelsList']/element");
        NodeList xpathNodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        XPathExpression accountNumberExtractor =
                xpath.compile(
                        "/datasources/datasource[@key='response']/element[@key='customizedAccountMetaModelsList']/element/element[@key='accountNumber']");
        Node accountNumber =
                (Node) accountNumberExtractor.evaluate(xpathNodeList.item(0), XPathConstants.NODE);
        Assert.assertEquals(1, xpathNodeList.getLength());
    }

    @Test
    public void shouldParseAccountsFromAssumedSettings()
            throws ParserConfigurationException, XPathExpressionException, IOException,
                    SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document doc =
                documentBuilder.parse(
                        new ByteArrayInputStream(
                                BankAustriaTestData.SETTINGS_ASSUMED_DATA_SOURCES.getBytes(
                                        "UTF-8")));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expression =
                xpath.compile(
                        "/datasources/datasource[@key='response']/element[@key='customizedAccountMetaModelsList']/element");
        NodeList xpathNodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        XPathExpression accountNumberExtractor = xpath.compile(".//element[@key='accountNumber']");
        Node accountNumber1 =
                (Node) accountNumberExtractor.evaluate(xpathNodeList.item(0), XPathConstants.NODE);
        Node accountNumber2 =
                (Node) accountNumberExtractor.evaluate(xpathNodeList.item(1), XPathConstants.NODE);
        Assert.assertEquals(2, xpathNodeList.getLength());
        Assert.assertEquals(
                BankAustriaTestData.RandomData.IBAN_1,
                accountNumber1.getAttributes().getNamedItem("val").getNodeValue());
        Assert.assertEquals(
                BankAustriaTestData.RandomData.IBAN_2,
                accountNumber2.getAttributes().getNamedItem("val").getNodeValue());
    }

    @Test
    public void shouldGetBalanceCurrencyFromGenericBalanceMovement()
            throws ParserConfigurationException, XPathExpressionException, IOException,
                    SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document doc =
                documentBuilder.parse(
                        new ByteArrayInputStream(
                                BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT.getBytes(
                                        "UTF-8")));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expression =
                xpath.compile(
                        "/datasources/datasource[@key='response']/element[@key='balance']/element[@key='accountable']/element[@key='currency']");
        NodeList xpathNodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        Node currencyNode = (Node) xpathNodeList.item(0);
        Assert.assertEquals("EUR", currencyNode.getAttributes().getNamedItem("val").getNodeValue());
    }

    @Test
    public void shouldGetTransactionsFromGenericBalanceMovement()
            throws ParserConfigurationException, XPathExpressionException, IOException,
                    SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();

        Document doc =
                documentBuilder.parse(
                        new ByteArrayInputStream(
                                BankAustriaTestData.BALANCE_MOVEMENTS_FOR_ACCOUNT.getBytes(
                                        "UTF-8")));
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expression =
                xpath.compile(
                        "/datasources/datasource[@key='response']/element[@key='movements']/element");
        NodeList movements = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
        Assert.assertEquals(3, movements.getLength());

        XPathExpression movementAmountDecimalPartExtractor =
                xpath.compile(".//element[@key='amount']/element[@key='decimal']");
        Node decimalPartOfAmount =
                (Node)
                        movementAmountDecimalPartExtractor.evaluate(
                                movements.item(0), XPathConstants.NODE);
        Assert.assertEquals(
                "68", decimalPartOfAmount.getAttributes().getNamedItem("val").getNodeValue());
    }
}

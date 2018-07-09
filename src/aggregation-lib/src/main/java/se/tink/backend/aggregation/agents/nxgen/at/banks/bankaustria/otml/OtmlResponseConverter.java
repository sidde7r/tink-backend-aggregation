package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml;

import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OtmlResponseConverter {
    private static final AggregationLogger LOGGER = new AggregationLogger(OtmlResponseConverter.class);

    private final DocumentBuilderFactory factory;
    private XPathFactory xPathfactory;

    public OtmlResponseConverter() {
        factory = DocumentBuilderFactory.newInstance();
        xPathfactory = XPathFactory.newInstance();
    }

    private Document parseDocument(String otmlDocument) {
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            return documentBuilder.parse(new ByteArrayInputStream(otmlDocument.getBytes("UTF-8")));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new IllegalStateException(e);
        }

    }

    public String getValue(Node node) {
        return node.getAttributes().getNamedItem("val").getNodeValue();
    }

    public Optional<Node> getResultNode(String xml) {
        try {
            Document document = parseDocument(xml);
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expression = xpath.compile("/datasources/datasource[@key='response']/element[@key='result']");
            NodeList xpathNodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            if (xpathNodeList.getLength() != 1) {
                return Optional.empty();
            }
            return Optional.of(xpathNodeList.item(0));
        } catch (XPathExpressionException e) {
            LOGGER.error("Xpath expression not valid, test in unittest");
        }
        return Optional.empty();
    }

    public Collection<TransactionalAccount> getAccountsFromSettings(String xml) {
        Document document = parseDocument(xml);
        NodeList nodeList = getNodeList(document, "/datasources/datasource[@key='response']/element[@key='customizedAccountMetaModelsList']/element");
        if (nodeList == null) {
            LOGGER.warn("No accounts found.");
            return Collections.EMPTY_LIST;
        }

        List<TransactionalAccount> transactionalAccounts =
                IntStream.range(0, nodeList.getLength())
                        .mapToObj(i -> getAccountFromSetting(nodeList.item(i)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return transactionalAccounts;
    }

    private TransactionalAccount getAccountFromSetting(Node accountNode) {
        String accountNumber = getValue(getNode(accountNode, ".//element[@key='accountNumber']"));
        String accountNickName = getValue(getNode(accountNode, ".//element[@key='accountNickname']"));
        String accountKey = getValue(getNode(accountNode, ".//element[@key='accountKey']"));
        String accountType = getValue(getNode(accountNode, ".//element[@key='accountType']"));
        if (accountType.equals("CURRENT"))
            return SavingsAccount.builder(accountNumber, Amount.inEUR(0D))
                    .setName(accountNickName)
                    .setBankIdentifier(accountKey)
                    .build();
        else {
            LOGGER.error(String.format("Unknown account type %s", accountType));
            return null;
        }
    }

    private Node getNode(Node node, String expression) {
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression accountNumberExtractor = xpath.compile(expression);
            return (Node) accountNumberExtractor.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            LOGGER.error("Xpath expression not valid, test in unittest");
        }
        return null;
    }

    private NodeList getNodeList(Document document, String expression) {
        XPath xpath = xPathfactory.newXPath();
        NodeList xpathNodeList = null;
        try {
            XPathExpression xpathExpression = xpath.compile(expression);
            xpathNodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOGGER.error("Xpath expression not valid, test in unittest");
        }
        return xpathNodeList;
    }

    public TransactionalAccount fillAccountInformation(String accountMovementXml, TransactionalAccount account)  {
        Document document = parseDocument(accountMovementXml);
        NodeList nodeList = getNodeList(document, "/datasources/datasource[@key='response']/element[@key='balance']/element[@key='accountable']");
        Node balanceNode = nodeList.item(0);
        String balanceCurrency = getValue(getNode(balanceNode, ".//element[@key='currency']"));
        String balanceValue = getValue(getNode(balanceNode, ".//element[@key='value']"));
        Amount amount = new Amount(balanceCurrency, Double.valueOf(balanceValue));

        String iban = getNodeValueFromDocument(document, "/datasources/datasource[@key='response']/element[@key='account']/element[@key='iban']");
        logAdditonalDataToIdentifyAccountTypes(account, document);

        nodeList = getNodeList(document, "/datasources/datasource[@key='response']/element[@key='account']/element[@key='companies']");
        if(nodeList.getLength() > 1) {
            LOGGER.warn("Multiple companies/account holders");
        }
        Node companyNode = nodeList.item(0);
        String name = getValue(getNode(companyNode, ".//element[@key='name']"));
        HolderName holderName = new HolderName(name);

        TransactionalAccount filledAccount;
        switch (account.getType()) {
            case CHECKING:
                filledAccount = CheckingAccount.builder()
                        .setHolderName(holderName)
                        .setBalance(amount)
                        .setBankIdentifier(account.getBankIdentifier())
                        .setAccountNumber(iban)
                        .setUniqueIdentifier(iban)
                        .setName(account.getName())
                        .build();
                break;
            case SAVINGS:
                filledAccount = SavingsAccount
                        .builder(iban, amount)
                        .setHolderName(holderName)
                        .setName(account.getName())
                        .setBankIdentifier(account.getBankIdentifier())
                        .setUniqueIdentifier(iban)
                        .build();
                break;

            default:
                filledAccount = null;
                LOGGER.error(String.format("Not implemented account type %s", account.getType()));
                break;
        }

        return filledAccount;
    }

    private void logAdditonalDataToIdentifyAccountTypes(TransactionalAccount account, Document document) {
        String type = getNodeValueFromDocument(document, "/datasources/datasource[@key='response']/element[@key='account']/element[@key='type']");
        String dataBaseCode = getNodeValueFromDocument(document, "/datasources/datasource[@key='response']/element[@key='account']/element[@key='dataBaseCode']");
        LOGGER.info(String.format("AccountType:%s, type:%s, dataBaseCode:%s", account.getType(), type, dataBaseCode));
    }

    private String getNodeValueFromDocument(Document document, String expression) {
        NodeList nodeList = getNodeList(document, expression);
        Node node = nodeList.item(0);
        return getValue(node);
    }

    public Collection<? extends Transaction> getTransactions(String balanceMovementsForAccount)  {
        Document document = parseDocument(balanceMovementsForAccount);
        NodeList movements = getNodeList(document, "/datasources/datasource[@key='response']/element[@key='movements']/element");
        List<Transaction> transactions =
                IntStream.range(0, movements.getLength())
                        .mapToObj(i -> getTransactionFromMovement(movements.item(i)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
        return transactions;
    }

    private Transaction getTransactionFromMovement(Node movement)  {
        String amountCurrency = getValue(getNode(movement, ".//element[@key='amount']/element[@key='currency']"));
        String amountValue = getValue(getNode(movement, ".//element[@key='amount']/element[@key='value']"));

        String description = getValue(getNode(movement, ".//element[@key='bookingText']"));

        String movementDate = getValue(getNode(movement, ".//element[@key='transactionDate']/element[@key='date']"));

        Date date;
        try {
            date = DateUtils.parseDate(movementDate, "yyyy-MM-dd'T'HH:mm:ssZ");
        } catch (ParseException e) {
            throw new IllegalStateException(String.format("Unable to parse %s", movementDate), e);
        }

        Amount amount = new Amount(amountCurrency, Double.valueOf(amountValue) );

        return Transaction.builder()
                .setAmount(amount)
                .setDate(date)
                .setDescription(description)
                .setPending(false)
                .build();
    }

    public boolean getAccountNodeExists(String dataSources) {
        Document document = parseDocument(dataSources);
        NodeList nodeList = getNodeList(document, "/datasources/datasource[@key='otml_store_session']/element[@key='account']/element");
        return nodeList != null && nodeList.getLength() > 0;
    }
}

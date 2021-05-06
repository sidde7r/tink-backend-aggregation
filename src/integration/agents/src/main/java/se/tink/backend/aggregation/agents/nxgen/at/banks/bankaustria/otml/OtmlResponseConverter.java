package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.BankAustriaConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.entities.RtaMessage;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class OtmlResponseConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OtmlResponseConverter.class);
    private static final String XPATH_NOT_VALID = "Xpath expression not valid, test in unittest";

    private final DocumentBuilderFactory factory;
    private XPathFactory xPathfactory;

    public OtmlResponseConverter() {
        factory = DocumentBuilderFactory.newInstance();
        xPathfactory = XPathFactory.newInstance();
    }

    private Document parseDocument(String otmlDocument) {
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            return documentBuilder.parse(
                    new ByteArrayInputStream(otmlDocument.getBytes(StandardCharsets.UTF_8)));
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
            XPathExpression expression =
                    xpath.compile(BankAustriaConstants.XPathExpression.XPATH_RESPONSE_RESULT);
            NodeList xpathNodeList =
                    (NodeList) expression.evaluate(document, XPathConstants.NODESET);
            if (xpathNodeList.getLength() != 1) {
                return Optional.empty();
            }
            return Optional.of(xpathNodeList.item(0));
        } catch (XPathExpressionException e) {
            LOGGER.error(
                    withTag(BankAustriaConstants.LogTags.LOG_TAG_CODE_ERROR, XPATH_NOT_VALID), e);
        }
        return Optional.empty();
    }

    public Collection<TransactionalAccount> getAccountsFromSettings(String xml) {
        Document document = parseDocument(xml);
        Optional<NodeList> nodeList =
                getOptionalNodeList(
                        document,
                        BankAustriaConstants.XPathExpression.XPATH_SETTINGS_RESPONSE_ACCOUNTS);
        if (!nodeList.isPresent()) {
            LOGGER.warn(
                    withTag(BankAustriaConstants.LogTags.LOG_TAG_ACCOUNT, "No accounts found."));
            return Collections.emptyList();
        }

        return IntStream.range(0, nodeList.get().getLength())
                .mapToObj(i -> getAccountFromSetting(nodeList.get().item(i)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private TransactionalAccount getAccountFromSetting(Node accountNode) {
        String accountNumber =
                getValue(
                        getNode(
                                accountNode,
                                BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_NUMBER));
        String accountNickName =
                getValue(
                        getNode(
                                accountNode,
                                BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_NICKNAME));
        String accountKey =
                getValue(
                        getNode(
                                accountNode,
                                BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_KEY));
        String accountType =
                getValue(
                        getNode(
                                accountNode,
                                BankAustriaConstants.XPathExpression.XPATH_SETTINGS_ACCOUNT_TYPE));

        switch (accountType.toUpperCase()) {
            case BankAustriaConstants.BankAustriaAccountTypes.CURRENT:
                return TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(0)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(accountNickName)
                                        .addIdentifier(new IbanIdentifier(accountNumber))
                                        .build())
                        .setBankIdentifier(accountKey)
                        .build()
                        .get();
            case BankAustriaConstants.BankAustriaAccountTypes.SAVING:
                return TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withPaymentAccountFlag()
                        .withBalance(BalanceModule.of(ExactCurrencyAmount.inEUR(0)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(accountNickName)
                                        .addIdentifier(new IbanIdentifier(accountNumber))
                                        .build())
                        .setBankIdentifier(accountKey)
                        .build()
                        .get();
            case BankAustriaConstants.BankAustriaAccountTypes.CARDS:
                return null;
            default:
                LOGGER.error(
                        withTag(
                                BankAustriaConstants.LogTags.LOG_TAG_ACCOUNT,
                                String.format("Unknown account type %s", accountType)));
                return null;
        }
    }

    private Node getNode(Node node, String expression) {
        XPath xpath = xPathfactory.newXPath();
        try {
            XPathExpression accountNumberExtractor = xpath.compile(expression);
            return (Node) accountNumberExtractor.evaluate(node, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            LOGGER.error(
                    withTag(BankAustriaConstants.LogTags.LOG_TAG_CODE_ERROR, XPATH_NOT_VALID), e);
        }
        return null;
    }

    private Optional<NodeList> getOptionalNodeList(Document document, String expression) {
        XPath xpath = xPathfactory.newXPath();
        NodeList xpathNodeList = null;
        try {
            XPathExpression xpathExpression = xpath.compile(expression);
            xpathNodeList = (NodeList) xpathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            LOGGER.error(
                    withTag(BankAustriaConstants.LogTags.LOG_TAG_CODE_ERROR, XPATH_NOT_VALID), e);
        }
        return Optional.ofNullable(xpathNodeList);
    }

    private NodeList getNodeList(Document document, String expression) {
        return getOptionalNodeList(document, expression)
                .orElseThrow(() -> new IllegalStateException("Method requires not null nodelist"));
    }

    public TransactionalAccount fillAccountInformation(
            String accountMovementXml, TransactionalAccount account) {
        Document document = parseDocument(accountMovementXml);
        NodeList nodeList =
                getNodeList(document, BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_BALANCE);
        Node balanceNode = nodeList.item(0);
        String balanceCurrency =
                getValue(getNode(balanceNode, BankAustriaConstants.XPathExpression.XPATH_CURRENCY));
        String balanceValue =
                getValue(getNode(balanceNode, BankAustriaConstants.XPathExpression.XPATH_VALUE));
        ExactCurrencyAmount amount = ExactCurrencyAmount.of(balanceValue, balanceCurrency);
        String iban =
                getNodeValueFromDocument(
                        document, BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_IBAN);
        logAdditonalDataToIdentifyAccountTypes(account, document);

        nodeList =
                getNodeList(document, BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_COMPANIES);
        HolderName holderName = getHolderName(nodeList);

        TransactionalAccount filledAccount;
        switch (account.getType()) {
            case CHECKING:
                filledAccount =
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(amount))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(iban)
                                                .withAccountNumber(iban)
                                                .withAccountName(account.getName())
                                                .addIdentifier(new IbanIdentifier(iban))
                                                .build())
                                .setBankIdentifier(account.getApiIdentifier())
                                .addHolderName(holderName.toString())
                                .build()
                                .get();
                break;
            case SAVINGS:
                filledAccount =
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.SAVINGS)
                                .withPaymentAccountFlag()
                                .withBalance(BalanceModule.of(amount))
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(iban)
                                                .withAccountNumber(iban)
                                                .withAccountName(account.getName())
                                                .addIdentifier(new IbanIdentifier(iban))
                                                .build())
                                .setBankIdentifier(account.getApiIdentifier())
                                .addHolderName(holderName.toString())
                                .build()
                                .get();
                break;

            default:
                filledAccount = null;
                LOGGER.error(
                        withTag(
                                BankAustriaConstants.LogTags.LOG_TAG_ACCOUNT,
                                String.format(
                                        "Not implemented account type %s", account.getType())));
                break;
        }

        return filledAccount;
    }

    private HolderName getHolderName(NodeList nodeList) {
        HolderName holderName = new HolderName("");
        if (nodeList.getLength() > 1) {
            LOGGER.warn(
                    withTag(
                            BankAustriaConstants.LogTags.LOG_TAG_ACCOUNT,
                            "Multiple companies/account holders"));
        } else if (nodeList.getLength() == 1) {
            Node companyNode = nodeList.item(0);
            String name =
                    getValue(getNode(companyNode, BankAustriaConstants.XPathExpression.XPATH_NAME));
            holderName = new HolderName(name);
        }
        return holderName;
    }

    private void logAdditonalDataToIdentifyAccountTypes(
            TransactionalAccount account, Document document) {
        String type =
                getNodeValueFromDocument(
                        document, BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_TYPE);
        String dataBaseCode =
                getNodeValueFromDocument(
                        document, BankAustriaConstants.XPathExpression.XPATH_ACCOUNT_DATABASE_CODE);
        LOGGER.info(
                withTag(
                        BankAustriaConstants.LogTags.LOG_TAG_ACCOUNT,
                        String.format(
                                "AccountType:%s, type:%s, dataBaseCode:%s",
                                account.getType(), type, dataBaseCode)));
    }

    private String getNodeValueFromDocument(Document document, String expression) {
        NodeList nodeList = getNodeList(document, expression);
        Node node = nodeList.item(0);
        return getValue(node);
    }

    public Collection<Transaction> getTransactions(String balanceMovementsForAccount) {
        Document document = parseDocument(balanceMovementsForAccount);
        NodeList movements =
                getNodeList(
                        document,
                        BankAustriaConstants.XPathExpression.XPATH_TRANSACTIONS_MOVEMENTS);
        return IntStream.range(0, movements.getLength())
                .mapToObj(i -> getTransactionFromMovement(movements.item(i)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Transaction getTransactionFromMovement(Node movement) {
        String amountCurrency =
                getValue(
                        getNode(
                                movement,
                                BankAustriaConstants.XPathExpression.XPATH_TRANSACTION_CURRENCY));
        String amountValue =
                getValue(
                        getNode(
                                movement,
                                BankAustriaConstants.XPathExpression.XPATH_TRANSACTION_VALUE));

        String description =
                getValue(
                        getNode(
                                movement,
                                BankAustriaConstants.XPathExpression
                                        .XPATH_TRANSACTION_DESCRIPTION));

        String movementDate =
                getValue(
                        getNode(
                                movement,
                                BankAustriaConstants.XPathExpression.XPATH_TRANSACTION_DATE));

        Date date;
        try {
            date = DateUtils.parseDate(movementDate, "yyyy-MM-dd'T'HH:mm:ssZ");
        } catch (ParseException e) {
            throw new IllegalStateException(String.format("Unable to parse %s", movementDate), e);
        }

        ExactCurrencyAmount amount = ExactCurrencyAmount.of(amountValue, amountCurrency);

        return Transaction.builder()
                .setAmount(amount)
                .setDate(date)
                .setDescription(description)
                .setPending(false)
                .build();
    }

    public boolean getAccountNodeExists(String dataSources) {
        Document document = parseDocument(dataSources);
        NodeList nodeList =
                getNodeList(
                        document, BankAustriaConstants.XPathExpression.XPATH_RESPONSE_WITH_ACCOUNT);
        return nodeList != null && nodeList.getLength() > 0;
    }

    private String withTag(LogTag logTag, String string) {
        return String.format("%s: %s", logTag.toString(), string);
    }

    public Optional<RtaMessage> anyRtaMessageToAccept(String xml) {
        Document document = parseDocument(xml);
        NodeList nodeList =
                getNodeList(document, BankAustriaConstants.XPathExpression.XPATH_RTA_MESSAGE);
        if (nodeList == null || nodeList.getLength() == 0) {
            return Optional.empty();
        }

        String rtaMessageID =
                getValue(
                        getNode(
                                nodeList.item(0),
                                BankAustriaConstants.XPathExpression.XPATH_RTA_MESSAGE_ID));

        return Optional.of(new RtaMessage(rtaMessageID));
    }
}

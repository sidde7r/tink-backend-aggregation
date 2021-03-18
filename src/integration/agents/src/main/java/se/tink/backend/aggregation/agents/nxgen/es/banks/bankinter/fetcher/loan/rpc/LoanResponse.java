package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc;

import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.InstallmentStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.entities.PaginationKey;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanResponse extends HtmlResponse {
    final Map<String, List<String>> dataValues;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LoanResponse(String body) {
        super(body);
        this.dataValues = parseDataValues();
    }

    private List<String> listOfNodeValues(NodeList nodes) {
        final List<String> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add(nodes.item(i).getTextContent());
        }
        return list;
    }

    private Map<String, List<String>> parseDataValues() {
        final HashMap<String, List<String>> dataValues = new HashMap<>();

        // Parse values from "Datos del préstamo"
        final NodeList dataNodes =
                evaluateXPath("//div[contains(@class,'pensionPlanSUBData')]/div", NodeList.class);
        for (int i = 0; i < dataNodes.getLength(); i++) {
            parseLoanDataItem(dataNodes.item(i))
                    .map(entry -> dataValues.put(entry.getKey(), entry.getValue()));
        }

        // Parse values from "Condiciones del préstamo"
        final NodeList nodes = evaluateXPath("//div[contains(@class,'rowSaving')]", NodeList.class);
        for (int i = 0; i < nodes.getLength(); i++) {
            parseLoanConditionsItem(nodes.item(i))
                    .map(entry -> dataValues.put(entry.getKey(), entry.getValue()));
        }

        return dataValues;
    }

    private Optional<Entry<String, List<String>>> parseLoanDataItem(Node node) {
        final Node keyNode = evaluateXPath(node, "h3", Node.class);
        final NodeList valueNodes = evaluateXPath(node, "span", NodeList.class);
        if (!Objects.isNull(keyNode) && !Objects.isNull(valueNodes) && valueNodes.getLength() > 0) {
            return Optional.of(
                    Maps.immutableEntry(
                            keyNode.getTextContent().trim().toLowerCase(),
                            listOfNodeValues(valueNodes)));
        }

        return Optional.empty();
    }

    private Optional<Entry<String, List<String>>> parseLoanConditionsItem(Node node) {
        final Node keyNode = evaluateXPath(node, "span[1]", Node.class);
        final Node valueNode = evaluateXPath(node, "span[2]", Node.class);
        if (!Objects.isNull(keyNode) && !Objects.isNull(valueNode)) {
            return Optional.of(
                    Maps.immutableEntry(
                            keyNode.getTextContent().trim().toLowerCase(),
                            Collections.singletonList(
                                    valueNode.getTextContent().replaceAll("[\\s\\u00a0]+", ""))));
        }

        return Optional.empty();
    }

    public boolean isSingleCurrency() {
        return getBalance().getCurrencyCode().equals(getInitialBalance().getCurrencyCode());
    }

    public LoanAccount toLoanAccount() {
        final String accountNumber = getIban();
        final AccountIdentifier iban =
                AccountIdentifier.create(AccountIdentifierType.IBAN, accountNumber);
        final String formattedIban = iban.getIdentifier(new DisplayAccountIdentifierFormatter());
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(Type.OTHER)
                                .withBalance(getBalance())
                                .withInterestRate(getInterestRate().doubleValue())
                                .setApplicants(getApplicants())
                                .setInitialBalance(getInitialBalance())
                                .setLoanNumber(accountNumber)
                                .setInitialDate(getInitialDate())
                                .setMonthlyAmortization(getMonthlyAmortization())
                                .setNextDayOfTermsChange(getNextDayOfTermsChange())
                                .setNumMonthsBound(getNumMonthsBound().orElse(null))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(formattedIban)
                                .addIdentifier(iban)
                                .build())
                .putInTemporaryStorage(StorageKeys.FIRST_PAGINATION_KEY, getPaginationKey(0))
                .build();
    }

    private List<String> getApplicants() {
        return dataValues.get("titulares");
    }

    private BigDecimal getInterestRate() {
        final String value = dataValues.get("actual").get(0);
        return parseValue(value).divide(BigDecimal.valueOf(100));
    }

    private String getDataValue(String key) {
        final List<String> values = dataValues.get(key);
        if (values == null) {
            return null;
        }
        return values.get(0);
    }

    private String getIban() {
        return ObjectUtils.firstNonNull(getDataValue("prestamo"), getDataValue("préstamo"));
    }

    private String getAssociatedAccount() {
        return getDataValue("cuenta");
    }

    private ExactCurrencyAmount getBalance() {
        return parseAmount(getDataValue("deuda pendiente")).negate();
    }

    private ExactCurrencyAmount getInitialBalance() {
        return parseAmount(getDataValue("importe inicial")).negate();
    }

    private ExactCurrencyAmount getMonthlyAmortization() {
        return parseAmount(getDataValue("importe de la cuota"));
    }

    private LocalDate getInitialDate() {
        return LocalDate.parse(getDataValue("fecha inicio"), DATE_FORMATTER);
    }

    private LocalDate getNextDayOfTermsChange() {
        return LocalDate.parse(getDataValue("fecha revisión"), DATE_FORMATTER);
    }

    private Optional<Integer> getNumMonthsBound() {
        final String stringValue = getDataValue("plazo de revisión");
        if (Objects.isNull(stringValue)) {
            return Optional.empty();
        }
        final Matcher matcher =
                Pattern.compile("^(?<value>\\d+)(?<unit>[^\\d]+)$").matcher(stringValue);
        if (!matcher.find()) {
            throw new IllegalStateException("Cannot parse months bound value: " + stringValue);
        }

        final int value = Integer.parseInt(matcher.group("value"));
        final String unit = matcher.group("unit");
        if (unit.equals("mes(es)")) {
            return Optional.of(value);
        } else {
            throw new IllegalStateException("Cannot parse months bound value: " + stringValue);
        }
    }

    public PaginationKey getNextPaginationKey() {
        if (Objects.isNull(getElementById("prestaForm:linkMasPagos"))) {
            return null;
        }
        return getPaginationKey(getTransactionNodes().getLength());
    }

    private PaginationKey getPaginationKey(int skip) {
        final Node paginationKey =
                evaluateXPath(
                        "//div[@id='prestaForm:tablaPagos']/input[@type='hidden' and contains(@name,'prestaForm:')]",
                        Node.class);
        final String source = paginationKey.getAttributes().getNamedItem("id").getTextContent();
        final int offset =
                Integer.parseInt(
                        paginationKey.getAttributes().getNamedItem("value").getTextContent());
        final String viewState =
                evaluateXPath(
                        "//form[@id='prestaForm']/input[@type='hidden' and @name='javax.faces.ViewState']/@value",
                        String.class);

        return new PaginationKey(source, viewState, offset, skip);
    }

    private Transaction toTinkTransaction(Node node) {
        final String dateValue = evaluateXPath(node, "div[1]/span", String.class);
        final String status = evaluateXPath(node, "div[2]/span", String.class);
        final ExactCurrencyAmount amount =
                parseAmount(evaluateXPath(node, "div[3]/span", String.class));

        final boolean paid = status.equalsIgnoreCase(InstallmentStatus.PAID);
        final LocalDate date = LocalDate.parse(dateValue, DATE_FORMATTER);

        return Transaction.builder().setDate(date).setAmount(amount).setPending(!paid).build();
    }

    private NodeList getTransactionNodes() {
        return evaluateXPath(
                "//div[@id='prestaForm:tablaPagos']/div[contains(@class,'rowSummary')]",
                NodeList.class);
    }

    public List<? extends Transaction> toTinkTransactions(int skip) {
        // each new page contains all the previous transactions too, for our pagination we want
        // to skip some, so we only get the new ones

        final NodeList transactionNodes = getTransactionNodes();
        final List<Transaction> transactions = new ArrayList<>();
        for (int i = skip; i < transactionNodes.getLength(); i++) {
            transactions.add(toTinkTransaction(transactionNodes.item(i)));
        }
        return transactions;
    }
}

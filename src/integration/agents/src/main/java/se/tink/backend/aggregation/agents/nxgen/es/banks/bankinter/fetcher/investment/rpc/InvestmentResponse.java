package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.investment.rpc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc.HtmlResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentResponse extends HtmlResponse {
    final Map<String, String> dataValues;

    private static final Map<Long, String> MONTH_ABBREVIATIONS = new HashMap<>();
    private static final DateTimeFormatter DATE_FORMAT;

    static {
        MONTH_ABBREVIATIONS.put(1L, "ene");
        MONTH_ABBREVIATIONS.put(2L, "feb");
        MONTH_ABBREVIATIONS.put(3L, "mar");
        MONTH_ABBREVIATIONS.put(4L, "abr");
        MONTH_ABBREVIATIONS.put(5L, "may");
        MONTH_ABBREVIATIONS.put(6L, "jun");
        MONTH_ABBREVIATIONS.put(7L, "jul");
        MONTH_ABBREVIATIONS.put(8L, "ago");
        MONTH_ABBREVIATIONS.put(9L, "sep");
        MONTH_ABBREVIATIONS.put(10L, "oct");
        MONTH_ABBREVIATIONS.put(11L, "nov");
        MONTH_ABBREVIATIONS.put(12L, "dic");

        DATE_FORMAT =
                new DateTimeFormatterBuilder()
                        .appendPattern("dd ")
                        .appendText(ChronoField.MONTH_OF_YEAR, MONTH_ABBREVIATIONS)
                        .appendPattern(" uu")
                        .toFormatter()
                        .withLocale(new Locale("es", "ES"));
    }

    public InvestmentResponse(String body) {
        super(body);
        this.dataValues = parseDataValues();
    }

    private Map<String, String> parseDataValues() {
        // Parse values from "FICHA TÉCNICA" and "Datos Adicionales"
        final NodeList nodes =
                evaluateXPath("//div[contains(@class,'asideSUBData')]/div", NodeList.class);
        final HashMap<String, String> dataValues = new HashMap<String, String>();
        for (int i = 0; i < nodes.getLength(); i++) {
            final Node node = nodes.item(i);
            final Node keyNode = evaluateXPath(node, "h3", Node.class);
            final Node valueNode = evaluateXPath(node, "span", Node.class);
            if (!Objects.isNull(keyNode) && !Objects.isNull(valueNode)) {
                dataValues.put(
                        keyNode.getTextContent().trim().toLowerCase(),
                        valueNode.getTextContent().trim());
            }
        }

        // Parse amounts values from "Su inversión"
        final NodeList balanceNodes =
                evaluateXPath("//div[contains(@class,'rowSaving')]", NodeList.class);
        for (int i = 0; i < balanceNodes.getLength(); i++) {
            final Node node = balanceNodes.item(i);
            final Node keyNode = evaluateXPath(node, "span[1]", Node.class);
            final Node valueNode = evaluateXPath(node, "span[2]", Node.class);
            if (!Objects.isNull(keyNode) && !Objects.isNull(valueNode)) {
                dataValues.put(
                        keyNode.getTextContent().trim().toLowerCase(),
                        valueNode.getTextContent().replaceAll("\\s+", ""));
            }
        }

        return dataValues;
    }

    public String getName() {
        return dataValues.get("nombre");
    }

    public String getIsin() {
        return dataValues.get("isin");
    }

    public String getCurrency() {
        return dataValues.get("divisa");
    }

    public String getFundAccount() {
        return dataValues.get("cuenta del fondo");
    }

    public String getAssociatedAccount() {
        return dataValues.get("cuenta asociada");
    }

    public BigDecimal getTotalBalance() {
        return parseAmount(dataValues.get("saldo actual")).getExactValue();
    }

    public BigDecimal getAvailabeBalance() {
        return parseAmount(dataValues.get("saldo disponible")).getExactValue();
    }

    public BigDecimal getContributions() {
        return parseAmount(dataValues.get("aportaciones")).getExactValue();
    }

    public BigDecimal getProfit() {
        return parseAmount(dataValues.get("rentabilidad")).getExactValue();
    }

    public BigDecimal getNumberOfShares() {
        return parseValue(dataValues.get("participaciones"));
    }

    public BigDecimal getSharePrice() {
        final Node node = evaluateXPath("//div[contains(@class,'rentValueResults')]", Node.class);
        if (Objects.isNull(node)) {
            return BigDecimal.ZERO;
        }
        return parseAmount(node.getTextContent()).getExactValue();
    }

    private Transaction toTinkTransaction(Node node) {
        final String description =
                evaluateXPath(
                                node,
                                "div[contains(@class,'tableFirst')]/div[contains(@class,'heightRowDetails')]/p",
                                Node.class)
                        .getTextContent()
                        .trim();
        final String date =
                evaluateXPath(
                                node,
                                "div[contains(@class,'tableFirst')]/div[contains(@class,'heightRowDetails')]/span[contains(@class,'subDate')]",
                                Node.class)
                        .getTextContent();
        final Node amountNode =
                evaluateXPath(
                        node,
                        "div[contains(@class,'tableSecond')]/div[contains(@class,'heightRowDetails')]/p",
                        Node.class);
        final ExactCurrencyAmount amount = parseAmount(amountNode.getTextContent());

        return new Transaction.Builder()
                .setAmount(amount)
                .setDescription(description)
                .setDate(LocalDate.parse(date, DATE_FORMAT))
                .setType(TransactionTypes.TRANSFER)
                .build();
    }

    public List<Transaction> toTinkTransactions() {
        final NodeList nodes =
                evaluateXPath(
                        "//div[@id='inverForm:tablaOperaciones']/div[contains(@class,'rowSummary')]",
                        NodeList.class);
        final ArrayList<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            transactions.add(toTinkTransaction(nodes.item(i)));
        }
        return transactions;
    }
}

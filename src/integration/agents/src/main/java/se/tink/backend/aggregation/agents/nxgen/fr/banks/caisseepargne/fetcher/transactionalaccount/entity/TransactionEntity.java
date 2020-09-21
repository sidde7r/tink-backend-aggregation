package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.ResponseValues;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JacksonXmlRootElement(localName = "HistoCptEntity")
public class TransactionEntity {

    @JacksonXmlProperty(localName = "DateOprt")
    private String date;

    @JacksonXmlProperty(localName = "LiblOprt")
    private String description;

    @JacksonXmlProperty(localName = "MtOprt")
    private long unscaledAmount;

    @JacksonXmlProperty(localName = "CodeDevise")
    private String currency;

    @JacksonXmlProperty(localName = "SensOprt")
    private String meaningOfOprt;

    @JacksonXmlProperty(localName = "RefrOprt")
    private String refrPort;

    @JacksonXmlProperty(localName = "CodeTypeEcrit")
    private String codeTypeWritten;

    @JacksonXmlProperty(localName = "CodeTypeOp")
    private String codeTypeOp;

    public Transaction toTinkTransaction() {

        Transaction.Builder builder =
                Transaction.builder()
                        .setDate(LocalDateTime.parse(date).toLocalDate())
                        .setAmount(getExactCurrencyAmount())
                        .setType(getTransactionTypes())
                        .setDescription(description);

        return builder.build();
    }

    private ExactCurrencyAmount getExactCurrencyAmount() {
        if (ResponseValues.TRANSACTION_TYPE_INCOME.equalsIgnoreCase(meaningOfOprt)) {
            return ExactCurrencyAmount.of(BigDecimal.valueOf(unscaledAmount, 2), currency);
        }
        return ExactCurrencyAmount.of(BigDecimal.valueOf(-unscaledAmount, 2), currency);
    }

    private TransactionTypes getTransactionTypes() {
        if (StringUtils.isBlank(description)) {
            return TransactionTypes.DEFAULT;
        }

        if (description.startsWith("VIR")) {
            return TransactionTypes.TRANSFER;
        }

        if (description.startsWith("PRLV")) {
            return TransactionTypes.PAYMENT;
        }

        if (description.startsWith("CB")) {
            return TransactionTypes.CREDIT_CARD;
        }

        if (description.startsWith("RETRAIT")) {
            return TransactionTypes.WITHDRAWAL;
        }

        return TransactionTypes.DEFAULT;
    }
}

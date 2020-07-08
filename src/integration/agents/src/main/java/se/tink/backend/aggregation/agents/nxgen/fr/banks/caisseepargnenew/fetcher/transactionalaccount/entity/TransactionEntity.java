package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.CaisseEpargneConstants.ResponseValue;
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
                        .setDescription(description);

        return builder.build();
    }

    private ExactCurrencyAmount getExactCurrencyAmount() {
        if (ResponseValue.TRANSACTION_TYPE_INCOME.equalsIgnoreCase(meaningOfOprt)) {
            return ExactCurrencyAmount.of(BigDecimal.valueOf(unscaledAmount, 2), currency);
        }
        return ExactCurrencyAmount.of(BigDecimal.valueOf(-unscaledAmount, 2), currency);
    }
}

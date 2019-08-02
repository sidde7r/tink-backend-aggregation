package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class UpcomingTransactionEntity {
    @JsonProperty("ROW_ID")
    private Integer rowId;

    @JsonProperty("VERIF_TIME_STAMP")
    private String verificationTimestamp;

    @JsonProperty("VERIF_SUB_ID")
    private String verificationSubId;

    @JsonProperty("SEB_KUND_NR")
    private String customerNumber;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("KTOSLAG_TXT")
    private String accountName;

    @JsonProperty("KHAV")
    private String accountOwner;

    @JsonProperty("BETAL_DATUM") // yyyy-mm-dd
    private String paymentDate;

    @JsonProperty("MOTTAGARE")
    private String recipient;

    @JsonProperty("MOTTAGARE_PREFIX")
    private String recipientPrefix;

    @JsonProperty("UPPDRAG_BEL")
    private BigDecimal amount;

    @JsonProperty("UPPDRAGS_ID")
    private String paymentId;

    @JsonProperty("UPPDRAGS_TYP")
    private String paymentType;

    @JsonProperty("BOKF_SALDO")
    private BigDecimal balance;

    @JsonProperty("DISP_BEL")
    private BigDecimal availbleAmount;

    @JsonProperty("KREDBEL")
    private BigDecimal availableCredit;

    @JsonProperty("UPPDAT_FLG")
    private String updateFlag;

    @JsonProperty("REG_TIMESTAMP")
    private String regTimestamp;

    @JsonProperty("TABORT_ID")
    private String removalId;

    @JsonIgnore
    private String getCurrency() {
        return SEBConstants.DEFAULT_CURRENCY;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @JsonIgnore
    public UpcomingTransaction toTinkTransaction() {
        return UpcomingTransaction.builder()
                .setDate(LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(paymentDate)))
                .setAmount(ExactCurrencyAmount.of(amount, getCurrency()))
                .setDescription(StringUtils.trim(recipient))
                .build();
    }
}

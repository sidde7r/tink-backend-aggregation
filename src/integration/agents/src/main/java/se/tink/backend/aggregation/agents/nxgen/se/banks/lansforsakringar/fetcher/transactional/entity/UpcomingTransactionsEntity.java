package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.banks.lansforsakringar.model.TransferInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class UpcomingTransactionsEntity {
    private String id;
    private String description;
    private BigDecimal amount;
    private String date;
    private String type;
    private String typeAsString;
    private TransferInfoEntity transferInfo;
    // `paymentInfo` is null - cannot define it!
    // `loanInfo` is null - cannot define it
    private DirectDebitInfoEntity directDebitInfo;
    // `statusInfo` is null - cannot define it!


    public String getDate() {
        return date;
    }

    @JsonIgnore
    public UpcomingTransaction toTinkTransaction() {
        return UpcomingTransaction.builder()
                .setDate(LocalDate.parse(date))
                .setAmount(ExactCurrencyAmount.of(amount, Accounts.CURRENCY))
                .setDescription(description)
                .build();
    }
}

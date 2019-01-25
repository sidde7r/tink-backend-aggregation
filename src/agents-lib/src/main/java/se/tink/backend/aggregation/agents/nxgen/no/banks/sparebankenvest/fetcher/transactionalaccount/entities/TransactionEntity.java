package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    private String id;
    private String accountNumber;
    private String prettyDescription;
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date postingDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date accountingDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date valueDate;
    private Double amount;
    private String archiveRef;
    private Integer serialNo;
    private Long numRef;
    private String alfaRef;
    private Integer txCode;
    private String descriptionShort;
    private String description;
    private Double balanceAfterTransaction;
    private String draweeAccount;
    private String orgUnit;
    private Long functionCode;
    private Integer movementDetailIndex;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setDescription(getFormattedTinkDescription())
                .setAmount(Amount.inNOK(amount))
                .setDate(accountingDate)
                .build();
    }

    private String getFormattedTinkDescription() {
        String prefixRegex = "^(\\d{2}\\.\\d{2}\\s)?";
        return description.replaceAll(prefixRegex, "");
    }
}

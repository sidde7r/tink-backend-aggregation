package se.tink.backend.aggregation.agents.banks.sbab.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferAccountEntity {
    private String name;

    @JsonProperty("number")
    private String accountNumber;

    private String description;
    private double balance;
    private double availableForWithdrawal;
    private double interestRate;
    private double accruedInterestCredit;
    private String accountType;
    private String mandateType;
    private String productCode;
    private int statusCode;
    private String statusText;
    private String auditReference;
    private boolean isTaxAccount;
    private List<AccountHolderEntity> accountHolders;
    private List<MandateEntity> mandates;
    private String startDate;
    private boolean isActionGranted;

    public String getAccountNumber() {
        return accountNumber;
    }
}

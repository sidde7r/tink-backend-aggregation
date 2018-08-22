package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;

@JsonObject
public class LoanEntity {
    private String id;
    private String name;
    private String formattedNumber;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private String type;
    private Boolean paymentFromEnabled;
    private Boolean balancePreferred;
    private Boolean transferFromEnabled;
    private Boolean transferToEnabled;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;

    @JsonIgnore
    public LoanAccount toTinkLoan(LoanDetailsEntity loanDetails) {
        return LoanAccount.builder(formattedNumber,
                Sparebank1AmountUtils.constructAmount(balanceAmountInteger, balanceAmountFraction))
                .setAccountNumber(formattedNumber)
                .setName(name)
                .setInterestRate(loanDetails.getInterestRate())
                .setDetails(LoanDetails.builder()
                        .setName(loanDetails.getName())
                        .setInitialBalance(loanDetails.getInitialBalance())
                        .setSecurity(loanDetails.getCollateral())
                        .build())
                .build();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormattedNumber() {
        return formattedNumber;
    }

    public String getBalanceAmountInteger() {
        return balanceAmountInteger;
    }

    public String getBalanceAmountFraction() {
        return balanceAmountFraction;
    }

    public String getType() {
        return type;
    }

    public Boolean getPaymentFromEnabled() {
        return paymentFromEnabled;
    }

    public Boolean getBalancePreferred() {
        return balancePreferred;
    }

    public Boolean getTransferFromEnabled() {
        return transferFromEnabled;
    }

    public Boolean getTransferToEnabled() {
        return transferToEnabled;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }
}

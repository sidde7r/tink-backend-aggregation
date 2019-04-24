package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {
    private String id;
    private String country;

    @JsonProperty("account_numbers")
    private List<AccountNumbersEntity> accountNumbers;

    private String currency;

    @JsonProperty("account_name")
    private String accountName;

    private String product;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("available_balance")
    private String availableBalance;

    @JsonProperty("booked_balance")
    private String bookedBalance;

    @JsonProperty("value_dated_balance")
    private String valueDatedBalance;

    private BankEntity bank;
    private String status;

    @JsonProperty("credit_limit")
    private String creditLimit;

    @JsonProperty("latest_transaction_booking_date")
    private String latestTransactionBookingDate;

    @JsonProperty("_links")
    private LinkListEntity links;

    @JsonIgnore
    public Amount getAccountBalance() {
        return new Amount(currency, new BigDecimal(availableBalance));
    }

    @JsonIgnore
    public boolean isOpen() {
        // if Nordea stops sending status for account, we will use them all
        return !NordeaBaseConstants.Account.CLOSED.equalsIgnoreCase(status);
    }

    @JsonIgnore
    public AccountTypes tinkAccountType() {
        return NordeaBaseConstants.ACCOUNT_TYPE
                .translate(accountType)
                .orElseGet(() -> AccountTypes.OTHER);
    }

    public String getId() {
        return id;
    }

    public List<AccountNumbersEntity> getAccountNumbers() {
        return accountNumbers;
    }

    public LinkListEntity getLinks() {
        return links;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getProduct() {
        return product;
    }
}

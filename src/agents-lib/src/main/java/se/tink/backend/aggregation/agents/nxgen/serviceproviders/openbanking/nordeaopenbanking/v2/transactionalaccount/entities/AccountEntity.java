package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.entities.LinkListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

@JsonObject
public class AccountEntity {
    @JsonProperty("_id")
    private String id;
    private String country;
    private List<AccountNumbersEntity> accountNumbers;
    private String currency;
    private String accountName;
    private String product;
    private String accountType;
    private String availableBalance;
    private String bookedBalance;
    private String valueDatedBalance;
    private BankEntity bank;
    private String status;
    private String creditLimit;
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
        return NordeaBaseConstants.ACCOUNT_TYPE.translate(accountType).get();
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

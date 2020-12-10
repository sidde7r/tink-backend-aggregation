package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class Product {

    private String type;
    private String group;
    private String shortName;
    private String accountId;
    private Boolean hasSubscribedToSMSBalance;
    private String enterCardAccountNumber;
    private Boolean isICInsurancePresent;

    public String getType() {
        return type;
    }

    public String getGroup() {
        return group;
    }

    public String getShortName() {
        return shortName;
    }

    public String getAccountId() {
        return accountId;
    }

    public Boolean getHasSubscribedToSMSBalance() {
        return hasSubscribedToSMSBalance;
    }

    public String getEnterCardAccountNumber() {
        return enterCardAccountNumber;
    }

    public Boolean getICInsurancePresent() {
        return isICInsurancePresent;
    }

    @JsonIgnore
    public boolean isCreditCard() {
        return EnterCardConstants.AccountType.CARD.equalsIgnoreCase(type);
    }
}

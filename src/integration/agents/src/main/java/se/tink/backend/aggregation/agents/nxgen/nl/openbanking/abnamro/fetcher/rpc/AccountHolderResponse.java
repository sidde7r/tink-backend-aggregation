package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.AbnAmroConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHolderResponse {

    private String accountNumber;
    private String currency;
    private String accountHolderName;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    // Check the last 3 characters including space is acronym for COMPTE JOINT
    @JsonIgnore
    public String getFilteredAccountHolderName() {
        final Matcher matcher =
                AbnAmroConstants.JOINT_ACCOUNT_SUFFIX_PATTERN.matcher(accountHolderName);
        return matcher.replaceAll("");
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private String description;
    private String accountName;
    private String value;
    private String href;
    private String target;

    public String getDescription() {
        return description;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getValue() {
        return value;
    }

    public String getHref() {
        return href;
    }

    public String getTarget() {
        return target;
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accountdetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

/** Response may come wrapped or unwrapped. */
@JsonObject
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDetailsResponse {

    @JsonUnwrapped private AccountDetailsEntity accountUnwrapped;

    private AccountDetailsEntity account;

    public AccountDetailsEntity getAccount() {
        return ObjectUtils.firstNonNull(account, accountUnwrapped);
    }
}

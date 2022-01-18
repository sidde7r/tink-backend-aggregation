package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;

@Slf4j
@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountEntity {
    private String accountId;

    private String currency;

    @JsonProperty("AccountType")
    private String rawAccountType;

    @JsonProperty("AccountSubType")
    private String rawAccountSubType;

    private String nickname;

    private String description;

    private String switchStatus;

    @JsonProperty("Account")
    private List<AccountIdentifierEntity> identifiers = new ArrayList<>();

    @JsonIgnore
    public boolean hasAccountId() {
        if (Strings.isNullOrEmpty(accountId)) {
            log.warn("[AccountEntity] AccountId is empty");
            return false;
        }
        return true;
    }

    public boolean isNotSwitchedOutAccount() {
        if (!Strings.isNullOrEmpty(switchStatus)
                && UkOpenBankingV31Constants.SWITCHED_OUT_ACCOUNT.equals(switchStatus)) {
            log.warn("[AccountEntity] Account is switched out.");
            return false;
        }
        return true;
    }
}

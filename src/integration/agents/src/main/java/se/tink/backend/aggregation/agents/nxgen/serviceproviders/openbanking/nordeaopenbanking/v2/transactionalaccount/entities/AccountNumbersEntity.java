package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountNumbersEntity {
    private String value;
    @JsonProperty("_type")
    private String type;

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public Optional<AccountIdentifier> toTinkIdentifier() {
        if (value == null) {
            return Optional.empty();
        }

        if (NordeaBaseConstants.Account.ACCOUNT_NUMBER_SE.equalsIgnoreCase(type)) {
            if (AccountIdentifier.create(AccountIdentifier.Type.SE, value).isValid()) {
                return Optional.of(AccountIdentifier.create(AccountIdentifier.Type.SE, value));
            }
            return Optional.empty();
        } else if (NordeaBaseConstants.Account.ACCOUNT_NUMBER_IBAN.equalsIgnoreCase(type)) {
            return Optional.of(AccountIdentifier.create(AccountIdentifier.Type.IBAN, value));
        }

        return Optional.empty();
    }
}

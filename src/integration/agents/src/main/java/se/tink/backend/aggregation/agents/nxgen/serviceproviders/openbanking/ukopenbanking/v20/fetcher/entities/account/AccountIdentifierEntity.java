package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.UkOpenBankingV20Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class AccountIdentifierEntity {

    @JsonProperty("SchemeName")
    private UkOpenBankingV20Constants.AccountIdentifier identifierType;
    @JsonProperty("Identification")
    private String identification;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("SecondaryIdentification")
    private String secondaryIdentification;

    public String getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }

    public String getSecondaryIdentification() {
        return secondaryIdentification;
    }

    public Optional<AccountIdentifier> toAccountIdentifier(String accountName) {
        switch (identifierType) {
        case IBAN:
            IbanIdentifier ibanIdentifier = new IbanIdentifier(null, identification);
            ibanIdentifier.setName(accountName);
            return Optional.of(ibanIdentifier);
        case SORT_CODE_ACCOUNT_NUMBER:
            SortCodeIdentifier sortCodeIdentifier = new SortCodeIdentifier(identification);
            sortCodeIdentifier.setName(accountName);
            return Optional.of(sortCodeIdentifier);
        case PAN:
            return Optional.empty();
        default:
            throw new IllegalStateException(
                    String.format("Unknown identifier type: %s", identifierType)
            );
        }
    }
}

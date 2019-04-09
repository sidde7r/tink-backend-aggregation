package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
public class AccountIdentifierEntity {

    @JsonProperty("SchemeName")
    private UkOpenBankingConstants.ExternalAccountIdentification2Code identifierType;

    @JsonProperty("Identification")
    private String identification;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("SecondaryIdentification")
    private String secondaryIdentification;

    public boolean isSortCode() {
        return identifierType
                == UkOpenBankingConstants.ExternalAccountIdentification2Code
                        .SORT_CODE_ACCOUNT_NUMBER;
    }

    public String getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }

    public AccountIdentifier toAccountIdentifier(String accountName) {
        switch (identifierType) {
            case IBAN:
                IbanIdentifier ibanIdentifier = new IbanIdentifier(null, identification);
                ibanIdentifier.setName(accountName);
                return ibanIdentifier;
            case SORT_CODE_ACCOUNT_NUMBER:
                SortCodeIdentifier sortCodeIdentifier = new SortCodeIdentifier(identification);
                sortCodeIdentifier.setName(accountName);
                return sortCodeIdentifier;
            default:
                throw new IllegalStateException(
                        String.format("Unknown identifier type: %s", identifierType));
        }
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;

@JsonObject
@Data
public class AccountIdentifierEntity {
    @JsonProperty("SchemeName")
    private ExternalAccountIdentification4Code identifierType;

    @JsonProperty("Identification")
    private String identification;

    @JsonProperty("Name")
    private String ownerName;

    @JsonProperty("SecondaryIdentification")
    private String secondaryIdentification;

    // TODO: find OBBranchAndFinancialInstitutionIdentification5 definition
    @JsonProperty("Servicer")
    private String servicer;

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
            case BBAN:
                return Optional.of(new BbanIdentifier(identification));
            case SAVINGS_ROLL_NUMBER:
                return Optional.of(new NonValidIdentifier(identification));
            default:
                throw new IllegalStateException(
                        String.format("Unknown identifier type: %s", identifierType));
        }
    }
}

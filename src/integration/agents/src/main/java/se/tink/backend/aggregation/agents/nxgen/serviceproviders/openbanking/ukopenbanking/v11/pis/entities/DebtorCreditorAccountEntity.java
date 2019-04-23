package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorCreditorAccountEntity {
    @JsonProperty("SchemeName")
    private UkOpenBankingApiDefinitions.ExternalAccountIdentification2Code schemeName;

    @JsonProperty("Identification")
    private String identification; // Max34Text

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("Name")
    private String name; // Max70Text

    private DebtorCreditorAccountEntity(
            @JsonProperty("SchemeName")
                    UkOpenBankingApiDefinitions.ExternalAccountIdentification2Code schemeName,
            @JsonProperty("Identification") String identification,
            @JsonProperty("Name") String name) {
        this.schemeName = schemeName;
        this.identification = identification;
        this.name = name;
    }

    @JsonIgnore
    private static String cleanAccountNumber(String accountNumber) {
        return accountNumber.replaceAll("[^0-9]", "");
    }

    @JsonIgnore
    public static DebtorCreditorAccountEntity createSortCodeAccount(
            String identification, String name) {
        return new DebtorCreditorAccountEntity(
                UkOpenBankingApiDefinitions.ExternalAccountIdentification2Code
                        .SORT_CODE_ACCOUNT_NUMBER,
                cleanAccountNumber(identification),
                name);
    }

    @JsonIgnore
    public static DebtorCreditorAccountEntity createIbanAccount(
            String identification, String name) {
        return new DebtorCreditorAccountEntity(
                UkOpenBankingApiDefinitions.ExternalAccountIdentification2Code.IBAN,
                cleanAccountNumber(identification),
                name);
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@JsonObject
public class AccountEntity {
    @JsonProperty("_type")
    private String type;

    private String currency;
    private String value;

    public AccountEntity() {}

    public AccountEntity(String type, String currency, String value) {
        this.type = type;
        this.currency = currency;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }

    private AccountIdentifierType toTinkAccountType() {
        return NordeaAccountType.fromString(type).mapToTinkAccountType();
    }

    public AccountIdentifier toTinkAccountIdentifier() {
        switch (toTinkAccountType()) {
            case IBAN:
                return new IbanIdentifier(value);

            case SE:
                return new SwedishIdentifier(value);

            case SE_BG:
                return new BankGiroIdentifier(value);

            case SE_PG:
                return new PlusGiroIdentifier(value);

            case NO:
                return new NorwegianIdentifier(value);

            case DK:
                return new DanishIdentifier(value);

            default:
                throw new IllegalArgumentException(
                        "Unrecognized Tink account type " + toTinkAccountType());
        }
    }
}

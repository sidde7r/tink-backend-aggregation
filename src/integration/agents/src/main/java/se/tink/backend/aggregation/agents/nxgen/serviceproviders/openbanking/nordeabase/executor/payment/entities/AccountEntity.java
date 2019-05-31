package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.enums.NordeaAccountType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
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

    private AccountIdentifier.Type toTinkAccountType() {
        return NordeaAccountType.fromString(type).mapToTinkAccountType();
    }

    public AccountIdentifier toTinkAccountIdentifier() {
        AccountIdentifier accountIdentifier;
        switch (toTinkAccountType()) {
            case IBAN:
                accountIdentifier = new IbanIdentifier(value);
                break;

            case SE:
                accountIdentifier = new SwedishIdentifier(value);
                break;

            case SE_BG:
                accountIdentifier = new BankGiroIdentifier(value);
                break;

            case SE_PG:
                accountIdentifier = new PlusGiroIdentifier(value);
                break;

            default:
                throw new IllegalArgumentException(
                        "Unrecognized Tink account type " + toTinkAccountType());
        }
        return accountIdentifier;
    }
}

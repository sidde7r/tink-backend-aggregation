package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AccountEntity {
    private Double currentBalance;
    private String iban;
    private String number;
    private String currency;
    private String title;
    private String bic;
    private String type;

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public String getIban() {
        return iban;
    }

    public String getNumber() {
        return number;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTitle() {
        return title;
    }

    public String getBic() {
        return bic;
    }

    public String getType() {
        return type;
    }
}

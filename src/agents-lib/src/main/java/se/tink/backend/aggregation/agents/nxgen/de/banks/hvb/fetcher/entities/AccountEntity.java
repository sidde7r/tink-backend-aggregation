package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AccountEntity {
    @JsonProperty
    private Double currentBalance;
    @JsonProperty
    private String iban;
    @JsonProperty
    private String number;
    @JsonProperty
    private String currency;
    @JsonProperty
    private String title;
    @JsonProperty
    private String bic;
    @JsonProperty
    private String type;

    @JsonIgnore
    public Optional<Double> getCurrentBalance() {
        return Optional.ofNullable(currentBalance);
    }

    @JsonIgnore
    public Optional<String> getIban() {
        return Optional.ofNullable(iban);
    }

    @JsonIgnore
    public Optional<String> getNumber() {
        return Optional.ofNullable(number);
    }

    @JsonIgnore
    public Optional<String> getCurrency() {
        return Optional.ofNullable(currency);
    }

    @JsonIgnore
    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    @JsonIgnore
    public Optional<String> getBic() {
        return Optional.ofNullable(bic);
    }

    @JsonIgnore
    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }
}

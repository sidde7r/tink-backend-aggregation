package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

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

    public Optional<Double> getCurrentBalance() {
        return Optional.ofNullable(currentBalance);
    }

    public Optional<String> getIban() {
        return Optional.ofNullable(iban);
    }

    public Optional<String> getNumber() {
        return Optional.ofNullable(number);
    }

    public Optional<String> getCurrency() {
        return Optional.ofNullable(currency);
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<String> getBic() {
        return Optional.ofNullable(bic);
    }

    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }
}

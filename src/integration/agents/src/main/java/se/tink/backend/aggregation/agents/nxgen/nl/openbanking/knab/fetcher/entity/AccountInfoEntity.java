package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class AccountInfoEntity {

    private String bban;
    private String iban;

    @JsonIgnore
    public String getAccountNumber() {
        return Stream.of(bban, iban).filter(Objects::nonNull).findFirst().orElse("");
    }
}

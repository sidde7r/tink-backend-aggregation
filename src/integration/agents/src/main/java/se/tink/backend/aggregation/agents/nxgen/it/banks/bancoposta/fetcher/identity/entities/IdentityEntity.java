package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class IdentityEntity {
    private Claims claims;

    @JsonObject
    @Getter
    public static class Claims {
        @JsonProperty("name")
        private String firstName;

        private String surname;

        @JsonProperty("taxcode")
        private String ssn;

        @JsonProperty("birthdate")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate birthDate;
    }
}

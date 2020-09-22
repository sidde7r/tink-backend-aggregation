package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserEntity {
    @JsonProperty("id")
    public String id;

    @JsonProperty("username")
    public String username;

    @JsonProperty("name")
    public String name;

    @JsonProperty("ssn")
    public String ssn;

    @JsonProperty public String dateOfBirth;

    public String getName() {
        return name;
    }

    public String getSsn() {
        return ssn;
    }

    public LocalDate getDateOfBirth() {
        return LocalDate.parse(dateOfBirth);
    }
}

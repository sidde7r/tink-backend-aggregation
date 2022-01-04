package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class NickelAccountHolder {
    private NickelAddress address;
    private String firstName;
    private String lastName;
    private String title;

    @JsonIgnore
    public String getName() {
        return String.format("%s %s", firstName, lastName);
    }
}

package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.identitydata.entity.NameEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
@Getter
public class IdentityDataResponse {
    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("person_id")
    private String personId;

    @JsonProperty("preferred_name")
    private NameEntity nameEntity;

    public IdentityData toTinkIdentityData() {
        final String fullName = nameEntity.getName();

        return IdentityData.builder()
                .setSsn(personId)
                .setFullName(fullName)
                .setDateOfBirth(LocalDate.parse(birthDate))
                .build();
    }
}

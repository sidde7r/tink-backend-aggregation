package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.identitydata.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class IdentityDataResponse {
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthDate;

    private String firstName;
    private String lastName;
}

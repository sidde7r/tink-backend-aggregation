package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
import se.tink.agent.sdk.utils.serialization.local_date.LocalDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class IdentityResponse {

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthdayDate;

    private String firstName;
    private String lastName;
}

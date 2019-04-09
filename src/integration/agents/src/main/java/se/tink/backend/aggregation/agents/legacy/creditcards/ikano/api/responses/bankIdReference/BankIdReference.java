package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdReference;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.errors.UserErrorException;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdReference extends BaseResponse {
    @JsonProperty("Response")
    public Response response;

    public String getReference() throws UserErrorException {
        if (response != null && response.getReference() != null) {
            return response.getReference();
        }

        throw new UserErrorException(
                "Det angivna personnumret kunde inte identifieras, vänligen kontrollera personnumret och försök igen");
    }
}

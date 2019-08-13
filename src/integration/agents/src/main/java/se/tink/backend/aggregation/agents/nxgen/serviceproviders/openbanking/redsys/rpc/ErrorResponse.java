package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class ErrorResponse {
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    public static ErrorResponse fromResponse(HttpResponse response) {
        final String errorBody = response.getBody(String.class);
        return SerializationUtils.deserializeFromString(errorBody, ErrorResponse.class);
    }

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        return tppMessages.stream().anyMatch(msg -> msg.getCode().equalsIgnoreCase(errorCode));
    }
}

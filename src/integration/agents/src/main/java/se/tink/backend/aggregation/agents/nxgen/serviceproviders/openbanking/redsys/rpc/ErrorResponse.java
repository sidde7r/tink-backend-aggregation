package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class ErrorResponse extends TppMessageEntity {
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    public static ErrorResponse fromResponse(HttpResponse response) {
        final String errorBody = response.getBody(String.class);
        return SerializationUtils.deserializeFromString(errorBody, ErrorResponse.class);
    }

    @JsonIgnore
    private List<TppMessageEntity> getAllTppMessages() {
        final ArrayList<TppMessageEntity> messages = Lists.newArrayList();
        if (this.isValidMessageEntity()) {
            messages.add(this);
        }
        if (!Objects.isNull(tppMessages)) {
            messages.addAll(tppMessages);
        }
        return messages;
    }

    @JsonIgnore
    public boolean hasErrorCode(String errorCode) {
        return getAllTppMessages().stream()
                .anyMatch(msg -> msg.getCode().equalsIgnoreCase(errorCode));
    }
}

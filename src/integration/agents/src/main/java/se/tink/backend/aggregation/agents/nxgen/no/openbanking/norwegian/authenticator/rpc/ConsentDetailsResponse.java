package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateTimeDeserializer;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConsentDetailsResponse {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime validUntil;

    private String consentStatus;

    public boolean isValid() {
        return "valid".equalsIgnoreCase(consentStatus);
    }

    public boolean isExpired() {
        return "expired".equalsIgnoreCase(consentStatus);
    }

    public boolean isRevokedByPsu() {
        return "revokedByPsu".equalsIgnoreCase(consentStatus);
    }
}

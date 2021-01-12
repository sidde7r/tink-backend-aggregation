package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonObject
@EqualsAndHashCode
@ToString
public class FortisAuthData {

    private String username;

    private String clientNumber;

    private String ocraKey;

    private String deviceId;

    private String oathTokenId;

    private String cardFrameId;

    private FortisLegacyAuthData legacyAuthData;

    public boolean hasLegacyCredentials() {
        return legacyAuthData != null;
    }

    public boolean hasCredentials() {
        return ObjectUtils.allNotNull(ocraKey, deviceId, oathTokenId, cardFrameId);
    }

    public void clearLegacyAuthData() {
        this.legacyAuthData = null;
    }
}

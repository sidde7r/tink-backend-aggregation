package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonObject
@EqualsAndHashCode
@ToString
public class FortisLegacyAuthData {
    private String agreementId;
    private String password;
    private String deviceFingerprint;
    private String muid;
}

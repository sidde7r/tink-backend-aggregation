package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class CreateEnrollmentRequestEntity {

    private String devicePinningKey;

    private String mobileAppId;

    private String signingKey;

    private List<CredentialsEntity> credentials;

    private DescriptionEntity description;

    private ClientInfoEntity clientInfo;
}

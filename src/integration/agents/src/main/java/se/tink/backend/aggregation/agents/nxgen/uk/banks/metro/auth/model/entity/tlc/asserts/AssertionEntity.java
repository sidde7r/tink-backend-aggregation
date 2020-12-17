package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionData.OtpEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.asserts.AssertionData.PublicKeyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@JsonNaming(value = SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
public class AssertionEntity {
    private ActionType action;

    @JsonProperty("assert")
    private AssertionType assertionType;

    private String assertionId;

    @JsonProperty("fch")
    private String challenge;

    private MethodType method;

    @JsonProperty("data")
    private OtpEntity otp;

    private PublicKeyEntity publicKey;

    private String version;
}

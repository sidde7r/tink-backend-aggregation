package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class NemIdEnrollmentRequest {

    @JsonRawValue private final String data;
}

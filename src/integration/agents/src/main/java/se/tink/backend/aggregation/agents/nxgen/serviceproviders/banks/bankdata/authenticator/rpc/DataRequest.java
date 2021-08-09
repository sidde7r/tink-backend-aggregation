package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

// TODO: Lots of requests only contain data, maybe refactor to something like this?
@JsonObject
@RequiredArgsConstructor
public class DataRequest {

    @JsonRawValue private final String data;
}

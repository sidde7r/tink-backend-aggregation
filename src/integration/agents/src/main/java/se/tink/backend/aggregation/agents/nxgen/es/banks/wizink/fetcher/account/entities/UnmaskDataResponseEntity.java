package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnmaskDataResponseEntity extends BaseResponse {

    @JsonProperty("otp")
    private SessionEntity sessionEntity;

    public Optional<SessionEntity> getSessionEntity() {
        return Optional.ofNullable(sessionEntity);
    }
}

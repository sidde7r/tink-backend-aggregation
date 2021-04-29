package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.UnmaskDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Slf4j
@Getter
@JsonObject
public class UnmaskDataResponse {

    @JsonProperty("UnmaskDataResponse")
    private UnmaskDataResponseEntity unmaskDataResponseEntity;

    public Optional<String> getSessionId() {
        return Optional.ofNullable(unmaskDataResponseEntity)
                .flatMap(UnmaskDataResponseEntity::getSessionEntity)
                .flatMap(SessionEntity::getSessionId);
    }
}

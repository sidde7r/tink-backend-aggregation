package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.BusinessDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@JsonObject
public class RegisterPaymentRequest {
    private BusinessDataEntity businessData;
    private String language;
    private String signatureType;
}

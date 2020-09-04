package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.RequestBusinessDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class CreditorResponse extends AbstractResponse {
    private String bankName;
    private String creditorName;
    private RequestBusinessDataEntity requestBusinessData;
}

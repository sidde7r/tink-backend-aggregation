package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity.PayeesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ListPayeesResponse extends AbstractResponse {
    private String languageCode;
    private List<PayeesEntity> payees;
}

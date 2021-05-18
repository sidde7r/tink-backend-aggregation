package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.ResourcesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {
    private ConsentDataEntity data;
    private ResourcesEntity resources;
}

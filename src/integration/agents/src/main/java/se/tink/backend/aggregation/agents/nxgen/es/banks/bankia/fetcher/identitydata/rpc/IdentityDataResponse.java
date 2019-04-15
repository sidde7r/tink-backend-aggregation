package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity.AccessInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity.IdentityDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityDataResponse {
    @JsonProperty("datosPersonalesCliente")
    private IdentityDataEntity identityData;

    @JsonProperty("informacionAcceso")
    private AccessInformationEntity accessInformation;

    public IdentityDataEntity getIdentityData() {
        return identityData;
    }

    public AccessInformationEntity getAccessInformation() {
        return accessInformation;
    }
}

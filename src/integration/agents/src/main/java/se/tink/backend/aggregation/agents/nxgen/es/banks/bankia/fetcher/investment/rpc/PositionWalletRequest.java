package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.DataHomeModelEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionWalletRequest {

    @JsonProperty("datosInicioModel")
    private DataHomeModelEntity dataHomeModel;

    public PositionWalletRequest(DataHomeModelEntity dataHomeModel) {
        this.dataHomeModel = dataHomeModel;
    }

    public DataHomeModelEntity getDataHomeModel() {
        return dataHomeModel;
    }
}

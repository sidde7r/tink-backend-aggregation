package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment.entities.QualificationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PositionWalletResponse {

    @JsonProperty("datosRellamadaSalida")
    private String dataRedialExit;

    @JsonProperty("indicadorMasElementos")
    private boolean hasMoreIndicator;

    @JsonProperty("titulaciones")
    private List<QualificationEntity> qualificationList;

    public String getDataRedialExit() {
        return dataRedialExit;
    }

    public boolean isHasMoreIndicator() {
        return hasMoreIndicator;
    }

    public List<QualificationEntity> getQualificationList() {
        return qualificationList;
    }
}

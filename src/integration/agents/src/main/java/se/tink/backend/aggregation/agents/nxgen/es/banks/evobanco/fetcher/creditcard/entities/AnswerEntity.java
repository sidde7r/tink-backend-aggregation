package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AnswerEntity {
    @JsonProperty("acuerdo")
    private String agreement;

    @JsonProperty("masDatos")
    private String moreData;

    @JsonProperty("numeroLlamadas")
    private String callNumber;

    @JsonProperty("numeroRegistros")
    private String registersNumber;

    @JsonProperty("ListaMovimientosTarjeta")
    private List<ListMovementsCardEntity> listMovementsCard;

    public boolean getMoreData() {
        return "1".equals(moreData);
    }

    public List<ListMovementsCardEntity> getListMovementsCard() {
        return listMovementsCard;
    }
}

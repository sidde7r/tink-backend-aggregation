package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

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

    public String getMoreData() {
        return moreData;
    }

    public List<ListMovementsCardEntity> getListMovementsCard() {
        return listMovementsCard;
    }
}

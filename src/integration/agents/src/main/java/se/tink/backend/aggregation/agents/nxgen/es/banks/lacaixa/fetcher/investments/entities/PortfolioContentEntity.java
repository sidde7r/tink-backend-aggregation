package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioContentEntity {
    private String id;

    @JsonProperty("listaDepositos")
    private List<DepositEntity> depositList;

    @JsonProperty("masDatos")
    private boolean moreData;

    public String getId() {
        return id;
    }

    public List<DepositEntity> getDepositList() {
        return depositList;
    }

    public boolean isMoreData() {
        return moreData;
    }
}

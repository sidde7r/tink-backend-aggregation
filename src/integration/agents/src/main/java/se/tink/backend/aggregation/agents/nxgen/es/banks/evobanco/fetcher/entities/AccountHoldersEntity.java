package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountHoldersEntity {

    @JsonProperty("ListaIntervinientes")
    private List<AccountHolderEntity> accountHolderEntityList;

    @JsonProperty("masDatos")
    private String moreData;

    @JsonProperty("numeroRegistros")
    private String numberOfRecords;

    public List<AccountHolderEntity> getAccountHolderEntityList() {
        return accountHolderEntityList;
    }

    public String getMoreData() {
        return moreData;
    }

    public String getNumberOfRecords() {
        return numberOfRecords;
    }
}

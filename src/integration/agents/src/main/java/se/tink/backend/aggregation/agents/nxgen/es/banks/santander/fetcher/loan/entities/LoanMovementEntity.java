package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "methodResult")
public class LoanMovementEntity {
    private InfoEntity info;

    @JsonProperty("titular")
    private String mainHolder;

    /* S/N */
    @JsonProperty("finLista")
    private String fullList;

    @JsonProperty("repos")
    private ReposEntity repos;

    @JsonProperty("lista")
    private List<LoanOperationEntity> operationList;
}

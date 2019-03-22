package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.DgoNumberEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MovementDatesEntity {

    @JsonProperty("datosAux")
    private OperationDatesEntity dates;

    @JsonProperty("numSecMovimiento")
    private String numberOfMovements;

    @JsonProperty("operacionDGO")
    private DgoNumberEntity dgoNumberEntity;

    @JsonProperty("operacionBancaria")
    private BankOperationEntity bankOperation;
}

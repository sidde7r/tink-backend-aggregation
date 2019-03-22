package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement(name = "dato")
public class LoanOperationEntity {

    @JsonProperty("saldo")
    private AmountEntity saldoAfterOperation;

    @JsonProperty("impMovimiento")
    private AmountEntity movement;

    @JsonProperty("descMovimiento")
    private String movementDescription;

    @JsonProperty("datosMovimiento")
    private MovementDatesEntity movementDates;
}

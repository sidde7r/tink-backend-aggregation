package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SearchCriteriaEntity {
    @JsonProperty("cantidadUltimosMovimientos")
    private String quantityLastMovements;

    @JsonProperty("fechaOperacionDesde")
    private DateEntity dateOperationFrom;

    @JsonProperty("fechaOperacionHasta")
    private DateEntity operationDateUntil;

    @JsonProperty("tipoCriterioImporte")
    private String typeCriterionAmount;

    @JsonProperty("importeConsulta")
    private String consultationAmount;

    public void setQuantityLastMovements(String quantityLastMovements) {
        this.quantityLastMovements = quantityLastMovements;
    }

    public void setDateOperationFrom(DateEntity dateOperationFrom) {
        this.dateOperationFrom = dateOperationFrom;
    }

    public void setOperationDateUntil(DateEntity operationDateUntil) {
        this.operationDateUntil = operationDateUntil;
    }

    public void setTypeCriterionAmount(String typeCriterionAmount) {
        this.typeCriterionAmount = typeCriterionAmount;
    }

    public void setConsultationAmount(String consultationAmount) {
        this.consultationAmount = consultationAmount;
    }
}

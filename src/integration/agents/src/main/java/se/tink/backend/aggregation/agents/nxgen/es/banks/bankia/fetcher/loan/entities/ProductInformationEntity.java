package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductInformationEntity {
    @JsonProperty("fechaFormalizacion")
    private DateEntity initialDate;

    @JsonProperty("nombreProductoContratado")
    private String productName;

    @JsonProperty("identificadorCuentaAsociada")
    private AccountIdentifierEntity associatedAccountIdentifier;

    @JsonProperty("fechaFinContratoReal")
    private DateEntity endDate;

    public DateEntity getInitialDate() {
        return initialDate;
    }

    public String getProductName() {
        return productName;
    }
}

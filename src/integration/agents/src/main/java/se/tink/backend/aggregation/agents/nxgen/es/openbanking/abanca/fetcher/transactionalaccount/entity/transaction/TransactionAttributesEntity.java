package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAttributesEntity {

    @JsonProperty("fechaDeOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date operationDate;

    @JsonProperty("fechaContable")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date accountingDate;

    @JsonProperty("fechaValor")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date valueDate;

    @JsonProperty("codigoDeReferencia")
    private String referenceCode;

    @JsonProperty("codigoDeTipoDeMovimiento")
    private String movementTypeCode;

    @JsonProperty("codigoSubtipoDeMovimiento")
    private String movementTypeSubcode;

    @JsonProperty("concepto")
    private String concept;

    @JsonProperty("importe")
    private AmountEntity imported;

    @JsonProperty("saldoDespuesDeLaOperacion")
    private AmountEntity balanceAfterOperation;

    @JsonProperty("numeroDeOperacion")
    private String operationNumber;

    @JsonProperty("tieneJustificante")
    private Boolean hasProof;

    public AmountEntity getBalanceAfterOperation() {
        return balanceAfterOperation;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public String getConcept() {
        return concept;
    }
}

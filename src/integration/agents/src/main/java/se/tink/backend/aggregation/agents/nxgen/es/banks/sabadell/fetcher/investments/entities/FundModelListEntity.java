package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundModelListEntity {
    @JsonProperty("codigoFondo")
    private String fundCode;

    @JsonProperty("codisin")
    private String isinCode;

    @JsonProperty("comisionGestora")
    private String managementCommission;

    @JsonProperty("cuentaParticipe")
    private String participateAccount;

    @JsonProperty("desFondoClase")
    private String desFundClass;

    @JsonProperty("descripcion")
    private String description;

    @JsonProperty("fechaValorLiq")
    private String dateValueLiq;

    @JsonProperty("fondoClase")
    private String fundClass;

    private String fundFileName;
    private String fundType;

    @JsonProperty("importeOpPend")
    private AmountEntity importsOpPend;

    @JsonProperty("indiceOperacion")
    private String operationIndex;

    @JsonProperty("minimumInvestment")
    private String inversionMinima;

    @JsonProperty("liquidacion")
    private String liquidation;

    @JsonProperty("numParticipaciones")
    private String numParticipations;

    @JsonProperty("revalorizacion")
    private String revalorization;

    @JsonProperty("saldoActual")
    private AmountEntity currentBalance;

    @JsonProperty("saldoMinimo")
    private String minimumBalance;

    @JsonProperty("saldoNeto")
    private AmountEntity netBalance;

    @JsonProperty("valorLiquidativo")
    private AmountEntity liquidationValue;
}

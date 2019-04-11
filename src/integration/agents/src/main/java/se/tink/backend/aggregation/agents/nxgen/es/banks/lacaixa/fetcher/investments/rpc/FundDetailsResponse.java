package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundDetailsResponse {
    @JsonProperty("titulares")
    private List<String> holders;

    @JsonProperty("fondoNombre")
    private String fundName;

    @JsonProperty("expediente")
    private String accountId;

    @JsonProperty("cuenta")
    private String account;

    private String fechaAlta;
    private String entidadGestora;
    private String entidadDeposito;
    private BalanceEntity inversionMinimaSucesiva;
    private String fechaValoracionActualFondo;

    @JsonProperty("saldoActual")
    private BalanceEntity currentBalance;

    @JsonProperty("participaciones")
    private double quantity;

    @JsonProperty("valorParticipacion")
    private BalanceEntity marketPrice;

    private double participacionesRetenidas;

    @JsonProperty("inversionInicial")
    private BalanceEntity initialInvestment;

    @JsonProperty("plusvaliaInicial")
    private BalanceEntity valueChange;

    @JsonProperty("porcentajePlusvaliaInicial")
    private double valueChangePercent;

    private boolean mostrarEvolucionFondo;
    private String refValEmpresaGestora;
    private String refValCodigoFondo;
    private String refValExpediente;
    private String refValCuenta;

    @JsonIgnore
    public Instrument toTinkInstruments() {
        Instrument instrument = new Instrument();
        instrument.setProfit(valueChange.doubleValue());
        instrument.setPrice(marketPrice.doubleValue());
        instrument.setCurrency(marketPrice.getCurrency());
        instrument.setType(Instrument.Type.FUND);
        instrument.setQuantity(quantity);
        instrument.setName(fundName);
        instrument.setMarketValue(currentBalance.doubleValue());
        instrument.setUniqueIdentifier(getUniqueIdentifier());

        return instrument;
    }

    private String getUniqueIdentifier() {
        return fundName.replaceAll("[^\\dA-Za-z]", "");
    }
}

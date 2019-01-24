package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.PositionDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class DepositEntity {
    private String id;
    private String codemian;
    @JsonProperty("nombre")
    private String name;
    @JsonProperty("numeroTitulos")
    private int quantity;
    private boolean cotizaEnTitulos;
    @JsonProperty("valoracion")
    private BalanceEntity currentValue;
    private boolean mostrarValoracion;
    @JsonProperty("plusvalia")
    private BalanceEntity valueChange;
    private boolean mostrarPlusvaliaRentabilidad;
    private BalanceEntity ultimaCotizacion;
    private boolean mostrarUltimaCotizacion;
    private boolean permiteCompra;
    private boolean permiteVenta;
    private boolean permiteDetalle;
    private boolean existeMensajeIncumplimientos;
    private String mensajeIncumplimientos;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public BalanceEntity getCurrentValue() {
        return currentValue;
    }

    public BalanceEntity getValueChange() {
        return valueChange;
    }

    public Instrument toTinkInstrument(PositionDetailsResponse positionDetailsResponse, Map<String, String> contractToCode) {
        return positionDetailsResponse.toTinkInstrument(name, contractToCode);
    }
}

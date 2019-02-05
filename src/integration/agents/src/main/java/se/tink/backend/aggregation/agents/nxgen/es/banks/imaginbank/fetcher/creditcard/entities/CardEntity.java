package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    private String aliasTarjeta;
    @JsonProperty("claveTarjeta")
    private String cardKey;
    private String marcaTarjetaDescripcion;
    private NumeroContratoEntity numeroContrato;
    private String numeroContrato28;
    private String numeroTarjeta;
    private String tipoCargoTarjeta;
    private String binTarjeta;
    private String affinityTarjeta;
    private String descripcionTarjeta;
    private String formatoTap;
    private String permiteHCE;
    private String permiteTandem;
    private DateEntity fechaContratacion;
    private DateEntity fechaCaducidad;
    private String codigoEstado;
    private String permiteModificarPin;
    private String tipoTarjetaMovil;
    private BalanceEntity saldoDisponible;
    private BalanceEntity saldoDispuesto;
    private BalanceEntity saldoLimite;
    private String refValInformacionContrato;
    private String refValInformacionTarjeta;
    private String identificadorImagen;
    private boolean indicadorTarjetaCyberwallet;
    private boolean recuperarSaldoPrepago;
    private BalanceEntity saldoDisponiblePrepago;
    private String titularidad;
    private String personaBeneficiario;
    private BalanceEntity saldoDispuestoYRetenido;
    private BalanceEntity saldoRetenido;
    private String colectivo;
    private String tipoContratacion;
    private String idPeticion;
    private boolean tarjetaFinancieraMovil;
    private String bine9Cifras;
    private String contrato2;
    private String plastico;
    private String tipoTarjeta;

    public String getCardKey() {
        return cardKey;
    }
}

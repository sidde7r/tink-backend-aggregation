package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.account.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;

/*
Fields available, not used
"signoPosicion": "H",
"fecSald2": null,
"posicion2": 0,
"signoPosicion2": "H",
"monedaPosicion2": "",
"fecSald3": null,
"posicion3": 0,
"signoPosicion3": "",
"monedaPosicion3": "",
"fecSald4": null,
"posicion4": 0,
"signoPosicion4": "",
"monedaPosicion4": "",
"marca": 0,
"modalidad": 1,
"situacion": 0,
"activacion": 0,
"numIntPrimerTitular": 96952780,
"nomTitContrato": "KARL ALFRED",
"numIntSegundoTitular": 0,
"nomTitContrato2": "",
"numIntTercerTitular": 0,
"nomTitContrato3": "",
"numIntCuartoTitular": 0,
"nomTitContrato4": "",
"indicatrans1": 0,
"indicapago": 1,
"indica1": 0,
"indica2": 0,
"indica3": 0,
"alias": "",
"indDigDoc": "S"
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularCuenta {
    private long numIntContrato;
    private String iban;
    private String banco;
    private String sucursal;
    private String idExternaContrato;
    private Date fecSald;
    private double posicion;
    private String monedaPosicion;
    private String tipoContrato;
    private int producto;

    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder(formatAccountNumber(), Amount.inEUR(posicion))
                .setName(tipoContrato)
                .setUniqueIdentifier(constructUniqueIdentifier())
                .build();
    }

    // Format iban spanish style
    private String formatAccountNumber() {
        String externalId = idExternaContrato.replaceAll(" ", "");
        if (externalId.length() == 12) {
            return String.format("%s %s %s %s %s %s",
                    iban, banco, sucursal,
                    externalId.substring(0, 4),
                    externalId.substring(4, 8),
                    externalId.substring(8));
        }

        return idExternaContrato;
    }

    // using internal identifier
    private String constructUniqueIdentifier() {
        return String.format("%d", numIntContrato);
    }
    public long getNumIntContrato() {
        return numIntContrato;
    }

    public void setNumIntContrato(long numIntContrato) {
        this.numIntContrato = numIntContrato;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getIdExternaContrato() {
        return idExternaContrato;
    }

    public void setIdExternaContrato(String idExternaContrato) {
        this.idExternaContrato = idExternaContrato;
    }

    public Date getFecSald() {
        return fecSald;
    }

    public void setFecSald(Date fecSald) {
        this.fecSald = fecSald;
    }

    public double getPosicion() {
        return posicion;
    }

    public void setPosicion(double posicion) {
        this.posicion = posicion;
    }

    public String getMonedaPosicion() {
        return monedaPosicion;
    }

    public void setMonedaPosicion(String monedaPosicion) {
        this.monedaPosicion = monedaPosicion;
    }

    public String getTipoContrato() {
        return tipoContrato;
    }

    public void setTipoContrato(String tipoContrato) {
        this.tipoContrato = tipoContrato;
    }

    public int getProducto() {
        return producto;
    }

    public void setProducto(int producto) {
        this.producto = producto;
    }
}

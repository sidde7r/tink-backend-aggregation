package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class GenericCardEntity {

    @JsonProperty("numeroTarjeta")
    private String cardNumber;
    private String bin;
    private String affinity;
    @JsonProperty("descripcion")
    private String description;
    @JsonProperty("indTarjetaEmpresa")
    private String indBusinessCard;
    @JsonProperty("fechaAlta")
    private String dateHigh;
    @JsonProperty("deposito")
    private StorageAreaEntity storageArea;
    @JsonProperty("estado")
    private String state;
    @JsonProperty("tipoTarjeta")
    private String cardType;
    @JsonProperty("identificadorImagen")
    private String identifierImage;
    private String indpre;
    @JsonProperty("tipoTAP")
    private String tittap;
    @JsonProperty("tipoTMOV")
    private String types;
    private String tandem;
    private String hceable;
    @JsonProperty("idColectivo")
    private String collectiveId;
    @JsonProperty("contrato")
    private String contract;
    @JsonProperty("marca")
    private String brand;
    @JsonProperty("servicioKeep")
    private String keepService;
    @JsonProperty("seguroSaldoPendiente")
    private String safeBalancePending;
    @JsonProperty("nomTitular")
    private String titularName;
    private String cyberWallet;
    @JsonProperty("tipoTarMovil")
    private String tarMovilType;
    @JsonProperty("datosLiquidacion")
    private LiquidationDataEntity liquidationData;
    @JsonProperty("refValIdtarjeta")
    private String valIdtarjeta;
    private String refValNumtarjeta;
    @JsonProperty("refValIdCuenta")
    private String valIdAccount;
    @JsonProperty("refValIdContrato")
    private String refValIdContract;
    @JsonProperty("refValIdSolicitudTarjeta")
    private String valValIdRequestCard;
    @JsonProperty("indTitularidad")
    private String indOwnership;
    @JsonProperty("numPersonaBeneficiario")
    private String numBeneficiaryPerson;
    @JsonProperty("limiteTarjeta")
    private String limitCard;
    @JsonProperty("bine9Cifras")
    private String bine9Figures;
    private String contrato2;
    @JsonProperty("plastico")
    private String plastic;
    @JsonProperty("indFraccionable")
    private String inFraccionable;
    @JsonProperty("dispositivoTarMovil")
    private String tarMovilDevice;
    @JsonProperty("idProveedorTarMovil")
    private String idMobileTarSupplier;

    public CreditCardAccount toTinkCard() {
        return CreditCardAccount.builderFromFullNumber(cardNumber, description)
                .setBalance(this.getBalance())
                .setAvailableCredit(this.getAvailableCredit())
                .setBankIdentifier(refValNumtarjeta)
                .build();
    }

    public boolean isCreditCard() {
        // P = prepaid (treated as credit card, since it's not a debit card linked to one of the checking accounts)
        // D = debit
        // C = credit
        return "C".equalsIgnoreCase(cardType) || "P".equalsIgnoreCase(cardType);
    }

    private Amount getAvailableCredit() {
        return "P".equalsIgnoreCase(cardType) ?
                liquidationData.getPrepaidAmount() :
                liquidationData.getAvailableCredit();
    }

    private Amount getBalance() {
        return liquidationData.getBalance();
    }

}

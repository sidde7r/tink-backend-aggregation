package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class GenericCardEntity {
    private static final Logger LOG = LoggerFactory.getLogger(GenericCardEntity.class);

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
    private GenericLiquidationDataEntity liquidationData;

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

    @JsonIgnore
    public CreditCardAccount toTinkCard(ExactCurrencyAmount balance) {
        final ExactCurrencyAmount availableCredit;
        if (isPrepaidCard()) {
            availableCredit = ExactCurrencyAmount.zero(LaCaixaConstants.CURRENCY);
        } else {
            availableCredit = getAvailableCredit();
        }

        return CreditCardAccount.builderFromFullNumber(cardNumber, description)
                .setExactBalance(balance)
                .setExactAvailableCredit(availableCredit)
                .setBankIdentifier(refValNumtarjeta)
                .build();
    }

    @JsonIgnore
    public boolean isCreditCard() {
        // P = prepaid (treated as credit card, since it's not a debit card linked to one of the
        // checking accounts)
        // D = debit
        // C = credit
        return "C".equalsIgnoreCase(cardType) || "P".equalsIgnoreCase(cardType);
    }

    @JsonInclude
    public boolean isPrepaidCard() {
        return "P".equalsIgnoreCase(cardType);
    }

    @JsonIgnore
    public ExactCurrencyAmount getAvailableCredit() {
        final BigDecimal availableCredit = liquidationData.getAvailableCredit();
        if (Objects.isNull(availableCredit)) {
            LOG.warn("Available credit is null: {}", SerializationUtils.serializeToString(this));
            return ExactCurrencyAmount.zero(LaCaixaConstants.CURRENCY);
        } else {
            return ExactCurrencyAmount.of(availableCredit, LaCaixaConstants.CURRENCY);
        }
    }

    public String getRefValIdContract() {
        return refValIdContract;
    }

    public String getContract() {
        return contract;
    }

    @JsonIgnore
    public BigDecimal getBalance() {
        return liquidationData.getBalance();
    }
}

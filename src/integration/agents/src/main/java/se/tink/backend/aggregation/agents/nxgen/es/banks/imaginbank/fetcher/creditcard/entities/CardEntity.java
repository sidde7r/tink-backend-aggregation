package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardEntity {
    @JsonProperty("aliasTarjeta")
    private String cardAlias;

    @JsonProperty("claveTarjeta")
    private String cardKey;

    private String marcaTarjetaDescripcion;
    private NumeroContratoEntity numeroContrato;
    private String numeroContrato28;

    @JsonProperty("numeroTarjeta")
    private String cardNumber;

    @JsonProperty("tipoCargoTarjeta")
    private String cardType;

    private String binTarjeta;
    private String affinityTarjeta;

    @JsonProperty("descripcionTarjeta")
    private String cardDescription;

    private String formatoTap;
    private String permiteHCE;
    private String permiteTandem;
    private DateEntity fechaContratacion;
    private DateEntity fechaCaducidad;
    private String codigoEstado;
    private String permiteModificarPin;
    private String tipoTarjetaMovil;

    @JsonProperty("saldoDisponible")
    private BalanceEntity availbaleCredit;

    @JsonProperty("saldoDispuesto")
    private BalanceEntity balance;

    @JsonProperty("saldoLimite")
    private BalanceEntity creditLimit;

    private String refValInformacionContrato;
    private String refValInformacionTarjeta;
    private String identificadorImagen;
    private boolean indicadorTarjetaCyberwallet;
    private boolean recuperarSaldoPrepago;
    private BalanceEntity saldoDisponiblePrepago;
    private String titularidad;

    @JsonProperty("titularTarjeta")
    private String cardHolder;

    private String personaBeneficiario;
    private BalanceEntity saldoDispuestoYRetenido;
    private BalanceEntity saldoRetenido;
    private String colectivo;
    private String tipoContratacion;
    private String idPeticion;

    @JsonProperty("tarjetaFinancieraMovil")
    private boolean mobileCard;

    private String bine9Cifras;
    private String contrato2;
    private String plastico;
    private String tipoTarjeta;

    @JsonProperty("formatoLargo")
    private String formattedAccountNumber;

    public String getCardKey() {
        return cardKey;
    }

    @JsonIgnore
    public boolean isCreditCard() {
        return ImaginBankConstants.CreditCard.CREDIT.equalsIgnoreCase(cardType) && !mobileCard;
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard() {
        return CreditCardAccount.builder(numeroContrato28)
                .setAccountNumber(formattedAccountNumber)
                .setExactAvailableCredit(
                        ExactCurrencyAmount.of(
                                availbaleCredit.getValue(), availbaleCredit.getCurrency()))
                .setExactBalance(ExactCurrencyAmount.of(balance.getValue(), balance.getCurrency()))
                .setHolderName(getHolderName())
                .setName(getName())
                .setBankIdentifier(cardKey)
                .build();
    }

    @JsonIgnore
    private HolderName getHolderName() {
        return Optional.ofNullable(cardHolder).map(HolderName::new).orElse(null);
    }

    @JsonIgnore
    private String getName() {
        if (Strings.isNullOrEmpty(cardAlias)) {
            return String.format("%s %s", cardDescription, getPartialCardNumber());
        }

        return cardAlias;
    }

    @JsonIgnore
    private String getPartialCardNumber() {
        int cardNumberLength = cardNumber.length();
        if (cardNumberLength > 4) {
            return cardNumber.substring(cardNumberLength - 4);
        }

        return cardNumber;
    }
}

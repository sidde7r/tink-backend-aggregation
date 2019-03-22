package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.GeneralInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.SubProductTypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class CardEntity {
    @JsonProperty("comunes")
    private GeneralInfoEntity generalInfo;

    @JsonProperty("pan")
    private String cardNumber;

    @JsonProperty("descTipoTarjeta")
    private String cardType;

    @JsonProperty("descSituacionContratoTarjeta") // Active or not
    private String contractDescription;

    @JsonProperty("impSaldoDispuesto")
    private AmountEntity disposableBalance;

    @JsonProperty("impDisponible")
    private AmountEntity disposable;

    @JsonProperty("importeSalDisponibleContravalor")
    private AmountEntity disposableBalanceCountervalue;

    @JsonProperty("importeSalDisptoContravalor")
    private AmountEntity disposableCounterValue;

    @JsonProperty("permiteDineroDirecto")
    private String allowsDirectMoney;

    @JsonProperty("subtipoProd")
    private SubProductTypeEntity subProductType;

    @JsonProperty("indECashPrepago")
    private String eCashPrepaidIndicator;

    @JsonProperty("tipoInterv")
    private int typeInterv;

    public GeneralInfoEntity getGeneralInfo() {
        return generalInfo;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getContractDescription() {
        return contractDescription;
    }

    public AmountEntity getDisposableBalance() {
        return disposableBalance;
    }

    public AmountEntity getDisposable() {
        return disposable;
    }

    public AmountEntity getDisposableBalanceCountervalue() {
        return disposableBalanceCountervalue;
    }

    public AmountEntity getDisposableCounterValue() {
        return disposableCounterValue;
    }

    public String getAllowsDirectMoney() {
        return allowsDirectMoney;
    }

    public SubProductTypeEntity getSubProductType() {
        return subProductType;
    }

    public String geteCashPrepaidIndicator() {
        return eCashPrepaidIndicator;
    }

    public int getTypeInterv() {
        return typeInterv;
    }
}

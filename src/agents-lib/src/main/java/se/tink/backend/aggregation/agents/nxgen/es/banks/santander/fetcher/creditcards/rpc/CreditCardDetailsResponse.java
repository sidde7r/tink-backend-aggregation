package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
@XmlRootElement(name = "methodResult")
public class CreditCardDetailsResponse {
    private InfoEntity info;
    @JsonProperty("titularContr")
    private String contractHolder;
    @JsonProperty("beneficiarioTarjeta")
    private String cardHolder;
    @JsonProperty("fechaCaducidad")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date validUntil;
    @JsonProperty("cuentaCargo")
    private ContractEntity creditAccount;
    @JsonProperty("limiteCredito")
    private AmountEntity creditLimit;
    @JsonProperty("importeSalDispto")
    private AmountEntity balance;
    @JsonProperty("importeSalDisponible")
    private AmountEntity availableBalance;
    @JsonProperty("limiteCompra")
    private AmountEntity purchaseLimit;
    @JsonProperty("descTipoTarjeta")
    private String cardType;
    @JsonProperty("descCuentaCargo")
    private String formattedCreditAccount;
    @JsonProperty("limiteOffLine")
    private AmountEntity offLineLimit;
    @JsonProperty("limiteOnLine")
    private AmountEntity onLineLimit;
    @JsonProperty("tipoSituacion")
    private String situationType;

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard(String userDataXml, CardEntity card) {
        return CreditCardAccount.builderFromFullNumber(card.getCardNumber())
                .setBalance(balance.getTinkAmount())
                .setAvailableCredit(availableBalance.getTinkAmount())
                .setHolderName(new HolderName(getCardHolder()))
                .setName(card.getGeneralInfo().getAlias())
                .putInTemporaryStorage(SantanderEsConstants.Storage.USER_DATA_XML, userDataXml)
                .putInTemporaryStorage(SantanderEsConstants.Storage.CARD_ENTITY, card)
                .build();
    }

    public InfoEntity getInfo() {
        return info;
    }

    public String getCardHolder() {
        return cardHolder != null ? cardHolder.trim() : "";
    }

    public ContractEntity getCreditAccount() {
        return creditAccount;
    }

    public AmountEntity getCreditLimit() {
        return creditLimit;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public String getCardType() {
        return cardType;
    }

    public String getFormattedCreditAccount() {
        return formattedCreditAccount;
    }
}

package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardInfoEntity {

    @JsonProperty("cardInfo")
    private CardInfoEntity cardInfo;

    @JsonProperty("cardImage")
    private String cardImage;

    @JsonProperty("progressCard")
    private double progressCard;

    @JsonProperty("repaymentMethod")
    private String repaymentMethod;

    @JsonProperty("holderName")
    private String holderName;

    @JsonProperty("accountsForIncreaseAvailableAmount")
    private List<String> accountsForIncreaseAvailableAmount;

    @JsonProperty("cardImagePlaceholder")
    private String cardImagePlaceholder;

    @JsonProperty("cardBlockingText2")
    private String cardBlockingText2;

    @JsonProperty("resetVirtualCardPaymentCount")
    private boolean resetVirtualCardPaymentCount;

    @JsonProperty("replacementCardSupported")
    private boolean replacementCardSupported;

    @JsonProperty("posEnabled")
    private boolean posEnabled;

    @JsonProperty("cardBlockingText1")
    private String cardBlockingText1;

    @JsonProperty("progressCardAuthorized")
    private double progressCardAuthorized;

    @JsonProperty("availableAmount")
    private AmountEntity availableAmount;

    @JsonProperty("balance")
    private BalanceEntity balance;

    @JsonProperty("atmEnabled")
    private boolean atmEnabled;

    @JsonProperty("cardBlockingButton2")
    private CardBlockingButton2Entity cardBlockingButton2;

    @JsonProperty("cardBlockingButton1")
    private CardBlockingButton1Entity cardBlockingButton1;

    @JsonProperty("panSuffix")
    private String panSuffix;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("cardLimit")
    private CardLimitEntity cardLimit;

    @JsonProperty("onlineShoppingEnabled")
    private boolean onlineShoppingEnabled;

    public CardInfoEntity getCardInfo() {
        return cardInfo;
    }

    public String getCardImage() {
        return cardImage;
    }

    public double getProgressCard() {
        return progressCard;
    }

    public String getRepaymentMethod() {
        return repaymentMethod;
    }

    public String getHolderName() {
        return holderName;
    }

    public List<String> getAccountsForIncreaseAvailableAmount() {
        return accountsForIncreaseAvailableAmount;
    }

    public String getCardImagePlaceholder() {
        return cardImagePlaceholder;
    }

    public String getCardBlockingText2() {
        return cardBlockingText2;
    }

    public boolean isResetVirtualCardPaymentCount() {
        return resetVirtualCardPaymentCount;
    }

    public boolean isReplacementCardSupported() {
        return replacementCardSupported;
    }

    public boolean isPosEnabled() {
        return posEnabled;
    }

    public String getCardBlockingText1() {
        return cardBlockingText1;
    }

    public double getProgressCardAuthorized() {
        return progressCardAuthorized;
    }

    public AmountEntity getAvailableAmount() {
        return availableAmount;
    }

    public BalanceEntity getBalance() {
        return balance;
    }

    public boolean isAtmEnabled() {
        return atmEnabled;
    }

    public CardBlockingButton2Entity getCardBlockingButton2() {
        return cardBlockingButton2;
    }

    public CardBlockingButton1Entity getCardBlockingButton1() {
        return cardBlockingButton1;
    }

    public String getPanSuffix() {
        return panSuffix;
    }

    public String getBrand() {
        return brand;
    }

    public CardLimitEntity getCardLimit() {
        return cardLimit;
    }

    public boolean isOnlineShoppingEnabled() {
        return onlineShoppingEnabled;
    }
}

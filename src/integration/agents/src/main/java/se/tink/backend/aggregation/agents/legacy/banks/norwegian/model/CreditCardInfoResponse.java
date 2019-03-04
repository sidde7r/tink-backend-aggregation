package se.tink.backend.aggregation.agents.banks.norwegian.model;

import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardInfoResponse {
    private List<AccountListEntity> accountList;
    private String accountNo;
    private HashMap<String, Double> amountToPay;
    private HashMap<String, String> amountToPayFormatted;
    private boolean isApp;
    private String myPageUrl;
    private int region;
    private String selectedPeriod;
    private boolean showAccountList;
    private boolean showCurrencyAmount;
    private boolean showForeignAccountInfo;
    private boolean showMerchantInfo;
    private boolean showPdfStatement;
    private StringsEntity strings;
    private String tempUnavailableText;

    public List<AccountListEntity> getAccountList() {
        return accountList;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public HashMap<String, Double> getAmountToPay() {
        return amountToPay;
    }

    public HashMap<String, String> getAmountToPayFormatted() {
        return amountToPayFormatted;
    }

    public boolean isApp() {
        return isApp;
    }

    public String getMyPageUrl() {
        return myPageUrl;
    }

    public int getRegion() {
        return region;
    }

    public String getSelectedPeriod() {
        return selectedPeriod;
    }

    public boolean isShowAccountList() {
        return showAccountList;
    }

    public boolean isShowCurrencyAmount() {
        return showCurrencyAmount;
    }

    public boolean isShowForeignAccountInfo() {
        return showForeignAccountInfo;
    }

    public boolean isShowMerchantInfo() {
        return showMerchantInfo;
    }

    public boolean isShowPdfStatement() {
        return showPdfStatement;
    }

    public StringsEntity getStrings() {
        return strings;
    }

    public String getTempUnavailableText() {
        return tempUnavailableText;
    }
}

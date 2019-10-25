package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.common.entity;

import java.util.HashMap;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoResponse {
    private List<AccountListEntity> accountList;
    private String accountNo;
    private HashMap<String, Double> amountToPay;
    private HashMap<String, String> amountToPayFormatted;
    private boolean isApp;
    private String myPageUrl;
    private String region;
    private String selectedPeriod;
    private boolean showAccountList;
    private boolean showCurrencyAmount;
    private boolean showForeignAccountInfo;
    private boolean showMerchantInfo;
    private boolean showPdfStatement;
    private String tempUnavailableText;

    public String getAccountNo() {
        return accountNo;
    }
}

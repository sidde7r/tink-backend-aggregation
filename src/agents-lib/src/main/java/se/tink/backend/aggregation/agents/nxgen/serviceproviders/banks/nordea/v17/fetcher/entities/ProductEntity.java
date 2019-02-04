package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ProductEntity {
    private static final String personalAccountClearingNumber = "3300";
    private static final Set<String> personalAccountCodes = Sets.newHashSet("SE0000", "SE0200", "SE0300");

    private Map<String, Object> accountType = new HashMap<String, Object>();
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double balance;
    private Map<String, Object> branchId = new HashMap<String, Object>();
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardGroup;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> isMainCard = new HashMap<String, Object>();
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String nickName;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productName;
    private Map<String, Object> productCode = new HashMap<String, Object>();
    private Map<String, Object> productId = new HashMap<String, Object>();
    private Map<String, Object> productNumber = new HashMap<String, Object>();
    private Map<String, Object> productType = new HashMap<String, Object>();
    private Map<String, Object> productTypeExtension = new HashMap<String, Object>();
    private Map<String, Object> warningCode = new HashMap<String, Object>();

    public Map<String, Object> getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance != null ? balance : 0;
    }

    public Map<String, Object> getBranchId() {
        return branchId;
    }

    public String getCardGroup() {
        return cardGroup;
    }

    public Optional<String> getCurrency() {
        return Optional.ofNullable(Strings.emptyToNull(currency));
    }

    public Optional<Amount> getBalanceAmount() {
        return getCurrency().map(currency -> new Amount(currency, getBalance()));
    }

    public Optional<Amount> getNegativeBalanceAmount() {
        return getCurrency().map(currency -> new Amount(currency, -1 * getBalance()));
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public Map<String, Object> getIsMainCard() {
        return isMainCard;
    }

    public Optional<String> getNickName() {
        return Optional.ofNullable(nickName);
    }

    public String getProductName() {
        return productName;
    }

    public Map<String, Object> getProductCode() {
        return productCode;
    }

    public Map<String, Object> getProductId() {
        return productId;
    }

    public Map<String, Object> getProductNumber() {
        return productNumber;
    }

    public Map<String, Object> getProductType() {
        return productType;
    }

    public Map<String, Object> getProductTypeExtension() {
        return productTypeExtension;
    }

    public Map<String, Object> getWarningCode() {
        return warningCode;
    }

    public void setAccountType(Map<String, Object> accountType) {
        this.accountType = accountType;
    }

    public void setBranchId(Map<String, Object> branchId) {
        this.branchId = branchId;
    }

    public void setCardGroup(String cardGroup) {
        this.cardGroup = cardGroup;
    }

    public void setFundsAvailable(Map<String, Object> fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public void setIsMainCard(Map<String, Object> isMainCard) {
        this.isMainCard = isMainCard;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductCode(Map<String, Object> productCode) {
        this.productCode = productCode;
    }

    public void setProductId(Map<String, Object> productId) {
        this.productId = productId;
    }

    public void setProductNumber(Map<String, Object> productNumber) {
        this.productNumber = productNumber;
    }

    public void setProductType(Map<String, Object> productType) {
        this.productType = productType;
    }

    public void setProductTypeExtension(Map<String, Object> productTypeExtension) {
        this.productTypeExtension = productTypeExtension;
    }

    public void setWarningCode(Map<String, Object> warningCode) {
        this.warningCode = warningCode;
    }

    public String getAccountNumber(boolean includeClearingNumber) {

        String accountNumber = productNumber.get("$").toString();

        if (includeClearingNumber) {
            String accountTypeCode = productTypeExtension.get("$").toString();

            if (accountTypeCode != null && personalAccountCodes.contains(accountTypeCode.toUpperCase())
                    && accountNumber.length() == 10) {
                return personalAccountClearingNumber + accountNumber;
            }
        }

        return accountNumber;
    }

    @JsonIgnore
    public boolean canView() {
        return getProductIdBoolean("@view");
    }

    @JsonIgnore
    public boolean canMakePayment() {
        return getProductIdBoolean("@paymentAccount");
    }

    @JsonIgnore
    public boolean canReceiveInternalTransfer() {
        return getProductIdBoolean("@ownTransferTo");
    }

    @JsonIgnore
    public boolean canMakeInternalTransfer() {
        return getProductIdBoolean("@ownTransferFrom");
    }

    @SuppressWarnings("rawtypes")
    public String getInternalId() {
        return (String) productId.get("$");
    }

    @JsonIgnore
    public String getAccountId() {
        if (productId.containsKey("$")) {
            Object o = productId.get("$");
            if (o instanceof String) {
                return (String) o;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public String getNordeaAccountIdV2() {
        return URL.urlDecode(((Map<String, Object>) getProductId().get("@id")).get("$").toString());
    }

    @JsonIgnore
    public String getNordeaProductType() {
        return getProductType().get("$").toString();
    }

    @JsonIgnore
    public String getNordeaProductTypeExtension() {
        Map<String, Object> productTypeExtension = getProductTypeExtension();

        if (productTypeExtension == null || !productTypeExtension.containsKey("$")) {
            return null;
        }

        return productTypeExtension.get("$").toString();
    }

    @SuppressWarnings("rawtypes")
    public Boolean getProductIdBoolean(String key) {
        Object object = productId.get(key);
        if (object instanceof Map) {
            Object value = ((Map) object).get("$");
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }

    @JsonIgnore
    public boolean isCreditCard() {
        return NordeaV17Constants.CardType.CREDIT_CARD.equalsIgnoreCase(cardGroup);
    }
}

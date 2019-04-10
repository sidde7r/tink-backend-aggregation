package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.Sets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class ProductEntity {
    private static final String personalAccountClearingNumber = "3300";
    private static final Set<String> personalAccountCodes =
            Sets.newHashSet("SE0000", "SE0200", "SE0300");

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardGroup;

    private Map<String, Object> productId = new HashMap<>();

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productNumber;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String accountType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productTypeExtension;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String nickName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double balance;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double fundsAvailable;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String branchId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String productRole;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String mtgLoanName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double nextPayment;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date nextPaymentDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String loanId;

    public String getProductType() {
        return productType;
    }

    public String getCardGroup() {
        return cardGroup;
    }

    public Map<String, Object> getProductId() {
        return productId;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getProductTypeExtension() {
        return productTypeExtension;
    }

    public String getCurrency() {
        return currency;
    }

    public Optional<String> getNickName() {
        return Optional.ofNullable(nickName);
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getFundsAvailable() {
        return fundsAvailable;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getProductRole() {
        return productRole;
    }

    public String getMtgLoanName() {
        return mtgLoanName;
    }

    public Double getNextPayment() {
        return nextPayment;
    }

    public Date getNextPaymentDate() {
        return nextPaymentDate;
    }

    public String getLoanId() {
        return loanId;
    }

    public String getAccountNumber(boolean includeClearingNumber) {
        String accountNumber = productNumber;

        if (includeClearingNumber) {
            String accountTypeCode = productTypeExtension;

            if (accountTypeCode != null
                    && personalAccountCodes.contains(accountTypeCode.toUpperCase())
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
}

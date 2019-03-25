package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonObject
public class AccountEntity {
    private String alias;
    private String description;
    private String availability;
    private String owner;
    private String product;
    private String productType;
    private String entityCode;
    private String contractCode;
    private String bic;
    private String number;
    private String iban;
    private String hashIban;
    private AmountEntity amount;
    private int numOwners;
    private boolean isOwner;
    private boolean isSBPManaged;
    private boolean isIberSecurities;
    private String joint;
    private String mobileWarning;
    private String contractNumberFormatted;
    private String value;

    @JsonIgnore
    public InvestmentAccount toTinkInvestmentAccount(List<Portfolio> portfolios) {
        return InvestmentAccount.builder(iban)
                .setAccountNumber(contractNumberFormatted)
                .setName(alias)
                .setHolderName(new HolderName(owner))
                .setCashBalance(Amount.inEUR(0.0))
                .setBankIdentifier(number)
                .setPortfolios(portfolios)
                .build();
    }

    @JsonIgnore
    public List<Portfolio> toTinkPortfolios(List<Instrument> instruments) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(iban);
        portfolio.setInstruments(instruments);
        portfolio.setTotalValue(AgentParsingUtils.parseAmount(amount.getValue()));
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setCashValue(0.0);

        return Collections.singletonList(portfolio);
    }

    @JsonIgnore
    public Map<String, String> getMappedAttributes() {

        Map<String, String> mappedAttributes = new HashMap<>();

        Map<String, Object> map =
                new ObjectMapper().convertValue(this, new TypeReference<Map<String, Object>>() {});

        map.forEach(
                (firstKey, v) -> {
                    if (v instanceof Map) {
                        ((Map<String, String>) v)
                                .forEach(
                                        (secondKey, l) ->
                                                mappedAttributes.put(
                                                        composeKey(firstKey, secondKey), l));
                    } else {
                        mappedAttributes.put(
                                composeKey(firstKey), v == null ? "" : String.valueOf(v));
                    }
                });

        return mappedAttributes;
    }

    @JsonIgnore
    private String composeKey(String firstKey, String... otherKeys) {
        final String firstKeyPrefix = "account[";
        final String otherKeyPrefix = "[";
        final String keySuffix = "]";

        StringBuffer buffer = new StringBuffer();
        buffer.append(firstKeyPrefix);
        buffer.append(firstKey);
        buffer.append(keySuffix);

        for (String otherKey : otherKeys) {
            buffer.append(otherKeyPrefix);
            buffer.append(otherKey);
            buffer.append(keySuffix);
        }

        return buffer.toString();
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }

    public String getAvailability() {
        return availability;
    }

    public String getOwner() {
        return owner;
    }

    public String getProduct() {
        return product;
    }

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public String getIban() {
        return iban;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public int getNumOwners() {
        return numOwners;
    }

    public boolean isOwner() {
        return isOwner;
    }

    @JsonProperty("isSBPManaged")
    public boolean isSBPManaged() {
        return isSBPManaged;
    }

    @JsonProperty("isIberSecurities")
    public boolean isIberSecurities() {
        return isIberSecurities;
    }

    public String getJoint() {
        return joint;
    }

    public String getMobileWarning() {
        return mobileWarning;
    }

    public String getContractNumberFormatted() {
        return contractNumberFormatted;
    }
}

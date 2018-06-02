package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class PortfolioEntity {
    private String id;
    private String name;
    private String type;
    private String nickName;
    private String customerName;
    private String customerId;
    private List<String> defaultAccountNumbers;
    private double totalPurchaseValue;
    private double totalMarketValue;
    private double totalMarketValueChange;
    private double totalMarketValueChangeInPercent;
    private double totalValue;
    private double totalValueChangeInPercent;
    private String currencyCode;
    private int itemCount;
    private int openOrderCount;
    private List<PortfolioContentsEntity> categories;
    private boolean isDefault;
    private boolean pledged;
    private boolean useOnlyDefaultAccount;
    private boolean ownedByCurrentUser;
    private boolean subscribable;
    private boolean redeemable;
    private String formattedName;

    @JsonIgnore
    public boolean isPortfolio() {
        return !SpankkiConstants.Investment.AGGREGATE_PORTFOLIO.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public Portfolio toTinkPortfolio(Map<String, String> fundIdIsinMapper) {
        Portfolio portfolio = new Portfolio();

        portfolio.setRawType(type);
        portfolio.setTotalProfit(totalMarketValueChange);
        portfolio.setUniqueIdentifier(id);
        portfolio.setTotalValue(totalValue);
        portfolio.setType(SpankkiConstants.Investment.PortfolioType.toTinkType(type));
        portfolio.setInstruments(
                categories.stream()
                        .map(content -> content.getTinkInstruments(fundIdIsinMapper))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );

        return portfolio;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getPortfolioName() {
        return String.format("%s, %s", name, customerName);
    }

    public double getTotalValue() {
        return totalValue;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}

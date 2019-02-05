package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class HoldingEntity {
    private Double marketValue;
    private Double marketValueTotal;
    private ReturnsEntity returns;
    private String customerCurrency;
    private List<SecurityGroupEntity> securityGroups;

    public boolean isDKKCurrency() {
        return JyskeConstants.Fetcher.Investment.CURRENCY.equalsIgnoreCase(customerCurrency);
    }

    public Portfolio toPortfolio(CustodyAccountEntity custodyAccount) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setRawType(custodyAccount.getName());
        portfolio.setUniqueIdentifier(custodyAccount.createUniqueIdentifier());
        portfolio.setTotalValue(marketValueTotal);
        portfolio.setTotalProfit(
                Optional.ofNullable(returns).orElseThrow(() -> new IllegalStateException("No returns found"))
                        .getSinceBought());
        portfolio.setInstruments(getSecurityGroups()
                .flatMap(SecurityGroupEntity::getInstruments)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        return portfolio;
    }

    private Stream<SecurityGroupEntity> getSecurityGroups() {
        if (securityGroups == null) {
            return Stream.empty();
        }
        return securityGroups.stream();
    }
}

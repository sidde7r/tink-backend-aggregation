package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
public class PoolAccountEntity {

    private static final AggregationLogger LOGGER = new AggregationLogger(PoolAccountEntity.class);

    private IdEntity id;
    private double balance;
    private double unsettledValue;
    private String currency;
    private String name;
    private String type;
    private String group;

    public Optional<InvestmentAccount> toInvestmentAccount() {
        if (!JyskeConstants.Fetcher.Investment.CURRENCY.equalsIgnoreCase(currency)) {
            return Optional.empty();
        }
        String uniqueIdentifier = IdEntity.createUniqueIdentifier(id);
        return Optional.of(
                InvestmentAccount.builder(uniqueIdentifier)
                        .setAccountNumber(uniqueIdentifier)
                        .setName(name)
                        .setCashBalance(Amount.inDKK(balance))
                        .setPortfolios(Collections.singletonList(toPortfolio(uniqueIdentifier)))
                        .build()
        );
    }

    private Portfolio toPortfolio(String uniqueIdentifier) {
        Portfolio portfolio = new Portfolio();
        portfolio.setRawType(type);
        portfolio.setType(getType());
        portfolio.setUniqueIdentifier(uniqueIdentifier);
        portfolio.setTotalValue(unsettledValue);
        return portfolio;
    }

    private Portfolio.Type getType() {
        if (JyskeConstants.Fetcher.Investment.PENSION_TYPE.equalsIgnoreCase(type)) {
            return Portfolio.Type.PENSION;
        }
        if (JyskeConstants.Fetcher.Investment.CHILD_SAVING_TYPE.equalsIgnoreCase(type)) {
            return Portfolio.Type.OTHER;
        }
        LOGGER.info(JyskeConstants.Log.INVESTMENT_LOGGING.concat(
                String.format("Found pool account in group: '%s' and type: '%s'", group, type)));
        return Portfolio.Type.OTHER;
    }
}

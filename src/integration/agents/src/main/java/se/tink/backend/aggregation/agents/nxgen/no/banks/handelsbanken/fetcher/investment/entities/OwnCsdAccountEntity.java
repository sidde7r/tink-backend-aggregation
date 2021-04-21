package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class OwnCsdAccountEntity {
    private String whenCreated;
    private String sendPaymentNotificationMode;
    private String whenTerminated;
    private String status;
    private boolean clientAccount;
    private String pensionType;
    private InvestorEntity investor;
    private String csdAccountNumber;
    private BankAccountsEntity bankAccounts;
    private String uri;
    private String changeNotificationMode;
    private String name;
    private MainLocalParticipantEntity mainLocalParticipant;
    private Integer changeNotificationMailDays;
    private String ask;
    private String ownName;
    private List<CsdAccountRightsEntity> rights;

    @JsonIgnore
    public boolean isNotClosed() {
        return !HandelsbankenNOConstants.InvestmentConstants.STATUS_CLOSED.equalsIgnoreCase(status);
    }

    @JsonIgnore
    public InvestmentAccount toTinkAccount(
            List<PositionEntity> positions,
            HashMap<String, Double> availableBalanceByCsdAccountNumber) {

        double totalMarketValue = getTotalMarketValue(positions);

        Portfolio portfolio =
                toPortfolio(positions, totalMarketValue, availableBalanceByCsdAccountNumber);

        return InvestmentAccount.builder(csdAccountNumber)
                .setAccountNumber(csdAccountNumber)
                .setName(name)
                .setCashBalance(ExactCurrencyAmount.of(portfolio.getCashValue(), "NOK"))
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    @JsonIgnore
    private Portfolio toPortfolio(
            List<PositionEntity> positions,
            double totalMarketValue,
            HashMap<String, Double> availableBalanceByCsdAccountNumber) {

        Portfolio portfolio = new Portfolio();

        portfolio.setTotalValue(totalMarketValue);
        portfolio.setTotalProfit(getTotalProfit(positions));
        portfolio.setCashValue(setCashValueIfExists(availableBalanceByCsdAccountNumber));
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setUniqueIdentifier(csdAccountNumber);
        portfolio.setInstruments(getInstruments(positions));

        return portfolio;
    }

    @JsonIgnore
    private List<Instrument> getInstruments(List<PositionEntity> positions) {
        return positions.stream().map(PositionEntity::toInstrument).collect(Collectors.toList());
    }

    @JsonIgnore
    private double getTotalMarketValue(List<PositionEntity> positions) {
        return positions.stream().mapToDouble(PositionEntity::getMarketValue).sum();
    }

    @JsonIgnore
    private double getTotalProfit(List<PositionEntity> positions) {
        return positions.stream().mapToDouble(PositionEntity::getProfit).sum();
    }

    @JsonIgnore
    private Double setCashValueIfExists(
            HashMap<String, Double> availableBalanceByCsdAccountNumber) {
        // Cash value is only present for stock accounts, null for funds.
        Double cashValue = availableBalanceByCsdAccountNumber.get(csdAccountNumber);
        return cashValue != null ? cashValue : 0;
    }
}

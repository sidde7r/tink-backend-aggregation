package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;

@JsonObject
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

    public String getWhenCreated() {
        return whenCreated;
    }

    public String getSendPaymentNotificationMode() {
        return sendPaymentNotificationMode;
    }

    public String getWhenTerminated() {
        return whenTerminated;
    }

    public String getStatus() {
        return status;
    }

    public boolean isClientAccount() {
        return clientAccount;
    }

    public String getPensionType() {
        return pensionType;
    }

    public InvestorEntity getInvestor() {
        return investor;
    }

    public String getCsdAccountNumber() {
        return csdAccountNumber;
    }

    public BankAccountsEntity getBankAccounts() {
        return bankAccounts;
    }

    public String getUri() {
        return uri;
    }

    public String getChangeNotificationMode() {
        return changeNotificationMode;
    }

    public String getName() {
        return name;
    }

    public MainLocalParticipantEntity getMainLocalParticipant() {
        return mainLocalParticipant;
    }

    public Integer getChangeNotificationMailDays() {
        return changeNotificationMailDays;
    }

    public String getAsk() {
        return ask;
    }

    public String getOwnName() {
        return ownName;
    }

    public List<CsdAccountRightsEntity> getRights() {
        return rights;
    }

    @JsonIgnore
    public InvestmentAccount toTinkAccount(List<PositionEntity> positions,
            HashMap<String, Double> availableBalanceByCsdAccountNumber) {

        double totalMarketValue = getTotalMarketValue(positions);

        Portfolio portfolio = toPortfolio(positions, totalMarketValue,
                availableBalanceByCsdAccountNumber);
        return InvestmentAccount.builder(csdAccountNumber)
                .setAccountNumber(csdAccountNumber)
                .setName(name)
                .setCashBalance(Amount.inNOK(portfolio.getCashValue()))
                .setPortfolios(Collections.singletonList(portfolio))
                .build();
    }

    @JsonIgnore
    private Portfolio toPortfolio(List<PositionEntity> positions, double totalMarketValue,
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
        return positions.stream()
                .map(PositionEntity::toInstrument)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    private double getTotalMarketValue(List<PositionEntity> positions) {
        return positions.stream().mapToDouble(PositionEntity::getMarketValue).sum();
    }

    @JsonIgnore
    private double getTotalProfit(List<PositionEntity> positions) {
        return positions.stream().mapToDouble(PositionEntity::getProfit).sum();
    }

    @JsonObject
    private Double setCashValueIfExists(HashMap<String, Double> availableBalanceByCsdAccountNumber) {
        // Cash value is only present for stock accounts, null for funds.
        return availableBalanceByCsdAccountNumber.get(csdAccountNumber);
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsOverviewResponse {
    private int numberOfIntradayTransfers;
    private double totalPerformance;
    private double totalBalance;
    private int numberOfTransfers;
    private double totalOwnCapital;
    private double totalPerformancePercent;
    private List<AccountEntity> accounts;
    private int numberOfOrders;
    private double totalBuyingPower;
    private int numberOfDeals;

    public int getNumberOfIntradayTransfers() {
        return numberOfIntradayTransfers;
    }

    public double getTotalPerformance() {
        return totalPerformance;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public int getNumberOfTransfers() {
        return numberOfTransfers;
    }

    public double getTotalOwnCapital() {
        return totalOwnCapital;
    }

    public double getTotalPerformancePercent() {
        return totalPerformancePercent;
    }

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList());
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
    }

    public double getTotalBuyingPower() {
        return totalBuyingPower;
    }

    public int getNumberOfDeals() {
        return numberOfDeals;
    }
}

package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    private String productName;
    private BalanceEntity originalBalance;
    private Object balanceEUR;
    private Object creditLimit;
    private int totalTransactions;
    private int pageCount;
    private boolean kontoAlarmActivated;
    private List<String> validActions;
    private List<PfmTransactionsEntity> pfmTransactions;
    private Object transactionsWithBookingGreaterToday;
    private Object realtimeTransactions;
    private Object scheduledOrders;
    private Object type;

    public String getProductName() {
        return productName;
    }

    public BalanceEntity getOriginalBalance() {
        return originalBalance;
    }

    public int getTotalTransactions() {
        return totalTransactions;
    }

    public int getPageCount() {
        return pageCount;
    }

    public boolean isKontoAlarmActivated() {
        return kontoAlarmActivated;
    }

    public List<String> getValidActions() {
        return validActions;
    }

    public List<PfmTransactionsEntity> getPfmTransactions() {
        return pfmTransactions;
    }

    public Object getBalanceEUR() {
        return balanceEUR;
    }

    public Object getCreditLimit() {
        return creditLimit;
    }

    public Object getTransactionsWithBookingGreaterToday() {
        return transactionsWithBookingGreaterToday;
    }

    public Object getRealtimeTransactions() {
        return realtimeTransactions;
    }

    public Object getScheduledOrders() {
        return scheduledOrders;
    }

    public Object getType() {
        return type;
    }
}

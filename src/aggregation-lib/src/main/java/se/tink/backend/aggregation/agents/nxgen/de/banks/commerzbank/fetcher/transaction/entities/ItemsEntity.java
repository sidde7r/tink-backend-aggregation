package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ItemsEntity {
    private String productName;
    private BalanceEntity originalBalance;
    // `balanceEUR` is null - cannot define it!
    // `creditLimit` is null - cannot define it!
    private int totalTransactions;
    private int pageCount;
    private boolean kontoAlarmActivated;
    private List<String> validActions;
    private List<PfmTransactionsEntity> pfmTransactions;

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
    // `transactionsWithBookingGreaterToday` is null - cannot define it!
    // `realtimeTransactions` is null - cannot define it!
    // `scheduledOrders` is null - cannot define it!
    // `type` is null - cannot define it!
}

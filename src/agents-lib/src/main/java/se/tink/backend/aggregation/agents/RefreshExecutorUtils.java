package se.tink.backend.aggregation.agents;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import se.tink.backend.aggregation.rpc.RefreshableItem;

public class RefreshExecutorUtils {

    private final static Map<RefreshableItem, Class> REFRESHABLEITEM_EXECUTOR_MAP = ImmutableMap.<RefreshableItem, Class>builder()
            .put(RefreshableItem.EINVOICES, RefreshEInvoiceExecutor.class)
            .put(RefreshableItem.TRANSFER_DESTINATIONS, RefreshTransferDestinationExecutor.class)
            .put(RefreshableItem.CHECKING_ACCOUNTS, RefreshCheckingAccountsExecutor.class)
            .put(RefreshableItem.CHECKING_TRANSACTIONS, RefreshCheckingAccountsExecutor.class)
            .put(RefreshableItem.SAVING_ACCOUNTS, RefreshSavingsAccountsExecutor.class)
            .put(RefreshableItem.SAVING_TRANSACTIONS, RefreshSavingsAccountsExecutor.class)
            .put(RefreshableItem.CREDITCARD_ACCOUNTS, RefreshCreditCardAccountsExecutor.class)
            .put(RefreshableItem.CREDITCARD_TRANSACTIONS, RefreshCreditCardAccountsExecutor.class)
            .put(RefreshableItem.LOAN_ACCOUNTS, RefreshLoanAccountsExecutor.class)
            .put(RefreshableItem.LOAN_TRANSACTIONS, RefreshLoanAccountsExecutor.class)
            .put(RefreshableItem.INVESTMENT_ACCOUNTS, RefreshInvestmentAccountsExecutor.class).build();

    public static Class getRefreshExecutor(RefreshableItem item) {
        return REFRESHABLEITEM_EXECUTOR_MAP.get(item);
    }
}

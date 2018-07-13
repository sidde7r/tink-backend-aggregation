package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.GenericGroupedMovementEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CreditCardTransactionsResponse implements TransactionPagePaginatorResponse {
    private String contractNumber;
    private String contractOwner;
    private String paymentType;
    private String ptorete;
    private AmountEntity previousBalance;
    private AmountEntity currentMonthBalance;
    private AmountEntity totalAmount;
    private AmountEntity pendingLiquidationBalance;
    private AmountEntity availableBalance;
    private AmountEntity willingBalance;
    private AmountEntity chargeAccount;
    private CreditCardEntity card;
    private PaginatorEntity paginator;
    private GenericGroupedMovementEntity genericGroupedMovement;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        return Collections.emptyList();
    }

    @Override
    public boolean canFetchMore() {
        // Always return false until we can parse transactions, makes no sense to paginate until then.
        // When we know how to parse transactions we can use genericGroupedMovement.hasMoreElements()
        return false;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getContractOwner() {
        return contractOwner;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getPtorete() {
        return ptorete;
    }

    public AmountEntity getPreviousBalance() {
        return previousBalance;
    }

    public AmountEntity getCurrentMonthBalance() {
        return currentMonthBalance;
    }

    public AmountEntity getTotalAmount() {
        return totalAmount;
    }

    public AmountEntity getPendingLiquidationBalance() {
        return pendingLiquidationBalance;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getWillingBalance() {
        return willingBalance;
    }

    public AmountEntity getChargeAccount() {
        return chargeAccount;
    }

    public CreditCardEntity getCard() {
        return card;
    }

    public PaginatorEntity getPaginator() {
        return paginator;
    }

    public GenericGroupedMovementEntity getGenericGroupedMovement() {
        return genericGroupedMovement;
    }
}

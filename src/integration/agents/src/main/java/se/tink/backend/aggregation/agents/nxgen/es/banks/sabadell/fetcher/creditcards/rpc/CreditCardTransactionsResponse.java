package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.CreditCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities.GroupedMovementsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.PaginatorEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class CreditCardTransactionsResponse implements PaginatorResponse {
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
    private GroupedMovementsEntity genericGroupedMovement;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        CreditCardAccount creditCardAccount = card.toTinkAccount();
        return genericGroupedMovement.toTinkTransactions(creditCardAccount);
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(genericGroupedMovement.hasMoreElements());
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

    public GroupedMovementsEntity getGenericGroupedMovement() {
        return genericGroupedMovement;
    }
}

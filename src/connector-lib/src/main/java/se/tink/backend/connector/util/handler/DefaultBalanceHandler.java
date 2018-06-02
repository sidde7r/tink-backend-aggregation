package se.tink.backend.connector.util.handler;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.connector.rpc.PartnerAccountPayload;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;

public class DefaultBalanceHandler implements BalanceHandler {

    private AccountRepository accountRepository;
    private static final LogUtils log = new LogUtils(DefaultBalanceHandler.class);

    @Inject
    DefaultBalanceHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void calculateAndUpdateBalance(Account account, CRUDType crudType, List<Transaction> transactions) {

        if (transactions == null) {
            return;
        }

        // Filter out pending trx. We only calculate balance on non-pending trx for now.
        transactions = transactions.stream().filter(t -> !t.isPending()).collect(Collectors.toList());

        double amountToAdd;

        switch (crudType) {
        case CREATE:
            amountToAdd = transactions.stream().mapToDouble(Transaction::getOriginalAmount).sum();
            break;
        case DELETE:
            amountToAdd = -(transactions.stream().mapToDouble(Transaction::getOriginalAmount).sum());
            break;
        case UPDATE:
            Preconditions.checkArgument(transactions.size() == 2);
            Transaction newTransaction = transactions.get(0);
            Transaction oldTransaction = transactions.get(1);
            amountToAdd = newTransaction.getOriginalAmount() - oldTransaction.getOriginalAmount();
            break;
        default:
            throw new IllegalStateException("Unknown CRUD type: " + crudType);
        }

        doAtomicAdditionToBalance(account.getId(), amountToAdd);
    }

    @Override
    public void setNewBalance(Account account, Double reservedAmountObject, Double balanceObject,
            Map<String, Object> payload) {

        Optional<Double> newBalance = getBalance(reservedAmountObject, balanceObject,
                payload.get(PartnerAccountPayload.IGNORE_BALANCE));

        newBalance.ifPresent(account::setBalance);
    }

    private Optional<Double> getBalance(Double reservedAmountObject, Double balanceObject, Object ignoreBalance) {

        if (Objects.equals(ignoreBalance, true)) {
            return Optional.empty();
        }

        Preconditions.checkNotNull(balanceObject);
        double reservedAmount = (reservedAmountObject == null ? 0 : reservedAmountObject);
        return Optional.of(balanceObject - reservedAmount);
    }

    /**
     * Does an atomic write to the balance with the specified diff. This is to avoid race conditions.
     */
    private void doAtomicAdditionToBalance(String accountId, double diff) {
        try {
            accountRepository.addToBalanceById(accountId, diff);
        } catch (Exception e) {
            log.error(String.format(
                    "Could not save balance for accountId: %s. Try re-calculating it with the balance cron job.",
                    accountId), e);
        }
    }
}

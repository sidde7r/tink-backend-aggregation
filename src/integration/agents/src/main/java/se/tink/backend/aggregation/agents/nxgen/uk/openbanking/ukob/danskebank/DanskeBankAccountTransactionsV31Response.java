package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.danskebank;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.TransactionMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class DanskeBankAccountTransactionsV31Response extends AccountTransactionsV31Response {

    @Override
    public List<? extends Transaction> toTinkTransactions(TransactionMapper transactionMapper) {
        return getData().orElse(Collections.emptyList()).stream()
                .filter(TransactionEntity::isNotRejected)
                .filter(TransactionEntity::isNotZeroBalancingTransaction)
                .map(transactionMapper::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends Transaction> toTinkCreditCardTransactions(
            TransactionMapper transactionMapper, CreditCardAccount account) {
        return getData().orElse(Collections.emptyList()).stream()
                .filter(TransactionEntity::isNotRejected)
                .filter(TransactionEntity::isNotZeroBalancingTransaction)
                .map(
                        transactionEntity ->
                                transactionMapper.toTinkCreditCardTransaction(
                                        transactionEntity, account))
                .collect(Collectors.toList());
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.transactionalaccount.converter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.AccountsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.BalanceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.TransactionsResponseDto;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class AmexTransactionalAccountConverter {

    public Optional<TransactionalAccount> toTransactionalAccount(
            AccountsResponseDto accountsResponse, List<BalanceDto> balances) {
        final String iban = accountsResponse.getIdentifiers().getDisplayAccountNumber();
        final String cardName =
                accountsResponse.getProduct().getAccountTypes().getLineOfBusinessType();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(getBalance(balances)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(cardName)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .build();
    }

    public List<AggregationTransaction> convertResponseToAggregationTransactions(
            List<TransactionsResponseDto> transactionsResponseList) {

        return transactionsResponseList.stream()
                .map(TransactionsResponseDto::getTransactions)
                .flatMap(List::stream)
                .map(AmexTransactionalAccountConverter::convertTransactionResponseToTransaction)
                .collect(Collectors.toList());
    }

    private static Transaction convertTransactionResponseToTransaction(
            TransactionDto transactionDto) {
        return Transaction.builder()
                .setAmount(convertTransactionEntityToExactCurrencyAmount(transactionDto))
                .setDate(transactionDto.getChargeDate())
                .setDescription(transactionDto.getDescription())
                .setPending(false)
                .build();
    }

    private static ExactCurrencyAmount getBalance(List<BalanceDto> balances) {
        return balances.stream()
                .findFirst()
                .map(AmexTransactionalAccountConverter::convertBalanceEntityToExactCurrencyAmount)
                .orElseThrow(() -> new IllegalStateException("No balance found"));
    }

    private static ExactCurrencyAmount convertBalanceEntityToExactCurrencyAmount(
            BalanceDto balanceDto) {
        return new ExactCurrencyAmount(
                balanceDto.getRemainingStatementBalanceAmount(),
                balanceDto.getIsoAlphaCurrencyCode());
    }

    private static ExactCurrencyAmount convertTransactionEntityToExactCurrencyAmount(
            TransactionDto transactionDto) {
        return new ExactCurrencyAmount(
                transactionDto.getAmount(), transactionDto.getIsoAlphaCurrencyCode());
    }
}

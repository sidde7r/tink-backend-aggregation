package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount.converter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.AccountCategoryCode;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountSummaryItemDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionInformationDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TransactionsAndLockedEventsResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
public class AktiaTransactionalAccountConverter {

    public Optional<TransactionalAccount> toTransactionalAccount(
            AccountSummaryItemDto accountSummaryItem) {
        final String iban = accountSummaryItem.getIban();

        return getAccountType(accountSummaryItem)
                .flatMap(
                        type ->
                                TransactionalAccount.nxBuilder()
                                        .withType(type)
                                        .withInferredAccountFlags()
                                        .withBalance(
                                                BalanceModule.of(getBalance(accountSummaryItem)))
                                        .withId(
                                                IdModule.builder()
                                                        .withUniqueIdentifier(iban)
                                                        .withAccountNumber(iban)
                                                        .withAccountName(
                                                                accountSummaryItem.getName())
                                                        .addIdentifier(new IbanIdentifier(iban))
                                                        .build())
                                        .setApiIdentifier(accountSummaryItem.getId())
                                        .setBankIdentifier(accountSummaryItem.getBic())
                                        .build());
    }

    public TransactionKeyPaginatorResponse<String> toPaginatorResponse(
            TransactionsAndLockedEventsResponseDto responseDto) {
        final List<Transaction> transactions =
                responseDto.getTransactions().stream()
                        .map(
                                AktiaTransactionalAccountConverter
                                        ::convertTransactionInformationDtoToTransaction)
                        .collect(Collectors.toList());

        return new TransactionKeyPaginatorResponseImpl<>(
                transactions, responseDto.getContinuationKey());
    }

    private static Optional<TransactionalAccountType> getAccountType(
            AccountSummaryItemDto accountSummaryItem) {
        final AccountCategoryCode categoryCode =
                accountSummaryItem.getAccountType().getCategoryCode();

        if (categoryCode == AccountCategoryCode.OTHER) {
            log.info("Got 'OTHER' account category code.");
            return Optional.empty();
        } else if (categoryCode == AccountCategoryCode.CURRENT_ACCOUNT) {
            return Optional.of(TransactionalAccountType.CHECKING);
        } else {
            return Optional.of(TransactionalAccountType.SAVINGS);
        }
    }

    private static Transaction convertTransactionInformationDtoToTransaction(
            TransactionInformationDto transactionInformationDto) {
        return Transaction.builder()
                .setAmount(getAmount(transactionInformationDto))
                .setDate(transactionInformationDto.getBookingDate())
                .setDescription(createDescription(transactionInformationDto))
                .setPending(false)
                .build();
    }

    private static ExactCurrencyAmount getBalance(AccountSummaryItemDto accountSummaryItem) {
        return new ExactCurrencyAmount(accountSummaryItem.getBalance(), "EUR");
    }

    private static ExactCurrencyAmount getAmount(
            TransactionInformationDto transactionInformationDto) {
        return new ExactCurrencyAmount(transactionInformationDto.getAmount(), "EUR");
    }

    private static String createDescription(TransactionInformationDto transactionInformationDto) {
        return Stream.of(
                        transactionInformationDto.getReceiverOrPayerName(),
                        transactionInformationDto.getMessage(),
                        transactionInformationDto.getTransactionType())
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(", "));
    }
}

package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.BalanceResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.account.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionResourceDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction.TransactionsResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter.LclDataConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class LclCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final LclApiClient lclApiClient;
    private final LclDataConverter lclDataConverter;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return lclApiClient.getAccountsResponse().getAccounts().stream()
                .filter(acc -> isCreditCard(acc))
                .map(this::convertToTinkCreditCard)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        final TransactionsResponseDto transactionsResponseDto =
                getTransactionsResponse(account, page);
        final boolean canFetchMore = canFetchMoreTransactions(transactionsResponseDto);

        return transactionsResponseDto.getTransactions().stream()
                .map(this::mapTransaction)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                transactions ->
                                        PaginatorResponseImpl.create(transactions, canFetchMore)));
    }

    private CreditCardAccount convertToTinkCreditCard(AccountResourceDto accountResourceDto) {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountResourceDto.creditCardIdentifier())
                                .withBalance(getCreditCardBalance(accountResourceDto))
                                .withAvailableCredit(getAvailableCredit())
                                .withCardAlias(accountResourceDto.getProduct())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountResourceDto.getResourceId())
                                .withAccountNumber(accountResourceDto.getLinkedAccount())
                                .withAccountName(accountResourceDto.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                accountResourceDto
                                                        .getAccountId()
                                                        .getOther()
                                                        .getIdentification()))
                                .setProductName(accountResourceDto.getProduct())
                                .build())
                .setApiIdentifier(accountResourceDto.getResourceId())
                .build();
    }

    private ExactCurrencyAmount getCreditCardBalance(AccountResourceDto accountResourceDto) {
        BalanceResourceDto balanceResourceDto = accountResourceDto.getBalances().get(0);
        return ExactCurrencyAmount.of(
                balanceResourceDto.getBalanceAmount().getAmount(),
                balanceResourceDto.getBalanceAmount().getCurrency());
    }

    private ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.of(0, "EUR");
    }

    private boolean isCreditCard(AccountResourceDto accountResourceDto) {
        return CashAccountType.CARD == accountResourceDto.getCashAccountType();
    }

    private Transaction mapTransaction(TransactionResourceDto transaction) {
        return Transaction.builder()
                .setAmount(getTransactionAmount(transaction))
                .setDescription(
                        StringUtils.join(
                                transaction.getRemittanceInformation().getUnstructured(), ';'))
                .setDate(transaction.getBookingDate())
                .setPending(transaction.getStatus() != TransactionStatus.BOOK)
                .setRawDetails(transaction.getEntryReference())
                .build();
    }

    private ExactCurrencyAmount getTransactionAmount(TransactionResourceDto transaction) {
        return lclDataConverter.convertAmountDtoToExactCurrencyAmount(
                transaction.getTransactionAmount());
    }

    private TransactionsResponseDto getTransactionsResponse(CreditCardAccount account, int page) {
        final String resourceId = account.getApiIdentifier();

        return lclApiClient.getTransactionsResponse(resourceId, page);
    }

    private static boolean canFetchMoreTransactions(
            TransactionsResponseDto transactionsResponseDto) {
        return transactionsResponseDto.getLinks() != null
                && transactionsResponseDto.getLinks().getNext() != null;
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcReservation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.FilterAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.pair.Pair;

public class SdcNoAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SdcNoApiClient bankClient;

    public SdcNoAccountFetcher(SdcNoApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        FilterAccountsRequest request =
                new FilterAccountsRequest()
                        .setIncludeCreditAccounts(true)
                        .setIncludeDebitAccounts(true)
                        .setOnlyFavorites(false)
                        .setOnlyQueryable(true);

        return bankClient.filterAccounts(request).getTinkAccounts();
    }

    public static class SdcNoTransactionParser implements SdcTransactionParser {

        @Override
        public Transaction parseTransaction(SdcTransaction bankTransaction) {
            return Transaction.builder()
                    .setAmount(bankTransaction.getAmount().toExactCurrencyAmount())
                    .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                    .setDescription(bankTransaction.getLabel())
                    .build();
        }

        @Override
        public Transaction parseTransaction(SdcReservation bankReservation) {
            return Transaction.builder()
                    .setAmount(bankReservation.getAmount().toExactCurrencyAmount())
                    .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                    .setDescription(bankReservation.getDescription())
                    .setPending(true)
                    .build();
        }

        @Override
        public CreditCardTransaction parseCreditCardTransaction(
                CreditCardAccount creditCardAccount, SdcTransaction bankTransaction) {
            return CreditCardTransaction.builder()
                    .setAmount(bankTransaction.getAmount().toExactCurrencyAmount())
                    .setDate(DateUtils.parseDate(bankTransaction.getPaymentDate()))
                    .setDescription(bankTransaction.getLabel())
                    .setCreditAccount(creditCardAccount)
                    .build();
        }

        @Override
        public CreditCardTransaction parseCreditCardTransaction(
                CreditCardAccount creditCardAccount, SdcReservation bankReservation) {
            return CreditCardTransaction.builder()
                    .setAmount(bankReservation.getAmount().toExactCurrencyAmount())
                    .setDate(DateUtils.parseDate(bankReservation.getCreateDate()))
                    .setDescription(bankReservation.getDescription())
                    .setCreditAccount(creditCardAccount)
                    .setPending(true)
                    .build();
        }
    }

    static class AccountIdPairs {
        private final Document document;

        AccountIdPairs(final String webpage) {
            document = Jsoup.parse(webpage);
        }

        Set<Pair<String, String>> extractAll() {
            return document.select("a.list__anchor").stream()
                    .map(e -> new Pair<>(e.attr("data-id"), e.attr("data-idkey")))
                    .filter(
                            p ->
                                    !Strings.isNullOrEmpty(p.first)
                                            && !Strings.isNullOrEmpty(p.second))
                    .collect(Collectors.toSet());
        }
    }
}

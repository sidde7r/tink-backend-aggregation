package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.builder.ToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcReservation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.parser.SdcTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SearchTransactionsResponse {
    private List<SdcTransaction> transactions;
    private List<SdcReservation> reservations;
    private Object totalReservationsAmount;

    public Collection<Transaction> getTinkTransactions(SdcTransactionParser transactionParser) {
        List<Transaction> result = new ArrayList<>();

        List<Transaction> nonPending =
                transactions.stream()
                        .map(transactionParser::parseTransaction)
                        .collect(Collectors.toList());
        result.addAll(nonPending);

        if (reservations != null) {
            List<Transaction> pending =
                    reservations.stream()
                            .map(transactionParser::parseTransaction)
                            .collect(Collectors.toList());
            result.addAll(pending);
        }

        return result;
    }

    public Collection<CreditCardTransaction> getTinkCreditCardTransactions(
            CreditCardAccount creditCardAccount, SdcTransactionParser transactionParser) {
        List<CreditCardTransaction> result =
                transactions.stream()
                        .map(
                                transaction ->
                                        transactionParser.parseCreditCardTransaction(
                                                creditCardAccount, transaction))
                        .collect(Collectors.toList());

        if (reservations != null) {
            List<CreditCardTransaction> pending =
                    reservations.stream()
                            .map(
                                    reservation ->
                                            transactionParser.parseCreditCardTransaction(
                                                    creditCardAccount, reservation))
                            .collect(Collectors.toList());
            result.addAll(pending);
        }

        return result;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("transactions", transactions)
                .append("reservations", reservations)
                .append("totalReservationsAmount", totalReservationsAmount)
                .toString();
    }

    public List<SdcTransaction> getTransactions() {
        return transactions;
    }

    public List<SdcReservation> getReservations() {
        return reservations;
    }

    public Object getTotalReservationsAmount() {
        return totalReservationsAmount;
    }
}

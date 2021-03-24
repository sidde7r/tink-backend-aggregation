package se.tink.backend.aggregation.agents.utils.transfer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class InferredTransferDestinations {

    /**
     * Build a list of transfer destinations for all of the supplied accounts which are marked with
     * {@link AccountFlag#PSD2_PAYMENT_ACCOUNT} based on the supplied destination types. Each
     * destination type will receive {@link TransferDestinationPattern#ALL}.
     */
    public static FetchTransferDestinationsResponse forPaymentAccounts(
            List<Account> accounts, AccountIdentifierType... destinationTypes) {

        final Map<Account, List<TransferDestinationPattern>> destinations =
                accounts.stream()
                        .filter(account -> account.getFlags() != null)
                        .filter(isPsd2PaymentAccount())
                        .collect(
                                Collectors.toMap(
                                        x -> x,
                                        x ->
                                                Arrays.stream(destinationTypes)
                                                        .map(
                                                                TransferDestinationPattern
                                                                        ::createForMultiMatchAll)
                                                        .collect(Collectors.toList())));

        return new FetchTransferDestinationsResponse(destinations);
    }

    private static Predicate<Account> isPsd2PaymentAccount() {
        return account -> account.getFlags().contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
    }
}

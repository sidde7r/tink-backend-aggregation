package se.tink.backend.aggregation.agents.utils.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.enums.AccountFlag;

public class InferredTransferDestinations {

    /**
     * Build a list of transfer destinations for all of the supplied accounts which are marked with
     * {@link AccountFlag#PSD2_PAYMENT_ACCOUNT} based on the supplied destination types. Each
     * destination type will receive {@link TransferDestinationPattern#ALL}.
     */
    public static FetchTransferDestinationsResponse forPaymentAccounts(
            List<Account> accounts, AccountIdentifier.Type... destinationTypes) {

        List<Account> psd2PaymentAccounts =
                accounts.stream()
                        .filter(account -> account.getFlags() != null)
                        .filter(isPsd2PaymentAccount())
                        .collect(Collectors.toList());

        TransferDestinationPatternBuilder builder =
                new TransferDestinationPatternBuilder()
                        .setTinkAccounts(psd2PaymentAccounts)
                        .setSourceAccounts(
                                psd2PaymentAccounts.stream()
                                        .map(GeneralAccountEntityImpl::createFromCoreAccount)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()))
                        .setDestinationAccounts(new ArrayList<>());

        for (Type identifier : destinationTypes) {
            builder = builder.addMultiMatchPattern(identifier, TransferDestinationPattern.ALL);
        }

        return new FetchTransferDestinationsResponse(builder.build());
    }

    private static Predicate<Account> isPsd2PaymentAccount() {
        return account -> account.getFlags().contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
    }
}

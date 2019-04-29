package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaSePaymentExecutorSelector extends NordeaBasePaymentExecutor {
    // The key is a pair where the key is debtor account type and value is creditor account type.
    // The mapping follows the instructions in:
    // https://developer.nordeaopenbanking.com/app/documentation?api=Payments%20API%20Domestic%20transfer&version=3.3#payment_types_field_combinations
    private static final ImmutableMap<
                    Pair<AccountIdentifier.Type, AccountIdentifier.Type>, PaymentType>
            accountIdentifiersToPaymentType =
                    ImmutableMap
                            .<Pair<AccountIdentifier.Type, AccountIdentifier.Type>, PaymentType>
                                    builder()
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.SE),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE, AccountIdentifier.Type.IBAN),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_BG),
                                    PaymentType.DOMESTIC)
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.SE,
                                            AccountIdentifier.Type.SE_PG),
                                    PaymentType.DOMESTIC)
                            .build();

    public NordeaSePaymentExecutorSelector(NordeaBaseApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected PaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();
        PaymentType requestPaymentType = accountIdentifiersToPaymentType.get(accountIdentifiersKey);
        if (requestPaymentType == null) {
            throw new NotImplementedException(
                    "No PaymentType found for your AccountIdentifiers pair "
                            + accountIdentifiersKey);
        }
        return requestPaymentType;
    }

    @Override
    protected Collection<PaymentType> getSupportedPaymentTypes() {
        return accountIdentifiersToPaymentType.values().stream()
                .distinct()
                .collect(Collectors.toList());
    }
}

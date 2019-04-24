package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.NordeaBasePaymentExecutorSelector;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaSePaymentExecutorSelector extends NordeaBasePaymentExecutorSelector {
    private final ImmutableMap<PaymentType, PaymentExecutor> paymentTypeToExecutorMap;
    private final ImmutableSet<PaymentExecutor> executorsSet;

    // The key is a pair where the key is debtor account type and value is creditor account type.
    // The mapping follows the instructions in:
    // https://developer.nordeaopenbanking.com/app/documentation?api=Payments%20API%20Domestic%20transfer&version=3.3#payment_types_field_combinations
    private static final ImmutableMap<
                    Pair<AccountIdentifier.Type, AccountIdentifier.Type>, PaymentType>
            accountIdentifiersToPaymentTypeMap =
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
                            .put(
                                    new Pair<>(
                                            AccountIdentifier.Type.IBAN,
                                            AccountIdentifier.Type.IBAN),
                                    PaymentType.SEPA)
                            .build();

    public NordeaSePaymentExecutorSelector(NordeaBaseApiClient apiClient) {
        NordeaSeDomesticPaymentExecutor nordeaSeDomesticPaymentExecutor =
                new NordeaSeDomesticPaymentExecutor(apiClient);

        this.executorsSet =
                ImmutableSet.<PaymentExecutor>builder()
                        .add(nordeaSeDomesticPaymentExecutor)
                        .build();

        this.paymentTypeToExecutorMap =
                ImmutableMap.<PaymentType, PaymentExecutor>builder()
                        .put(PaymentType.DOMESTIC, nordeaSeDomesticPaymentExecutor)
                        .build();
    }

    @Override
    protected PaymentExecutor getRelevantExecutor(PaymentRequest paymentRequest) {
        Pair<AccountIdentifier.Type, AccountIdentifier.Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();
        PaymentType paymentType = accountIdentifiersToPaymentTypeMap.get(accountIdentifiersKey);
        PaymentExecutor relevantPaymentExecutor = paymentTypeToExecutorMap.get(paymentType);
        if (relevantPaymentExecutor == null) {
            throw new NotImplementedException(
                    "No relevant PaymentExecutor found for your AccountIdentifiers pair "
                            + accountIdentifiersKey);
        }
        return relevantPaymentExecutor;
    }

    @Override
    protected Collection<PaymentExecutor> getAllExecutors() {
        return executorsSet;
    }
}

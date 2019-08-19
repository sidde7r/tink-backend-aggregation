package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.HandelsbankenBasePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.enums.HandelsbankenPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.utils.AccountTypePair;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;

public class HandelsbankenPaymentExecutorSelector extends HandelsbankenBasePaymentExecutor {

    public HandelsbankenPaymentExecutorSelector(HandelsbankenBaseApiClient apiClient) {
        super(apiClient);
    }

    private static final GenericTypeMapper<HandelsbankenPaymentType, AccountTypePair>
            accountIdentifiersToPaymentProductMapper =
                    GenericTypeMapper.<HandelsbankenPaymentType, AccountTypePair>genericBuilder()
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTIC_CREDIT_TRANSFER,
                                    new AccountTypePair(Type.SE, Type.SE))
                            .put(
                                    HandelsbankenPaymentType.SWEDISH_DOMESTICGIRO_PAYMENT,
                                    new AccountTypePair(Type.SE, Type.SE_BG),
                                    new AccountTypePair(Type.SE, Type.SE_PG))
                            .build();

    @Override
    protected HandelsbankenPaymentType getPaymentType(PaymentRequest paymentRequest) {
        Pair<Type, Type> accountIdentifiersKey =
                paymentRequest.getPayment().getCreditorAndDebtorAccountType();

        return accountIdentifiersToPaymentProductMapper
                .translate(new AccountTypePair(accountIdentifiersKey))
                .orElseGet(() -> getSepaOrCrossCurrencyPaymentType(paymentRequest));
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

public class FabricPaymentSessionStorage {

    private final SessionStorage session;

    public FabricPaymentSessionStorage(SessionStorage session) {
        this.session = session;
        initDefaultSession();
    }

    public void updatePaymentProductIfNeeded(Payment payment) {
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()) {
            session.put(
                    FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                    FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_INSTANT);
        }
    }

    public void updatePaymentServiceIfNeeded(Payment payment) {
        if (PaymentServiceType.PERIODIC == payment.getPaymentServiceType()) {
            session.put(
                    FabricConstants.PathParameterKeys.PAYMENT_SERVICE,
                    FabricConstants.PathParameterValues.PAYMENT_SERVICE_PERIODIC_PAYMENTS);
        } else {
            session.put(
                    FabricConstants.PathParameterKeys.PAYMENT_SERVICE,
                    FabricConstants.PathParameterValues.PAYMENT_SERVICE_PAYMENTS);
        }
    }

    private void initDefaultSession() {
        session.put(
                FabricConstants.PathParameterKeys.PAYMENT_SERVICE,
                FabricConstants.PathParameterValues.PAYMENT_SERVICE_PAYMENTS);
        session.put(
                FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_CREDIT);
    }

    public String get(String key) {
        return session.get(key);
    }

    public void put(String key, String value) {
        session.put(key, value);
    }
}

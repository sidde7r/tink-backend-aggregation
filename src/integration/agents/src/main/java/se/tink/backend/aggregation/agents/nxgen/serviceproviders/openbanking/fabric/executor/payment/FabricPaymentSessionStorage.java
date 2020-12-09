package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;

public class FabricPaymentSessionStorage {

    private final SessionStorage session;

    public FabricPaymentSessionStorage(SessionStorage session) {
        this.session = session;
        updateDefaultPaymentProduct();
    }

    public void updatePaymentProductIfNeeded(Payment payment) {
        if (PaymentScheme.SEPA_INSTANT_CREDIT_TRANSFER == payment.getPaymentScheme()) {
            session.put(
                    FabricConstants.PathParameterKeys.PAYMENT_PRODUCT,
                    FabricConstants.PathParameterValues.PAYMENT_PRODUCT_SEPA_INSTANT);
        }
    }

    private void updateDefaultPaymentProduct() {
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

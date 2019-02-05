package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities.DetailedPermissions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.interfaces.UpdatablePayment;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.HandelsbankenSEPaymentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.nxgen.http.URL;

public class EInvoiceDetails extends EInvoice implements UpdatablePayment {
    private DetailedPermissions detailedPermissions;
    private HandelsbankenSEPaymentContext context;

    public HandelsbankenSEPaymentContext getContext() {
        return context;
    }

    public DetailedPermissions getDetailedPermissions() {
        return detailedPermissions;
    }

    public URL toPaymentContext() {
        return findLink(HandelsbankenConstants.URLS.Links.PAYMENT_CONTEXT);
    }

    public Optional<URL> toUpdate() {
        return searchLink(HandelsbankenConstants.URLS.Links.UPDATE);
    }

    public Optional<URL> toApproval() {
        return searchLink(HandelsbankenConstants.URLS.Links.APPROVAL);
    }
}

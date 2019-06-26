package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice;

import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaEInvoiceFetcher implements EInvoiceFetcher {
    private final NordeaSEApiClient apiClient;

    public NordeaEInvoiceFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        return apiClient.fetchPayments().getPayments().stream()
                .filter(Predicates.not(PaymentEntity::isTransfer).and(PaymentEntity::isUnconfirmed))
                .map(this::fetchDetails)
                .filter(
                        Predicates.and(PaymentEntity::isPayment, PaymentEntity::hasEIvoiceDetails)
                                .or(PaymentEntity::isEInvoice))
                .map(PaymentEntity::toTinkTransfer)
                .collect(Collectors.toList());
    }

    private PaymentEntity fetchDetails(PaymentEntity paymentEntity) {
        return apiClient.fetchPaymentDetails(paymentEntity.getApiIdentifier());
    }
}

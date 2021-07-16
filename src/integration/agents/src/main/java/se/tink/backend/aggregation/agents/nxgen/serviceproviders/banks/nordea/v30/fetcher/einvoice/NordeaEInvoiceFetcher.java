package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice;

import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;

public class NordeaEInvoiceFetcher implements EInvoiceFetcher {
    private final NordeaBaseApiClient apiClient;

    public NordeaEInvoiceFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        return fetchAsPaymentStream()
                .map(PaymentEntity::toTinkTransfer)
                .collect(Collectors.toList());
    }

    private PaymentEntity fetchDetails(PaymentEntity paymentEntity) {
        return apiClient.fetchPaymentDetails(paymentEntity.getApiIdentifier());
    }

    public Stream<PaymentEntity> fetchAsPaymentStream() {
        return apiClient.fetchPayments().getPayments().stream()
                .filter(Predicates.not(PaymentEntity::isTransfer).and(PaymentEntity::isUnconfirmed))
                .map(this::fetchDetails)
                .filter(
                        Predicates.and(PaymentEntity::isPayment, PaymentEntity::hasEIvoiceDetails)
                                .or(PaymentEntity::isEInvoice));
    }
}

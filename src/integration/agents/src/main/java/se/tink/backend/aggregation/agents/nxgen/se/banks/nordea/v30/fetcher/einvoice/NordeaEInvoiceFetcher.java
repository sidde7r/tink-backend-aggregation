package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice;

import static com.google.common.base.Predicates.not;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;

public class NordeaEInvoiceFetcher implements EInvoiceFetcher {
    private final NordeaSEApiClient apiClient;

    public NordeaEInvoiceFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<se.tink.libraries.transfer.rpc.Transfer> fetchEInvoices() {
        return apiClient.fetchPayments().getPayments().stream()
                .filter(not(PaymentEntity::isConfirmed))
                .map(this::fetchDetailsIfNotPlusgiro) // plusgiro does not seem to have an id
                .map(PaymentEntity::toTinkTransfer)
                .collect(Collectors.toList());
    }

    private PaymentEntity fetchDetailsIfNotPlusgiro(PaymentEntity paymentEntity) {
        return paymentEntity.isNotPlusgiro()
                ? apiClient.fetchPaymentDetails(paymentEntity.getId())
                : paymentEntity;
    }
}

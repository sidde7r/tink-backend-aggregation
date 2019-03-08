package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice;

import static com.google.common.base.Predicates.not;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;

public class NordeaEInvoiceFetcher implements EInvoiceFetcher {
    private final NordeaSEApiClient apiClient;

    public NordeaEInvoiceFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<se.tink.libraries.transfer.rpc.Transfer> fetchEInvoices() {
        return apiClient.fetchEInvoice().getEInvoices().stream()
                .filter(not(EInvoiceEntity::isConfirmed))
                .map(this::fetchDetailsIfNotPlusgiro) // plusgiro does not seem to have an id
                .map(EInvoiceEntity::toTinkTransfer)
                .collect(Collectors.toList());
    }

    private EInvoiceEntity fetchDetailsIfNotPlusgiro(EInvoiceEntity eInvoiceEntity) {
        return eInvoiceEntity.isNotPlusgiro()
                ? apiClient.fetchEInvoiceDetails(eInvoiceEntity.getId())
                : eInvoiceEntity;
    }
}

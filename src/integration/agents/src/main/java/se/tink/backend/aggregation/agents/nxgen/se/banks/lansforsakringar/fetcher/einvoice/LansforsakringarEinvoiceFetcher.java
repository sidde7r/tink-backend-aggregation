package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities.EInvoicesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.rpc.FetchEinvoiceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;

public class LansforsakringarEinvoiceFetcher implements EInvoiceFetcher {

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarEinvoiceFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        FetchEinvoiceResponse fetchEinvoiceResponse = apiClient.fetchEinvoices();
        if (fetchEinvoiceResponse.getResponse() == null) {
            return Collections.emptyList();
        }
        return fetchEinvoiceResponse.getResponse().getEInvoices().stream()
                .map(EInvoicesEntity::toTinkTransfer)
                .collect(Collectors.toList());
    }
}

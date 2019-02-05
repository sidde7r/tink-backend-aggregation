package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.i18n.Catalog;

public class IcaBankenEInvoiceFetcher implements EInvoiceFetcher {
    public final Catalog catalog;
    private final IcaBankenApiClient apiClient;

    public IcaBankenEInvoiceFetcher(IcaBankenApiClient apiClient, Catalog catalog) {
        this.apiClient = apiClient;
        this.catalog = catalog;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        return apiClient.fetchEInvoices().toTinkTransfers(catalog);
    }
}

package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Policies;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

public class IcaBankenEInvoiceFetcher implements EInvoiceFetcher {

    private final IcaBankenSessionStorage sessionStorage;
    public final Catalog catalog;
    private final IcaBankenApiClient apiClient;

    public IcaBankenEInvoiceFetcher(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage sessionStorage, Catalog catalog) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.catalog = catalog;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        if (!sessionStorage.hasPolicy(Policies.PAYMENTS)) {
            return Collections.emptyList();
        }
        return apiClient.fetchEInvoices().toTinkTransfers(catalog);
    }
}

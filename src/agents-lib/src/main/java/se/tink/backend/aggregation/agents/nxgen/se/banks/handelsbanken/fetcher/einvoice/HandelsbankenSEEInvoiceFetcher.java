package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.rpc.PendingEInvoicesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEEInvoiceFetcher implements EInvoiceFetcher {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

    public HandelsbankenSEEInvoiceFetcher(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        return sessionStorage.applicationEntryPoint().map(client::pendingEInvoices)
                .map(PendingEInvoicesResponse::toTinkTransfers)
                .orElse(Collections.emptyList());
    }
}

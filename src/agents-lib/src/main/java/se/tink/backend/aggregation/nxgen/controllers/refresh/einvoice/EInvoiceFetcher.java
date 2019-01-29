package se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice;

import java.util.Collection;
import se.tink.libraries.transfer.rpc.Transfer;

public interface EInvoiceFetcher {
    Collection<Transfer> fetchEInvoices();
}

package se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice;

import java.util.Collection;
import se.tink.backend.core.transfer.Transfer;

public interface EInvoiceFetcher {
    Collection<Transfer> fetchEInvoices();
}

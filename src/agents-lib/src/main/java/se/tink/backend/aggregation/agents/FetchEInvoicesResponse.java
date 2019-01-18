package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.core.transfer.Transfer;

public class FetchEInvoicesResponse {
    private final List<Transfer> eInvoices;

    public FetchEInvoicesResponse(List<Transfer> eInvoices) {
        this.eInvoices = eInvoices;
    }

    public List<Transfer> getEInvoices() {
        return eInvoices;
    }
}

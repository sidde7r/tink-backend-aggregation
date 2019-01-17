package se.tink.backend.aggregation.agents;

import java.util.List;
import se.tink.backend.core.transfer.Transfer;

public class FetchEInvoicesResponse {
    private List<Transfer> eInvoices;

    public List<Transfer> getEInvoices() {
        return eInvoices;
    }

    public void setEInvoices(List<Transfer> eInvoices) {
        this.eInvoices = eInvoices;
    }
}

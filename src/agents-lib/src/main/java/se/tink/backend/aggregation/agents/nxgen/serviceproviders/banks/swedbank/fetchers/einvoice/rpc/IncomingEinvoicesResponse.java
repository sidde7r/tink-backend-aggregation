package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IncomingEinvoicesResponse {
    private List<FromAccountGroupEntity> fromAccountGroups;
    private List<EInvoiceEntity> einvoices;

    public List<FromAccountGroupEntity> getFromAccountGroups() {
        return fromAccountGroups;
    }

    public List<EInvoiceEntity> getEinvoices() {
        return einvoices;
    }
}

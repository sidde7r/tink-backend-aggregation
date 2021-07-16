package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.einvoice.entities;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseEntity {
    private int numberOfEInvoices;
    private List<EInvoicesEntity> eInvoices;
    private boolean moreExists;

    public int getNumberOfEInvoices() {
        return numberOfEInvoices;
    }

    public List<EInvoicesEntity> getEInvoices() {
        return Optional.ofNullable(eInvoices).orElse(Lists.newArrayList());
    }

    public boolean isMoreExists() {
        return moreExists;
    }
}

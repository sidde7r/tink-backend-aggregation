package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Objects;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.i18n.Catalog;

@JsonObject
public class EInvoiceBody {

    @JsonProperty("ElectronicInvoices")
    private List<EInvoiceEntity> eInvoices;

    public List<Transfer> toTinkTransfers(Catalog catalog) {
        Preconditions.checkState(eInvoices != null, new ArrayList<>());

        List<Transfer> transfers = new ArrayList<>();
        for (EInvoiceEntity eInvoice : eInvoices) {
            Transfer transfer = eInvoice.toTinkTransfer(catalog);
            transfers.add(transfer);
        }
        return transfers;
    }

    public Optional<EInvoiceEntity> getInvoiceById(String uuid) {
        Preconditions.checkNotNull(uuid);

        for (EInvoiceEntity eInvoice : eInvoices) {
            if (Objects.equal(eInvoice.getUuid(), uuid)) {
                return Optional.of(eInvoice);
            }
        }
        return Optional.empty();
    }

    public List<EInvoiceEntity> getEInvoices() {
        return eInvoices;
    }
}

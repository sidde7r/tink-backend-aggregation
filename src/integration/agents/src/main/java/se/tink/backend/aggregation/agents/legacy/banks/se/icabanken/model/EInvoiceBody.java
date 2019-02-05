package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Objects;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceBody {

    @JsonProperty("ElectronicInvoices")
    private List<EInvoiceEntity> eInvoices;

    public List<Transfer> toTinkTransfers(Catalog catalog) {

        if (eInvoices == null || eInvoices.isEmpty()) {
            return Lists.newArrayList();
        }

        List<Transfer> transfers = Lists.newArrayList();
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

    public void setEInvoices(List<EInvoiceEntity> eInvoices) {
        this.eInvoices = eInvoices;
    }
}

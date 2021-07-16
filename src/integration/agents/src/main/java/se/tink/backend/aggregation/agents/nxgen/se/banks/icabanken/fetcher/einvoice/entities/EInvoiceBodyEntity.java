package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.Objects;
import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonObject
public class EInvoiceBodyEntity {
    @JsonProperty("ElectronicInvoices")
    private List<EInvoiceEntity> eInvoices;

    @JsonIgnore
    public List<Transfer> toTinkTransfers(Catalog catalog) {

        return Optional.ofNullable(eInvoices).orElseGet(Collections::emptyList).stream()
                .map(eInvoice -> eInvoice.toTinkTransfer(catalog))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Optional<EInvoiceEntity> getInvoiceById(String invoiceId) {
        Preconditions.checkNotNull(invoiceId);

        for (EInvoiceEntity eInvoice : eInvoices) {
            if (Objects.equal(eInvoice.getUniqueId(), invoiceId)) {
                return Optional.of(eInvoice);
            }
        }
        return Optional.empty();
    }

    public List<EInvoiceEntity> getEInvoices() {
        return eInvoices;
    }
}

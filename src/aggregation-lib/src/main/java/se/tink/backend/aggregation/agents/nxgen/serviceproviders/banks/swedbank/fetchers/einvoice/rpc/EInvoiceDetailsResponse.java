package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.transfer.Transfer;

@JsonObject
public class EInvoiceDetailsResponse {
    private List<FromAccountGroupEntity> fromAccountGroups;
    private EInvoicePaymentEntity payment;

    public List<FromAccountGroupEntity> getFromAccountGroups() {
        return fromAccountGroups;
    }

    public EInvoicePaymentEntity getPayment() {
        return payment;
    }

    public Optional<Transfer> toEInvoiceTransfer(String currency) {
        return Optional.ofNullable(payment).flatMap(EInvoicePaymentEntity -> EInvoicePaymentEntity.toTinkTransfer(currency));
    }
}

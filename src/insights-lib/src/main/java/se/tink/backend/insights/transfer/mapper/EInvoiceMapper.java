package se.tink.backend.insights.transfer.mapper;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.insights.core.valueobjects.Amount;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.EInvoiceId;

public class EInvoiceMapper {

    public static EInvoice translate(Transfer transfer) {
        return EInvoice.of(
                EInvoiceId.of(transfer.getId().toString()),
                Amount.of(transfer.getAmount().getValue()),
                transfer.getDueDate(),
                transfer.getSourceMessage()
        );
    }

    public static List<EInvoice> translate(List<Transfer> transfers) {
        return transfers.stream().map(EInvoiceMapper::translate).collect(Collectors.toList());
    }
}

package se.tink.backend.insights.transfer;

import java.util.List;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.UserId;

public interface TransferQueryService {
    List<EInvoice> getEInvoices(UserId userId);
}

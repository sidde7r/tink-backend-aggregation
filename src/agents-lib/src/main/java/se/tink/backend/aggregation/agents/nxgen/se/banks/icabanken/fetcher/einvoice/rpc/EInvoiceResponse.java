package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.einvoice.entities.EInvoiceBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EInvoiceResponse extends BaseResponse<EInvoiceBodyEntity> {
}

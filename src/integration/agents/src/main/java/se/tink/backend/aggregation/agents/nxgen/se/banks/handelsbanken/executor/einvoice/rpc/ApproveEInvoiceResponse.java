package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.einvoice.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class ApproveEInvoiceResponse extends BaseResponse {
    public Optional<URL> toSignature() {
        return searchLink(HandelsbankenConstants.URLS.Links.SIGNATURE);
    }
}

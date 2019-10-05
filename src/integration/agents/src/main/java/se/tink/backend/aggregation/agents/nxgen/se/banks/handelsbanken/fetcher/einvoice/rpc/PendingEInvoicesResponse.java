package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.EInvoice;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.einvoice.entities.EInvoicesGrouped;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.transfer.rpc.Transfer;

public class PendingEInvoicesResponse extends BaseResponse {
    private List<EInvoicesGrouped> eInvoicesGrouped;

    public Stream<EInvoice> getEinvoiceStream() {
        return eInvoicesGrouped.stream()
                .map(EInvoicesGrouped::getEInvoices)
                .flatMap(Collection::stream);
    }

    public List<Transfer> toTinkTransfers() {
        return getEinvoiceStream().map(EInvoice::toTinkTransfer).collect(Collectors.toList());
    }

    public Optional<URL> toApproval() {
        return searchLink(HandelsbankenConstants.URLS.Links.APPROVAL);
    }
}

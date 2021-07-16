package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceFetcher;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankDefaultEinvoiceFetcher implements EInvoiceFetcher {
    private final SwedbankSEApiClient apiClient;

    public SwedbankDefaultEinvoiceFetcher(SwedbankSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<Transfer> fetchEInvoices() {
        List<Transfer> eInvoices = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);

            List<EInvoiceEntity> eInvoiceEntities = apiClient.incomingEInvoices();
            if (eInvoiceEntities == null) {
                return Collections.emptyList();
            }

            for (EInvoiceEntity eInvoiceEntity : eInvoiceEntities) {
                Optional.ofNullable(eInvoiceEntity.getLinks())
                        .map(LinksEntity::getNext)
                        .map(apiClient::eInvoiceDetails)
                        .flatMap(
                                eInvoiceDetails ->
                                        eInvoiceDetails.toEInvoiceTransfer(
                                                eInvoiceEntity.getCurrency(),
                                                eInvoiceEntity.getHashedEinvoiceRefNo()))
                        .ifPresent(eInvoices::add);
            }
        }

        return eInvoices;
    }
}

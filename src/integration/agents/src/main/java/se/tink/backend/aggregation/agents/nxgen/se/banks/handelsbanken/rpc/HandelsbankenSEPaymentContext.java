package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEAccountContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public class HandelsbankenSEPaymentContext extends BaseResponse {

    private HandelsbankenSEAccountContext fromAccounts;
    private List<PaymentRecipient> recipients;

    public URL toLookupRecipient() {
        return findLink(HandelsbankenConstants.URLS.Links.LOOKUP_RECIPIENT);
    }

    public Optional<URL> toCreate() {
        return searchLink(HandelsbankenConstants.URLS.Links.CREATE);
    }

    public List<PaymentRecipient> paymentRecipients() {
        return Optional.ofNullable(recipients).orElseGet(Collections::emptyList);
    }

    public List<GeneralAccountEntity> retrieveOwnedSourceAccounts() {
        return Optional.ofNullable(fromAccounts)
                .flatMap(HandelsbankenSEAccountContext::asOwnedAccountEntities)
                .orElseGet(Collections::emptyList);
    }

    public List<GeneralAccountEntity> retrieveDestinationAccounts() {
        return Optional.ofNullable(recipients)
                .map(Collection::stream)
                .map(
                        recipients ->
                                recipients
                                        .map(PaymentRecipient::retrieveGeneralAccountEntities)
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }
}

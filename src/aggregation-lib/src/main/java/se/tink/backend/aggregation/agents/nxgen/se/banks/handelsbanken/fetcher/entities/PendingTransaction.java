package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.entities.HandelsbankenRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.core.Amount;

public class PendingTransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private HandelsbankenRecipient recipient;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    public UpcomingTransaction toTinkTransaction(HandelsbankenSEApiClient client) {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inSEK(-1 * amount.asDouble()))
                .setDate(dueDate)
                .setDescription(recipient.getName())
                .setUpcomingTransfer(
                        client.paymentDetails(this)
                                .filter(PaymentDetails::isChangeAllowed)
                                .map(PaymentDetails::toTransfer)
                                .orElse(null)
                )
                .build();
    }

    public boolean isNotSuspended() {
        return doesNotHaveLinkWith("status=suspended");
    }

    public boolean isNotAbandoned() {
        return doesNotHaveLinkWith("status=abandoned");
    }

    private boolean doesNotHaveLinkWith(String parameter) {
        return !getLinks().stream().anyMatch(link -> link.hasParameter(parameter));
    }

    public Optional<URL> paymentDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.PAYMENT_DETAIL);
    }
}

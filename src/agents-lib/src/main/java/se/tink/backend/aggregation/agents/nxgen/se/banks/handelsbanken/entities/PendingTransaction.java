package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.amount.Amount;
import se.tink.backend.core.transfer.Transfer;

public class PendingTransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private HandelsbankenRecipient recipient;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    public UpcomingTransaction toTinkTransaction(Transfer transfer) {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inSEK(-1 * amount.asDouble()))
                .setDate(dueDate)
                .setDescription(recipient.getName())
                .setUpcomingTransfer(transfer)
                .build();
    }

    public boolean isNotSuspended() {
        return doesNotHaveLinkWith("status=suspended");
    }

    public boolean isNotAbandoned() {
        return doesNotHaveLinkWith("status=abandoned");
    }

    private boolean doesNotHaveLinkWith(String parameter) {
        return !getLinks().values().stream().anyMatch(link -> link.hasParameter(parameter));
    }

    public Optional<URL> toPaymentDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.PAYMENT_DETAIL);
    }

    @JsonIgnore
    public Amount getTinkAmount() {
        if (amount == null) {
            return null;
        }

        return amount.asAmount();
    }
}

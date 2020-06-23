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
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.transfer.rpc.Transfer;

public class PendingTransaction extends BaseResponse {

    private HandelsbankenAmount amount;
    private HandelsbankenRecipient recipient;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    public UpcomingTransaction toTinkTransaction(Transfer transfer) {
        return UpcomingTransaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount.asDouble(), "SEK").negate())
                .setDate(dueDate)
                .setDescription(recipient.getName())
                .setUpcomingTransfer(transfer)
                .build();
    }

    boolean isNotSuspended() {
        return doesNotHaveLinkWith("status=suspended");
    }

    boolean isNotAbandoned() {
        return doesNotHaveLinkWith("status=abandoned");
    }

    private boolean doesNotHaveLinkWith(String parameter) {
        return getLinks().values().stream().noneMatch(link -> link.hasParameter(parameter));
    }

    public Optional<URL> toPaymentDetails() {
        return searchLink(HandelsbankenConstants.URLS.Links.PAYMENT_DETAIL);
    }

    @JsonIgnore
    public ExactCurrencyAmount getTinkAmount() {
        if (amount == null) {
            return null;
        }

        return amount.toExactCurrencyAmount();
    }
}

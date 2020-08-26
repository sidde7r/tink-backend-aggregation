package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidatePaymentDateResponse extends AbstractResponse {
    private Date bookingDate;

    public boolean isTransferDateSameAsBookingDate(Date transferDate) {
        return bookingDate.equals(transferDate);
    }

    public Date getBookingDate() {
        return bookingDate;
    }
}

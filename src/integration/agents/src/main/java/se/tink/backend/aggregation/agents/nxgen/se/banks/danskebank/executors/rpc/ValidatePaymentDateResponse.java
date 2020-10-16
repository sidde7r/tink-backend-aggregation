package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.DateUtils;

@Getter
@JsonObject
public class ValidatePaymentDateResponse extends AbstractResponse {
    private Date bookingDate;

    @JsonIgnore
    public boolean isTransferDateSameAsBookingDate(Date transferDate) {
        return DateUtils.isSameDay(transferDate, bookingDate);
    }
}

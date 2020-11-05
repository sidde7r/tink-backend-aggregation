package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import lombok.Getter;
import org.apache.commons.lang3.time.DateUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class ValidatePaymentDateResponse extends AbstractResponse {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
    private Date bookingDate;

    @JsonIgnore
    public boolean isTransferDateSameAsBookingDate(Date transferDate) {
        return DateUtils.isSameDay(transferDate, bookingDate);
    }
}

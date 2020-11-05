package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ValidatePaymentDateResponseTest {

    private static final String DATE_RESPONSE =
            "{\n"
                    + "  \"bookingDate\": \"2020-10-28\",\n"
                    + "  \"eupToken\": null,\n"
                    + "  \"responseCode\": 200,\n"
                    + "  \"responseMessage\": \"\",\n"
                    + "  \"statusCode\": 200,\n"
                    + "  \"traceId\": null\n"
                    + "}";

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DIFFERENT_TRANSFER_DATE = "2020-10-29 10:10:10";

    @Test
    public void testForDifferentDate() throws ParseException {
        ValidatePaymentDateResponse fetchPaymentResponse =
                SerializationUtils.deserializeFromString(
                        DATE_RESPONSE, ValidatePaymentDateResponse.class);
        Date transferDate = new SimpleDateFormat(DATE_TIME_FORMAT).parse(DIFFERENT_TRANSFER_DATE);

        Assert.assertFalse(fetchPaymentResponse.isTransferDateSameAsBookingDate(transferDate));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForNullDate() {
        ValidatePaymentDateResponse fetchPaymentResponse =
                SerializationUtils.deserializeFromString(
                        DATE_RESPONSE, ValidatePaymentDateResponse.class);
        Assert.assertFalse(fetchPaymentResponse.isTransferDateSameAsBookingDate(null));
    }
}

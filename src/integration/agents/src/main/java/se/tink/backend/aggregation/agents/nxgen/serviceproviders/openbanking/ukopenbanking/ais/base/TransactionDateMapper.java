package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import se.tink.libraries.chrono.AvailableDateInformation;

/*
   This class was implemented to deal with default transaction timestamps returned by banks:
       A DateTime element has been used instead of a complex choice element of Date and DateTime.
       Where time elements do not exist in ASPSP systems, the time portion of the DateTime element will be defaulted to 00:00:00+00:00.
       https://openbankinguk.github.io/read-write-api-site3/v3.1.8/resources-and-data-models/aisp/Transactions.html
   According to our documentation in such cases:
       This field (bookedDateTime and valueDateTime) is only returned when a valid timestamp is provided by the financial institution.
       https://docs.tink.com/api#data-v2/transaction/list-transactions/response-listtransactionsresponse/transaction
*/

public class TransactionDateMapper {

    public static AvailableDateInformation prepareTransactionDate(Instant transactionDateTime) {
        return new AvailableDateInformation()
                .setDate(LocalDateTime.ofInstant(transactionDateTime, ZoneOffset.UTC).toLocalDate())
                .setInstant(handleInstantWithDefaultTime(transactionDateTime));
    }

    private static Instant handleInstantWithDefaultTime(Instant transactionDateTime) {
        ZonedDateTime zonedDateTime = transactionDateTime.atZone(ZoneOffset.UTC);
        if (zonedDateTime.getHour() == 0
                && zonedDateTime.getMinute() == 0
                && zonedDateTime.getSecond() == 0) {
            return null;
        }
        return transactionDateTime;
    }
}

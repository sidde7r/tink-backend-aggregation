package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import se.tink.libraries.transfer.mocks.TransferMock;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.transfer.enums.TransferPayloadType;
import static org.assertj.core.api.Assertions.assertThat;

public class EInvoiceDetailsResponseTest {
    private static final String RESPONSE_EXAMPLE = "{\"MagicKey\": \"asdf\",\"ServerTime\": \"\\/Date(1462877697128+0200)\\/\",\"Status\": {\"StatusCode\": 0,\"StatusText\": null},\"FromAccounts\": [{\"AccountId\": \"1234\",\"AccountName\": \"Account Name\",\"AccountNumber\": \"12780123456\"}, {\"AccountId\": \"adsf\",\"AccountName\": \"adsf\",\"AccountNumber\": \"adsf\"}, {\"AccountId\": \"adsf\",\"AccountName\": \"adsf\",\"AccountNumber\": \"asdf\"}],\"Invoice\": \"https:\\/\\/mobil.danskebank.se\\/blablablalblalba\",\"Transaction\": {\"Amount\": 1.00,\"Receiver\": \"TINK AB\",\"Reference\": \"123456789\",\"Text\": \"\",\"Time\": \"\\/Date(1464213600000+0200)\\/\",\"ToAccountId\": \"687-5496\"}}";

    @Test
    public void deserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        EInvoiceDetailsResponse eInvoiceDetailsResponse = mapper
                .readValue(RESPONSE_EXAMPLE, EInvoiceDetailsResponse.class);

        assertThat(eInvoiceDetailsResponse.getFromAccounts()).hasSize(3);
        assertThat(eInvoiceDetailsResponse.getTransaction()).isNotNull();
    }

    @Test
    public void deserializeToTransfer() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Transfer transfer = mapper
                .readValue(RESPONSE_EXAMPLE, EInvoiceDetailsResponse.class)
                .toEInvoiceTransfer("abcd");

        Transfer expected = TransferMock.eInvoice()
                .from(null)
                .to(AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "6875496", "TINK AB"))
                .withAmount(Amount.inSEK(1.0))
                .withSourceMessage("TINK AB")
                .withDestinationMessage("123456789")
                .withDueDate(new DateTime().withMillisOfDay(0).withZone(DateTimeZone.forOffsetHours(2))
                        .withYear(2016).withMonthOfYear(5).withDayOfMonth(26)
                        .withHourOfDay(12).toDate())
                .build();

        expected.setId(transfer.getId());
        expected.addPayload(TransferPayloadType.PROVIDER_UNIQUE_ID, "abcd");
        assertThat(transfer).isEqualToComparingFieldByField(expected);
    }
}

package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.Payment;

//import java.util.Calendar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferExecutor;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.i18n.Catalog;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BelfiusTransferExecutorTest extends BelfiusTest {

    private static String anyNoneBlank() {
        return argThat(StringUtils::isNoneBlank);
    }

    @Test
    public void transferTest() throws Exception {
        BelfiusTransferDestinationFetcher btdf = new BelfiusTransferDestinationFetcher(apiClient);
        Transfer t = new Transfer();
        AccountIdentifier destinationAcc = new IbanIdentifier("", "");
        AccountIdentifier sourceAcc = new IbanIdentifier("", "");
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setValue(0.02);
        Date d = CountryDateUtils.getBelgianDateUtils().getToday();
        d.setDate(d.getDate() + 9);
        t.setDestination(destinationAcc);
        t.setSource(sourceAcc);
        t.setAmount(amount);
        t.setDueDate(d);
        destinationAcc.setName("");
        t.setDestinationMessage("FromTink");
        t.getDestination().setName("");
        autoAuthenticate();
        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(apiClient, null, sessionStorage,
                new Catalog(new Locale("fr", "BE")));

        bte.executeTransfer(t);
        verify(this.apiClient, times(1))
                .login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }

    @Test
    public void transferTestSign() throws Exception {
        BelfiusTransferDestinationFetcher btdf = new BelfiusTransferDestinationFetcher(apiClient);
        Transfer t = new Transfer();
        AccountIdentifier destinationAcc = new IbanIdentifier("", "");
        AccountIdentifier sourceAcc = new IbanIdentifier("", "");
        Amount amount = new Amount();
        amount.setCurrency("");
        amount.setValue(1000.0);
        Date d = CountryDateUtils.getBelgianDateUtils().getToday();
        d.setDate(d.getDate() + 9);
        t.setDestination(destinationAcc);
        t.setSource(sourceAcc);
        t.setAmount(amount);
        t.setDueDate(d);
        destinationAcc.setName("");
        t.setDestinationMessage("TestFromTink");
        t.getDestination().setName("");
        autoAuthenticate();
        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(apiClient, null, sessionStorage,
                new Catalog(new Locale("fr", "BE")));

        bte.executeTransfer(t);
        verify(this.apiClient, times(1))
                .login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }

    @Test
    public void transferTestStructuredMessage() throws Exception {
        BelfiusTransferDestinationFetcher btdf = new BelfiusTransferDestinationFetcher(apiClient);

        Transfer t = new Transfer();
        AccountIdentifier destinationAcc = new IbanIdentifier("", "");
        AccountIdentifier sourceAcc = new IbanIdentifier("", "");
        Amount amount = new Amount();
        amount.setCurrency("EUR");
        amount.setValue(0.02);
        Date d = CountryDateUtils.getBelgianDateUtils().getToday();
        d.setDate(d.getDate() + 9);
        t.setDestination(destinationAcc);
        t.setSource(sourceAcc);
        t.setAmount(amount);
        t.setDueDate(d);
        destinationAcc.setName("");
        t.setDestinationMessage("123/3456/12328");
        autoAuthenticate();
        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(apiClient, null, sessionStorage,
                new Catalog(new Locale("fr", "BE")));

        bte.executeTransfer(t);
        verify(this.apiClient, times(1))
                .login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }
}

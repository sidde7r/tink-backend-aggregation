package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.Payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.date.CountryDateUtils;
import se.tink.libraries.i18n.Catalog;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BelfiusTransferExecutorTest extends BelfiusTest {

    private static String anyNoneBlank() {
        return argThat(StringUtils::isNoneBlank);
    }

    protected final SupplementalInformationController supplementalInformationController = mock(SupplementalInformationController
            .class);

    private static final ObjectMapper mapper = new ObjectMapper();

    // TODO Move this out to test helper.
    private ProviderConfig readProvidersConfiguration(String market) {
        String providersFilePath = "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
        File providersFile = new File(providersFilePath);
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
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
        ProviderConfig marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-belfius-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());

        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(
                apiClient,
                sessionStorage,
                new Catalog(new Locale("fr", "BE")),
                new SupplementalInformationHelper(provider,supplementalInformationController));

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
        ProviderConfig marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-belfius-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(
                apiClient,
                sessionStorage,
                new Catalog(new Locale("fr", "BE")),
                new SupplementalInformationHelper(provider,supplementalInformationController));

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
        ProviderConfig marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-belfius-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());
        BelfiusTransferExecutor bte = new BelfiusTransferExecutor(
                apiClient,
                sessionStorage,
                new Catalog(new Locale("fr", "BE")),
                new SupplementalInformationHelper(provider,supplementalInformationController));

        bte.executeTransfer(t);
        verify(this.apiClient, times(1))
                .login(anyNoneBlank(), anyNoneBlank(), anyNoneBlank());
    }
}

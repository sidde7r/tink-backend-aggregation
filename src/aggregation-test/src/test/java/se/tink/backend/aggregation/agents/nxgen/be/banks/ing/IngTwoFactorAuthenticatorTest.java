package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ProviderConfigModel;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller.IngCardReaderAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.i18n.Catalog;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class IngTwoFactorAuthenticatorTest {
    private Credentials credentials;
    private IngApiClient apiClient;
    private PersistentStorage persistentStorage;
    private IngHelper ingHelper;

    @Before
    public void setUp() throws Exception {
        IngTestConfig ingTestConfig = IngTestConfig.createForTwoFactorAuthentication();
        this.credentials = ingTestConfig.getTestUserCredentials();
        this.apiClient = ingTestConfig.getTestApiClient();
        this.persistentStorage = ingTestConfig.getTestPersistentStorage();
        this.ingHelper = ingTestConfig.getTestIngHelper();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Save the following values to the test config in order to auto authenticate: " + persistentStorage);
        System.out.println("VirtualCardnumber: " + persistentStorage.get(IngConstants.Storage.VIRTUAL_CARDNUMBER));
        persistentStorage.keySet().iterator().forEachRemaining(key -> System.out.println("this.persistentStorage.put(\"" + key + "\", \"" + persistentStorage.get(key) + "\");" ));
    }

    // Debug
    // Get OTP from keyreader
    // Insert card, pin, identify gives OTP
    // Lock at the console and put a break point before "Challange me!" is used, get challenge from console
    // Insert card, pin, sign challenge, Change "Challenge me!" to response

    @Test
    public void authenticate_2Factor() throws Exception {
        IngCardReaderAuthenticator authenticator = new IngCardReaderAuthenticator(apiClient,
                persistentStorage, ingHelper);

        SupplementalInformationController supplementalInformationController =
                mock(SupplementalInformationController.class);

        when(supplementalInformationController.askSupplementalInformation(any()))
                .thenReturn(ImmutableMap.of("otp", "OTP!"))
                .thenReturn(ImmutableMap.of("challengeResponse", "Challenge me!"));

        ProviderConfigModel marketProviders = readProvidersConfiguration("be");
        Provider provider = marketProviders.getProvider("be-ing-cardreader");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());

        new IngCardReaderAuthenticationController(
                authenticator,
                new SupplementalInformationHelper(provider, supplementalInformationController)).authenticate(credentials);

        assertFalse(persistentStorage.isEmpty());
    }

    // TODO Move this out to test helper.
    private ProviderConfigModel readProvidersConfiguration(String market) {
        String providersFilePath = "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
        File providersFile = new File(providersFilePath);
        try {
            return mapper.readValue(providersFile, ProviderConfigModel.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private static final ObjectMapper mapper = new ObjectMapper();
}

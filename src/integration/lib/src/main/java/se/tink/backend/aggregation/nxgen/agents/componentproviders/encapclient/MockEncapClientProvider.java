package se.tink.backend.aggregation.nxgen.agents.componentproviders.encapclient;

import static se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration.IOS_ENCAP_MOCK;

import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.MockEncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapSoapUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.MockEncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.MockEncapMessageUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;

public final class MockEncapClientProvider implements EncapClientProvider {

    private EncapClient encapClient;

    @Override
    public EncapClient getEncapClient(
            PersistentStorage persistentStorage,
            EncapConfiguration configuration,
            DeviceProfile deviceProfile,
            TinkHttpClient httpClient) {
        if (encapClient == null) {
            httpClient.disableSignatureRequestHeader();
            MockEncapStorage storage = new MockEncapStorage(persistentStorage);

            // I do not like the solution below - all the values should be injected.
            // It should be a part of different PR to change that.
            EncapConfiguration mockConfiguration = new MockEncapConfiguration();
            EncapSoapUtils soapUtils = new EncapSoapUtils(mockConfiguration, storage);
            EncapMessageUtils messageUtils =
                    new MockEncapMessageUtils(
                            mockConfiguration, storage, httpClient, IOS_ENCAP_MOCK);
            encapClient = new EncapClient(storage, soapUtils, messageUtils);
        }
        return encapClient;
    }
}

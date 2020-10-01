package se.tink.backend.aggregation.nxgen.agents.componentproviders.encapclient;

import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.MockEncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapSoapUtils;
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
            EncapSoapUtils soapUtils = new EncapSoapUtils(configuration, storage);
            EncapMessageUtils messageUtils =
                    new MockEncapMessageUtils(configuration, storage, httpClient, deviceProfile);
            encapClient = new EncapClient(storage, soapUtils, messageUtils);
        }
        return encapClient;
    }
}

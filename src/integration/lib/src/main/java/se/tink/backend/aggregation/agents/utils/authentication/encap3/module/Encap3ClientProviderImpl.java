package se.tink.backend.aggregation.agents.utils.authentication.encap3.module;

import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.storage.EncapStorage;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.BaseEncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapSoapUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;

public final class Encap3ClientProviderImpl implements EncapClientProvider {

    private EncapClient encapClient;

    @Override
    public EncapClient getEncapClient(
            PersistentStorage persistentStorage,
            EncapConfiguration configuration,
            DeviceProfile deviceProfile,
            TinkHttpClient httpClient) {
        if (encapClient == null) {
            httpClient.disableSignatureRequestHeader();
            EncapStorage storage = new EncapStorage(persistentStorage);
            EncapSoapUtils soapUtils = new EncapSoapUtils(configuration, storage);
            EncapMessageUtils messageUtils =
                    new BaseEncapMessageUtils(configuration, storage, httpClient, deviceProfile);
            encapClient = new EncapClient(storage, soapUtils, messageUtils);
        }

        return encapClient;
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;

public class HandelsbankenNOEncapConfiguration implements EncapConfiguration {
    @Override
    public String getEncapApiVersion() {
        return EncapConstants.ENCAP_API_VERSION;
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return EncapConstants.CREDENTIALS_APP_NAME_FOR_EDB;
    }

    @Override
    public String getAppId() {
        return EncapConstants.APP_ID;
    }

    @Override
    public String getRsaPubKeyString() {
        return EncapConstants.RSA_PUB_KEY_STRING;
    }

    @Override
    public String getClientPrivateKeyString() {
        return null;
    }

    @Override
    public String getLocale() {
        return "nb_SE";
    }
}

package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants.EncapConstants;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConfiguration;

public class HandelsbankenNOEncapConfiguration implements EncapConfiguration {

    @Override
    public String getApplicationVersion() {
        return EncapConstants.applicationVersion;
    }

    @Override
    public String getEncapApiVersion() {
        return EncapConstants.encapApiVersion;
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return EncapConstants.credentialsAppNameForEdb;
    }

    @Override
    public String getCredentialsBankCodeForEdb() {
        return EncapConstants.credentialsBankCodeForEdb;
    }

    @Override
    public String getSaIdentifier() {
        return EncapConstants.saIdentifier;
    }

    @Override
    public String getAppId() {
        return EncapConstants.appId;
    }

    @Override
    public String getRsaPubKeyString() {
        return EncapConstants.rsaPubKeyString;
    }

    @Override
    public String getClientPrivateKeyString() {
        return EncapConstants.clientPrivateKeyString;
    }
}

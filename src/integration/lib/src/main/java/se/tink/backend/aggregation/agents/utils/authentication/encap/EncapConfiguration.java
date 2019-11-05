package se.tink.backend.aggregation.agents.utils.authentication.encap;

import se.tink.backend.aggregation.configuration.ClientConfiguration;

public interface EncapConfiguration extends ClientConfiguration {

    String getApplicationVersion(); // meta information in message exchange

    String getEncapApiVersion(); // meta information in message exchange

    String getCredentialsAppNameForEdb(); // eg. AKTIA_MOBILE_BANK

    String getCredentialsBankCodeForEdb(); // eg. (null) for Aktia bank

    String getSaIdentifier(); // eg. samobile_aktia_ios_v1

    String getAppId(); // eg. com.aktia.mobilebank

    String getRsaPubKeyString(); // base64 encoded

    String getClientPrivateKeyString(); // base64 encoded
}

package se.tink.backend.aggregation.agents.utils.authentication.encap3;

public interface EncapConfiguration {

    String getEncapApiVersion(); // meta information in message exchange

    String getCredentialsAppNameForEdb(); // eg. AKTIA_MOBILE_BANK

    String getAppId(); // eg. com.aktia.mobilebank

    String getRsaPubKeyString(); // base64 encoded

    String getClientPrivateKeyString(); // base64 encoded

    String getLocale(); // eg. en_SE
}

package se.tink.backend.aggregation.agents.utils.authentication.encap3.utils;

import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;

public class MockEncapConfiguration implements EncapConfiguration {

    @Override
    public String getEncapApiVersion() {
        return "3.5.4";
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return "SOR_MOBILBANK";
    }

    @Override
    public String getAppId() {
        return "no.sor.mobilbank-smbm";
    }

    @Override
    public String getRsaPubKeyString() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAv0zsNwsaDIgQ/6DKhpVeqdfRf"
                + "8Xd6Bl+azzeTXA6jA7jzz65FmOWIKWDxj+NJDGvgbqYpawpLus1nYA/OzB9n82CGz/lFgx"
                + "r//0JbASQP2QnCr19p0EXtwAHI1ctAFW3rxeR/+Y1Ji1Qa5h6pmuWggyC9TGNcrsrk8zRV"
                + "Z9GBTavkQzDu4oxznfw9ERmWjkaYdGst7ULaH5rpPRuSiOAK2wHjP0yRrK1hSbNsedTCSR"
                + "jDXl3/ISc12E9RNMKwk4YHFXhy8kqBwTW8rgDAaZdIWuqj650aYOGD4yDI3Fm1+yyIKAEq"
                + "/f5nf7i+K8ZasjcqJ62nW3MV3cjJ/x2yUM8FwIDAQAB";
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

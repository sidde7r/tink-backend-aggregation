package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.encap;

import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;

public class SpankkiEncapConfiguration implements EncapConfiguration {
    @Override
    public String getEncapApiVersion() {
        return "3.9.2";
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return null;
    }

    @Override
    public String getAppId() {
        return "fi.smobiili";
    }

    @Override
    public String getRsaPubKeyString() {
        return "MIICgzCCAWsCAQAwPjE8MDoGA1UEAxMzcmVnaXN0cmF0aW9uSWQ6YjMzYmU4ZWE"
                + "tNjU2Ni00Mjc3LWI1M2EtOTg1Yjk1YmUwYjAwMIIBIjANBgkqhkiG9w0BAQEFAAO"
                + "CAQ8AMIIBCgKCAQEAtxciMyJ28Ay3ZTTEoDaBA0DyRprVHanUY8IoS8lT37g/FQb"
                + "DDtkSg3V/i0CawV4XPaAEsP4KlardY45LzAHp7GcSROcVsEKcz+v9u6+q9aozodv"
                + "HX7aDR/nQokCv2EnD3hgY18KXcoBRdBeMw4J9knnx6B0PXL+sbI8LRR9GCYhIL5S"
                + "szztpaMMV+iVpqmXoX/rkzMXbcK6Exms6Ac/Ipc4f/QeELkeAl9tD1WnfFWQCvSP"
                + "fIoHyHDkQWYZs888Afwfi32T24ifp6VAyOZdZ7fV7dqcnJkwTlUugHKKWZ9l0oL5"
                + "rb6bPgEUH43qCB2kXb1VgxxKWDTjCKwtcUB+wMwIDAQABoAAwDQYJKoZIhvcNAQE"
                + "LBQADggEBAEYKI+QWschZp9AVXl8UFzfFQJ97Fz0AbisgEO7gLUatnbYPJkYR3Aw"
                + "6Eu17SZTN0T9i8lWPIC0oSMSRY85f9yco8FEWtcgV3k7IR5eZN6xYt41kWhNOjng"
                + "xq7N2LzlWlKA/6JonD4Cq/W+w0gbV+AEdCD9vwoZD2Q2skR01ZiF1fTHltROlfEh"
                + "TO9JcoSSCAYzzbNIqaRFMbKdWUiXp5M/W6aPY6NtpndHT81TycBHoyJXqmRWWstC"
                + "QsGcKmCZVyV+Ks2zwCC13cxZGuElse/0+l0YbfB+Fhp3cXBNjUMcI++MPRu4cLdD"
                + "4BspkD8ZGrOY/RHztBrTRYboV6QK1SoI=";
    }

    @Override
    public String getClientPrivateKeyString() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxhAOFIxrrUcm5OMbG5B"
                + "kpJ8NaWT6uyxwScfYtgHj90cfVOaQJ/buwLETLZ+Gju4IfUK5zV96lA1hJAdWzKf"
                + "/kcyLSdWB4PDzCDk9xUE9B5V+53tCu4eQdmY+gAVbuTb0+94C8V4G4vmKOKDXDQ2"
                + "iRGubXto9oKrkHYZUUkeFQcEGqF6ILY9SjgE0CbRsJny7cZP3hnLiZcbEB+Uy4zF"
                + "Ccuv1ZeaGu9OjqZ1O3YOFpWBcZwAva8ACS/h0uzWCkhLMICeYoc+TFkNd2sxXR9b"
                + "/QBt1JQRvaGq0vI00+1Mj92bH3ViO2VUEiGq2ENXGSrlYRaqwIsF75QT4xwA7FJ4"
                + "4CwIDAQAB";
    }

    @Override
    public String getLocale() {
        return null;
    }
}

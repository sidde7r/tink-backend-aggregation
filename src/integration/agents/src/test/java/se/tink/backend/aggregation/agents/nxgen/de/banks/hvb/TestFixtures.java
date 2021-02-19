package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Ignore;

@Ignore
public class TestFixtures {

    public static String givenBaseUrl() {
        return "https://my.hypovereinsbank.de:443/mfp/api";
    }

    public static MultivaluedMap<String, Object> givenStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle(
                "User-Agent",
                "HVB Banking/4.2.7 (iPhone; iOS 13.5.1; Scale/3.00),HVB Banking/4.2.7 (iPhone; iOS 13.5.1; Scale/3.00),HVB Banking/4.2.7 (iPhone; iOS 13.5.1; Scale/3.00)/WLNativeAPI/8.0.0.00.2016-01-24T11:48:54Z");
        headers.putSingle("Accept-Language", "en-SE;q=1,en-SE;q=1,en");
        headers.putSingle("Accept-Encoding", "br, gzip, deflate");
        headers.putSingle("Connection", "keep-alive");
        headers.putSingle("X-Requested-With", "XMLHttpRequest");
        return headers;
    }

    public static String givenDeviceId() {
        return "0CA60A31-ADB5-45E6-B7AE-3A4D7C22BB81";
    }

    public static byte[] givenJsonRegistrationCallPayloadEntity() {
        return ("{\"application\":{\"id\":\"de.unicredit.apptan\",\"clientPlatform\":\"ios\","
                        + "\"version\":\"4.2.7\"},\"device\":{"
                        + "\"id\":\"0CA60A31-ADB5-45E6-B7AE-3A4D7C22BB81\","
                        + "\"platform\":\"ios 13.5.1\",\"hardware\":\"iPhone\"},"
                        + "\"attributes\":{\"sdk_protocol_version\":1}}")
                .getBytes();
    }

    public static String givenBase64EncodedRegistrationCallPayloadEntity() {
        return "eyJhcHBsaWNhdGlvbiI6eyJpZCI6ImRlLnVuaWNyZWRpdC5hcHB0YW4iLCJjbGllbnRQbGF0Zm9ybSI6ImlvcyIsInZlcnNpb24iOiI0LjIuNyJ9LCJkZXZpY2UiOnsiaWQiOiIwQ0E2MEEzMS1BREI1LTQ1RTYtQjdBRS0zQTREN0MyMkJCODEiLCJwbGF0Zm9ybSI6ImlvcyAxMy41LjEiLCJoYXJkd2FyZSI6ImlQaG9uZSJ9LCJhdHRyaWJ1dGVzIjp7InNka19wcm90b2NvbF92ZXJzaW9uIjoxfX0=";
    }

    public static String givenClientId() {
        return "e7a14a8a-e61b-4e76-a84c-f714417a6c9f";
    }

    public static String givenDirectBankingNumber() {
        return "12345678";
    }

    public static String givenPin() {
        return "1234";
    }

    public static String givenApplicationSessionId() {
        return "3a6692e6";
    }

    public static String givenSessionId() {
        return "sessionId";
    }

    public static String givenCode() {
        return "12234567865433";
    }

    public static KeyPair givenKeyPairMock() {
        RSAPrivateCrtKey rsaPrivateKeyMock = mock(RSAPrivateCrtKey.class);
        when(rsaPrivateKeyMock.getPublicExponent()).thenReturn(new BigInteger("1"));
        when(rsaPrivateKeyMock.getModulus()).thenReturn(new BigInteger("1"));
        return new KeyPair(null, rsaPrivateKeyMock);
    }

    public static String givenAuthorization() {
        return "Bearer eyJhbGciOiJSUzI1NiIsImp3ayI6eyJrdHkiOiJSU0EiLCJlIjoiQV";
    }

    public static String givenIban() {
        return "DE26760200700023281111";
    }

    public static String givenBranchId() {
        return "0460";
    }

    public static String givenCurrency() {
        return "EUR";
    }
}

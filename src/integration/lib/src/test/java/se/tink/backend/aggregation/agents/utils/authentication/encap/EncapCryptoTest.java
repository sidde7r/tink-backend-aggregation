package se.tink.backend.aggregation.agents.utils.authentication.encap;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Map;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public class EncapCryptoTest {
    // Test data represented in hex
    private static final String rand16BytesKeyInHex = "f892c67b120e67f70db4df3d862d5597";
    private static final String rand16BytesIvInHex = "bcdb975f8e37e342d4697c83170043e0";

    // Test data represented as array of bytes
    private static byte[] rand16BytesKey;
    private static byte[] rand16BytesIv;

    // Test data represented in base64, from Charles logs
    private static final String urlDecodedEMDResponse =
            "uuHc15btVnbrReUUlrJ4bRRO37Q9KNUBMYDDtM/uhKXANDtPRZb9DX0XUA27x1KSvQ5phA/mQ9WqMnmiGEu0W2I4aQMsnW17evhbUZw6vq64dcsAM5P6X6HYP3RExjKpcYsHAYhG8BXQwpKusktLnrVcVYGwlXCqj6q97/gH23egSnI+Zopi5dl6yUgRF/i8RUcyA9JmImB29qQ9HDPYmZjyTZtcgyk0G2SogdOThsa4o8cm1uk2UGV1GJge2es5UgqvKHUPGdRwyw0jOKRvmoZimDypG61Jc2guyVxlkLpbormE1jzJBN5wTm9o3gTEVxwtQl2PZtnzU7pny0PcNjpHQ0G9pP8HiLoipz7MuvtZdjloRZJyTa6UZq9/CqlLlhSyGGDLesLsvwxB0knRXgiR2EBvXMviL6OXiXoAhsCuJjS0w1f85vzIDd0DvMnR2bHNCZQbCEFsHutcwefF5i/SvXWQdobLKZh1sCsLSq7X5CvfZCxBZNevT3KxSh4Qsc1yLWENi2ssiM343QJZ0t2bggxBZwVxv28rkG58wf8Ce+NbnNUK1EyCnceEhFO1bxCzlW3deZVs/liNGuqXj8yhlzTYjZdAlOeFnTR4ELtXthjNA/Gz7LKwFcB7+P5scbfdsopqiMSBHhXNJ4UnekaKj5KW0othcbXe4CRLfqY=";
    private static final String urlDecodedMACResponse =
            "nPx60Ken9MmEgVQSvKlvPkuR6qW0CtmhShOgEgDLPnU=";
    private static final String expectedEMDInBase64 =
            "aS7+iuCjE+80XrWl+sWbksMJAw7YZUNEyu7rpygS2Kxu/JOVg1pAdIxiOXRSVms6qnmd3T946Indb+yL0SwDG7ImpOlnNKsWC9VcGDwJN2PfPixTZjf73OcswZtDc7kqNWVArp9fJlFsro0UbRlQo+Dc2bExTxPfPPtc6JyRzo9f5wS96k3x1++RKq9cMg+IaemtxfrJ8n0b8nAprwCo3pJ9ac8S2XxKtZhtmpYO3j7iMRA8dDDG6xfaQ64lvv2goFoGw0M6ninRu8wrRng/S9DnQD1RLz8WXklzTzF6gtGb9f4CSnPj9XO5LaSVo6CPBeVnDXq1xwc3ZNRH2ghRPm9f7+ZfUyF+ep8qtJRwwumciJHvTnsMYFqlX+zYA3nl5p6qTPWm2sA4LIRnVZA5QpXIfYnNmBOFS47t3SmRoLv2UIE+kw2aDJmaNOUW/gMPwJ6g6uJ5id6vIuopAh8iHXSLtszPiy6fta+nWP8FjR6xosKjX0ZOKO/WDXGiqRtfrWe4HbNx5XyZ3wC5vpu3DECNPW6xJQhHBbrtrkC3w8B6J56dRt3/iTtrlavKn2fZpv8uygNQo28AR+3l9an5pkzfDPgtssr6XbctjeKmftQYyNGpNpBLtwD951QcBaC0pAUFGWmwREm3jBZioQmnjAp411rAvXLLZbe+Gj3p+xO4M/vGZ6mKaTGJHMPkchFzPmTn4gbrEUr2XSWjZ5M0cXF+N5hs4l06kbOUI7fv6HvW9dLrJqIAB1N97aMw2EsPAcQvGINFc4e8Yupw+ENByJviqSi/xAcPLMrBJFUVhkphxrHQopARR75KmS2+0bOJMC69GAE9SCukLztwmjVlF4lCXdYoXOQQEUKFpIUxD2ckkzXKdgC5lsbTpmgclhDvf4j5hP5E9EgjZNG1Sng8V+y2i9Rieingb7APXKZh7I4C70/kVygwoCkvhC1l5MqP";
    private static final String expectedMACInBase64 =
            "rPmZwX4Tj6bpxKYeFtECKFIzkNCrzA0TYaxhyzHni0k=";
    private static final String expectedPKHInBase64 =
            "mfHdGJnijG2ScYKOr+Z/ArY3zeNobUj8hFwCr3wIMv8=";
    private static final String expectedResponseEMDInHex =
            "295d7d270a7b0a202022636f6465223a20302c0a2020226f75746461746564223a2066616c73652c0a202022726573756c74223a207b0a2020202022636c69656e7453616c7443757272656e744b65794964223a203434373835373831382c0a2020202022623634436c69656e7453616c7443757272656e744b6579223a20224758496b336e73767536424158673437333442464d773d3d222c0a2020202022707572706f7365223a20312c0a2020202022746f74616c417474656d707473223a20332c0a202020202272656d61696e696e67417474656d707473223a20332c0a202020202261757468656e7469636174696f6e576974686f757450696e223a20747275652c0a2020202022616c6c6f776564417574684d6574686f6473223a205b0a202020202020224445564943453a50494e222c0a20202020202022444556494345220a202020205d2c0a20202020226236344f74704368616c6c656e6765223a20226b7675475444524c347071367a7a326d77534a6c31513d3d222c0a2020202022623634436c69656e7453616c744e6578744b6579223a2022576365516b55784e704c652b55665a6e7a487a326c513d3d222c0a2020202022636c69656e7453616c744e6578744b65794964223a202d313336313231393331372c0a2020202022706c7567696e223a207b7d0a20207d0a7d";

    private static final String inputDataInPlainText =
            "applicationId=encap&clientOnly=true&clientSaltCurrentKeyId=447857818&device.ApplicationHash=Xb0YQ4dJkynrezUlydX3%2FAw0TjnPpgZM%2BnNE1x0%2BHFc%3D&device.DeviceHash=qzgX1%2FPvnIkX%2FFa7x4iOZTi%2BGfGrobaN8EGCJ0Ob7Vc%3D&device.DeviceManufacturer=Apple&device.DeviceModel=iPhone9%2C3&device.DeviceName=Tommy%E2%80%99s%20iPhone&device.DeviceUUID=F038938B-F770-44CF-B9CF-6F929F584E5C&device.IsRootAvailable=true&device.OperatingSystemName=iOS&device.OperatingSystemType=iOS&device.SignerHashes=&device.SystemVersion=10.1.1&device.UserInterfaceIdiom=0&meta.applicationVersion=11408&meta.encapAPIVersion=3.3.5&operation=IDENTIFY&purpose=1&registrationId=a056b316-052f-4659-abc6-c2b1c214d53a&response.requireToken=true";

    @Before
    public void setUp() throws DecoderException {
        rand16BytesKey = Hex.decodeHex(rand16BytesKeyInHex.toCharArray());
        rand16BytesIv = Hex.decodeHex(rand16BytesIvInHex.toCharArray());
    }

    @Test
    public void testEMDComputation() throws Exception {
        String EMDinBase64 =
                EncapCrypto.computeEMD(
                        rand16BytesKey, rand16BytesIv, inputDataInPlainText.getBytes());
        Assertions.assertThat(EMDinBase64).isEqualTo(expectedEMDInBase64);
    }

    @Test
    public void testMACGeneration() throws Exception {
        String MACinBase64 =
                EncapCrypto.computeMAC(
                        rand16BytesKey, rand16BytesIv, inputDataInPlainText.getBytes());
        Assertions.assertThat(MACinBase64).isEqualTo(expectedMACInBase64);
    }

    @Test
    public void testPKHGeneration() throws Exception {
        byte[] pubKeyBytes =
                Base64.getDecoder().decode(EncapConstants.B64_ELLIPTIC_CURVE_PUBLIC_KEY);
        String PKHinBase64 = EncapCrypto.computePublicKeyHash(pubKeyBytes);
        Assertions.assertThat(PKHinBase64).isEqualTo(expectedPKHInBase64);
    }

    @Test
    public void testCryptoResponseParsing() {
        String mockedResponse =
                "EMD=uuHc15btVnbrReUUlrJ4bRRO37Q9KNUBMYDDtM%2FuhKXANDtPRZb9DX0XUA27x1KSvQ5phA%2FmQ9WqMnmiGEu0W2I4aQMsnW17evhbUZw6vq64dcsAM5P6X6HYP3RExjKpcYsHAYhG8BXQwpKusktLnrVcVYGwlXCqj6q97%2FgH23egSnI%2BZopi5dl6yUgRF%2Fi8RUcyA9JmImB29qQ9HDPYmZjyTZtcgyk0G2SogdOThsa4o8cm1uk2UGV1GJge2es5UgqvKHUPGdRwyw0jOKRvmoZimDypG61Jc2guyVxlkLpbormE1jzJBN5wTm9o3gTEVxwtQl2PZtnzU7pny0PcNjpHQ0G9pP8HiLoipz7MuvtZdjloRZJyTa6UZq9%2FCqlLlhSyGGDLesLsvwxB0knRXgiR2EBvXMviL6OXiXoAhsCuJjS0w1f85vzIDd0DvMnR2bHNCZQbCEFsHutcwefF5i%2FSvXWQdobLKZh1sCsLSq7X5CvfZCxBZNevT3KxSh4Qsc1yLWENi2ssiM343QJZ0t2bggxBZwVxv28rkG58wf8Ce%2BNbnNUK1EyCnceEhFO1bxCzlW3deZVs%2FliNGuqXj8yhlzTYjZdAlOeFnTR4ELtXthjNA%2FGz7LKwFcB7%2BP5scbfdsopqiMSBHhXNJ4UnekaKj5KW0othcbXe4CRLfqY%3D&MAC=nPx60Ken9MmEgVQSvKlvPkuR6qW0CtmhShOgEgDLPnU%3D";
        Map<String, String> responseValues = EncapUtils.parseResponseQuery(mockedResponse);
        Assertions.assertThat(responseValues.containsKey("EMD")).isTrue();
        Assertions.assertThat(responseValues.containsKey("MAC")).isTrue();
        Assertions.assertThat(responseValues.get("EMD")).isEqualTo(urlDecodedEMDResponse);
        Assertions.assertThat(responseValues.get("MAC")).isEqualTo(urlDecodedMACResponse);
    }

    @Test
    public void testEMDDecryption() throws DecoderException, UnsupportedEncodingException {
        String decryptedString =
                EncapCrypto.decryptEMDResponse(
                        rand16BytesKey, rand16BytesIv, urlDecodedEMDResponse);
        Assertions.assertThat(decryptedString).isEqualTo(expectedResponseEMDInHex);
    }

    @Test
    public void testMACVerification() throws DecoderException {
        Assertions.assertThat(
                        EncapCrypto.verifyMACValue(
                                rand16BytesKey,
                                rand16BytesIv,
                                expectedResponseEMDInHex,
                                urlDecodedMACResponse))
                .isTrue();
    }

    @Test
    public void testShaApplication() throws UnsupportedEncodingException {
        String expectedApplicationHash = "BDAU1jUdpq90JondTdP7zFwR8iaKCxcHlrmYgsTbOGk%3D";
        String applicationId = "com.aktia.mobilebank";
        byte[] hash = Hash.sha256(applicationId);
        String applicationHash =
                URLEncoder.encode(Base64.getEncoder().encodeToString(hash), "UTF-8");

        Assertions.assertThat(applicationHash).isEqualTo(expectedApplicationHash);
    }

    @Test
    public void testShaDevice() throws UnsupportedEncodingException {
        String expectedDeviceHash = "VSKIZeZiu46kYGWe%2F5pEXLdfIeaumvYXvb4c7Mh7cII%3D";
        String deviceString = "D2B89BEA-22E2-408D-BBC2-2E0D1869B85Fijljlsfss";
        byte[] hash = Hash.sha256(deviceString);
        String applicationHash =
                URLEncoder.encode(Base64.getEncoder().encodeToString(hash), "UTF-8");

        Assertions.assertThat(applicationHash).isEqualTo(expectedDeviceHash);
    }

    @Test
    public void testComputeB64ChallengeResponse() {
        String expectedChallengeResponse = "Ft3pIonPDssyencueTQavw==";
        String challenge = "aJwmV+YC35PDBY048MdLwA==";
        String authKey = "fyvEnItSfNZOochskler07rVn0eCFDyBVRBC+4w3K5k=";
        String challengeResponse = EncapCrypto.computeB64ChallengeResponse(authKey, challenge);
        System.out.println(expectedChallengeResponse);
        System.out.println(challengeResponse);
        assertEquals(expectedChallengeResponse, challengeResponse);
    }
}

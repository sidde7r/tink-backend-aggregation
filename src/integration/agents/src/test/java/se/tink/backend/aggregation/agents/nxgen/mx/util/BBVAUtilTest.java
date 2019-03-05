package se.tink.backend.aggregation.agents.nxgen.mx.util;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BBVAUtils;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc.DeviceActivationRequest;

public class BBVAUtilTest {

    @Test
    public void verifyGenerateTokenHash() {
        String result = BBVAUtils.generateTokenHash("0119763213");
        Assert.assertEquals("VE_LHUNk51PnRmwbeSrp245u-cDlTB6fQHw0IhMhUVU=", result);
    }

    @Test
    public void verifyCalculateSoftwareAuthCodeHash() {
        String result = BBVAUtils.calculateSoftwareAuthCodeHash("0119763213");
        Assert.assertEquals(
                "544fcb1d4364e753e7466c1b792ae9db8e6ef9c0e54c1e9f407c342213215155", result);
    }

    @Test
    public void verifyCalculateData() {
        String result =
                BBVAUtils.calculateData(
                        "544fcb1d4364e753e7466c1b792ae9db8e6ef9c0e54c1e9f407c342213215155",
                        "RETAILMX",
                        "1.0",
                        "30820122300d06092a864886f70d01010105000382010f003082010a0282010100d8ccfa19a1e665ae13c0892b2a29bccaae5793f06d7604eb9f72857ca4743341e592c677500119cd279b0daaa2b1ef917b44ef05afa98177f6c0f7397d665b8ea8a5d5e413e6944087b5fd543f9400bc78396679896c1a04c328d1e8c9302057b4a51aa1044609fc4e00125dceb8609ffd8e5acbf7fe2f14e443933188c64931f696bcf16c939a47ba5899d674eee384c65540aeaa23c38ed72b309250a90b410eb503d82b01c77521158b31bd679a962e78ead1f383fab7d49db3161e4269ece4433e286fa2eeaf887c3b06af7535e30ccee5901ffd35803d87aed90bc8a340d0123b1c34763e7fa24d50b6844f89b4aca6f1a6157ad8b204453da2c600d6130203010001");
        Assert.assertEquals(
                "945b17144ac38e80ea67294dae037a1f0d876828486e842725c875d880d32ab9", result);
    }

    @Test
    public void verifyGetAESData() {
        String tokenHash = "544fcb1d4364e753e7466c1b792ae9db8e6ef9c0e54c1e9f407c342213215155";
        String applicationCode = "RETAILMX";
        String applicationVersion = "1.0";
        String publicKeyHex =
                "30820122300d06092a864886f70d01010105000382010f003082010a0282010100d8ccfa19a1e665ae13c0892b2a29bccaae5793f06d7604eb9f72857ca4743341e592c677500119cd279b0daaa2b1ef917b44ef05afa98177f6c0f7397d665b8ea8a5d5e413e6944087b5fd543f9400bc78396679896c1a04c328d1e8c9302057b4a51aa1044609fc4e00125dceb8609ffd8e5acbf7fe2f14e443933188c64931f696bcf16c939a47ba5899d674eee384c65540aeaa23c38ed72b309250a90b410eb503d82b01c77521158b31bd679a962e78ead1f383fab7d49db3161e4269ece4433e286fa2eeaf887c3b06af7535e30ccee5901ffd35803d87aed90bc8a340d0123b1c34763e7fa24d50b6844f89b4aca6f1a6157ad8b204453da2c600d6130203010001";

        String result =
                BBVAUtils.calculateData(
                        tokenHash, applicationCode, applicationVersion, publicKeyHex);

        String aesDataResult =
                BBVAUtils.getAuthenticationCodePlaintext(
                        tokenHash, applicationCode, applicationVersion, result, publicKeyHex);

        Assert.assertEquals(
                "544fcb1d4364e753e7466c1b792ae9db8e6ef9c0e54c1e9f407c342213215155#RETAILMX#1.0#30820122300d06092a864886f70d01010105000382010f003082010a0282010100d8ccfa19a1e665ae13c0892b2a29bccaae5793f06d7604eb9f72857ca4743341e592c677500119cd279b0daaa2b1ef917b44ef05afa98177f6c0f7397d665b8ea8a5d5e413e6944087b5fd543f9400bc78396679896c1a04c328d1e8c9302057b4a51aa1044609fc4e00125dceb8609ffd8e5acbf7fe2f14e443933188c64931f696bcf16c939a47ba5899d674eee384c65540aeaa23c38ed72b309250a90b410eb503d82b01c77521158b31bd679a962e78ead1f383fab7d49db3161e4269ece4433e286fa2eeaf887c3b06af7535e30ccee5901ffd35803d87aed90bc8a340d0123b1c34763e7fa24d50b6844f89b4aca6f1a6157ad8b204453da2c600d6130203010001#945b17144ac38e80ea67294dae037a1f0d876828486e842725c875d880d32ab9",
                aesDataResult);
    }

    @Test
    public void verifyGenerateAuthenticationCode() {
        String softwareTokenAuthCode = "0119763213";
        String tokenHash = "544fcb1d4364e753e7466c1b792ae9db8e6ef9c0e54c1e9f407c342213215155";
        String applicationCode = "RETAILMX";
        String applicationVersion = "1.0";
        String salt = "sI+fd0i74v4=";
        String publicKeyHex =
                "30820122300d06092a864886f70d01010105000382010f003082010a0282010100d8ccfa19a1e665ae13c0892b2a29bccaae5793f06d7604eb9f72857ca4743341e592c677500119cd279b0daaa2b1ef917b44ef05afa98177f6c0f7397d665b8ea8a5d5e413e6944087b5fd543f9400bc78396679896c1a04c328d1e8c9302057b4a51aa1044609fc4e00125dceb8609ffd8e5acbf7fe2f14e443933188c64931f696bcf16c939a47ba5899d674eee384c65540aeaa23c38ed72b309250a90b410eb503d82b01c77521158b31bd679a962e78ead1f383fab7d49db3161e4269ece4433e286fa2eeaf887c3b06af7535e30ccee5901ffd35803d87aed90bc8a340d0123b1c34763e7fa24d50b6844f89b4aca6f1a6157ad8b204453da2c600d6130203010001";

        String result =
                BBVAUtils.generateAuthenticationCode(
                        softwareTokenAuthCode,
                        tokenHash,
                        applicationCode,
                        applicationVersion,
                        salt,
                        publicKeyHex);

        Assert.assertEquals(
                "bJ11BbUdTGtVuCwv/XqmIqEfMLq3LLpFz6N5uF866xzFg8+m3XPkAB51P+zDq1oI61CTQ4upPlnP6I+dbcOi2LKszc5KQA+v+BQ5w7Mgu23IyvVpMDNXu89Mf+k+GFVDlGmeUQy48NKVq23gYGioHLQxr/bGSvzY1cbw47E7pwhNBKiuaKK3S6gpbyAIy75tNZhXnTYBC0+rLr7y0WABwFVumjoqQ+4Y26GpJu6Plw+0cz3oKOp7JyQtUhJFrMbzoLxWAPH3acD7MxS+7BF7wGxKRShabV8TXy0dDuwFtqhyKzgMoIvm86GY0PDoMXuVWcYiFd8jb5aiPHtiKA5vEhxU3DBhThCCOaKkNZZBDCe1bEEJ48K5TZzz9PmzSrMzICPEyq0VpikfOcu4rXdjapOJP/lewyJIiDm2f8bXDjePKdJ9hsPvfZ8hbI/bh+9TKVQYhHpq24Dd0LMUOJISZvFY3kDfbhLe7gbEvyjTRrJfMi6FjSO02b96J40f60yTb6tMR5nlJNO8cRWmB96Wiz1yLLyBBU9apQ1Y0loah1dsksqmx/Q7KfA0BS4YP3r+zgfdfFuGd+5Q639CJAy8+4w4cHsvZ5Zj9VS5fin7qBTNaI1rvSvvKeSrW+mueLavi7OJ62AWeEfZIHRwtigwvyJOzXZE+Jdt9bXA9WUEM1fJmo5Bl9R/HQT5hUf4iryJEgsaqsCQtBHAM85jETIt9OIboH8p+DLuR7+g8yLOkJxiUeuipMHF3h9m3LUCUVfoRi7QZp9EGET6CfXWlE8CeA8tav9a4B5ZqrWflFTmZDrvInv3PrAwePyXjOzXC1slryHlTmbBeQUN1GLiZ7FYw70XsHQaBAu/+djPtDBQ683Ai214tPvRzTjEkeZBinndTqTNPJC/1LjQWx2TtLa9CD9xog9XiULEXaP+o0IzTVkMItyWCmZMPJ2wCBf1hWNOf+PJEd4Clc49RnFrzVoMPw==",
                result);
    }

    @Test
    public void verifyGetActivationBoundaryData() {
        String expected =
                "--6B5FC74F-F77B-4CE0-AD34-68522714BFEE\n"
                        + "Content-Disposition: form-data;name=\"data\"\n"
                        + "\n"
                        + "{\"isSoftwareTokenActivation\":false,\"contactDetail\":{\"contact\":\"PhoneNumber\",\"contactType\":{\"id\":\"MOBILE_NUMBER\"}},\"contract\":{\"numberType\":{\"id\":\"PAN\"},\"number\":\"cardNumber\"},\"device\":{\"model\":\"iPhone \",\"id\":\"deviceIdentifier\",\"platform\":{\"id\":\"iOS\",\"version\":\"11.1.2\"},\"brand\":\"Apple\"}}\n"
                        + "--6B5FC74F-F77B-4CE0-AD34-68522714BFEE\n"
                        + "Content-Disposition: form-data;name=\"biometricFile\"\n"
                        + "\n"
                        + "\n"
                        + "--6B5FC74F-F77B-4CE0-AD34-68522714BFEE--";

        String boundary = "--6B5FC74F-F77B-4CE0-AD34-68522714BFEE";
        DeviceActivationRequest req =
                new DeviceActivationRequest(
                        boundary, "PhoneNumber", "deviceIdentifier", "cardNumber");
        String result = BBVAUtils.getActivationDataBoundary(req, boundary);

        Assert.assertEquals(expected, result);
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.validator.IdTokenValidator.ValidatorMode;

public class IdTokenValidatorTest {

    private static final String ID_TOKEN_WITH_AT_HASH =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyLCJhdF9oYXNoIjoidGlEUGg1MThLVnM3aXVlVEhTV"
                    + "DZRUSJ9.hGqyyEadZx6dUNi60u6t7dFoPIQu6L2Y5pgTTga6eLrT23"
                    + "0H9MckEqDcWx70M2zo3j7K_YZWKQ2r4C1igD5PA4L-g_lpW64k0SL8"
                    + "t0bjaibZ4Z-KDObFqom8kQeTRN7HnJLWJ7PivNxXMNHR9LoOb513TR"
                    + "ATHfF8PKfBdvLijsyPGCqqqZjCiLNDIAXfVcWmYGNVPTttOFZgPTtR"
                    + "BsJsB8UfwhAPsk5J48POp4kUVxC6oH-xfavuoZOWvpA83b9gmVZUmq"
                    + "vDyX-D_lkG-uI2pTLP31WzSJxZ3HtjF2VOwNAN9L9V1CPxiskddHXE"
                    + "gXOI0jSzxLUkZh5lV2_E-RLRWQ";

    private static final String ID_TOKEN_WITH_C_HASH =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyLCJjX2hhc2giOiJCelRVTl9kY3U2SDV6QWhwT2o2d"
                    + "Ut3In0.AooEOzj84wh5rpDTKWwrkrtgcISd5DyuVeGtEb0nMMD6Xsb"
                    + "x7hm6L6PeTMmWcrHsMBrYzl4qKSphnB-n8ZxheEsG6N_AJrVIsWXio"
                    + "Bujpp4XLpExsp08-ZJMOjxqQ6XOImBU69tsumtcRWty36nJ0np92yn"
                    + "CHk6ORIYajO-e18_VFyWmlSo47H2vYsr5Q6JYqtwfjxvgzLSX38WRX"
                    + "1NagzUGXLNvL9ZLHbExhNbvvX4oTSw7klOVFcKJpJbV_6Tc0JAPhOh"
                    + "14Do4OUrnnCaxJPPMqOJ_kSpamy2s7ldjeseEA7ebmCKYSkt5HP6Qa"
                    + "VMBXHXC9385lRKqNiS3bCQnCQ";

    private static final String ID_TOKEN_WITH_S_HASH =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyLCJzX2hhc2giOiI2SmtzaWpNSktFUnpBLWp6ZHpER"
                    + "2p3In0.IrFyD_bmIaUVPJhO6c5dcF3AS_Z-D90NJTAzoBVJMxE62-R"
                    + "gc_AEeqTlQDEXN-_riAEpHvzPhRkx35Kju5vieDShOcRwlqpCLy58Z"
                    + "-mQ48FIi2SADlQ1MLyCBuaNEn0lO6Tql6hNm7NLZbYC4vREBlE2zJd"
                    + "z-i9LYj2VeDgoR48U4owWS7L5LBP7YH6v7Kz4mpgh8t6n2aHOnN82l"
                    + "2tc_HclBDhPNpioFoeyg-wq5rKB-mvwF4RC9vwZL6gdAmWwSASY6er"
                    + "xr9fycOSHu80zDjT3nW11qP2g7orjDh22bR4ynDwAr84L2yQGuSUWl"
                    + "EEVkNV6MeohaGxojvB0nJ-nyQ";

    private static final String TOKEN_WITHOUT_HASHES =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyfQ.cQAv_0bpsSwr8HxqdWWcV7BZkpZ4jDTv2H3MaE"
                    + "hrmdjgw3wIvt1CN7xLujJ4kTrd3E-a4VcUsAQQNiEKdMv-PrqrXNL5"
                    + "zSS0ix-HDd-d20hgwOAPpJ5bvdent7lq9aYT9x9BnrJ-2XvUdwcnWB"
                    + "DzwoPylzj42XhAyRrqTCz3e7mjM2ZSqMcubi9lDQe1GW-5SKApGbCQ"
                    + "6B4CGURfeZOGjeSl_udfWrMvhPDGPn78NOvg8dT2kBNzZtDsGCmuib"
                    + "NDXUrHO73ezNUXacmv2kXFk-LTGbG6VMNxq95aj_1N3m5olIeyjCBU"
                    + "X_vyxJUNEJ-dY4tCRNiy-p3tHWBp_Je0Ug";

    private static final String TOKEN_PS384 =
            "eyJhbGciOiJQUzM4NCIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyfQ.jvV7Qy69e4u1JQ8GzOEf7CPOXbKTylR3Rjt6dV"
                    + "_1WKEOCYcv0i33gQyzRbGM4rdiSg59zWUA9GalvX78b6Tj6RtUW5D8"
                    + "VlgV0GKO62U5DuDkOhIZkoFQB8TKWxNFA6SrNhHOOkJbonjK0vP9fd"
                    + "XhMOIYnFvyI-EMwTxA-QzcNk_GZpDoYnWOOUXxgcN5vqDd5UopvYX5"
                    + "E_dOkzxNwAxT0fuQTcf8MullvChuKHJ1hBBcG1dSVOed7ssrbhopfI"
                    + "qagQrj9N5w2LBnsqLKoGHP1R1RCYfpKNOPkEvUWv27vDkJz3L5JYfSy"
                    + "Kf5qdcCKg_KGiMUv_d29I_zDllujhtI5A";

    private static final String TOKEN_WRONG_SIGNATURE =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImFiYy0xMjMifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyfQ.cQAv_0bpsSwr8HxqdWWcV7BZkpZ4jDTv2H3MaE"
                    + "hrmdjgw3wIvt1CN7XLujJ4kTrd3E-a4VcUsAQQNiEKdMv-PrqrXNL5"
                    + "zSS0ix-HDd-d20hgwOAPpJ5bvdent7lq9aYT9x9BnrJ-2XvUdwcnWB"
                    + "DzwoPylzj42XhAyRrqTCz3e7mjM2ZSqMcubi9lDQe1GW-5SKApGbCQ"
                    + "6B4CGURfeZOGjeSl_udfWrMvhPDGPn78NOvg8dT2kBNzZtDsGCmuib"
                    + "NDXUrHO73ezNUXacmv2kXFk-LTGbG6VMNxq95aj_1N3m5olIeyjCBU"
                    + "X_vyxJUNEJ-dY4tCRNiy-p3tHWBp_Je0Ug";

    private static final String TOKEN_WRONG_KEY =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InVua25vd24ifQ.eyJzd"
                    + "WIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0Ijo"
                    + "xNTE2MjM5MDIyfQ.F9wx_30VLiGCHIrHnszVwuOey2ASxrJJIiEw-S"
                    + "JNyZka5XNQ3mUO77xStmN_wsOjkRtXkals1o_L7OdIgpK2RV__eTVS"
                    + "Fo86CNiwzxQ3NUd7GWiIWl9Y5xymMRaqnpbm-Ee23HuOQIHMhgpySx"
                    + "x6LrkgSTkKPRr0bZfUttv8BfEoM8biU3ziq5HC7JAwuX-M-KZhyjT4"
                    + "xp4-Qs5TwPIAmgQpYF9ay8SRZx8GyHrtpW56QWjsc7Tl2N_FJI9wKw"
                    + "SAeADWxcWCWeQw33IYOJwKuIAb_FNPozn9WHRiS_Hzz1DpfDUfsxI7l"
                    + "c50UR0NgVDXwdfG0O2ocGdetI0OgNpGUQ";

    private static final String MALFORMED_TOKEN = "someheader.somepayload";

    private static final String RAW_MODULUS =
            "009111e68c2d61558887b634eab840eafe1f1d1ebd837c8179be1d53ea0f06ef"
                    + "e22cd7896715aa3682488581930067792f52184fac1857be8b08f5"
                    + "f6d2e340243632eef90a20f6083566b01fb50554cfd7c82d588292"
                    + "8d5c11bcb3b236d4f1f6c8cb7fe06ea9bc9077d2304a5dd3b7f341"
                    + "8419c806de5914ad8acfee50180fc1c32a7b15e89856f33264936f"
                    + "011d3b77c6137f9da0c94604520952b0155bd51486b4a8c062ff5f"
                    + "e7b36122489d4a4fe699707217a341643d94296013369cfb651476"
                    + "d8739e431ee81f2570f4e92736af069aa749939bd14333a7a44ed7"
                    + "040617f012601f40a061152495bc0ce24af2cc11f54dc93207df0c"
                    + "76e90813a2ad219d29";

    private static final BigInteger exponent = BigInteger.valueOf(65537);

    private final Map<String, PublicKey> publicKeyMap = new HashMap<>();

    @Before
    public void setup() throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger modulus = new BigInteger(RAW_MODULUS, 16);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKeyMap.put(
                "abc-123", keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent)));
    }

    @Test
    public void shouldValidateAtHashSuccess() {
        boolean result =
                new IdTokenValidator(ID_TOKEN_WITH_AT_HASH, publicKeyMap)
                        .withAtHashValidation("most-secure-access-token")
                        .execute();
        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateAtHashError() {
        Throwable throwable =
                catchThrowable(
                        () ->
                                new IdTokenValidator(ID_TOKEN_WITH_AT_HASH, publicKeyMap)
                                        .withAtHashValidation("wrong-token")
                                        .execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage("ID Token validation failed: Invalid at_hash");
    }

    @Test
    public void shouldValidateCHashSuccess() {
        boolean result =
                new IdTokenValidator(ID_TOKEN_WITH_C_HASH, publicKeyMap)
                        .withCHashValidation("so-so-secure-access-code")
                        .execute();
        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateCHashError() {
        Throwable throwable =
                catchThrowable(
                        () ->
                                new IdTokenValidator(ID_TOKEN_WITH_C_HASH, publicKeyMap)
                                        .withCHashValidation("wrong-code")
                                        .execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage("ID Token validation failed: Invalid c_hash");
    }

    @Test
    public void shouldValidateSHashSuccess() {
        boolean result =
                new IdTokenValidator(ID_TOKEN_WITH_S_HASH, publicKeyMap)
                        .withSHashValidation("that-is-a-state")
                        .execute();
        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateSHashError() {
        Throwable throwable =
                catchThrowable(
                        () ->
                                new IdTokenValidator(ID_TOKEN_WITH_S_HASH, publicKeyMap)
                                        .withSHashValidation("wrong-state")
                                        .execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage("ID Token validation failed: Invalid s_hash");
    }

    @Test
    public void shouldValidateSignatureOnly() {
        boolean result = new IdTokenValidator(TOKEN_WITHOUT_HASHES, publicKeyMap).execute();
        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateSignatureWrongSignature() {
        Throwable throwable =
                catchThrowable(
                        () -> new IdTokenValidator(TOKEN_WRONG_SIGNATURE, publicKeyMap).execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage(
                        "ID Token validation failed: Invalid signature (alg:RS256, kid:abc-123)");
    }

    @Test
    public void shouldValidateSignatureWrongSignatureLoggingMode() {
        boolean result =
                new IdTokenValidator(TOKEN_WRONG_SIGNATURE, publicKeyMap)
                        .withMode(ValidatorMode.LOGGING)
                        .execute();

        assertThat(result).isFalse();
    }

    @Test
    public void shouldValidateWrongFormat() {
        Throwable throwable =
                catchThrowable(() -> new IdTokenValidator(MALFORMED_TOKEN, publicKeyMap).execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage("ID Token validation failed: Invalid format");
    }

    @Test
    public void shouldValidatePS384() {
        boolean result = new IdTokenValidator(TOKEN_PS384, publicKeyMap).execute();

        assertThat(result).isTrue();
    }

    @Test
    public void shouldValidateNoKeyAvailable() {
        Throwable throwable =
                catchThrowable(() -> new IdTokenValidator(TOKEN_WRONG_KEY, publicKeyMap).execute());

        assertThat(throwable)
                .isExactlyInstanceOf(IdTokenValidationException.class)
                .hasMessage("ID Token validation failed: Did not find key with id unknown");
    }

    @Test
    public void shouldValidateNoKeyAvailableLoggingMode() {
        boolean result =
                new IdTokenValidator(TOKEN_WRONG_KEY, publicKeyMap)
                        .withMode(ValidatorMode.LOGGING)
                        .execute();

        assertThat(result).isFalse();
    }
}

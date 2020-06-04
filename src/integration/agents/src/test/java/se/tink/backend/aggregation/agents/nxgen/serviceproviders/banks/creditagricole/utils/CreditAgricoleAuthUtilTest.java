package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.utils;

import com.sun.jersey.core.util.Base64;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CreditAgricoleAuthUtilTest {

    private static final String PUBLIC_KEY_AS_BASE64 =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCTxiWDrsKPmZENoLPL7XXf6DV2fS7w8dVvj1lBIfN1maJIj/0h9NAcTDoJTSMOYvxMdtqL5PNXGiGVdszLv+K7vDg16kqt0DZzX0rj0cmy1oXy5ZrsZeBvMqDEusRF+1lwqT2CU8ek6cqweu42TXbykbbpZrOeR0WLPiY8ZnQOwQIDAQAB";

    @Test
    public void shouldCreatePublicKeyFromBase64() {
        // given
        BigInteger modulus =
                new BigInteger(
                        "103770438731100858657430872211218900660468596918577785345456421362832088463666294324060191796067189092601687071336075148952235815805241202207857345682648045720105727892389247790224242484653887987060424702976837311321525395075064239146317265384729856340477226200859911363711828743347520949475295340906184707777");
        BigInteger exponent = new BigInteger("65537");

        // when
        RSAPublicKey publicKey = CreditAgricoleAuthUtil.getPublicKey(PUBLIC_KEY_AS_BASE64);

        // then
        Assertions.assertThat(publicKey).isNotNull();
        Assertions.assertThat(publicKey.getAlgorithm()).isEqualTo("RSA");
        Assertions.assertThat(publicKey.getPublicExponent()).isEqualTo(exponent);
        Assertions.assertThat(publicKey.getModulus()).isEqualTo(modulus);
    }

    @Test
    public void shouldMapAccountCodeToNumpadSequence() {
        // given
        String numpadSequence = "0;7;3;2;8;4;1;6;5;9";
        String realAccountCode = "123456";
        String realMappedAccountCode = "6;3;2;5;8;7";

        // when
        String mappedAccountCode =
                CreditAgricoleAuthUtil.mapAccountCodeToNumpadSequence(
                        numpadSequence, realAccountCode);

        // then
        Assertions.assertThat(mappedAccountCode).isEqualTo(realMappedAccountCode);
    }

    @Test
    public void shouldCreateEncryptedAccountCode() {
        // given
        String mappedAccountCode = "6;3;2;5;8;7";
        RSAPublicKey publicKey = CreditAgricoleAuthUtil.getPublicKey(PUBLIC_KEY_AS_BASE64);

        // when
        String encryptedAccountCode =
                CreditAgricoleAuthUtil.createEncryptedAccountCode(mappedAccountCode, publicKey);

        // then
        Assertions.assertThat(encryptedAccountCode).isNotEmpty();
        Assertions.assertThat(encryptedAccountCode).hasSize(172);
        Assertions.assertThat(Base64.isBase64(encryptedAccountCode)).isTrue();
    }
}

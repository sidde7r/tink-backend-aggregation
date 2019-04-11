package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.util;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils.ArgentaCardNumber;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.utils.ArgentaSecurityUtil;

public class ArgentaSecurityUtilTest {

    @Test
    public void testExtractionOfUakFromLongUakResponse() {
        String uak =
                "9d4109c921e397ede9ac9406ba3ec198ce05f3ae1beeed55bb17b398206126f9857f6e53e42294c66aa2b06bc186e628e90b3dcc97ef5ef8aadd113b9c2b87f019833ffa1e5fa795a5bea996dcdf761e";
        String oldUak = "83a1fefae960e980501982ecfe86441001698ce31930ac739a9120129f20a942";
        String deviceId = "fc8f041e-7c8e-45fa-9147-7a439cfc7e3b";

        String newUak = ArgentaSecurityUtil.getUak(oldUak, uak, deviceId);
        Assert.assertEquals(
                "5b99e6a80472876fef34aba6edd9cba8eb0b6b1eb491f64dcae808c840082abd", newUak);
    }

    @Test
    public void testTwoExtractionOfUakFromLongUakResponse() {
        String uak =
                "d03c085a82d522c22abc2979cd5ac12c3c137743db2d20e87d6076c247e73a2f40c1e6afc3436ef2f01f2d5085d7bd2b7a5165e648ba291fd772e312c44ba66b3a08209912b2cc1e2194c51955d72195";
        String oldUak = "19044d870d402ce9e1b8ee665d8b331fcbc8b320a2f047e53fd22ced4c842047";
        String deviceId = "fc8f041e-7c8e-45fa-9147-7a439cfc7e3b";

        String newUak = ArgentaSecurityUtil.getUak(oldUak, uak, deviceId);
        Assert.assertEquals(
                "772eb388b78f90a3b3d4fc55308c4045e2d23e410e53b6270feb28e8a0e8540e", newUak);
    }

    @Test
    public void testGenerationOfPinChallengeResponseFromUakExtractedFromLongUak() {
        String uak =
                "8474585567df089a6684944b4eacb7528067c1a6711a32b36b50f9d4d9c4e969ca43f6f81c3905e79976e8ad805e312824961ba2d472519dee277204da911a10ec8ec59486c5e22d52cad46505c51280";
        String oldUak = "09281e02565956fe15adf9a6ed7763f5bcb34da6054f1a85efb73caab5e4c980";

        String newUak =
                ArgentaSecurityUtil.getUak(oldUak, uak, "D7156BF1-890C-4763-B63A-4598CECA0472");
        String challenge = "91909698";
        String response2 = ArgentaSecurityUtil.generatePinResponseChallenge(challenge, newUak);
        Assert.assertEquals(
                "58b491c02f4e8426ce3d3fc655d446813d972e815df043b81905cb0306a5178a", response2);
    }

    @Test
    public void testGenerationOfPinChallengeResponse() {
        String uak = "aec2896d42d78e00888ef358034668db1fe027d66ffa76efc04d12997973528a";
        String challenge = "37206233";
        String response = ArgentaSecurityUtil.generatePinResponseChallenge(challenge, uak);
        Assert.assertEquals(
                "e312b0602d52c77d87a17e2c48673b3ee2a0de27fc3bf782e6e660b9c92bbb2b", response);
    }

    @Test
    public void testGenerationOfPin2ChallengeResponse() {
        String uak = "4dd45de5f19f979b0d7b7f03d3e05418f2d1016c249698e65c7a4c4707cb152c";
        String challenge = "24941610";

        String response = ArgentaSecurityUtil.generatePinResponseChallenge(challenge, uak);
        Assert.assertEquals(
                "1f6c7edc855793428c630aa330a92f5e0e21844fe9befb739ed08ac44a8e1ae8", response);
    }

    @Test
    public void testCardNumberFormater() {
        String cardnumber = "1111111";

        System.out.println("cardnumber = " + cardnumber);
        Assert.assertEquals(
                "1111 1111 1111 1111 1",
                ArgentaCardNumber.formatCardNumber("1111 1111 1111 1111 1"));
        Assert.assertEquals(
                "1111 1111 1111 1111 1", ArgentaCardNumber.formatCardNumber("11111111111111111"));
        Assert.assertEquals(
                "1111 1111 1111 1111 1",
                ArgentaCardNumber.formatCardNumber("1 1 1 1 1 1 1 1 1 1 1 1     1 1 1 1 1 "));
        Assert.assertEquals(
                "1111 1111 1111 1111 12",
                ArgentaCardNumber.formatCardNumber(" 1 1 1 1 1 1 1 1 1 1 1 1     1 1 1 1 1 2 "));
    }
}

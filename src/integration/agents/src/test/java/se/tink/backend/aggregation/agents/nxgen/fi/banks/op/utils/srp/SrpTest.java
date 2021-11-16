package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.utils.srp;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.encoding.EncodingUtils;

public class SrpTest {

    @Test
    public void testSrpClientEvidenceMessage() {
        // https://integration-proxy.global-production.tink.network/analyze?query=filename%3A%22fi_op%20bank_28.0.1_reg_login_full_hashlogs%22%20AND%20operation%3A%22srp_exchange%22

        String serverPublicValueAsHex =
                "533C0FF6695AAC143B20C3D5B1E14ED406A224536793CBDE6A4C9D23200535D65CC019C229222270934A3BC59ED00143A7F7EDD1057F0C116FC61055DCD724CD5CCCB69D9C96F7281B8BBD7EDD16B99DBBF50104E67E4A3873D71AEFD28881C665EA0C781F7846F5F00180F90082EB5B7802E839716104330EB450F7F674A125EB65E762F7D3AC900E792499F63CAD088B03A00D4C72342BDD3B7A5CCC7A89A01A10C2412983B7B57A4340FF9A1EC8665B28C2009DBCC6AD04F69592271BB4B8D05713D5A9A4BA07ACC940AB9869B402E25514113414CF4C05C80AA108D05516812CFE5E1FAC477C18174F4E66B91DD738CFC0DE933A627CF0C36411291C0D0F";
        String saltAsHex = "09D84CFCD242B25386C2866B60F334E8";
        String userID = "28738912"; // Note: this is fake!
        String password = "83902"; // Note: this is fake!

        BigInteger privateValue =
                new BigInteger(
                        "1892203002721429242718052565312135029700057529496616782823477244154382904154166748430333504156488284574940085459223446089401442984057026042544318990581535827963285473541897798073042941175280517071307232941023004794174987742199757395070160912489408895682013943750879146654624374411359536823966401310389513766148845445684040456381105655532609274491235338623088720557167575459382089499928642880214679133806383739513335987128007226128243072768293468607509250229425209326043262030505891940765481978505088100485136245379217852511045635972698183128482378088622516091484274693428863584991536805568948190025267640756067473054");

        Srp srp = Srp.withStaticPrivateValue(privateValue);
        ClientEvidenceMessageResponse response =
                srp.calculateClientEvidenceMessage(
                        serverPublicValueAsHex, saltAsHex, userID, password);

        String expectedClientPublicValue =
                "458316348b480dab2c682cba9142eaefc637998aa6f6c540f5fb8100661e3b22e68748401f8341bd64b11da2b497c6ed5f0a1de53b8930424d24815a8a84abe0ff2e9095c64ceaf90fac4552170c300246e8ffd1cb174c563b5b4120df1af857f3ca2ef9605cbe7ad2fbb150adc8f3edec55f87c13975775e1fc8794bbe28cc2fe8f81eda4587555f400ff63e7a4a8baaebea6fc4a1b119fcab595b969d87e54514477d79434426f60877c4affac5ce638e467b716c1169b320a9a28f3e78bde8c69bd9ea687c3f59dc4816bbecc5ea52081db38336ef2ca2f85355395c52b252750a9bff7500e62dc947ac2210ad81d4eec751d532ee5463f14e519215880ed";
        String calculatedClientPublicValue =
                EncodingUtils.encodeHexAsString(response.getClientPublicValueAsBytes());
        Assert.assertEquals(
                "Calculated client public value (A) was not correct.",
                expectedClientPublicValue,
                calculatedClientPublicValue);

        String expectedClientEvidenceMessage =
                "00f76c78ecf2d7149d1c0e790b85f75497964b569e4ba7572f914dbdcde500bea1";
        String calculatedClientEvidenceMessage =
                EncodingUtils.encodeHexAsString(response.getClientEvidenceMessageAsBytes());
        Assert.assertEquals(
                "Calculated client evidence message (M1) was not correct.",
                expectedClientEvidenceMessage,
                calculatedClientEvidenceMessage);

        String expectedSessionKey =
                "00aaad20167f756c6c7ca46df197083f0a516d56f7e8f4cca5be4d0dab2afc100ba5d73fc81741636f91a3662d5603619cc32a590e5c487a5bab4e35b99e558ef55414f5e9d69e0feb10e33c93258a5898f508ecabe04ef3118ca9d401d859ef8505c1d1de162c10d980af38f14774ffa6c46947841ff231619ee40fb4600acdc8e0c43a69fff956aa0772cca0157071873fc351add8208e3a2b39553ee376d211990df17653fe84ce632d18e155366fc1d7068e0ba5e511d567956d5edc97065a34a57149f567ae8aaed9f2f14adfe1288d6dc6736b9cdbea267bc315eb518e93f8d63c2f768b620da1e289a5ea9935f9858bf3e5210d50f9a49bdf500ebd9774";
        String calculatedSessionKey =
                EncodingUtils.encodeHexAsString(response.getSessionKeyAsBytes());
        Assert.assertEquals(
                "Calculated session key (S) was not correct.",
                expectedSessionKey,
                calculatedSessionKey);
    }
}

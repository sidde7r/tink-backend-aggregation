package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.Payment;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.PrepareRoot;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static junit.framework.TestCase.assertTrue;

public class ParseTest  extends BelfiusTest {

    public boolean isBeneficiary(String accountNum, List<BeneficiariesContacts> beneficiaries){
        boolean ret = false;
        for(BeneficiariesContacts beneficiary : beneficiaries){
            ret = ret || beneficiary.isAccount(accountNum);
        }
        return ret;
    }

    @Test
    public void parseResponseTest() {
        PrepareRoot pr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.BENEFICIARY_RESPONSE, PrepareRoot.class);
        List<BeneficiariesContacts> beneficiaries = pr.getBeneficiaries();

        assertTrue(isBeneficiary("BE11111111111111", beneficiaries));
        BelfiusPaymentResponse bpr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.SIGN_REQUIRE, BelfiusPaymentResponse.class);
        assertTrue(bpr.requireSign());
        SignProtocolResponse spr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.signPrep, SignProtocolResponse.class);
        assertTrue(spr.cardReaderAllowed());
        spr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.PAYMENT_REQUEST, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("4420 5434 5740")); //BENEFICIARY_ADD_STRING
        assertTrue(spr.getSignType().equals("2000"));
        spr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.BENEFICIARY_ADD_STRING, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("3154 7130 2300"));
    }

    @Test
    public void parseSignBeneficiaryRequiredResponseTest() {
        SignProtocolResponse spr = SerializationUtils.deserializeFromString(
                BelfiusPaymentTestData.signBeneficiaryRequire, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("3812 9670 9279")); //BENEFICIARY_ADD_STRING
        assertTrue(spr.getSignType().equals("917360"));
    }

    @Test
    public void doublePaymentCheckTest() {
        BelfiusPaymentResponse bpr = SerializationUtils.deserializeFromString(BelfiusPaymentTestData.SIGN_REQUIRE, BelfiusPaymentResponse.class);
        assertTrue(bpr.isErrorOrContinueChangeButtonDoublePayment());
    }


}

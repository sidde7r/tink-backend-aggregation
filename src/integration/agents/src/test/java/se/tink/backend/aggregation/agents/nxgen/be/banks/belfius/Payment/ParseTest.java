package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.Payment;

import static junit.framework.TestCase.assertTrue;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusTest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.BelfiusPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol.SignProtocolResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.BeneficiariesContacts;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer.PrepareRoot;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ParseTest extends BelfiusTest {

    public boolean isBeneficiary(String accountNum, List<BeneficiariesContacts> beneficiaries) {
        boolean ret = false;
        for (BeneficiariesContacts beneficiary : beneficiaries) {
            ret = ret || beneficiary.isAccount(accountNum);
        }
        return ret;
    }

    @Test
    public void parseResponseTest() {
        PrepareRoot pr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.BENEFICIARY_RESPONSE, PrepareRoot.class);
        List<BeneficiariesContacts> beneficiaries = pr.getBeneficiaries();

        assertTrue(isBeneficiary("BE11111111111111", beneficiaries));
        BelfiusPaymentResponse bpr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.SIGN_REQUIRE, BelfiusPaymentResponse.class);
        assertTrue(bpr.isErrorMessageIdentifier());
        SignProtocolResponse spr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.signPrep, SignProtocolResponse.class);
        assertTrue(spr.cardReaderAllowed());
        spr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.PAYMENT_REQUEST, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("4420 5434 5740")); // BENEFICIARY_ADD_STRING
        assertTrue(spr.getSignType().equals("2000"));
        spr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.BENEFICIARY_ADD_STRING, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("3154 7130 2300"));
    }

    @Test
    public void parseSignBeneficiaryRequiredResponseTest() {
        SignProtocolResponse spr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.signBeneficiaryRequire, SignProtocolResponse.class);
        assertTrue(spr.getChallenge().equals("3812 9670 9279")); // BENEFICIARY_ADD_STRING
        assertTrue(spr.getSignType().equals("917360"));
    }

    @Test
    public void doublePaymentCheckDutchTest() {
        BelfiusPaymentResponse bpr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.SIGN_REQUIRE, BelfiusPaymentResponse.class);
        assertTrue(bpr.isErrorMessageIdentifier());
    }

    @Test
    public void doublePaymentCheckFrenchTest() {
        BelfiusPaymentResponse bpr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.SIGN2_REQUIRE_FRENCH, BelfiusPaymentResponse.class);
        assertTrue(bpr.isErrorMessageIdentifier());
    }

    @Test
    public void shouldGetErrorMessageFromSignResponse() {
        SignProtocolResponse spr =
                SerializationUtils.deserializeFromString(
                        BelfiusPaymentTestData.SIGN2_REQUIRE_FRENCH, SignProtocolResponse.class);
        Assert.assertEquals(
                "Attention!  Vous avez déjà introduit un virement similaire. Si vous souhaitez poursuivre "
                        + "l'exécution de ce virement, veuillez cliquer sur le bouton 'Continuer'. "
                        + "Sinon, veuillez cliquer sur le bouton 'Modifier'.\n"
                        + "(MOBILEBANKINGTRANSFERCREATION   /12DB/000000)"
                        + " ERROR IN OperationMobileBankingTransferCreation (RK9HI510/12DB/000000)",
                (spr).getErrorMessage());
    }
}

package se.tink.backend.aggregation.agents.banks.nordea.utilities;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries.BeneficiaryEntity;

public class FiltersTest {

    /** The normal case is that SHB has 4 digit clearing number */
    @Test
    public void testSHBWith4DigitClearingNumber() {

        BeneficiaryEntity entity = new BeneficiaryEntity();
        entity.setBeneficiaryBankId("SHB");
        entity.setPaymentType("ThirdParty");
        entity.setToAccountId("6111 111 111 111");

        Assert.assertTrue(
                Filters.beneficiariesWithAccountNumber("61110 111 111 111".replaceAll(" ", ""))
                        .apply(entity));
    }

    /** Nordea can store the clearing number with 5 digits and it is still the same account */
    @Test
    public void testSHBWith5DigitClearingNumber() {

        BeneficiaryEntity entity = new BeneficiaryEntity();
        entity.setBeneficiaryBankId("SHB");
        entity.setPaymentType("ThirdParty");
        entity.setToAccountId("61110 111 111 111");

        Assert.assertTrue(
                Filters.beneficiariesWithAccountNumber("61110 111 111 111".replaceAll(" ", ""))
                        .apply(entity));
    }

    @Test
    public void testDifferentAccountNumber() {

        BeneficiaryEntity entity = new BeneficiaryEntity();
        entity.setBeneficiaryBankId("SHB");
        entity.setPaymentType("ThirdParty");
        entity.setToAccountId("6111 111 111 112");

        Assert.assertFalse(
                Filters.beneficiariesWithAccountNumber("6111 111 111 111".replaceAll(" ", ""))
                        .apply(entity));
    }

    @Test
    public void testDifferentClearingNumber() {

        BeneficiaryEntity entity = new BeneficiaryEntity();
        entity.setBeneficiaryBankId("SHB");
        entity.setPaymentType("ThirdParty");
        entity.setToAccountId("5111 111 111 111");

        Assert.assertFalse(
                Filters.beneficiariesWithAccountNumber("6111 111 111 111".replaceAll(" ", ""))
                        .apply(entity));
    }
}

package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public class BancoBpiProductTypeTest {

    @Test
    public void getLoanProductTypesShouldReturnAllSupportedLoansProduct() {
        // when
        BancoBpiProductType[] loanTypes = BancoBpiProductType.getLoanProductTypes();
        // then
        Assert.assertTrue(
                Arrays.stream(loanTypes)
                        .filter(t -> t == BancoBpiProductType.LOAN)
                        .findAny()
                        .isPresent());
        Assert.assertTrue(
                Arrays.stream(loanTypes)
                        .filter(t -> t == BancoBpiProductType.MORTGAGE)
                        .findAny()
                        .isPresent());
        Assert.assertTrue(
                Arrays.stream(loanTypes)
                        .filter(t -> t == BancoBpiProductType.LOAN_VEHICLE)
                        .findAny()
                        .isPresent());
    }

    @Test
    public void getTinkLoanTypeShouldReturnCorrectTinkLoanType() {
        // then
        Assert.assertEquals(Type.VEHICLE, BancoBpiProductType.LOAN_VEHICLE.getDomainLoanType());
        Assert.assertEquals(Type.MORTGAGE, BancoBpiProductType.MORTGAGE.getDomainLoanType());
        Assert.assertEquals(Type.CREDIT, BancoBpiProductType.LOAN.getDomainLoanType());
    }

    @Test
    public void isLoanShouldReturnTrueIfLoan() {
        // then
        Assert.assertTrue(BancoBpiProductType.LOAN_VEHICLE.isLoan());
        Assert.assertFalse(BancoBpiProductType.CREDIT_CARD.isLoan());
    }
}

package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UkOpenBankingV31PisUtilsTest {

    @Test(expected = TransferExecutionException.class)
    public void testInvalidEmptyRemittanceInformationForHSBC() {
        UkOpenBankingV31PisUtils.validateRemittanceWithProviderOrThrow(
                "uk-hsbc-oauth2", new RemittanceInformation());
    }

    @Test(expected = TransferExecutionException.class)
    public void testInvalidTooLongRemittanceInformationForHSBC() {
        RemittanceInformation toolong = new RemittanceInformation();
        toolong.setValue("aaaaaaaaaaaaaaaaaaaa");
        UkOpenBankingV31PisUtils.validateRemittanceWithProviderOrThrow(
                "uk-hsbc-oauth2", new RemittanceInformation());
    }

    @Test
    public void testValidRemittanceInformationForOtherUkBanks() {
        UkOpenBankingV31PisUtils.validateRemittanceWithProviderOrThrow(
                "uk-barclays-oauth2", new RemittanceInformation());
        Assert.assertTrue(true);
    }
}

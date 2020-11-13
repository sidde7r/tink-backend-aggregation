package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class UkOpenBankingPisUtilsTest {

    @Test(expected = TransferExecutionException.class)
    public void testInvalidEmptyRemittanceInformationForHSBC() {
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                "uk-hsbc-oauth2", new RemittanceInformation());
    }

    @Test(expected = TransferExecutionException.class)
    public void testInvalidTooLongRemittanceInformationForHSBC() {
        RemittanceInformation toolong = new RemittanceInformation();
        toolong.setValue("aaaaaaaaaaaaaaaaaaaa");
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                "uk-hsbc-oauth2", new RemittanceInformation());
    }

    @Test
    public void testValidRemittanceInformationForOtherUkBanks() {
        UkOpenBankingPisUtils.validateRemittanceWithProviderOrThrow(
                "uk-barclays-oauth2", new RemittanceInformation());
        Assert.assertTrue(true);
    }
}

package se.tink.backend.aggregation.agents.banks.fi;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgent;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgentUtils;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.savings.CustodyAccount;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.libraries.account.identifiers.TestAccount;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class NordeaAgentTest extends AbstractAgentTest<NordeaAgent> {
    public NordeaAgentTest() {
        super(NordeaAgent.class);
    }

    @Override
    protected Provider constructProvider() {
        Provider p = new Provider();
        p.setMarket("FI");
        return p;
    }

    @Test
    public void testJonatanKlintberg() throws Exception {
        testAgent("30971766", "ASK_JONATAN");
    }

    @Test
    public void testTransferInternalAccount() throws Exception {
        Transfer transfer = new Transfer();
        transfer.setAmount(Amount.inEUR(0.1));
        transfer.setSource(new FinnishIdentifier("FI4416303500186077"));
        transfer.setDestination(new FinnishIdentifier("FI2816303500033881"));
        transfer.setDestinationMessage("Tink Test");
        transfer.setSourceMessage("Tink Test");

        testTransfer("30971766", "ASK_JONATAN", CredentialsTypes.PASSWORD, transfer);
    }

    @Test
    public void nineteenDigitCustodyAccountNumberShouldBeValid() {

        CustodyAccount account = new CustodyAccount();
        account.setAccountId("TOIVO:0220000000080251111");

        Assert.assertTrue(account.hasValidBankId());
    }

    @Test
    public void testBeneficiaryLookup() {
        Assert.assertEquals("FSPA", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.SAVINGSBANK_AL)));
        Assert.assertEquals("DDB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.DANSKEBANK_FH)));
        Assert.assertEquals("SEB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.SEB_DL)));
        Assert.assertEquals("SHB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.HANDELSBANKEN_FH)));
        Assert.assertEquals("ICA", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.ICABANKEN_FH)));
        Assert.assertEquals("LFB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.LANSFORSAKRINGAR_FH)));
        Assert.assertEquals("NB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.NORDEA_EP)));
        Assert.assertEquals("NB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.NORDEASSN_EP)));
        Assert.assertEquals("SKB", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.SKANDIABANKEN_FH)));
        Assert.assertEquals("FSPA", NordeaAgentUtils.lookupBeneficiaryBankId(new SwedishIdentifier(TestAccount.SWEDBANK_FH)));
    }

    @Test(expected=ClassCastException.class)
    public void testFailingBeneficiaryLookup() {
        Assert.assertEquals("FSPA", NordeaAgentUtils.lookupBeneficiaryBankId(new IbanIdentifier("bic", "ibanNumber")));
    }
}

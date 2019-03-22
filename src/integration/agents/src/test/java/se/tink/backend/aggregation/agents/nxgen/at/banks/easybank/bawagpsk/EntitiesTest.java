package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInfo;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInformationListItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountInformationListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public final class EntitiesTest {
    @Test
    public void testGetInvalidIbans() {
        // Try triggering a NullPointerException
        GetAccountInformationListResponse response;

        response = new GetAccountInformationListResponse(null);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final Envelope envelope = new Envelope();
        response = new GetAccountInformationListResponse(envelope);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final Body body = new Body();
        envelope.setBody(body);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final GetAccountInformationListResponseEntity responseEntity =
                new GetAccountInformationListResponseEntity();
        body.setGetAccountInformationListResponseEntity(responseEntity);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final OK ok = new OK();
        responseEntity.setOk(ok);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final List<AccountInformationListItem> accountInformationListItemList = new ArrayList<>();
        ok.setAccountInformationListItemList(accountInformationListItemList);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        accountInformationListItemList.add(null);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final AccountInformationListItem accountInformationListItem =
                new AccountInformationListItem();
        accountInformationListItemList.add(accountInformationListItem);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final AccountInfo accountInfo = new AccountInfo();
        accountInformationListItem.setAccountInfo(accountInfo);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final ProductID productId = new ProductID();
        accountInfo.setProductID(productId);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final FinancialInstitute financialInstitute = new FinancialInstitute();
        productId.setFinancialInstitute(financialInstitute);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final String invalidBic = "mybic";
        financialInstitute.setBIC(invalidBic);
        Assert.assertTrue(response.getInvalidIbans().isEmpty());

        final String invalidIban = "myIban";
        productId.setIban(invalidIban);
        Assert.assertThat(
                response.getInvalidIbans(),
                is(Collections.singletonList(new IbanIdentifier(invalidBic, invalidIban))));
    }
}

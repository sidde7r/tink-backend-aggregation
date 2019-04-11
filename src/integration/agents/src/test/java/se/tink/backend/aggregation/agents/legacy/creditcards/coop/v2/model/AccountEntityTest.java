package se.tink.backend.aggregation.agents.creditcards.coop.v2.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

public class AccountEntityTest {
    @Test
    public void whenPlusgiroAndOcrPresent_accountHasIdentifier() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setTotalBalance(0.0);

        AccountDetailsEntity ocrDetails = new AccountDetailsEntity();
        ocrDetails.setId("OcrNumber");
        ocrDetails.setValue("9650066148837");

        AccountDetailsEntity plusGiroDetails = new AccountDetailsEntity();
        plusGiroDetails.setId("PlusGiroNumber");
        plusGiroDetails.setValue("4189901-4");

        accountEntity.setAccountDetails(Lists.newArrayList(ocrDetails, plusGiroDetails));

        Account account = accountEntity.toAccount();

        assertThat(account.getIdentifiers())
                .hasSize(1)
                .contains(new PlusGiroIdentifier("4189901-4", "9650066148837"));
    }

    @Test
    public void whenInvalidOcr_accountWithoutIdentifier() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setTotalBalance(0.0);

        AccountDetailsEntity ocrDetails = new AccountDetailsEntity();
        ocrDetails.setId("OcrNumber");
        ocrDetails.setValue("123123123123");

        AccountDetailsEntity plusGiroDetails = new AccountDetailsEntity();
        plusGiroDetails.setId("PlusGiroNumber");
        plusGiroDetails.setValue("4189901-4");

        accountEntity.setAccountDetails(Lists.newArrayList(ocrDetails, plusGiroDetails));

        Account account = accountEntity.toAccount();

        assertThat(account.getIdentifiers()).isEmpty();
    }

    @Test
    public void whenNoPlusgiro_accountWithoutIdentifier() {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setTotalBalance(0.0);

        AccountDetailsEntity ocrDetails = new AccountDetailsEntity();
        ocrDetails.setId("OcrNumber");
        ocrDetails.setValue("9650066148837");

        accountEntity.setAccountDetails(Lists.newArrayList(ocrDetails));

        Account account = accountEntity.toAccount();

        assertThat(account.getIdentifiers()).isEmpty();
    }
}

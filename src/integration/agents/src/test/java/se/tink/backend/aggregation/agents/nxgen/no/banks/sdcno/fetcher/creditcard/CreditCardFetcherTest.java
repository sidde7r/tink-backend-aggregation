package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard.entity.CardEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardFetcherTest {
    private SdcNoApiClient sdcNoApiClientMock;
    private CreditCardFetcher objUnderTest;

    @Before
    public void initSetup() {
        sdcNoApiClientMock = mock(SdcNoApiClient.class);
        objUnderTest = new CreditCardFetcher(sdcNoApiClientMock);
    }

    private CardEntity createCardsResponse(String type, String status) {
        return CardEntity.builder()
                .accountId("12345")
                .amount("0.0")
                .availableAmount("10000")
                .cardName("NAME")
                .cardNumber("12345")
                .status(status)
                .type(type)
                .build();
    }

    @Test
    public void fetchAccountsShouldReturnTransformedActiveCreditCards() {
        // given
        List<CardEntity> response =
                Arrays.asList(
                        createCardsResponse("CREDIT", "ACTIVE"),
                        createCardsResponse("DEBET", "ACTIVE"),
                        createCardsResponse("CREDIT", "EXPIRED"));

        given(sdcNoApiClientMock.fetchCreditCards()).willReturn(response);

        // when
        Collection<CreditCardAccount> accountList = objUnderTest.fetchAccounts();
        CreditCardAccount account = accountList.stream().findFirst().get();

        // then
        assertThat(accountList).hasSize(1);
        assertThat(account).isInstanceOf(CreditCardAccount.class);
    }
}

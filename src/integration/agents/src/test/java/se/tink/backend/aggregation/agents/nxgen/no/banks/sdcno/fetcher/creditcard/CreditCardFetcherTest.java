package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoApiClient;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CreditCardFetcherTest {
    private SdcNoApiClient sdcNoApiClientMock;
    private CreditCardFetcher objUnderTest;

    @Before
    public void initSetup() {
        sdcNoApiClientMock = mock(SdcNoApiClient.class);
        objUnderTest = new CreditCardFetcher(sdcNoApiClientMock);
    }

    private LinkedHashMap<String, String> createCardsResponse(String type, String status) {
        LinkedHashMap<String, String> cardResponse = new LinkedHashMap();
        cardResponse.put("kortnummer", "12345");
        cardResponse.put("utlopsdato", "01-01-2000");
        cardResponse.put("kontonummer", "12345");
        cardResponse.put("navn", "DUMMY-BANK-NAME");
        cardResponse.put("type", type);
        cardResponse.put("status", status);
        cardResponse.put("saldo", "0.0");
        cardResponse.put("kredittgrense", "10000");
        cardResponse.put("produktnavn", "DUMMY-NAME");
        return cardResponse;
    }

    @Test
    public void fetchAccountsShouldReturnTransformedActiveCreditCards() {
        // given
        List<Map<String, String>> response =
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

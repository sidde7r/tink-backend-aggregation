package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Collection;
import java.util.Optional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.assertj.core.api.IterableAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class IngInvestmentAccountFetcherTest {

    @Mock private IngApiClient apiClient;
    @Mock private IngHelper ingHelper;

    @InjectMocks private IngInvestmentAccountFetcher ingInvestmentAccountFetcher;

    @Test
    public void shouldFetchAccounts() throws Exception {
        when(ingHelper.retrieveLoginResponse()).thenReturn(Optional.of(mockLoginResponseEntity()));
        when(apiClient.fetchAccounts(any())).thenReturn(Optional.of(mockAccountsResponse()));
        when(apiClient.fetchInvestmentPortfolio(any(), eq("3771005805456")))
                .thenReturn(mockPortfolioResponse());

        Collection<InvestmentAccount> result = ingInvestmentAccountFetcher.fetchAccounts();
        IterableAssert<InvestmentAccount> elements = assertThat(result).isNotNull().hasSize(2);
        elements.filteredOn("name", "MEJ ELS NAME")
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("exactBalance", ExactCurrencyAmount.inEUR(9864.53));
        elements.filteredOn("name", "MEVR ELS NAME")
                .hasSize(1)
                .element(0)
                .hasFieldOrPropertyWithValue("exactBalance", ExactCurrencyAmount.inEUR(22505.32));
    }

    @Test
    public void shouldThrowException() throws Exception {
        when(ingHelper.retrieveLoginResponse()).thenReturn(Optional.of(mockLoginResponseEntity()));
        when(apiClient.fetchAccounts(any())).thenReturn(Optional.of(mockAccountsResponse()));
        when(apiClient.fetchInvestmentPortfolio(any(), eq("3771005805456")))
                .thenReturn(mockPortfolioErrorResponse());

        Throwable exception = catchThrowable(ingInvestmentAccountFetcher::fetchAccounts);
        assertThat(exception)
                .isExactlyInstanceOf(BankServiceException.class)
                .hasMessage("Technical failure (E50/01/G001-038)");
    }

    private PortfolioResponse mockPortfolioResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"mobileResponse\": {\n"
                        + "        \"header\": {\n"
                        + "            \"version\": \"1.0\",\n"
                        + "            \"url\": \"/eb/MobileRequest\"\n"
                        + "        },\n"
                        + "        \"returnCode\": \"OK\",\n"
                        + "        \"portfolios\": {\n"
                        + "            \"portfolio\": [\n"
                        + "                {\n"
                        + "                    \"portfolioAccountNumberBBAN\": \"3771005805678\",\n"
                        + "                    \"portfolioAccountName\": \"MEJ ELS NAME\",\n"
                        + "                    \"portfolioAccountType\": \"Portfolio\",\n"
                        + "                    \"portfolioBalance\": \"+0000000000000000000000000000000098635302\"\n"
                        + "                },\n"
                        + "                {\n"
                        + "                    \"portfolioAccountNumberBBAN\": \"3856403204433\",\n"
                        + "                    \"portfolioAccountName\": \"MEVR ELS NAME\",\n"
                        + "                    \"portfolioAccountType\": \"Star Fund\",\n"
                        + "                    \"portfolioBalance\": \"+0000000000000000000000000000000225043202\"\n"
                        + "                }\n"
                        + "            ]\n"
                        + "        },\n"
                        + "        \"portfolioTotalBalance\": \"+323678502\"\n"
                        + "    }\n"
                        + "}",
                PortfolioResponse.class);
    }

    private PortfolioResponse mockPortfolioErrorResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"mobileResponse\": {\n"
                        + "        \"returnCode\": \"NOK\",\n"
                        + "        \"errors\": [\n"
                        + "            {\n"
                        + "                \"code\": \"E50/01/G001-038\",\n"
                        + "                \"text\": \"Due to a technical failure,  we were not able to process your request. Please try again. (038)\"\n"
                        + "            }\n"
                        + "        ]\n"
                        + "    }\n"
                        + "}",
                PortfolioResponse.class);
    }

    private AccountsResponse mockAccountsResponse() throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(AccountsResponse.class);

        javax.xml.bind.Unmarshaller um = context.createUnmarshaller();

        return (AccountsResponse)
                um.unmarshal(
                        new StringReader(
                                "<mobileResponse>\n"
                                        + "  <header>\n"
                                        + "    <version>0.1</version>\n"
                                        + "    <url>https://ebanking.ing.be/hb_eb/eb/MobileRequest</url>\n"
                                        + "  </header>\n"
                                        + "  <returnCode>OK</returnCode>\n"
                                        + "  <categories>\n"
                                        + "    <category>\n"
                                        + "      <number>1</number>\n"
                                        + "      <label>Current Accounts</label>\n"
                                        + "      <totalBalance>+000000000000001002</totalBalance>\n"
                                        + "    </category>\n"
                                        + "    <category>\n"
                                        + "      <number>2</number>\n"
                                        + "      <label>Savings accounts</label>\n"
                                        + "    </category>\n"
                                        + "    <category>\n"
                                        + "      <number>3</number>\n"
                                        + "      <label>Credit cards</label>\n"
                                        + "    </category>\n"
                                        + "    <category>\n"
                                        + "      <number>4</number>\n"
                                        + "      <label>Other</label>\n"
                                        + "    </category>\n"
                                        + "  </categories>\n"
                                        + "  <accounts>\n"
                                        + "    <account>\n"
                                        + "      <type>ING Lion Account</type>\n"
                                        + "      <category>1</category>\n"
                                        + "      <ibanNumber>BE97377100581234</ibanNumber>\n"
                                        + "      <bbanNumber>3771005805412340000</bbanNumber>\n"
                                        + "      <account313>340000</account313>\n"
                                        + "      <mnemonic>CA </mnemonic>\n"
                                        + "      <currency>EUR</currency>\n"
                                        + "      <name>DEMO TEST</name>\n"
                                        + "      <address>DEMO STRAAT 2</address>\n"
                                        + "      <city>2800        MECHELEN</city>\n"
                                        + "      <country></country>\n"
                                        + "      <powerCode>1</powerCode>\n"
                                        + "      <signCode>1</signCode>\n"
                                        + "      <transactionAllowed>0</transactionAllowed>\n"
                                        + "      <beneficiaryAllowed>1</beneficiaryAllowed>\n"
                                        + "      <rulesCode></rulesCode>\n"
                                        + "      <beneficiaryRules>\n"
                                        + "        <rule>01</rule>\n"
                                        + "        <rule>03</rule>\n"
                                        + "        <rule>04</rule>\n"
                                        + "      </beneficiaryRules>\n"
                                        + "      <balance>+000000000000001002</balance>\n"
                                        + "      <centralisationCode>0</centralisationCode>\n"
                                        + "      <movementAvailability>1</movementAvailability>\n"
                                        + "      <dateAccountBalance>20200102</dateAccountBalance>\n"
                                        + "      <availableAmount>+000000000000001002</availableAmount>\n"
                                        + "    </account>\n"
                                        + "    <account>\n"
                                        + "      <type>ING Demo Investment</type>\n"
                                        + "      <category>4</category>\n"
                                        + "      <ibanNumber>BE97377100585678</ibanNumber>\n"
                                        + "      <bbanNumber>3771005805456780000</bbanNumber>\n"
                                        + "      <account313>780000</account313>\n"
                                        + "      <mnemonic>CA </mnemonic>\n"
                                        + "      <currency>EUR</currency>\n"
                                        + "      <name>DEMO TEST</name>\n"
                                        + "      <address>DEMO STRAAT 2</address>\n"
                                        + "      <city>2800        MECHELEN</city>\n"
                                        + "      <country></country>\n"
                                        + "      <powerCode>1</powerCode>\n"
                                        + "      <signCode>1</signCode>\n"
                                        + "      <transactionAllowed>0</transactionAllowed>\n"
                                        + "      <beneficiaryAllowed>1</beneficiaryAllowed>\n"
                                        + "      <rulesCode></rulesCode>\n"
                                        + "      <beneficiaryRules>\n"
                                        + "        <rule>01</rule>\n"
                                        + "        <rule>03</rule>\n"
                                        + "        <rule>04</rule>\n"
                                        + "      </beneficiaryRules>\n"
                                        + "      <balance>+000000000000001002</balance>\n"
                                        + "      <centralisationCode>0</centralisationCode>\n"
                                        + "      <movementAvailability>1</movementAvailability>\n"
                                        + "      <dateAccountBalance>20200102</dateAccountBalance>\n"
                                        + "      <availableAmount>+000000000000001002</availableAmount>\n"
                                        + "    </account>\n"
                                        + "  </accounts>\n"
                                        + "  <memoDates>\n"
                                        + "    <startDate>20200903</startDate>\n"
                                        + "    <endDate>20210902</endDate>\n"
                                        + "  </memoDates>\n"
                                        + "  <minimumAmount>+000000000000000032</minimumAmount>\n"
                                        + "</mobileResponse>"));
    }

    private LoginResponseEntity mockLoginResponseEntity() {
        return SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"mobileResponse\": {\n"
                                + "        \"requests\": [\n"
                                + "            {\n"
                                + "                \"name\": \"getSecuritiesPortfolios\",\n"
                                + "                \"url\": \"arbitrary\"\n"
                                + "            }\n"
                                + "        ]\n"
                                + "    }\n"
                                + "}",
                        LoginResponse.class)
                .getMobileResponse();
    }
}

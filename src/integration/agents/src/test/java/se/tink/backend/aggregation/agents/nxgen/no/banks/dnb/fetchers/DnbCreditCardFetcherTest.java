package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.DnbCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.GetCardResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc.ListCardResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCreditCardFetcherTest {

    private DnbApiClient apiClient;
    private DnbCreditCardFetcher creditCardFetcher;

    @Before
    public void setup() {
        apiClient = mock(DnbApiClient.class);
        creditCardFetcher = new DnbCreditCardFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        when(apiClient.listCards()).thenReturn(getListCardsResponse());
        when(apiClient.getCard(any())).thenReturn(getCardDetailsResponse());

        Collection<CreditCardAccount> transactionalAccounts = creditCardFetcher.fetchAccounts();

        assertThat(transactionalAccounts).hasSize(1);
        CreditCardAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("1111");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("**** **** ***7 1111");
        assertThat(transactionalAccount.getName()).isEqualTo("DNB Mastercard");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.MASKED_PAN
                                        && "**** **** ***7 1111"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "11112222333".equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(-500.0);
        assertThat(transactionalAccount.getExactAvailableBalance()).isNull();
        assertThat(transactionalAccount.getExactAvailableCredit().getDoubleValue())
                .isEqualTo(10000.0);
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private ListCardResponse getListCardsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"debetCards\": [],\n"
                        + "  \"creditCards\": [\n"
                        + "    {\n"
                        + "      \"kontonummer\": \"8384.49.89876\",\n"
                        + "      \"korthavernavn\": \"Smith, John\",\n"
                        + "      \"cardNumber\": \"**** **** ***7 1111\",\n"
                        + "      \"produktnavn\": \"DNB Mastercard\",\n"
                        + "      \"statustekst\": \"Aktivt\",\n"
                        + "      \"bildeurl\": \"/portalfront/dnb/images/applications/kort/096.png\",\n"
                        + "      \"kontonavn\": null,\n"
                        + "      \"cardid\": \"111111111111111111111111111111\",\n"
                        + "      \"corporateCard\": \"N\",\n"
                        + "      \"servicelistmap\": {\n"
                        + "        \"Sperre kredittkort\": \"/ps/applikasjoner/webforms/besok/kort/sperre_kort_mobil.html?cardid=111111111111111111111111111111\",\n"
                        + "        \"Siste transaksjoner\": \"moveTo: _consumerFinanceTransactionsView\",\n"
                        + "        \"OverfÃ¸r til konto\": \"moveTo: _consumerFinanceTransferView\",\n"
                        + "        \"Vis PIN-kode\": \"/segp/apps/pon/ponmobile?cardid=111111111111111111111111111111\",\n"
                        + "        \"Erstatt kredittkort\": \"/segp/apps/besok/reordercard?cardid=111111111111111111111111111111\",\n"
                        + "        \"Oppgrader med SAS EuroBonus\": \"/segp/ps/startsiden/artikler/sas-eurobonus.html?cardid=111111111111111111111111111111\",\n"
                        + "        \"Netthandel\": \"/segp/apps/besok/ecommerce?cardid=111111111111111111111111111111\"\n"
                        + "      },\n"
                        + "      \"activeStatus\": \"active\"\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"texts\": {\n"
                        + "    \"noDebetCardText\": \"Vi kan ikke se at du har bankkort hos oss.<br/><br/>Vi har samlet kort, konto og nettbank i en pakke. Pakken er for deg som Ã¸nsker enkle banktjenester og alt samlet pÃ¥ ett sted.<br/><br/>Med nettbank og BankID Ã¥pner du en pakke pÃ¥ 1-2-3.\",\n"
                        + "    \"noCreditCardText\": \"Vi kan ikke se at du har kredittkort hos oss.<br/><br/>Vi har samlet kort, konto og nettbank i en pakke. Pakken er for deg som Ã¸nsker enkle banktjenester og alt samlet pÃ¥ ett sted.<br/><br/>Med nettbank og BankID Ã¥pner du en pakke pÃ¥ 1-2-3.\",\n"
                        + "    \"newCardHtml\": \"\\n\\t\\t                      <li><strong>Ã˜nsker du Ã¥ bestille en annen type kort</strong> enn det du allerede har tilknyttet kontoen din, eller\\n\\t\\t              \\t    har du en annen konto som du Ã¸nsker kort til? Da kan du\\n\\t\\t              \\t    <a href=\\\"https://www.dnb.no/segp/apps/besok/orderdebetcard\\\">bestille ekstra kort her</a>.</li>\\n\\t\\t              \\t\"\n"
                        + "  },\n"
                        + "  \"error\": null\n"
                        + "}",
                ListCardResponse.class);
    }

    private GetCardResponse getCardDetailsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"type\": \"FINANCIAL\",\n"
                        + "  \"statusCode\": \"NORM\",\n"
                        + "  \"fakeSupplementaryCard\": false,\n"
                        + "  \"userId\": \"**HASHED:jI**\",\n"
                        + "  \"allowTransfers\": false,\n"
                        + "  \"creditLimit\": 10000,\n"
                        + "  \"displayTransactions\": true,\n"
                        + "  \"contractId\": \"**** **** ***7 1111\",\n"
                        + "  \"productId\": \"DNB MC\",\n"
                        + "  \"productName\": \"DNB Mastercard\",\n"
                        + "  \"statusDescription\": \"OK\",\n"
                        + "  \"cardNumber\": \"**** **** ***7 1111\",\n"
                        + "  \"mainCard\": true,\n"
                        + "  \"creditCard\": true,\n"
                        + "  \"transferStatusMessage\": null,\n"
                        + "  \"ssn\": \"**HASHED:jI**\",\n"
                        + "  \"availableAmount\": 10000,\n"
                        + "  \"balanceAmount\": 500,\n"
                        + "  \"coreSystem\": \"PRIME\",\n"
                        + "  \"balanceAmountAdditionalCard\": null,\n"
                        + "  \"accountNumber\": \"11112222333\",\n"
                        + "  \"externalNo\": null,\n"
                        + "  \"cardReplacedFrom\": null,\n"
                        + "  \"cardReplacedBy\": null,\n"
                        + "  \"lastName\": \"SMITH\",\n"
                        + "  \"firstName\": \"JOHN\",\n"
                        + "  \"owner\": true,\n"
                        + "  \"accountCreated\": 1583103600000,\n"
                        + "  \"allowSmsAlerts\": false,\n"
                        + "  \"allowPinOverNet\": false,\n"
                        + "  \"transferableAccounts\": [\n"
                        + "    \n"
                        + "  ]\n"
                        + "}",
                GetCardResponse.class);
    }
}

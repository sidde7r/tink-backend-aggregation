package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.SparebankenSorApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.SparebankenSorCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.rpc.CreditCardListResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankenSorCreditCardAccountFetcherTest {

    private SparebankenSorApiClient apiClient;
    private SparebankenSorCreditCardAccountFetcher accountFetcher;

    @Before
    public void setup() {
        apiClient = mock(SparebankenSorApiClient.class);
        accountFetcher = new SparebankenSorCreditCardAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchCreditCards() {
        when(apiClient.fetchCreditCards()).thenReturn(getFetchAccountResponse());

        Collection<CreditCardAccount> creditCardAccounts = accountFetcher.fetchAccounts();

        assertThat(creditCardAccounts).hasSize(1);
        CreditCardAccount account = creditCardAccounts.iterator().next();
        assertThat(account.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(account.getUniqueIdentifier()).isEqualTo("86011117947");
        assertThat(account.getAccountNumber()).isEqualTo("86011117947");
        assertThat(account.getName()).isEqualTo("Visa Kredittkort ");
        assertThat(account.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.MASKED_PAN
                                        && "111111******2222"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(account.getExactBalance().getDoubleValue()).isEqualTo(-5572.10);
        assertThat(account.getExactAvailableBalance()).isNull();
        assertThat(account.getExactAvailableCredit().getDoubleValue()).isEqualTo(34427.9);
        assertThat(account.getExactCreditLimit()).isNull();
        assertThat(account.getParties()).hasSize(1);
        assertThat(account.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(account.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private CreditCardListResponse getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"list\":\n"
                        + "    [\n"
                        + "        {\n"
                        + "            \"id\": \"86011117947\",\n"
                        + "            \"accountNumber\": \"86011117947\",\n"
                        + "            \"product\":\n"
                        + "            {\n"
                        + "                \"code\": \"42501\",\n"
                        + "                \"name\": \"Visa Kredittkort \"\n"
                        + "            },\n"
                        + "            \"customerRole\": \"M\",\n"
                        + "            \"owner\":\n"
                        + "            {\n"
                        + "                \"name\": \"John Smith\",\n"
                        + "                \"customerNumber\": \"**HASHED:0I**\"\n"
                        + "            },\n"
                        + "            \"properties\":\n"
                        + "            {\n"
                        + "                \"accountType\": \"credit\",\n"
                        + "                \"status\": \"active\",\n"
                        + "                \"currencyCode\": \"NOK\"\n"
                        + "            },\n"
                        + "            \"accountBalance\":\n"
                        + "            {\n"
                        + "                \"availableBalance\": 34427.9,\n"
                        + "                \"accountingBalance\": -5572.1,\n"
                        + "                \"limitAmount\": 40000,\n"
                        + "                \"reservedAmount\": 0\n"
                        + "            },\n"
                        + "            \"cards\":\n"
                        + "            [\n"
                        + "                {\n"
                        + "                    \"id\": \"11111111|3\",\n"
                        + "                    \"cardId\": \"222222222\",\n"
                        + "                    \"cardIdVNo\": \"3\",\n"
                        + "                    \"maskedPAN\": \"111111******2222\",\n"
                        + "                    \"accountNumber\": \"86011117947\",\n"
                        + "                    \"product\":\n"
                        + "                    {\n"
                        + "                        \"code\": \"42501\",\n"
                        + "                        \"name\": \"Visa Kredittkort \"\n"
                        + "                    },\n"
                        + "                    \"cardType\": \"M\",\n"
                        + "                    \"type\": \"credit\",\n"
                        + "                    \"owner\":\n"
                        + "                    {\n"
                        + "                        \"name\": \"JOhn Smith\",\n"
                        + "                        \"customerNumber\": \"**HASHED:0I**\"\n"
                        + "                    },\n"
                        + "                    \"expiryDate\": 1667257200000,\n"
                        + "                    \"expiryDate_iso8601\": \"2022-11-01T00:00:00+0100\",\n"
                        + "                    \"status\": \"active\",\n"
                        + "                    \"availableBalance\": 34427.9,\n"
                        + "                    \"limitAmount\": 40000,\n"
                        + "                    \"links\":\n"
                        + "                    {\n"
                        + "                        \"eurobonus\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/creditcardaccounts/enc!!123456789=/cards/enc!!123456==/eurobonus\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"PUT\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"refill\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/refills\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"POST\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"transfer\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/transfers\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"POST\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"pin\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/pin\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"GET\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"cardReorder\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/reorder\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"PUT\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"virtualCard\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/virtualcard\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"GET\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"self\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"DELETE\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"regionBlock\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/regionblock\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"GET\",\n"
                        + "                                \"PUT\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"cardBlocking\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/cards/enc!!123456==/blocking\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"PUT\"\n"
                        + "                            ]\n"
                        + "                        },\n"
                        + "                        \"extrapayment\":\n"
                        + "                        {\n"
                        + "                            \"href\": \"/creditcardaccounts/enc!!123456789=/cards/enc!!123456==/extrapaymentdetails\",\n"
                        + "                            \"verbs\":\n"
                        + "                            [\n"
                        + "                                \"GET\"\n"
                        + "                            ]\n"
                        + "                        }\n"
                        + "                    },\n"
                        + "                    \"eraCardId\": \"enc!!123456==\",\n"
                        + "                    \"isOwner\": true,\n"
                        + "                    \"expiryDateAsString\": \"2211\",\n"
                        + "                    \"hasCSC\": true,\n"
                        + "                    \"clientid\": \"17902549|3|credit\",\n"
                        + "                    \"instantAccount\": true,\n"
                        + "                    \"limitCanBeIncreased\": true\n"
                        + "                }\n"
                        + "            ],\n"
                        + "            \"links\":\n"
                        + "            {\n"
                        + "                \"self\":\n"
                        + "                {\n"
                        + "                    \"href\": \"/creditcardaccounts/enc!!123456789=\",\n"
                        + "                    \"verbs\":\n"
                        + "                    [\n"
                        + "                        \"GET\"\n"
                        + "                    ]\n"
                        + "                },\n"
                        + "                \"details\":\n"
                        + "                {\n"
                        + "                    \"href\": \"/creditcardaccounts/enc!!123456789=/details\",\n"
                        + "                    \"verbs\":\n"
                        + "                    [\n"
                        + "                        \"GET\"\n"
                        + "                    ]\n"
                        + "                },\n"
                        + "                \"invoice\":\n"
                        + "                {\n"
                        + "                    \"href\": \"/creditcardaccounts/enc!!123456789=/invoices\",\n"
                        + "                    \"verbs\":\n"
                        + "                    [\n"
                        + "                        \"GET\"\n"
                        + "                    ]\n"
                        + "                },\n"
                        + "                \"transactions\":\n"
                        + "                {\n"
                        + "                    \"href\": \"/creditcardaccounts/enc!!123456789=/transactions\",\n"
                        + "                    \"verbs\":\n"
                        + "                    [\n"
                        + "                        \"GET\"\n"
                        + "                    ]\n"
                        + "                }\n"
                        + "            }\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                CreditCardListResponse.class);
    }
}

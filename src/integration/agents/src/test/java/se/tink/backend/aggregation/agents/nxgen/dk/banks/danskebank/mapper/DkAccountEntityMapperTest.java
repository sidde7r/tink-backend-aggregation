package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.util.NoSuchElementException;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMarketMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.CardsListResponse;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DkAccountEntityMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/danskebank/resources";

    private static final String ACCOUNT_NO_EXT = "123234345";
    private static final String ACCOUNT_NO_INT = "567678789";
    private static final String IBAN_NUMBER = "DK3202400123234345";
    private static final String RESULT_IBAN_NUMBER = "DABADKKK/DK3202400123234345";
    private static final String BANK_IDENTIFIER = "bankIdentifier";
    private static final String DK_MARKET_CODE = "dk";
    private static final String ZERO = "0";
    private static final String IBAN = "iban";
    private static final String TEN_DIGIT_ACCOUNT_NO_EXT = ZERO + ACCOUNT_NO_EXT;
    private static final String TEN_DIGIT_ACCOUNT_NO_INT = ZERO + ACCOUNT_NO_INT;

    private AccountEntityMapper dkAccountEntityMapper;
    private AccountEntity accountEntity;
    private AccountDetailsResponse accountDetailsResponse;
    private DanskeBankConfiguration configuration;
    private CardEntity cardEntity;

    @Before
    public void setUp() {
        dkAccountEntityMapper = new AccountEntityMapper(new AccountEntityMarketMapper("DK"));
        accountEntity = getAccountEntity(ACCOUNT_NO_EXT, ACCOUNT_NO_INT);
        accountDetailsResponse = getAccountDetailsResponse();
        configuration = getDanskeBankConfiguration();
        cardEntity =
                SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "cards_response.json").toFile(),
                                CardsListResponse.class)
                        .getCards()
                        .get(0);
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given & when
        TransactionalAccount result =
                dkAccountEntityMapper
                        .toCheckingAccount(accountEntity, getAccountDetailsResponse())
                        .orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers()).hasSize(3);
        assertThat(getIbanAccountIdentifier(result).getIdentifier()).isEqualTo(RESULT_IBAN_NUMBER);
        assertThat(result.getIdentifiers())
                .anyMatch(
                        id ->
                                id.getIdentifier().equals(ACCOUNT_NO_EXT)
                                        && id.getType().toString().equals(DK_MARKET_CODE));
        assertThat(getResultAccountIdentifierByNumber(result).getIdentifier())
                .isEqualTo(RESULT_IBAN_NUMBER);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toCheckingAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        TransactionalAccount result =
                dkAccountEntityMapper
                        .toCheckingAccount(accountEntity, getAccountDetailsResponse())
                        .orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(3);
        assertThat(result.getIdentifiers())
                .anyMatch(id -> id.getIdentifier().equals(TEN_DIGIT_ACCOUNT_NO_EXT));
        assertThat(getResultAccountIdentifierByNumber(result).getIdentifier())
                .isEqualTo(RESULT_IBAN_NUMBER);
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given
        TransactionalAccount result =
                dkAccountEntityMapper
                        .toSavingsAccount(configuration, accountEntity, getAccountDetailsResponse())
                        .orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(ZERO + ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(3);
        assertThat(result.getIdentifiers())
                .anyMatch(id -> id.getIdentifier().equals(ACCOUNT_NO_EXT));
        assertThat(getResultAccountIdentifierByNumber(result).getIdentifier())
                .isEqualTo(RESULT_IBAN_NUMBER);
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toSavingsAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        TransactionalAccount result =
                dkAccountEntityMapper
                        .toSavingsAccount(configuration, accountEntity, getAccountDetailsResponse())
                        .orElse(null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIdModule().getUniqueId()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getIdentifiers().size()).isEqualTo(3);
        assertThat(result.getIdentifiers())
                .anyMatch(id -> id.getIdentifier().equals(TEN_DIGIT_ACCOUNT_NO_EXT));
        assertThat(getResultAccountIdentifierByNumber(result).getIdentifier())
                .isEqualTo(RESULT_IBAN_NUMBER);
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthIsShorterThanMinLength() {
        // given & when
        CreditCardAccount result =
                dkAccountEntityMapper.toCreditCardAccount(
                        configuration, accountEntity, accountDetailsResponse, cardEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER)).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
    }

    @Test
    public void toCreditCardAccountWhenAccountNumberLengthHasMinLength() {
        // given
        accountEntity = getAccountEntity(TEN_DIGIT_ACCOUNT_NO_EXT, TEN_DIGIT_ACCOUNT_NO_INT);

        // when
        CreditCardAccount result =
                dkAccountEntityMapper.toCreditCardAccount(
                        configuration, accountEntity, accountDetailsResponse, cardEntity);

        // then
        assertThat(result.getAccountNumber()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_EXT);
        assertThat(result.getFromTemporaryStorage(BANK_IDENTIFIER))
                .isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
        assertThat(result.getApiIdentifier()).isEqualTo(TEN_DIGIT_ACCOUNT_NO_INT);
    }

    private DanskeBankConfiguration getDanskeBankConfiguration() {
        DanskeBankConfiguration danskeBankConfiguration = mock(DanskeBankConfiguration.class);
        given(danskeBankConfiguration.canExecuteExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canReceiveExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canPlaceFunds(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        given(danskeBankConfiguration.canWithdrawCash(anyString()))
                .willReturn(AccountCapabilities.Answer.NO);
        return danskeBankConfiguration;
    }

    private AccountEntity getAccountEntity(String accountExtNo, String accountIntNo) {
        return SerializationUtils.deserializeFromString(
                "    {\n"
                        + "      \"showAvailableOverdraft\": false,\n"
                        + "      \"overdraftAvailable\": 0,\n"
                        + "      \"showCategory\": \"N\",\n"
                        + "      \"sortValue\": 1,\n"
                        + "      \"isFixedTermDeposit\": false,\n"
                        + "      \"isInLimitGroup\": false,\n"
                        + "      \"isJisaAccountProduct\": false,\n"
                        + "      \"isSavingGoalAccountProduct\": true,\n"
                        + "      \"isBreadcrumbAccountProduct\": true,\n"
                        + "      \"isLoanAccount\": false,\n"
                        + "      \"invIdOwner\": \"111111111.222222.3333333\",\n"
                        + "      \"mandateAccMk\": \"E\",\n"
                        + "      \"showAvailable\": true,\n"
                        + "      \"accessToCredit\": true,\n"
                        + "      \"accessToDebit\": true,\n"
                        + "      \"accessToQuery\": true,\n"
                        + "      \"currency\": \"DKK\",\n"
                        + "      \"cardType\": \"006\",\n"
                        + "      \"accountType\": \"\",\n"
                        + "      \"accountName\": \"Nem-konto\",\n"
                        + "      \"accountProduct\": \"13X\",\n"
                        + "      \"accountRegNoExt\": \"4426\",\n"
                        + "    \"accountNoExt\": \""
                        + accountExtNo
                        + "\",\n"
                        + "    \"accountNoInt\": \""
                        + accountIntNo
                        + "\",\n"
                        + "      \"languageCode\": null,\n"
                        + "      \"balanceAvailable\": 41.49,\n"
                        + "      \"balance\": 82.39\n"
                        + "    }\n",
                AccountEntity.class);
    }

    private AccountDetailsResponse getAccountDetailsResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"traceId\": null,\n"
                        + "    \"statusCode\": 200,\n"
                        + "    \"responseCode\": 200,\n"
                        + "    \"responseMessage\": null,\n"
                        + "    \"eupToken\": null,\n"
                        + "    \"accountInterestDetails\":\n"
                        + "    {\n"
                        + "        \"interestIntervalDetails\": null,\n"
                        + "        \"interestDetails\":\n"
                        + "        [\n"
                        + "            {\n"
                        + "                \"text\": \"Interest on credit balance:\",\n"
                        + "                \"rate\": \"0.000\",\n"
                        + "                \"period\": \"\"\n"
                        + "            }\n"
                        + "        ],\n"
                        + "        \"interestIntervalType\": \"\",\n"
                        + "        \"bodyText\": \"\",\n"
                        + "        \"headline\": \"Interest rate\"\n"
                        + "    },\n"
                        + "    \"accountOwners\":\n"
                        + "    [\n"
                        + "        \"John Smith\"\n"
                        + "    ],\n"
                        + "    \"feeAmount\": 0,\n"
                        + "    \"feeCurrency\": \"DKK\",\n"
                        + "    \"accountType\": \"Danske Konto\",\n"
                        + "    \"accountNumber\": null,\n"
                        + "    \"registrationNumber\": null,\n"
                        + "\"iban\":\""
                        + IBAN_NUMBER
                        + "\",\n"
                        + "    \"bic\": \"DABADKKK\"\n"
                        + "}",
                AccountDetailsResponse.class);
    }

    private AccountIdentifier getIbanAccountIdentifier(TransactionalAccount result) {
        return result.getIdentifiers().stream()
                .filter(id -> id.getType() == AccountIdentifierType.IBAN)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    private AccountIdentifier getResultAccountIdentifierByNumber(TransactionalAccount result) {
        return result.getIdentifiers().stream()
                .filter(id -> id.getIdentifier().equals(RESULT_IBAN_NUMBER))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}

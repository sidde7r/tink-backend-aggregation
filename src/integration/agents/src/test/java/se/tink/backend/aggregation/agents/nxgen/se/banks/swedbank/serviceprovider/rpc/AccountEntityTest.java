package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityTest {

    BankProfile bankProfile =
            SerializationUtils.deserializeFromString(getBankProfile(), BankProfile.class);

    @Test
    public void shouldMapSavingsAccountsCorrectly() {
        SavingAccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        getSavingsAccount(), SavingAccountEntity.class);

        Optional<TransactionalAccount> result =
                accountEntity.toTransactionalAccount(bankProfile, null, null);

        Assert.assertEquals("8420-2,222 222 222-2", result.get().getAccountNumber());
        Assert.assertEquals("842022222222222", result.get().getUniqueIdentifier());
        Assert.assertTrue(
                result.get().getIdentifiers().contains(new SwedishIdentifier("842022222222222")));

        Assert.assertEquals("Esbjorn College fund", result.get().getName());
        Assert.assertEquals(
                BigDecimal.valueOf(100.50),
                result.get().getExactAvailableBalance().getExactValue());
        Assert.assertEquals("SEK", result.get().getExactAvailableBalance().getCurrencyCode());
        Assert.assertEquals(AccountTypes.SAVINGS, result.get().getType());
        Assert.assertEquals(Collections.emptyList(), result.get().getAccountFlags());

        Assert.assertEquals(
                Answer.NO, result.get().getCapabilities().getCanExecuteExternalTransfer());
        Assert.assertEquals(Answer.YES, result.get().getCapabilities().getCanPlaceFunds());
        Assert.assertEquals(Answer.NO, result.get().getCapabilities().getCanWithdrawCash());
        Assert.assertEquals(
                Answer.UNKNOWN, result.get().getCapabilities().getCanReceiveExternalTransfer());
    }

    @Test
    public void shouldMapCheckingAccountsCorrectly() {
        TransactionalAccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        getCheckingAccount(), TransactionalAccountEntity.class);

        Optional<TransactionalAccount> result =
                accountEntity.toTransactionalAccount(bankProfile, null, null);

        Assert.assertEquals("8000-2,111 111 111-6", result.get().getAccountNumber());
        Assert.assertEquals("800021111111116", result.get().getUniqueIdentifier());
        Assert.assertTrue(
                result.get().getIdentifiers().contains(new SwedishIdentifier("800021111111116")));

        Assert.assertEquals("Privatkonto", result.get().getName());
        Assert.assertEquals(
                BigDecimal.valueOf(333.33),
                result.get().getExactAvailableBalance().getExactValue());
        Assert.assertEquals("SEK", result.get().getExactAvailableBalance().getCurrencyCode());
        Assert.assertEquals(AccountTypes.CHECKING, result.get().getType());
        Assert.assertEquals(
                Collections.singletonList(AccountFlag.PSD2_PAYMENT_ACCOUNT),
                result.get().getAccountFlags());

        Assert.assertEquals(
                Answer.YES, result.get().getCapabilities().getCanExecuteExternalTransfer());
        Assert.assertEquals(Answer.YES, result.get().getCapabilities().getCanPlaceFunds());
        Assert.assertEquals(Answer.YES, result.get().getCapabilities().getCanWithdrawCash());
        Assert.assertEquals(
                Answer.YES, result.get().getCapabilities().getCanReceiveExternalTransfer());
    }

    @Test
    public void shouldFilterOutPensionAccount() {
        SavingAccountEntity accountEntity =
                SerializationUtils.deserializeFromString(
                        getPensionAccount(), SavingAccountEntity.class);

        Optional<TransactionalAccount> result =
                accountEntity.toTransactionalAccount(bankProfile, null, null);

        Assert.assertEquals(result, Optional.empty());
    }

    private String getPensionAccount() {
        return " {"
                + "            \"productId\": \"IP-SPAR\","
                + "            \"availableAmount\": \"66 666,66\","
                + "            \"selectedForQuickbalance\": false,"
                + "            \"type\": \"PENSION\","
                + "            \"availableForFavouriteAccount\": false,"
                + "            \"availableForPriorityAccount\": true,"
                + "            \"id\": \"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\","
                + "            \"name\": \"Pensionssparande\","
                + "            \"accountNumber\": \"333 333 333-8\","
                + "            \"clearingNumber\": \"8103-3\","
                + "            \"fullyFormattedNumber\": \"8103-4,333 333 333-8\","
                + "            \"nonFormattedNumber\": \"810343333333338\","
                + "            \"balance\": \"66 666,66\","
                + "            \"currency\": \"SEK\""
                + "        }";
    }

    private String getCheckingAccount() {
        return "{"
                + "            \"productId\": \"TRP00502\","
                + "            \"availableAmount\": \"333,33\","
                + "            \"singleLegalAccount\": false,"
                + "            \"selectedForQuickbalance\": false,"
                + "            \"links\": {"
                + "                \"next\": {"
                + "                    \"method\": \"GET\","
                + "                    \"uri\": \"/v5/engagement/transactions/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\""
                + "                }"
                + "            },"
                + "            \"availableForFavouriteAccount\": true,"
                + "            \"availableForPriorityAccount\": true,"
                + "            \"favouriteAccount\": true,"
                + "            \"id\": \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\","
                + "            \"name\": \"Privatkonto\","
                + "            \"accountNumber\": \"111 111 111-6\","
                + "            \"clearingNumber\": \"8000-2\","
                + "            \"fullyFormattedNumber\": \"8000-2,111 111 111-6\","
                + "            \"nonFormattedNumber\": \"800021111111116\","
                + "            \"balance\": \"333,33\","
                + "            \"currency\": \"SEK\","
                + "            \"priority\": \"1\","
                + "            \"details\": {"
                + "                \"links\": {"
                + "                    \"next\": {"
                + "                        \"method\": \"GET\","
                + "                        \"uri\": \"/v5/engagement/account/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\""
                + "                    }"
                + "                }"
                + "            },"
                + "            \"originalName\": \"Privatkonto\","
                + "            \"transactions\": {"
                + "                \"links\": {"
                + "                    \"next\": {"
                + "                        \"method\": \"GET\","
                + "                        \"uri\": \"/v5/engagement/account/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa/transactions\""
                + "                    }"
                + "                }"
                + "            }"
                + "        }";
    }

    private String getSavingsAccount() {
        return " {"
                + "            \"productId\": \"SPP01306\","
                + "            \"availableAmount\": \"0,00\","
                + "            \"selectedForQuickbalance\": false,"
                + "            \"links\": {"
                + "                \"next\": {"
                + "                    \"method\": \"GET\","
                + "                    \"uri\": \"/v5/engagement/transactions/randomid\""
                + "                }"
                + "            },"
                + "            \"availableForFavouriteAccount\": true,"
                + "            \"availableForPriorityAccount\": true,"
                + "            \"id\": \"randomid\","
                + "            \"name\": \"Esbjorn College fund\","
                + "            \"accountNumber\": \"222 222 222-2\","
                + "            \"clearingNumber\": \"8001-0\","
                + "            \"fullyFormattedNumber\": \"8420-2,222 222 222-2\","
                + "            \"nonFormattedNumber\": \"842022222222222\","
                + "            \"balance\": \"100,50\","
                + "            \"currency\": \"SEK\","
                + "            \"priority\": \"2\","
                + "            \"details\": {"
                + "                \"links\": {"
                + "                    \"next\": {"
                + "                        \"method\": \"GET\","
                + "                        \"uri\": \"/v5/engagement/account/randomid\""
                + "                    }"
                + "                }"
                + "            },"
                + "            \"transactions\": {"
                + "                \"links\": {"
                + "                    \"next\": {"
                + "                        \"method\": \"GET\","
                + "                        \"uri\": \"/v5/engagement/account/randomid/transactions\""
                + "                    }"
                + "                }"
                + "            }"
                + "        }";
    }

    private String getBankProfile() {
        return "{"
                + "    \"bank\":"
                + "        {"
                + "            \"name\": \"Swedbank BB (publ)\","
                + "            \"bankId\": \"08999\","
                + "            \"url\": \"https://www.swedbank.se\","
                + "            \"privateProfile\": {"
                + "                \"activeProfileLanguage\": \"sv\","
                + "                \"targetType\": \"PRIVATE\","
                + "                \"customerName\": \"Wiremock User\","
                + "                \"customerNumber\": \"19900101-0101\","
                + "                \"id\": \"0000000011111111222222223333333344444444\","
                + "                \"bankId\": \"08999\","
                + "                \"bankName\": \"Swedbank AB (publ)\","
                + "                \"url\": \"https://www.swedbank.se\","
                + "                \"customerInternational\": false,"
                + "                \"youthProfile\": false,"
                + "                \"links\": {"
                + "                    \"edit\": {"
                + "                        \"method\": \"PUT\","
                + "                        \"uri\": \"/v5/profile/subscription/0000000011111111222222223333333344444444\""
                + "                    },"
                + "                    \"next\": {"
                + "                        \"method\": \"POST\","
                + "                        \"uri\": \"/v5/profile/0000000011111111222222223333333344444444\""
                + "                    }"
                + "                }"
                + "            },"
                + "            \"corporateProfiles\": ["
                + ""
                + "            ]"
                + "        }"
                + "    ,"
                + "    \"hasSwedbankProfile\": true,"
                + "    \"hasSavingbankProfile\": false,"
                + "    \"selectedProfileId\": \"0000000011111111222222223333333344444444\""
                + "}";
    }
}

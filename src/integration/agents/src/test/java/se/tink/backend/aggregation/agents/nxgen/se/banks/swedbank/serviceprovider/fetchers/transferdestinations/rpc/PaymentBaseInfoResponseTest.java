package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestinations.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.PaymentBaseinfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.TransferDestinationAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PaymentBaseInfoResponseTest {

    @Test
    public void shouldReturnEmptyListIfRecipientAccountsDoesNotHaveScopeTransferTo() {

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getTransferToScopeResponse(""), PaymentBaseinfoResponse.class);

        List<? extends GeneralAccountEntity> result =
                paymentBaseinfoResponse.getAllRecipientAccounts();

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnExternalRecipientsIfScopeTransferToExist() {

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getTransferToScopeResponse("\"TRANSFER_TO\","),
                        PaymentBaseinfoResponse.class);

        List<? extends GeneralAccountEntity> result =
                paymentBaseinfoResponse.getAllRecipientAccounts();

        assertFalse(result.isEmpty());
    }

    @Test
    public void shouldReturnPaymentDestinationByAccountIdIfIdIsValidAndPayeeExist() {
        AccountIdentifier accountId =
                AccountIdentifier.create(AccountIdentifierType.SE_PG, "900229-6", "name of id");

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getAccountValidAndPayeeExist(), PaymentBaseinfoResponse.class);

        Optional<String> result = paymentBaseinfoResponse.getPaymentDestinationAccountId(accountId);

        assertTrue(result.isPresent());
    }

    @Test
    public void shouldReturnTransferDestinationByAccountIdIfInternalOrExternalAccountExist() {
        AccountIdentifier accountId =
                AccountIdentifier.create(AccountIdentifierType.SE, "9272-000 000 1", "name of id");

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getExternalAccountIdExist(), PaymentBaseinfoResponse.class);

        Optional<String> result =
                paymentBaseinfoResponse.getTransferDestinationAccountId(accountId);

        assertTrue(result.isPresent());
    }

    @Test
    public void shouldReturnIdIfAccountScopeIsTransferFrom() {
        TransferDestinationAccountEntity transferDestinationAccountEntity =
                SerializationUtils.deserializeFromString(
                        getTransferFromAccount("\"id\": \"this is an account id\","),
                        TransferDestinationAccountEntity.class);

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getTransferToScopeResponse("\"TRANSFER_TO\","),
                        PaymentBaseinfoResponse.class);

        String result =
                paymentBaseinfoResponse.validateAndGetSourceAccountId(
                        transferDestinationAccountEntity);

        assertEquals("this is an account id", result);
    }

    @Test
    public void shouldThrowFailedTransferExceptionWhenAccountIdIsMissing() {
        TransferDestinationAccountEntity transferDestinationAccountEntity =
                SerializationUtils.deserializeFromString(
                        getTransferFromAccount(""), TransferDestinationAccountEntity.class);

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getTransferToScopeResponse("\"TRANSFER_TO\","),
                        PaymentBaseinfoResponse.class);

        Throwable throwable =
                catchThrowable(
                        () ->
                                paymentBaseinfoResponse.validateAndGetSourceAccountId(
                                        transferDestinationAccountEntity));

        assertThat(throwable).isExactlyInstanceOf(TransferExecutionException.class);
        assertEquals("Source account could not be found at bank.", throwable.getMessage());
        assertEquals(
                "Could not find source account",
                ((TransferExecutionException) throwable).getUserMessage());
    }

    @Test
    public void shouldReturnSourceAccountIfGivenIdExistsInCustomerTransactionAccountGroup() {
        AccountIdentifier accountId =
                AccountIdentifier.create(
                        AccountIdentifierType.SE, "8032-5,9 999 999-9", "EsbjornKonto");

        PaymentBaseinfoResponse paymentBaseinfoResponse =
                SerializationUtils.deserializeFromString(
                        getTransferToScopeResponse("\"TRANSFER_TO\","),
                        PaymentBaseinfoResponse.class);

        Optional<TransferDestinationAccountEntity> result =
                paymentBaseinfoResponse.getSourceAccount(accountId);

        assertTrue(result.isPresent());
    }

    private String getTransferToScopeResponse(String transferScope) {
        return "{"
                + "  \"transactionAccountGroups\": ["
                + "    {"
                + "      \"accounts\": ["
                + "        {"
                + "          \"currencyCode\": \"SEK\","
                + "          \"disposed\": true,"
                + "          \"silAccount\": false,"
                + "          \"currencyAccount\": false,"
                + "          \"scopes\": ["
                + transferScope
                + "            \"TRANSFER_FROM\""
                + "          ],"
                + "          \"amount\": \"0,00\","
                + "          \"iskAccount\": false,"
                + "          \"name\": \"EsbjornKonto\","
                + "          \"accountNumber\": \"9 999 999-9\","
                + "          \"clearingNumber\": \"8032-5\","
                + "          \"id\": \"5de18d62085928552b089ef6c5a5fd395daf47a9\","
                + "          \"fullyFormattedNumber\": \"8032-5,9 999 999-9\""
                + "        }"
                + "      ],"
                + "      \"name\": \"EsbjornKonto\""
                + "    }"
                + "  ],"
                + "    \"externalRecipients\": []"
                + "  }"
                + "}";
    }

    private String getAccountValidAndPayeeExist() {
        return "{"
                + "  \"transactionAccountGroups\": [],"
                + "  \"transfer\": {"
                + "    \"periodicities\": ["
                + "      \"WEEKLY\""
                + "    ],"
                + "    \"externalRecipients\": []"
                + "  },"
                + "  \"payment\": {"
                + "    \"payees\": ["
                + "      {"
                + "        \"type\": \"PGACCOUNT\","
                + "        \"name\": \"Transaktionskonto\","
                + "        \"accountNumber\": \"900229-6\","
                + "        \"clearingNumber\": \"8032-5\","
                + "        \"id\": \"e24787d2e04f53903ecd8dde9e4623ef5b12b4ee\","
                + "        \"fullyFormattedNumber\": \"8327-9, 123 456 789-0\","
                + "        \"referenceType\": \"referencetype\","
                + "        \"lastUsed\": \"2018-12-17\","
                + "        \"links\": {"
                + "          \"edit\": {"
                + "            \"method\": \"PUT\","
                + "            \"uri\": \"/v5/payment/recipient/306497637fd66851fa9c49442f45b69d6663eb94\""
                + "          }"
                + "        }"
                + "      }"
                + "    ]"
                + "  }"
                + "}";
    }

    private String getExternalAccountIdExist() {
        return "{"
                + "  \"transactionAccountGroups\": [],"
                + "  \"transfer\": {"
                + "    \"periodicities\": ["
                + "      \"WEEKLY\""
                + "    ],"
                + "    \"externalRecipients\": ["
                + "      {"
                + "        \"fullyFormattedNumber\": \"9272-000 000 1\","
                + "        \"clearingNumber\": \"8032-5\","
                + "        \"accountNumber\": \"900229-6\","
                + "        \"bank\": \"swedbank\","
                + "        \"name\": \"lönekonto\","
                + "        \"id\": \"1344354366778453\","
                + "        \"links\": {"
                + "          \"next\": {"
                + "            \"method\": \"POST\","
                + "            \"uri\": \"/v5/payment/registered/transfer\""
                + "          }"
                + "        }"
                + "      }"
                + "    ]"
                + "  },"
                + "  \"payment\": {"
                + "    \"payees\": []"
                + "  }"
                + "}";
    }

    private String getTransferFromAccount(String accountId) {
        return "{"
                + "  \"scopes\": ["
                + "  \"TRANSFER_TO\","
                + "  \"TRANSFER_FROM\""
                + "  ],"
                + "  \"amount\": \"99 999,99\","
                + "  \"name\": \"Transaktionskonto\","
                + "  \"accountNumber\": \"9 999 999-9\","
                + "  \"clearingNumber\": \"8032-5\","
                + accountId
                + "  \"fullyFormattedNumber\": \"8327-9, 123 456 789-0\""
                + "}";
    }
}

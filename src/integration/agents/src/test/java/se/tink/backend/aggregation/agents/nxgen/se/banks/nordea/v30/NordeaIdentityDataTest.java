package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.NameElement;
import se.tink.libraries.identitydata.NameElement.Type;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaIdentityDataTest {

    @Test
    public void testIdentityDataParsing() {
        Credentials cred = new Credentials();
        cred.setField(Key.USERNAME, "201212121212");
        final FetchAccountResponse response =
                SerializationUtils.deserializeFromString(
                        accountResponseDataWithValidIdentity, FetchAccountResponse.class);
        IdentityData id = response.getIdentityData(cred);
        Assert.assertEquals(id.getNameElements().stream().count(), 2);
        final String firstName =
                id.getNameElements().stream()
                        .filter(e -> e.getType() == Type.FIRST_NAME)
                        .map(NameElement::getValue)
                        .findFirst()
                        .orElse("");
        Assert.assertEquals(firstName, "FIRSTNAME");
        final String surname =
                id.getNameElements().stream()
                        .filter(e -> e.getType() == Type.SURNAME)
                        .map(NameElement::getValue)
                        .findFirst()
                        .orElse("");
        Assert.assertEquals(surname, "LASTNAME");
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidIdentityParsing() {
        Credentials cred = new Credentials();
        cred.setField(Key.USERNAME, "201212121212");
        final FetchAccountResponse response =
                SerializationUtils.deserializeFromString(
                        accountResponseDataWithInvalidIdentity, FetchAccountResponse.class);
        response.getIdentityData(cred);
    }

    private final String accountResponseDataWithValidIdentity =
            "{\n"
                    + "    \"result\": [\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-1212121212\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 60103.21,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 60103.21,\n"
                    + "            \"category\": \"transaction\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"121212-1212\",\n"
                    + "            \"iban\": \"SE1111111111111111111111\",\n"
                    + "            \"nickname\": \"PERSONKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": true,\n"
                    + "                \"can_pay_pgbg_from_account\": true,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE4001\",\n"
                    + "            \"product_name\": \"PERSONKONTO\",\n"
                    + "            \"product_type\": \"Transaktionskonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME,FIRSTNAME\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-11111111112\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 0.0,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 0.0,\n"
                    + "            \"category\": \"savings\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"1111 11 11112\",\n"
                    + "            \"iban\": \"SE1111111111111111111112\",\n"
                    + "            \"nickname\": \"FÃ\u0096RMÃ\u0085NSKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": true,\n"
                    + "                \"can_pay_pgbg_from_account\": false,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE1145\",\n"
                    + "            \"product_name\": \"FÃ\u0096RMÃ\u0085NSKONTO\",\n"
                    + "            \"product_type\": \"Sparkonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME,FIRSTNAME\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-11111111113\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 0.0,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 0.0,\n"
                    + "            \"category\": \"transaction\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"1111 11 11113\",\n"
                    + "            \"iban\": \"SE1111111111111111111113\",\n"
                    + "            \"nickname\": \"ISK DEPÃ\u0085LIKVIDKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": false,\n"
                    + "                \"can_pay_pgbg_from_account\": false,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE0052\",\n"
                    + "            \"product_name\": \"ISK DEPÃ\u0085LIKVIDKONTO\",\n"
                    + "            \"product_type\": \"Transaktionskonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME,FIRSTNAME\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                },\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME3,FIRSTNAME3\",\n"
                    + "                    \"role\": \"not_owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";

    private final String accountResponseDataWithInvalidIdentity =
            "{\n"
                    + "    \"result\": [\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-1212121212\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 60103.21,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 60103.21,\n"
                    + "            \"category\": \"transaction\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"121212-1212\",\n"
                    + "            \"iban\": \"SE1111111111111111111111\",\n"
                    + "            \"nickname\": \"PERSONKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": true,\n"
                    + "                \"can_pay_pgbg_from_account\": true,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE4001\",\n"
                    + "            \"product_name\": \"PERSONKONTO\",\n"
                    + "            \"product_type\": \"Transaktionskonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME,FIRSTNAME\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-11111111112\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 0.0,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 0.0,\n"
                    + "            \"category\": \"savings\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"1111 11 11112\",\n"
                    + "            \"iban\": \"SE1111111111111111111112\",\n"
                    + "            \"nickname\": \"FÃ\u0096RMÃ\u0085NSKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": true,\n"
                    + "                \"can_pay_pgbg_from_account\": false,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE1145\",\n"
                    + "            \"product_name\": \"FÃ\u0096RMÃ\u0085NSKONTO\",\n"
                    + "            \"product_type\": \"Sparkonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME,FIRSTNAME\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"account_number\": \"NAID-SE-SEK-11111111113\",\n"
                    + "            \"account_status\": \"open\",\n"
                    + "            \"available_balance\": 0.0,\n"
                    + "            \"bic\": \"NDEASESS\",\n"
                    + "            \"booked_balance\": 0.0,\n"
                    + "            \"category\": \"transaction\",\n"
                    + "            \"country_code\": \"SE\",\n"
                    + "            \"credit_limit\": 0.0,\n"
                    + "            \"currency\": \"SEK\",\n"
                    + "            \"display_account_number\": \"1111 11 11113\",\n"
                    + "            \"iban\": \"SE1111111111111111111113\",\n"
                    + "            \"nickname\": \"ISK DEPÃ\u0085LIKVIDKONTO\",\n"
                    + "            \"permissions\": {\n"
                    + "                \"can_deposit_to_account\": true,\n"
                    + "                \"can_pay_from_account\": false,\n"
                    + "                \"can_pay_pgbg_from_account\": false,\n"
                    + "                \"can_transfer_from_account\": true,\n"
                    + "                \"can_transfer_to_account\": true,\n"
                    + "                \"can_view\": true,\n"
                    + "                \"can_view_transactions\": true\n"
                    + "            },\n"
                    + "            \"product_code\": \"SE0052\",\n"
                    + "            \"product_name\": \"ISK DEPÃ\u0085LIKVIDKONTO\",\n"
                    + "            \"product_type\": \"Transaktionskonton\",\n"
                    + "            \"roles\": [\n"
                    + "                {\n"
                    + "                    \"customer_id\": \"\",\n"
                    + "                    \"last_name\": \"LASTNAME2,FIRSTNAME2\",\n"
                    + "                    \"role\": \"owner\"\n"
                    + "                }\n"
                    + "            ]\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
}

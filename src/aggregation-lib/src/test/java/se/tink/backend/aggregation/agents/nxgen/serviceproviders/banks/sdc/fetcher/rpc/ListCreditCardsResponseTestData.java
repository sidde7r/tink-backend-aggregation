package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ListCreditCardsResponseTestData {

    public static ListCreditCardsResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(TEST_DATA, ListCreditCardsResponse.class);
    }

    private static final String TEST_DATA = "[ {"
            + "  \"entityKey\" : {"
            + "    \"rifsIdfr\" : \"111122233344411\""
            + "  },"
            + "  \"creditcardType\" : \"MASTERCARD\","
            + "  \"creditcardTypeName\" : \"kredittkort\","
            + "  \"creditcardStatus\" : \"utlevert\","
            + "  \"statusEffectiveDate\" : \"2017-11-07\","
            + "  \"creditcardNumber\" : \"332211xxxxxx7788\","
            + "  \"cardHolderName\" : \"KALLE KULA\","
            + "  \"attachedAccount\" : {"
            + "    \"id\" : \"1234.6198765\","
            + "    \"entityKey\" : {"
            + "      \"accountId\" : \"1234.6198765\","
            + "      \"agreementId\" : null"
            + "    },"
            + "    \"localizedAccountId\" : \"1234.61.98765\","
            + "    \"type\" : \"KS-KONTO\","
            + "    \"currency\" : \"NOK\","
            + "    \"name\" : \"Kredittkort\""
            + "  },"
            + "  \"endDate\" : \"2021-11-28\","
            + "  \"creditcardStatusType\" : \"UDLEVERET\""
            + "}, {"
            + "  \"entityKey\" : {"
            + "    \"rifsIdfr\" : \"644221133557799\""
            + "  },"
            + "  \"creditcardType\" : \"VISA_DEBET_NO\","
            + "  \"creditcardTypeName\" : \"visa debet\","
            + "  \"creditcardStatus\" : \"utlevert\","
            + "  \"statusEffectiveDate\" : \"2017-11-07\","
            + "  \"creditcardNumber\" : \"111333xxxxxx7799\","
            + "  \"cardHolderName\" : \"KALLE KULA\","
            + "  \"attachedAccount\" : {"
            + "    \"id\" : \"8776.3243377\","
            + "    \"entityKey\" : {"
            + "      \"accountId\" : \"8776.3243377\","
            + "      \"agreementId\" : null"
            + "    },"
            + "    \"localizedAccountId\" : \"8776.32.43377\","
            + "    \"type\" : \"KS-KONTO\","
            + "    \"currency\" : \"NOK\","
            + "    \"name\" : \"Visakort\""
            + "  },"
            + "  \"endDate\" : \"2021-11-28\","
            + "  \"creditcardStatusType\" : \"UDLEVERET\""
            + "} ]";
}

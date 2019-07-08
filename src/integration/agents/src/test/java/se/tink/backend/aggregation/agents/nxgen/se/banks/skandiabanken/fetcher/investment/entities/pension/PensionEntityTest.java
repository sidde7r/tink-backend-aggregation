package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import org.junit.Test;

public class PensionEntityTest {

    @Test
    public void testHolderNameParsing() {
        final String expected = "Ylva Johansson";
        PensionEntity e1 = deserializeFromString(NO_HOLDER, PensionEntity.class);
        PensionEntity e2 = deserializeFromString(HOLDER_SECOND, PensionEntity.class);
        PensionEntity e3 = deserializeFromString(ROOT_HOLDER, PensionEntity.class);
        PensionEntity e4 = deserializeFromString(NULL_HOLDER, PensionEntity.class);

        assertEquals(expected, e1.toTinkInvestmentAccount().getHolderName().toString());
        assertEquals(expected, e2.toTinkInvestmentAccount().getHolderName().toString());
        assertEquals(expected, e3.toTinkInvestmentAccount().getHolderName().toString());
        assertNull(e4.toTinkInvestmentAccount().getHolderName().toString());
    }

    private static final String HOLDER_SECOND =
            "{\n"
                    + "  \"Holder\": {\n"
                    + "    \"Fullname\": \"Ylva Johansen\",\n"
                    + "    \"NationalIdentificationNumber\": null,\n"
                    + "    \"Surname\": null,\n"
                    + "    \"Firstname\": null\n"
                    + "  },\n"
                    + "  \"IsAiEInsurance\": false,\n"
                    + "  \"HasSecuritiesAccountPart\": false,\n"
                    + "  \"Parts\": [\n"
                    + "    {\n"
                    + "      \"Holder\": null\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"Holder\": {\n"
                    + "        \"Fullname\": \"Ylva Johansen\",\n"
                    + "        \"NationalIdentificationNumber\": null,\n"
                    + "        \"Surname\": \"Johansson\",\n"
                    + "        \"Firstname\": \"Ylva\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Number\": \"1234\",\n"
                    + "  \"TypeName\": \"TPSWithUnitLink\"\n"
                    + "}";

    private static final String ROOT_HOLDER =
            "{\n"
                    + "  \"Holder\": {\n"
                    + "    \"Fullname\": \"Ylva Johansson\",\n"
                    + "    \"NationalIdentificationNumber\": null,\n"
                    + "    \"Surname\": null,\n"
                    + "    \"Firstname\": null\n"
                    + "  },\n"
                    + "  \"IsAiEInsurance\": false,\n"
                    + "  \"HasSecuritiesAccountPart\": false,\n"
                    + "  \"Parts\": [\n"
                    + "    {\n"
                    + "      \"Holder\": null\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Number\": \"1234\",\n"
                    + "  \"TypeName\": \"TPSWithUnitLink\"\n"
                    + "}";

    private static final String NO_HOLDER =
            "{\n"
                    + "      \"Holder\": null,\n"
                    + "      \"IsAiEInsurance\": false,\n"
                    + "      \"HasSecuritiesAccountPart\": false,\n"
                    + "      \"Parts\": [\n"
                    + "        {\n"
                    + "          \"Holder\": {\n"
                    + "            \"Fullname\": \"Ylva Johansson\",\n"
                    + "            \"NationalIdentificationNumber\": null,\n"
                    + "            \"Surname\": null,\n"
                    + "            \"Firstname\": null\n"
                    + "          }\n"
                    + "        },\n"
                    + "        {\n"
                    + "          \"Holder\": {\n"
                    + "            \"Fullname\": \"Ylva Johansson\",\n"
                    + "            \"NationalIdentificationNumber\": null,\n"
                    + "            \"Surname\": \"Johansson\",\n"
                    + "            \"Firstname\": \"Ylva\"\n"
                    + "          }\n"
                    + "        }\n"
                    + "      ],\n"
                    + "      \"Number\": \"S461234-1234-12\",\n"
                    + "      \"TypeName\": \"TPSWithUnitLink\"\n"
                    + "    }";

    private static final String NULL_HOLDER =
            "{\n"
                    + "  \"Holder\": null,\n"
                    + "  \"IsAiEInsurance\": false,\n"
                    + "  \"HasSecuritiesAccountPart\": false,\n"
                    + "  \"Parts\": [\n"
                    + "    {\n"
                    + "      \"Holder\": null\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Number\": \"1234\",\n"
                    + "  \"TypeName\": \"TPSWithUnitLink\"\n"
                    + "}";
}

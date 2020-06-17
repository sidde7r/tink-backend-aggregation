package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import org.junit.Ignore;
import org.junit.Test;

public class PensionEntityTest {

    private static final String EXPECTED_NAME = "Ylva Johansson";

    @Test
    public void withFullNameOnlyHolderNameIsExpectedFullName() {
        PensionEntity e1 = deserializeFromString(NO_HOLDER, PensionEntity.class);

        assertEquals(EXPECTED_NAME, e1.toTinkInvestmentAccount().getHolderName().toString());
    }

    @Test
    @Ignore // TODO previously unmaintained -- should be fixed
    public void withHolderSecondHolderNameIsExpectedFullName() {
        PensionEntity e2 = deserializeFromString(HOLDER_SECOND, PensionEntity.class);

        assertEquals(EXPECTED_NAME, e2.toTinkInvestmentAccount().getHolderName().toString());
    }

    @Test
    public void withRootHolderHolderNameIsExpectedFullName() {
        PensionEntity e3 = deserializeFromString(ROOT_HOLDER, PensionEntity.class);

        assertEquals(EXPECTED_NAME, e3.toTinkInvestmentAccount().getHolderName().toString());
    }

    @Test
    public void withNullHolderHolderNameIsExpectedFullName() {
        PensionEntity e4 = deserializeFromString(NULL_HOLDER, PensionEntity.class);

        assertNull(e4.toTinkInvestmentAccount().getHolderName());
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

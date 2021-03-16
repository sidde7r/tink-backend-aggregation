package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.migration;

import org.junit.Ignore;

@Ignore
public class SwedbankPensionMigrationTestData {
    static final String PENSION_DETAIL =
            "{\n"
                    + "    \"name\": \"Individuellt pensionssparande\",\n"
                    + "    \"accountNumber\": \"111 222 333-2\",\n"
                    + "    \"clearingNumber\": \"8000-2\",\n"
                    + "    \"fullyFormattedNumber\": \"8000-2,111 222 333-2\",\n"
                    + "    \"type\": \"INDIVIDUAL_SAVINGS_PENSION\",\n"
                    + "    \"performance\": {\n"
                    + "        \"percent\": \"3,33\",\n"
                    + "        \"amount\": {\n"
                    + "            \"amount\": \"3 333,33\",\n"
                    + "            \"currencyCode\": \"SEK\"\n"
                    + "        }\n"
                    + "    },\n"
                    + "    \"totalValue\": {\n"
                    + "        \"amount\": \"111 111,11\",\n"
                    + "        \"currencyCode\": \"SEK\"\n"
                    + "    }\n"
                    + "}";
}

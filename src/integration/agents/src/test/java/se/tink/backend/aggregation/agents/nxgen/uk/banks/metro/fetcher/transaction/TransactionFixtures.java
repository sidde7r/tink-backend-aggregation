package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.transaction.model.TransactionEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public enum TransactionFixtures {
    TRANSACTION_WITH_ONE_PART_OF_DESCRIPTION(
            "{\n"
                    + "    \"amount\": \"0.01\",\n"
                    + "    \"date\": \"2020-02-01\",\n"
                    + "    \"details\": [],\n"
                    + "    \"line1\": \"Credit Interest\",\n"
                    + "    \"runningBalance\": \"21.08\",\n"
                    + "    \"transactionId\": \"190276713404399.111111\"\n"
                    + "}"),
    TRANSACTION_WITH_FULL_DESCRIPTION(
            "{\n"
                    + "    \"amount\": \"0.01\",\n"
                    + "    \"date\": \"2020-02-01\",\n"
                    + "    \"details\": [],\n"
                    + "    \"line1\": \"Credit Interest\",\n"
                    + "    \"line2\": \"Transaction\",\n"
                    + "    \"runningBalance\": \"21.08\",\n"
                    + "    \"transactionId\": \"190276713404399.222222\"\n"
                    + "}"),
    TRANSACTION_WITH_DETAILS(
            "{\n"
                    + "                \"amount\": \"0.01\",\n"
                    + "                \"date\": \"2017-06-17\",\n"
                    + "                \"details\": [\n"
                    + "                    {\n"
                    + "                        \"dataType\": \"STRING\",\n"
                    + "                        \"dataValue\": \"GBP30.00\",\n"
                    + "                        \"ordinal\": 1\n"
                    + "                    }\n"
                    + "                ],\n"
                    + "                \"line1\": \"Credit Interest\",\n"
                    + "                \"line2\": \"Transaction\",\n"
                    + "                \"runningBalance\": \"133.59\",\n"
                    + "                \"transactionId\": \"180663011383428.020001\"\n"
                    + "            }");
    private final String json;

    TransactionFixtures(String json) {
        this.json = json;
    }

    TransactionEntity toObject() {
        return SerializationUtils.deserializeFromString(json, TransactionEntity.class);
    }
}

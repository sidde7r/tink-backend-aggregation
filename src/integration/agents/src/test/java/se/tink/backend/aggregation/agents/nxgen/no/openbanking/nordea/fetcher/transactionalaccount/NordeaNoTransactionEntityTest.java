package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class NordeaNoTransactionEntityTest {

    @Test
    @Parameters(method = "allPossibilities")
    public void shouldReturnExpectedDescription(
            String counterpartyName,
            String narrative,
            String typeDescription,
            String expectedDescription) {
        // given
        NordeaNoTransactionEntity nordeaNoTransactionEntity =
                SerializationUtils.deserializeFromString(
                        buildJson(counterpartyName, narrative, typeDescription),
                        NordeaNoTransactionEntity.class);

        // when
        String description = nordeaNoTransactionEntity.getDescription();

        // then
        assertThat(description).isEqualTo(expectedDescription);
    }

    private Object[] allPossibilities() {
        return new Object[] {
            new Object[] {"FIRST", null, null, "FIRST"},
            new Object[] {"FIRST", null, "", "FIRST"},
            new Object[] {"FIRST", null, "THIRD", "FIRST"},
            new Object[] {"FIRST", "", null, "FIRST"},
            new Object[] {"FIRST", "", "", "FIRST"},
            new Object[] {"FIRST", "", "THIRD", "FIRST"},
            new Object[] {"FIRST", "SECOND", null, "FIRST"},
            new Object[] {"FIRST", "SECOND", "", "FIRST"},
            new Object[] {"FIRST", "SECOND", "THIRD", "FIRST"},
            new Object[] {"", null, null, ""},
            new Object[] {"", null, "", ""},
            new Object[] {"", null, "THIRD", "THIRD"},
            new Object[] {"", "", null, ""},
            new Object[] {"", "", "", ""},
            new Object[] {"", "", "THIRD", "THIRD"},
            new Object[] {"", "SECOND", null, "SECOND"},
            new Object[] {"", "SECOND", "", "SECOND"},
            new Object[] {"", "SECOND", "THIRD", "SECOND"},
            new Object[] {null, null, null, ""},
            new Object[] {null, null, "", ""},
            new Object[] {null, null, "THIRD", "THIRD"},
            new Object[] {null, "", null, ""},
            new Object[] {null, "", "", ""},
            new Object[] {null, "", "THIRD", "THIRD"},
            new Object[] {null, "SECOND", null, "SECOND"},
            new Object[] {null, "SECOND", "", "SECOND"},
            new Object[] {null, "SECOND", "THIRD", "SECOND"},
            new Object[] {null, "1111 1111,SECOND", "THIRD", "SECOND"},
            new Object[] {null, "1111 1111, SECOND SECOND ", "THIRD", "SECOND SECOND"},
        };
    }

    private String buildJson(String counterpartyName, String narrative, String typeDescription) {
        return String.format(
                "{\"counterparty_name\": %s, \"narrative\": %s, \"type_description\": %s}",
                fieldToString(counterpartyName),
                fieldToString(narrative),
                fieldToString(typeDescription));
    }

    private String fieldToString(String field) {
        return field == null ? "null" : "\"" + field + "\"";
    }
}

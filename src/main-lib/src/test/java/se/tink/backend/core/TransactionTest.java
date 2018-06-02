package se.tink.backend.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.utils.StringUtils;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;

public class TransactionTest {

    @Test
    public void testNullCategory() {

        Category category = new Category();
        category.setId(StringUtils.generateUUID());
        category.setType(CategoryTypes.EXPENSES);

        Transaction transaction = new Transaction();

        assertThatThrownBy(() -> transaction.setCategory(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> transaction.setCategory(null, null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> transaction.setCategory("categoryId", null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> transaction.setCategory(null, CategoryTypes.EXPENSES))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testAssignCategory() {

        Category category = new Category();
        category.setId(StringUtils.generateUUID());
        category.setType(CategoryTypes.EXPENSES);

        Transaction transaction = new Transaction();

        transaction.setCategory(category);

        assertThat(transaction.getCategoryId()).isEqualTo(category.getId());
        assertThat(transaction.getCategoryType()).isEqualTo(category.getType());
    }

    @Test
    public void testResetCategory() {
        Category category = new Category();
        category.setId(StringUtils.generateUUID());
        category.setType(CategoryTypes.EXPENSES);

        Transaction transaction = new Transaction();

        transaction.setCategory(category);

        assertThat(transaction.getCategoryId()).isEqualTo(category.getId());
        assertThat(transaction.getCategoryType()).isEqualTo(category.getType());

        transaction.resetCategory();

        assertThat(transaction.getCategoryId()).isNull();
        assertThat(transaction.getCategoryType()).isNull();
    }

    @Test
    public void deserializePartnerPayloadFromJson() throws IOException {
        assertNotNull(new ObjectMapper().readValue(
                "{\n"
                + "  \"partnerPayload\": {\n"
                + "    \"payloadField\": \"payloadFieldValue\"\n"
                + "  }\n"
                + "}",
                Transaction.class));
    }

    @Test
    public void convertPartnerPayloadFromMap() {
        assertNotNull(new ObjectMapper().convertValue(
                ImmutableMap.of("partnerPayload", emptyMap()),
                Transaction.class));
    }

}

package se.tink.backend.core;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionPartTest {

    @Test
    public void testSerializationAndDeserialization() {

        TransactionPart original = new TransactionPart();
        original.setAmount(BigDecimal.valueOf(100));
        original.setCategoryId(StringUtils.generateUUID());
        original.setCounterpartId(StringUtils.generateUUID());
        original.setCounterpartTransactionId(StringUtils.generateUUID());
        original.setDate(new Date());
        original.setLastModified(new Date());

        String serializedValue = SerializationUtils.serializeToString(original);

        assertThat(serializedValue).isNotNull();

        TransactionPart copy = SerializationUtils.deserializeFromString(serializedValue, TransactionPart.class);

        assertThat(original.getAmount()).isEqualTo(copy.getAmount());
        assertThat(original.getCategoryId()).isEqualTo(copy.getCategoryId());
        assertThat(original.getCounterpartId()).isEqualTo(copy.getCounterpartId());
        assertThat(original.getCounterpartTransactionId()).isEqualTo(copy.getCounterpartTransactionId());
        assertThat(original.getDate()).isEqualTo(copy.getDate());
        assertThat(original.getLastModified()).isEqualTo(copy.getLastModified());
    }

}

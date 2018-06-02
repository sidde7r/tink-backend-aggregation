package se.tink.backend.serialization.protobuf;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.RuntimeEnv;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class BigDecimalDelegateTest {

    @Before
    public void setUp() {
        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new BigDecimalDelegate());
    }

    public static class DoubleSerialized {
        @Tag(1)
        Double amount;
        @Exclude
        BigDecimal amount2;

        public DoubleSerialized() {
        }

        DoubleSerialized(Double amount, BigDecimal amount2) {
            this.amount = amount;
            this.amount2 = amount2;
        }
    }

    public static class BigDecimalSerialized {
        @Tag(1)
        BigDecimal amount;
        @Exclude
        Double amount2;

        public BigDecimalSerialized() {
        }

        BigDecimalSerialized(BigDecimal amount, Double amount2) {
            this.amount = amount;
            this.amount2 = amount2;
        }
    }

    private <T> byte[] write(T object) throws IOException {
        ProtobufMessageBodyWriter writer = new ProtobufMessageBodyWriter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writer.writeTo(object, object.getClass(), null, null, null, null, output);
        return output.toByteArray();
    }

    private <T> T read(byte[] bytes, Class<T> clazz) throws IOException {
        ProtobufMessageBodyReader reader = new ProtobufMessageBodyReader();
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        return (T) reader.readFrom(clazz, null, null, null, null, input);
    }

    @Test
    public void writeDoubleReadDouble() throws IOException {
        DoubleSerialized doubleSerialized = new DoubleSerialized(3d, BigDecimal.ONE);

        DoubleSerialized doubleSerialized2 = read(write(doubleSerialized), DoubleSerialized.class);

        assertThat(doubleSerialized2.amount).isEqualTo(3d);
        assertThat(doubleSerialized2.amount2).isNull();
    }

    @Test
    public void writeBigDecimalReadBigDecimal() throws IOException {
        BigDecimalSerialized bigDecimalSerialized = new BigDecimalSerialized(BigDecimal.ONE, 0.5);

        BigDecimalSerialized bigDecimalSerialized2 = read(write(bigDecimalSerialized), BigDecimalSerialized.class);

        assertThat(bigDecimalSerialized2.amount).isEqualByComparingTo("1");
        assertThat(bigDecimalSerialized2.amount2).isNull();
    }

    @Test
    public void writeNullBigDecimalReadNullBigDecimal() throws IOException {
        BigDecimalSerialized bigDecimalSerialized = new BigDecimalSerialized(null, 0.5);

        BigDecimalSerialized bigDecimalSerialized2 = read(write(bigDecimalSerialized), BigDecimalSerialized.class);

        assertThat(bigDecimalSerialized2.amount).isNull();
        assertThat(bigDecimalSerialized2.amount2).isNull();
    }

    @Test
    public void writeDoubleReadBigDecimal() throws IOException {
        DoubleSerialized doubleSerialized = new DoubleSerialized(3d, BigDecimal.ONE);

        BigDecimalSerialized bigDecimalSerialized = read(write(doubleSerialized), BigDecimalSerialized.class);

        assertThat(bigDecimalSerialized.amount).isEqualByComparingTo("3");
        assertThat(bigDecimalSerialized.amount2).isNull();
    }

    @Test
    public void writeNullDoubleReadNullBigDecimal() throws IOException {
        DoubleSerialized doubleSerialized = new DoubleSerialized(null, BigDecimal.ONE);

        BigDecimalSerialized bigDecimalSerialized = read(write(doubleSerialized), BigDecimalSerialized.class);

        assertThat(bigDecimalSerialized.amount).isNull();
        assertThat(bigDecimalSerialized.amount2).isNull();
    }

    @Test
    public void writeBigDecimalReadDouble() throws IOException {
        BigDecimalSerialized bigDecimalSerialized = new BigDecimalSerialized(BigDecimal.TEN, 4.5);

        DoubleSerialized doubleSerialized = read(write(bigDecimalSerialized), DoubleSerialized.class);

        assertThat(doubleSerialized.amount).isEqualTo(10);
        assertThat(doubleSerialized.amount2).isNull();
    }

    @Test
    public void writeNullBigDecimalReadNullDouble() throws IOException {
        BigDecimalSerialized bigDecimalSerialized = new BigDecimalSerialized(null, 4.5);

        DoubleSerialized doubleSerialized = read(write(bigDecimalSerialized), DoubleSerialized.class);

        assertThat(doubleSerialized.amount).isNull();
        assertThat(doubleSerialized.amount2).isNull();
    }
}
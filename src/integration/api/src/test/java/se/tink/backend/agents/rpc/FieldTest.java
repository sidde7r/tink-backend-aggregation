package se.tink.backend.agents.rpc;

import org.junit.Test;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Field;

public class FieldTest {

    @Test
    public void testFieldForCommerzbankSecrets() throws Exception{
        Field field = new Field();
        field.setDescription("iban");
        field.setImmutable(true);
        field.setName("iban");
        field.setMaxLength(22);
        field.setMinLength(0);
        se.tink.backend.agents.rpc.Field result = se.tink.backend.agents.rpc.Field.of(field);
    }
}

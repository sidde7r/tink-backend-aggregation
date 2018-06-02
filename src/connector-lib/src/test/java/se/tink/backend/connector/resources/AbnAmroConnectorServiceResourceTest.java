package se.tink.backend.connector.resources;

import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import se.tink.backend.connector.rpc.abnamro.TransactionEntity;
import se.tink.backend.core.AbnAmroBufferedTransaction;

public class AbnAmroConnectorServiceResourceTest {

    /**
     * Test that we map fields properly in {@link AbnAmroConnectorServiceResource}.
     */
    @Test
    public void testModelMappings() {
        ModelMapper mapper = new ModelMapper();
        mapper.addMappings(new TransactionMap());
        mapper.addMappings(new AbnAmroBufferedTransactionMap());

        mapper.getTypeMap(TransactionEntity.class, AbnAmroBufferedTransaction.class).validate();
        mapper.getTypeMap(AbnAmroBufferedTransaction.class, TransactionEntity.class).validate();
    }

}

class TransactionMap extends PropertyMap<TransactionEntity, AbnAmroBufferedTransaction> {
    @Override
    protected void configure() {
        // Fields that ModelMapper couldn't match, but are okay to not match.
        skip().setAccountNumber(0);
        skip().setCredentialsId(null);
        skip().setId(null);
    }
};

class AbnAmroBufferedTransactionMap extends PropertyMap<AbnAmroBufferedTransaction, TransactionEntity> {
    @Override
    protected void configure() {
        // Fields that ModelMapper couldn't match, but are okay to not match.
        skip().setCurrency(null);

        // Not used right now but will be used later when we know what ABN will send
        skip().setOrigin(null);
    }
};

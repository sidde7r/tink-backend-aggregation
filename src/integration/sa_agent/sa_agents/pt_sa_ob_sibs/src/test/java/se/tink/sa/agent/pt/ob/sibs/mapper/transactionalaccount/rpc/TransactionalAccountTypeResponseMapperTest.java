package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.TransactionalAccountType;

public class TransactionalAccountTypeResponseMapperTest {

    private TransactionalAccountTypeResponseMapper transactionalAccountTypeResponseMapper;

    private MappingContext mappingContext;

    @Before
    public void init() {
        transactionalAccountTypeResponseMapper = new TransactionalAccountTypeResponseMapper();
        mappingContext = MappingContext.newInstance();
    }

    @Test
    public void transactionalAccountTypeResponseMapperTest() {

        String source = "CHECKING";

        TransactionalAccountType type =
                transactionalAccountTypeResponseMapper.map(source, mappingContext);

        TestCase.assertNotNull(type);
        TestCase.assertEquals(TransactionalAccountType.CHECKING, type);
    }
}

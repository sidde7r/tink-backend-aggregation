package se.tink.sa.agent.pt.ob.sibs.mapper.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.services.fetch.account.AccountIdentifier;

public class IbanAccountIdentifierMapperTest {

    private static final String MOCK_IBAN = "SE7348218379946775691148";

    private IbanAccountIdentifierMapper ibanAccountIdentifierMapper;

    @Before
    public void init() {
        ibanAccountIdentifierMapper = new IbanAccountIdentifierMapper();
    }

    @Test
    public void testIbanAccountIdentifierMapper() {
        AccountEntity source = Mockito.mock(AccountEntity.class);
        Mockito.when(source.getIban()).thenReturn(MOCK_IBAN);

        AccountIdentifier result = ibanAccountIdentifierMapper.map(source);

        assertNotNull(result);
        assertNotNull(result.getAccountIdentifierTypeBasedFieldsList());
        assertEquals(1, result.getAccountIdentifierTypeBasedFieldsCount());
        assertEquals(MOCK_IBAN, result.getAccountIdentifierTypeBasedFieldsList().get(0).getValue());
    }
}

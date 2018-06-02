package se.tink.backend.categorization.lookup;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Giro;
import se.tink.backend.core.SwedishGiroType;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestUtil;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.metrics.MetricRegistry;

import static org.mockito.Mockito.verify;

@RunWith(GuiceRunner.class)
public class LookupGiroCommandIntegrationTest{
    private List<User> users;

    @Inject
    TestUtil testUtil;

    @Inject
    private GiroRepository giroRepository;

    @Inject
    private MerchantRepository merchantRepository;

    private MetricRegistry metricRegistry;

    @Inject
    private CategoryChangeRecordDao categoryChangeRecordDao;

    @Before
    public void setUp(){
        users = testUtil.getTestUsers("John Doe");
        metricRegistry = new MetricRegistry();
    }

    @Test
    @Ignore
    public void lookupExistingBankGiroNumberShouldBeImported() {

        // Three tests cases that are verified
        lookupExistingBankGiroNumberShouldBeImported("991-1298", "2120001165"); // Höganäs Kommun
        lookupExistingBankGiroNumberShouldBeImported("208-2949", "5567211288"); // Förorternas Bygg
        lookupExistingBankGiroNumberShouldBeImported("887-5262", "5569008815"); // Sjöbergs Markiser & Persienner

    }

    private void lookupExistingBankGiroNumberShouldBeImported(String correctBankGiro, String connectOrganizationId){
        users.forEach(u -> {
            Credentials credentials = getTestCredentials(u.getId());

            Transaction transaction1 = testUtil.getNewTransaction(u.getId(), -334, "");
            transaction1.setPayload(TransactionPayloadTypes.GIRO, correctBankGiro);
            transaction1.setCredentialsId(credentials.getId());

            TransactionProcessorContext context = getContext(u, credentials);
            LookupGiroCommand command = new LookupGiroCommand(
                    merchantRepository, giroRepository, metricRegistry, categoryChangeRecordDao);
            command.initialize();
            command.execute(transaction1);

            Giro giro = giroRepository.findOneByAccountNumber(correctBankGiro);
            Assert.assertNotNull(giro);
            Assert.assertEquals(connectOrganizationId, giro.getOrganizationId());
            Assert.assertEquals(SwedishGiroType.BG, giro.getType());
        });
    }

    @Test
    public void lookupBadBankGiroNumberShouldBeImportedEmpty() {
        users.forEach( u -> {
            Credentials credentials = getTestCredentials(u.getId());

            Transaction transaction1 = testUtil.getNewTransaction(u.getId(), -334, "");
            transaction1.setPayload(TransactionPayloadTypes.GIRO, "000-0001");
            transaction1.setCredentialsId(credentials.getId());

            TransactionProcessorContext context = getContext(u, credentials);
            LookupGiroCommand command = new LookupGiroCommand(
                    merchantRepository, giroRepository, metricRegistry, categoryChangeRecordDao);
            command.initialize();
            command.execute(transaction1);
            ArgumentCaptor<Giro> giroArgumentCaptor = ArgumentCaptor.forClass(Giro.class);
            verify(giroRepository).save(giroArgumentCaptor.capture());
            Giro giro = giroArgumentCaptor.getValue();
            Assert.assertNotNull(giro);
            Assert.assertNull(giro.getName());
            Assert.assertNull(giro.getOrganizationId());
            Assert.assertEquals(SwedishGiroType.BG, giro.getType());
        });
    }

    private TransactionProcessorContext getContext(User user, Credentials credentials) {
        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                Collections.<Transaction>emptyList()
        );

        TransactionProcessorUserData data = new TransactionProcessorUserData();
        data.setCredentials(Lists.newArrayList(credentials));

        context.setUserData(data);
        context.setCredentialsId(credentials.getId());

        return context;
    }

    private Credentials getTestCredentials(String userId) {
        Credentials c = new Credentials();
        c.setId(StringUtils.generateUUID());
        c.setUserId(userId);
        c.setProviderName("swedbank-bankid");
        return c;
    }

}

package se.tink.backend.consent.controllers;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.consent.cache.ConsentCache;
import se.tink.backend.consent.config.ConsentCacheConfiguration;
import se.tink.backend.consent.config.ConsentConfiguration;
import se.tink.backend.consent.core.Action;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.User;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.core.cassandra.CassandraUserConsent;
import se.tink.backend.consent.dao.ConsentDAO;
import se.tink.backend.consent.repository.cassandra.ConsentRepository;
import se.tink.backend.consent.repository.cassandra.UserConsentRepository;
import se.tink.backend.consent.rpc.ConsentRequest;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsentServiceControllerTest {
    @Mock
    private UserConsentRepository userConsentRepository;

    @Mock
    private ConsentRepository consentRepository;

    private ConsentServiceController consentServiceController;

    private ConsentConfiguration consentConfiguration = new ConsentConfiguration();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(consentRepository.findAll()).thenReturn(Lists.newArrayList(CassandraConsent.builder()
                .withBody("body")
                .withKey("key")
                .withLocale("en_US")
                .withTitle("Title")
                .withVersion("1.0.0")
                .build()));

        when(userConsentRepository.save(any(CassandraUserConsent.class))).then(AdditionalAnswers.returnsFirstArg());

        consentServiceController = new ConsentServiceController(
                new ConsentDAO(new ConsentCache(consentRepository, ConsentCacheConfiguration.NoCache()),
                        userConsentRepository), consentConfiguration);
    }

    @Test
    public void testGiveConsent() throws Exception {
        User user = new User();
        user.setId(UUIDUtils.generateUUID());
        user.setUsername("username");
        user.setLocale("en_US");

        List<UserConsent> userConsents = consentServiceController.list(user);

        assertThat(userConsents).isEmpty(); // User has not given any consents

        List<Consent> available = consentServiceController.available(user);

        assertThat(available).hasSize(1); // User has one available to accept

        Consent consentToAccept = available.get(0);

        ConsentRequest request = new ConsentRequest();
        request.setAction(Action.ACCEPTED);
        request.setKey(consentToAccept.getKey());
        request.setChecksum(consentToAccept.getChecksum());
        request.setVersion(consentToAccept.getVersion());

        UserConsent consent = consentServiceController.consent(user, request); // Give consent
        assertThat(consent).isNotNull();
        verify(userConsentRepository).save(any(CassandraUserConsent.class));
    }
}

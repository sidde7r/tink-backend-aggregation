package se.tink.backend.consent.dao;

import com.google.common.collect.Lists;
import java.security.SignatureException;
import java.util.UUID;
import org.junit.Test;
import se.tink.backend.consent.core.Action;
import se.tink.backend.consent.core.Consent;
import se.tink.backend.consent.core.UserConsent;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import se.tink.backend.consent.core.cassandra.CassandraUserConsent;
import se.tink.backend.consent.core.cassandra.LinkEntity;
import se.tink.backend.consent.core.cassandra.MessageEntity;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentDAOTest {
    @Test
    public void testConversionBetweenClasses() {
        ConsentDAO.getModelMapper().validate();
    }

    @Test
    public void testUserConsentToCassandraUserConsentMapping() throws SignatureException {
        CassandraConsent consent = CassandraConsent.builder()
                .withKey("KEY")
                .withVersion("1.0.0")
                .withLocale("nl_NL")
                .withTitle("Title")
                .withBody("body")
                .withMessage(new MessageEntity("foo"))
                .withMessage(new MessageEntity("bar"))
                .build();

        CassandraUserConsent cassandraUserConsent = CassandraUserConsent.builder()
                .withAction(Action.DECLINED)
                .withConsent(consent)
                .withUserId(UUID.randomUUID())
                .withUsername("erik.pettersson@tink.se")
                .build();

        UserConsent result = ConsentDAO.getModelMapper().map(cassandraUserConsent, UserConsent.class);

        assertThat(result.getKey()).isEqualTo(cassandraUserConsent.getKey());
        assertThat(result.getVersion()).isEqualTo(cassandraUserConsent.getVersion());
        assertThat(result.getAction().toString()).isEqualTo(cassandraUserConsent.getAction());
        assertThat(result.getTimestamp()).isEqualTo(cassandraUserConsent.getTimestamp());
        assertThat(result.getId()).isEqualTo(UUIDUtils.toTinkUUID(cassandraUserConsent.getId()));
    }

    @Test
    public void testConsentToCassandraConsentMapping() {
        MessageEntity message = new MessageEntity("foo");
        message.setLinks(Lists.newArrayList(new LinkEntity("link", 0, 3)));

        CassandraConsent cassandraConsent = CassandraConsent.builder()
                .withBody("body")
                .withKey("key")
                .withLocale("locale")
                .withTitle("title")
                .withVersion("1.0.0")
                .withMessage(message)
                .build();

        Consent result = ConsentDAO.getModelMapper().map(cassandraConsent, Consent.class);

        assertThat(result.getKey()).isEqualTo(cassandraConsent.getKey());
        assertThat(result.getVersion()).isEqualTo(cassandraConsent.getVersion());
        assertThat(result.getBody()).isEqualTo(cassandraConsent.getBody());
        assertThat(result.getTitle()).isEqualTo(cassandraConsent.getTitle());
        assertThat(result.getMessages().size()).isEqualTo(cassandraConsent.getMessages().size());

        assertThat(result.getMessages().get(0).getMessage()).isEqualTo("foo");
        assertThat(result.getMessages().get(0).getLinks().get(0).getDestination()).isEqualTo("link");
        assertThat(result.getMessages().get(0).getLinks().get(0).getStart()).isEqualTo(0);
        assertThat(result.getMessages().get(0).getLinks().get(0).getEnd()).isEqualTo(3);
    }
}

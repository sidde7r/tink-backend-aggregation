package se.tink.backend.connector.auth;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.libraries.auth.AuthorizationHeaderPredicate;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorAuthorizationFilterPredicatesTest {

    @Test
    public void testShouldThrowExceptionOnNullInput() {
        try {
            new ConnectorAuthorizationFilterPredicates(null);
        } catch (Exception e) {
            assertThat(e).hasMessage("Client list is null");
        }
    }

    @Test
    public void testShouldThrowExceptionOnEmptyClientList() {
        try {
            new ConnectorAuthorizationFilterPredicates(Collections.emptyMap());
        } catch (Exception e) {
            assertThat(e).hasMessage("Client list is empty");
        }
    }

    @Test
    public void testShouldThrowExceptionOnEmptyTokenList() {
        Map<String, List<String>> map = Collections.singletonMap("client1", emptyList());
        try {
            new ConnectorAuthorizationFilterPredicates(map);
        } catch (Exception e) {
            assertThat(e).hasMessage("Client does not have any tokens");
        }
    }

    @Test
    public void testShouldReturnTokensByClient() {
        Map<String,List<String>> map = Collections.singletonMap("client1", singletonList("token1"));

        ConnectorAuthorizationFilterPredicates predicates = new ConnectorAuthorizationFilterPredicates(map);

        AuthorizationHeaderPredicate predicate = predicates.getPredicateByClient("client1");

        assertThat(predicate.apply("token token1")).isTrue();
        assertThat(predicate.apply("token token3")).isFalse();
    }

    @Test
    public void testShouldThrowExceptionOnInvalidClient() {
        Map<String, List<String>> map = Collections.singletonMap("client1", singletonList("token1"));

        ConnectorAuthorizationFilterPredicates predicates = new ConnectorAuthorizationFilterPredicates(map);

        try {
            predicates.getPredicateByClient("client2");
        } catch (Exception e) {
            assertThat(e).hasMessage("Client not found");
        }
    }

}

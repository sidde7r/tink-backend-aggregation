package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;

@RunWith(JUnitParamsRunner.class)
public class DkSSAuthenticationControllerTest {

    private Map<DkSSMethod, DkSSAuthenticatorProvider> authenticatorProviders;
    private Map<DkSSMethod, Authenticator> authenticators;

    private InOrder mocksToVerifyInOrder;

    @Before
    public void setup() {
        Authenticator nemIdAuthenticator = mock(Authenticator.class);
        DkSSAuthenticatorProvider nemIdAuthenticatorProvider = () -> nemIdAuthenticator;

        Authenticator mitIdAuthenticator = mock(Authenticator.class);
        DkSSAuthenticatorProvider mitIdAuthenticatorProvider = () -> mitIdAuthenticator;

        authenticators =
                ImmutableMap.of(
                        DkSSMethod.NEM_ID, nemIdAuthenticator,
                        DkSSMethod.MIT_ID, mitIdAuthenticator);
        authenticatorProviders =
                ImmutableMap.of(
                        DkSSMethod.NEM_ID, nemIdAuthenticatorProvider,
                        DkSSMethod.MIT_ID, mitIdAuthenticatorProvider);
        mocksToVerifyInOrder = inOrder(authenticators.values().toArray());
    }

    @Test
    @Parameters(method = "paramsWithChooseAuthMethodTestCases")
    public void should_recognize_methods_and_authenticate_with_them(
            ChooseAuthMethodTestCase testCase) {
        // given
        DkSSAuthenticatorsConfig authenticatorsConfig =
                testCase.prepareConfig(authenticatorProviders);
        Credentials credentials = testCase.prepareCredentials();

        DkSSAuthenticationController authenticationController =
                new DkSSAuthenticationController(authenticatorsConfig);

        // when
        Throwable throwable =
                catchThrowable(() -> authenticationController.authenticate(credentials));

        // then
        if (testCase.getExpectedMethod() != null) {
            mocksToVerifyInOrder
                    .verify(authenticators.get(testCase.getExpectedMethod()))
                    .authenticate(credentials);
        }
        if (testCase.getExpectedException() != null) {
            assertThat(throwable)
                    .usingRecursiveComparison()
                    .isEqualTo(testCase.getExpectedException());
        }
        mocksToVerifyInOrder.verifyNoMoreInteractions();
    }

    @SuppressWarnings("unused")
    private static Object[] paramsWithChooseAuthMethodTestCases() {
        return Stream.of(
                        ChooseAuthMethodTestCase.builder()
                                .addCredentialsField(
                                        Field.Key.AUTH_METHOD_SELECTOR,
                                        DkSSMethod.MIT_ID.getSupplementalInfoKey())
                                .methodsWithProvider(asList(DkSSMethod.MIT_ID, DkSSMethod.NEM_ID))
                                .expectedMethod(DkSSMethod.MIT_ID)
                                .build(),
                        ChooseAuthMethodTestCase.builder()
                                .addCredentialsField(
                                        Field.Key.AUTH_METHOD_SELECTOR,
                                        DkSSMethod.NEM_ID.getSupplementalInfoKey())
                                .methodsWithProvider(asList(DkSSMethod.MIT_ID, DkSSMethod.NEM_ID))
                                .expectedMethod(DkSSMethod.NEM_ID)
                                .build(),
                        ChooseAuthMethodTestCase.builder()
                                .methodsWithProvider(asList(DkSSMethod.MIT_ID, DkSSMethod.NEM_ID))
                                .expectedException(
                                        new IllegalStateException(
                                                "Cannot find authentication method selector field"))
                                .build(),
                        ChooseAuthMethodTestCase.builder()
                                .addCredentialsField(Field.Key.AUTH_METHOD_SELECTOR, "unknownValue")
                                .methodsWithProvider(asList(DkSSMethod.MIT_ID, DkSSMethod.NEM_ID))
                                .expectedException(
                                        new IllegalStateException(
                                                "No method matching method for field value: unknownValue"))
                                .build(),
                        ChooseAuthMethodTestCase.builder()
                                .addCredentialsField(
                                        Field.Key.AUTH_METHOD_SELECTOR,
                                        DkSSMethod.MIT_ID.getSupplementalInfoKey())
                                .methodsWithProvider(singletonList(DkSSMethod.NEM_ID))
                                .expectedException(
                                        new IllegalStateException(
                                                "No authentication provider for method: MIT_ID"))
                                .build(),
                        ChooseAuthMethodTestCase.builder()
                                .addCredentialsField(
                                        Field.Key.AUTH_METHOD_SELECTOR,
                                        DkSSMethod.NEM_ID.getSupplementalInfoKey())
                                .methodsWithProvider(singletonList(DkSSMethod.MIT_ID))
                                .expectedException(
                                        new IllegalStateException(
                                                "No authentication provider for method: NEM_ID"))
                                .build())
                .toArray();
    }

    @Getter
    @Builder
    private static class ChooseAuthMethodTestCase {

        @Singular("addCredentialsField")
        private final Map<Field.Key, String> credentialsFields;

        private final List<DkSSMethod> methodsWithProvider;

        private final DkSSMethod expectedMethod;
        private final Exception expectedException;

        private Credentials prepareCredentials() {
            Credentials credentials = mock(Credentials.class);

            when(credentials.hasField(any()))
                    .thenAnswer(
                            invocation -> {
                                Field.Key fieldKey = invocation.getArgument(0);
                                return credentialsFields.containsKey(fieldKey);
                            });
            when(credentials.getField(any(Field.Key.class)))
                    .thenAnswer(
                            invocation -> {
                                Field.Key fieldKey = invocation.getArgument(0);
                                return credentialsFields.get(fieldKey);
                            });

            return credentials;
        }

        private DkSSAuthenticatorsConfig prepareConfig(
                Map<DkSSMethod, DkSSAuthenticatorProvider> authenticatorProviders) {
            DkSSAuthenticatorsConfig.DkSSAuthenticatorsConfigBuilder builder =
                    DkSSAuthenticatorsConfig.builder();

            for (DkSSMethod method : methodsWithProvider) {
                DkSSAuthenticatorProvider authenticatorProvider =
                        authenticatorProviders.get(method);
                if (authenticatorProvider == null) {
                    continue;
                }
                builder.addProvider(method, authenticatorProvider);
            }

            return builder.build();
        }
    }
}

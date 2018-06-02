package se.tink.backend.auth;

import com.google.common.collect.Lists;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthenticationRequirementsTest {

    @Test
    public void checkDefaultRequirements() {
        AuthenticationRequirements authenticationRequirements = AuthenticationRequirements.defaultRequirements();

        assertEquals("Authentication is required by default", true,
                authenticationRequirements.isAuthenticationRequired());
        assertEquals("Device authorization is required by default", true,
                authenticationRequirements.isAuthorizedDeviceRequired());
        assertTrue("FeatureGroups is empty by default",
                authenticationRequirements.getRequiredFeatureGroups().isEmpty());
        assertTrue("Authentication scopes is empty by default", authenticationRequirements.getScopes().isEmpty());
    }

    @Test
    public void correctConvertFromAuthenticated() {
        final boolean required = true;
        final boolean requireAuthorizedDevice = false;
        final FeatureFlags.FeatureFlagGroup[] requireFeatureGroup = { FeatureFlags.FeatureFlagGroup.APPLICATIONS_FEATURE };
        final String[] scopes = {
                OAuth2AuthorizationScopeTypes.CREDENTIALS_REFRESH,
                OAuth2AuthorizationScopeTypes.CREDENTIALS_WRITE
        };

        Authenticated authenticated = new Authenticated() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean required() {
                return required;
            }

            @Override
            public boolean requireAuthorizedDevice() {
                return requireAuthorizedDevice;
            }

            @Override
            public FeatureFlags.FeatureFlagGroup[] requireFeatureGroup() {
                return requireFeatureGroup;
            }

            @Override
            public String[] scopes() {
                return scopes;
            }
        };

        AuthenticationRequirements authenticationRequirements = AuthenticationRequirements
                .fromAuthenticated(authenticated);

        assertEquals(required, authenticationRequirements.isAuthenticationRequired());
        assertEquals(requireAuthorizedDevice, authenticationRequirements.isAuthorizedDeviceRequired());
        assertThat(authenticationRequirements.getRequiredFeatureGroups()).containsOnly(requireFeatureGroup);
        assertThat(authenticationRequirements.getScopes()).containsOnly(scopes);
    }

    @Test
    public void receiveDefaultValueOnNotChangedFields() {
        FeatureFlags.FeatureFlagGroup featureGroup = FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE;
        String scope = OAuth2AuthorizationScopeTypes.DOCUMENTS_READ;

        AuthenticationRequirements authenticationRequirements = AuthenticationRequirements.builder()
                .addRequireFeatureGroup(
                        featureGroup).addScope(scope).build();

        assertThat(authenticationRequirements.getRequiredFeatureGroups()).containsOnly(featureGroup);
        assertThat(authenticationRequirements.getScopes()).containsOnly(scope);
        assertEquals("Authentication is required by default", true,
                authenticationRequirements.isAuthenticationRequired());
        assertEquals("Device authorization is required by default", true,
                authenticationRequirements.isAuthorizedDeviceRequired());
    }

    @Test
    public void doNotRewriteValuesForAddingList() {
        FeatureFlags.FeatureFlagGroup featureGroup = FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE;
        String scope = OAuth2AuthorizationScopeTypes.DOCUMENTS_READ;

        List<FeatureFlags.FeatureFlagGroup> featureGroupList = Arrays
                .asList(FeatureFlags.FeatureFlagGroup.FRAUD_FEATURE_V2,
                        FeatureFlags.FeatureFlagGroup.TRACKING_DISABLED);
        List<String> scopeList = Arrays
                .asList(OAuth2AuthorizationScopeTypes.ACCOUNTS_READ, OAuth2AuthorizationScopeTypes.CREDENTIALS_READ);

        AuthenticationRequirements authenticationRequirements = AuthenticationRequirements.builder()
                .addRequireFeatureGroup(featureGroup).addRequireFeatureGroups(featureGroupList).addScope(scope)
                .addScopes(scopeList).build();

        List<FeatureFlags.FeatureFlagGroup> expFeatureGroupList = Lists.newArrayList(featureGroupList);
        expFeatureGroupList.add(featureGroup);

        List<String> extScopes = Lists.newArrayList(scopeList);
        extScopes.add(scope);

        assertThat(authenticationRequirements.getRequiredFeatureGroups()).containsOnlyElementsOf(expFeatureGroupList);
        assertThat(authenticationRequirements.getScopes()).containsOnlyElementsOf(extScopes);
    }
}

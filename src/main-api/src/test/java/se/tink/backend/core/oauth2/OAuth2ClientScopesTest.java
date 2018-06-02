package se.tink.backend.core.oauth2;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class OAuth2ClientScopesTest {

    private String scope = "accounts:read,user:read,transfer:read,transfer:execute";

    @Test
    public void allScopesExistInSet() {
        Set<String> scopes = new OAuth2ClientScopes(scope).getScopeSet();

        Assert.assertEquals(4, scopes.size());
        Assert.assertTrue(scopes.contains(OAuth2AuthorizationScopeTypes.ACCOUNTS_READ));
        Assert.assertTrue(scopes.contains(OAuth2AuthorizationScopeTypes.USER_READ));
        Assert.assertTrue(scopes.contains(OAuth2AuthorizationScopeTypes.TRANSFER_READ));
        Assert.assertTrue(scopes.contains(OAuth2AuthorizationScopeTypes.TRANSFER_EXECUTE));
    }

    @Test
    public void emptyScopeReturnsNothingInSet() {
        Set<String> scopes = new OAuth2ClientScopes("").getScopeSet();

        Assert.assertEquals(0, scopes.size());
    }

    @Test
    public void singleRequestedScopeExists() {
        OAuth2ClientScopes scopes = new OAuth2ClientScopes(scope);

        Assert.assertTrue(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.ACCOUNTS_READ));
        Assert.assertTrue(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.USER_READ));
        Assert.assertTrue(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.TRANSFER_READ));
        Assert.assertTrue(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.TRANSFER_EXECUTE));
    }

    @Test
    public void emptyStringIsInvalid() {
        OAuth2ClientScopes scopes = new OAuth2ClientScopes(scope);

        Assert.assertFalse(scopes.isRequestedScopeValid(","));
        Assert.assertFalse(scopes.isRequestedScopeValid(""));
    }

    @Test
    public void multipleRequestedScopeExist() {
        OAuth2ClientScopes scopes = new OAuth2ClientScopes(scope);

        Assert.assertTrue(scopes.isRequestedScopeValid("accounts:read,user:read"));
        Assert.assertTrue(scopes.isRequestedScopeValid("transfer:read,transfer:execute"));
        Assert.assertTrue(scopes.isRequestedScopeValid("transfer:execute,user:read"));
    }

    @Test
    public void singleRequestedScopesIsNonExisting() {
        OAuth2ClientScopes scopes = new OAuth2ClientScopes(scope);

        Assert.assertFalse(scopes.isRequestedScopeValid("accounts:write"));
        Assert.assertFalse(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.ALL));
        Assert.assertFalse(scopes.isRequestedScopeValid(OAuth2AuthorizationScopeTypes.CREDENTIALS_READ));
    }

    @Test
    public void multipleRequestedScopeAreNonExisting() {
        OAuth2ClientScopes scopes = new OAuth2ClientScopes(scope);

        Assert.assertFalse(scopes.isRequestedScopeValid("accounts:write,user:read"));
        Assert.assertFalse(scopes.isRequestedScopeValid("transfer:read,transfer:execute,credentials:read"));
        Assert.assertFalse(scopes.isRequestedScopeValid("credentials:write,credentials:read"));
        Assert.assertFalse(scopes.isRequestedScopeValid("user:read,accounts:read,all"));
    }
}

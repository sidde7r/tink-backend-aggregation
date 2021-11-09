package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class CreditAgricoleBranchMapperTest {

    private CreditAgricoleBranchMapper objectUnderTest;

    @Before
    public void init() {
        objectUnderTest = new CreditAgricoleBranchMapper();
    }

    @Test
    @Parameters(method = "givenBranchNames")
    public void shouldDetermineBranchFromGivenName(
            String providerName, CreditAgricoleBranchConfiguration expectedConfiguration) {
        // when
        CreditAgricoleBranchConfiguration branchConfiguration =
                objectUnderTest.determineBranchConfiguration(providerName);
        // then
        assertThat(branchConfiguration.getBaseUrl()).isEqualTo(expectedConfiguration.getBaseUrl());
        assertThat(branchConfiguration.getAuthorizeUrl())
                .isEqualTo(expectedConfiguration.getAuthorizeUrl());
    }

    @Test
    public void shouldThrowExceptionFromUnknownName() {
        // when
        Throwable throwable =
                catchThrowable(() -> objectUnderTest.determineBranchConfiguration("unknown-name"));

        // then
        assertThat(throwable)
                .hasMessage(
                        "Could now find CreditAgricole branch configuration for name: unknown-name");
    }

    @SuppressWarnings("unused")
    private Object[] givenBranchNames() {
        return new Object[] {
            new Object[] {
                "fr-creditagricolelacorse-ob",
                new CreditAgricoleBranchConfiguration(
                        "https://psd2-api.ca-corse.fr",
                        "https://psd2-portal.credit-agricole.fr/ca-corse/authorize")
            },
            new Object[] {
                "fr-creditagricolebanquechalus-ob",
                new CreditAgricoleBranchConfiguration(
                        "https://psd2-api.banque-chalus.fr",
                        "https://psd2-portal.banque-chalus.fr/banque-chalus/authorize")
            }
        };
    }
}

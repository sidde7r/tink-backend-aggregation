package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;

public class SwedbankBalticsAuthenticator extends StatelessProgressiveAuthenticator {

  private final List<AuthenticationStep> authenticationSteps;

  public SwedbankBalticsAuthenticator() {
    this.authenticationSteps = Arrays.asList();
  }

  @Override
  public List<AuthenticationStep> authenticationSteps() {
    return authenticationSteps;
  }
}

package se.tink.backend.system.product.mortgage;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.core.User;

public class CompoundMortgageParameterFinder implements MortgageParameterFinder {

    private final List<MortgageParameterFinder> parameterFinders;

    @Inject
    public CompoundMortgageParameterFinder(ImmutableList<MortgageParameterFinder> prioritizedParameterFinders) {
        this.parameterFinders = prioritizedParameterFinders;
    }

    @Override
    public MortgageParameters findMortgageParameters(User user) {
        MortgageParameters parameters = new MortgageParameters();

        // Based on order of parameterFinders, try to find missing parameter values
        for (MortgageParameterFinder parameterFinder : parameterFinders) {
            MortgageParameters foundParameters = parameterFinder.findMortgageParameters(user);

            if (!parameters.getPropertyType().isPresent()) {
                parameters.setPropertyType(foundParameters.getPropertyType().orElse(null));
            }

            if (!parameters.getMarketValue().isPresent()) {
                parameters.setMarketValue(foundParameters.getMarketValue().orElse(null));
            }

            if (!parameters.getMortgageAmount().isPresent()) {
                parameters.setMortgageAmount(foundParameters.getMortgageAmount().orElse(null));
            }

            if (!parameters.getNumberOfApplicants().isPresent()) {
                parameters.setNumberOfApplicants(foundParameters.getNumberOfApplicants().orElse(null));
            }

            if (parameters.hasAllParameters()) {
                break;
            }
        }

        return parameters;
    }
}

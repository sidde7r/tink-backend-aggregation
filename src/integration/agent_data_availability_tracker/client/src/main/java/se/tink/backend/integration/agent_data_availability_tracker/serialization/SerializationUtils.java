package se.tink.backend.integration.agent_data_availability_tracker.serialization;

import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.IdentityData;

public class SerializationUtils {

    public static AccountTrackingSerializer serializeAccount(
            final Account account, final AccountFeatures features) {
        return serializeAccount(account, features, null);
    }

    public static AccountTrackingSerializer serializeAccount(
            final Account account, final AccountFeatures features, Integer numberOfTransactions) {
        AccountTrackingSerializer serializer =
                new AccountTrackingSerializer(account, numberOfTransactions);

        if (features.getPortfolios() != null) {
            features.getPortfolios().stream()
                    .map(PortfolioTrackingSerializer::new)
                    .forEach(e -> serializer.addChild("portfolios", e));
        }

        if (features.getLoans() != null) {
            features.getLoans().stream()
                    .map(LoanTrackingSerializer::new)
                    .forEach(e -> serializer.addChild("loans", e));
        }

        return serializer;
    }

    public static IdentityDataSerializer serializeIdentityData(final IdentityData identityData) {
        return new IdentityDataSerializer(identityData);
    }
}

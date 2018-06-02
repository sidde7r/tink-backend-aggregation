package se.tink.backend.insights.app.generators;

import java.util.Optional;
import javax.inject.Inject;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.insights.app.CommandGateway;
import se.tink.backend.insights.app.commands.CreateResidenceDoYouOwnItCommand;
import se.tink.backend.insights.core.valueobjects.Address;
import se.tink.backend.insights.core.valueobjects.IdentityEventId;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.identity.IdentityQueryService;
import se.tink.backend.insights.utils.LogUtils;

public class ResidenceDoYouOwnItGenerator implements InsightGenerator {
    private static final LogUtils log = new LogUtils(ResidenceDoYouOwnItGenerator.class);

    private CommandGateway gateway;
    private IdentityQueryService identityQueryService;

    @Inject
    public ResidenceDoYouOwnItGenerator(CommandGateway gateway,
            IdentityQueryService identityQueryService) {
        this.gateway = gateway;
        this.identityQueryService = identityQueryService;
    }

    @Override
    public void generateIfShould(UserId userId) {
        //TODO: add reasonable trigger

        Optional<FraudDetails> fraudDetails = identityQueryService.getFraudAddressDetails(userId);
        if (!fraudDetails.isPresent()) {
            log.info(userId, "No insight generated. Reason: No fraud details found");
            return;
        }

        FraudAddressContent addressContent = (FraudAddressContent) fraudDetails.get().getContent();

        if (addressContent == null){
            log.info(userId, "No insight generated. Reason: No address content found");
            return;
        }

        CreateResidenceDoYouOwnItCommand command = new CreateResidenceDoYouOwnItCommand(userId, Address.of(addressContent.getAddress()),
                IdentityEventId.of(fraudDetails.get().getId()));

        gateway.on(command);
    }
}

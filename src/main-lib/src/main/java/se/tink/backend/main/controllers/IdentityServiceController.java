package se.tink.backend.main.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.common.workers.fraud.IdentityEventUtils;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudStatus;
import se.tink.libraries.identity.model.IdentityEvent;
import se.tink.libraries.identity.model.IdentityEventSummary;
import se.tink.libraries.identity.model.Identity;
import se.tink.libraries.identity.commands.AnswerIdentityEventCommand;
import se.tink.libraries.identity.commands.GetIdentityStateCommand;
import se.tink.libraries.identity.commands.GetIdentityEventCommand;
import se.tink.libraries.identity.commands.GetIdentityEventSummaryListCommand;
import se.tink.libraries.identity.commands.SeenIdentityEventCommand;

public class IdentityServiceController {

    private final static LogUtils log = new LogUtils(IdentityServiceController.class);
    private FraudDetailsRepository fraudDetailsRepository;

    @Inject
    public IdentityServiceController(FraudDetailsRepository fraudDetailsRepository) {
        this.fraudDetailsRepository = fraudDetailsRepository;
    }

    public IdentityEvent getIdentityEvent(GetIdentityEventCommand command) throws NoSuchElementException, IllegalArgumentException {
        FraudDetails fraudDetails = getFraudDetails(command.getId(), command.getUserId());
        fraudDetails = IdentityEventUtils.enrichFraudDetails(fraudDetails, command.getLocale(), command.getCurrency());
        return IdentityEventUtils.mapIdentityEvent(command.getLocale().toString(), fraudDetails);
    }

    public List<IdentityEventSummary> getIdentityEventSummaryList(GetIdentityEventSummaryListCommand command) {
        List<FraudDetails> fraudDetailsList = fraudDetailsRepository.findAllByUserId(command.getUserId());
        fraudDetailsList = IdentityEventUtils.enrichFraudDetails(command.getLocale(), command.getCurrency(), fraudDetailsList);
        return IdentityEventUtils.mapIdentityEventSummary(fraudDetailsList);
    }

    public List<IdentityEventSummary> seenIdentityEvents(SeenIdentityEventCommand command) throws IllegalArgumentException {
        List<FraudDetails> fraudDetailsList = fraudDetailsRepository.findAllForIds(command.getIdentityIds());
        for (FraudDetails fraudDetails : fraudDetailsList) {
            if (fraudDetails.isStatusEmpty()) {
                continue;
            }
            if (!Objects.equals(fraudDetails.getUserId(), command.getUserId())) {
                throw new IllegalArgumentException(String.format("Fraud details with id: %s does not belong to user (%s).", fraudDetails.getId(),
                        command.getUserId()));
            }
            if (fraudDetails.isSeen()) {
                log.info(command.getUserId(), String.format("FraudStatus of FraudDetails (id: %s), already set to SEEN.", fraudDetails.getId()));
                continue;
            }
            fraudDetails.setStatus(FraudStatus.SEEN);
        }
        fraudDetailsRepository.save(fraudDetailsList);
        fraudDetailsList = IdentityEventUtils.enrichFraudDetails(command.getLocale(), command.getCurrency(), fraudDetailsList);
        return IdentityEventUtils.mapIdentityEventSummary(fraudDetailsList);
    }

    public IdentityEvent answerIdentityEvent(AnswerIdentityEventCommand command) throws IllegalArgumentException {
        FraudDetails fraudDetails = getFraudDetails(command.getId(), command.getUserId());
        fraudDetails.setStatus(IdentityEventUtils.mapIdentityAnswerStatus(command.getAnswer()));
        fraudDetails = IdentityEventUtils.enrichFraudDetails(fraudDetailsRepository.save(fraudDetails), command.getLocale(), command.getCurrency());
        return IdentityEventUtils.mapIdentityEvent(command.getLocale().toString(), fraudDetails);
    }

    public Optional<Identity> getIdentityState(GetIdentityStateCommand command) {
        List<FraudDetails> fraudDetailsList = fraudDetailsRepository
                .findAllByUserIdOrderByCreatedAsc(command.getUserId());
        return getIdentityStateFromFraudDetails(fraudDetailsList);
    }

    private FraudDetails getFraudDetails(String id, String userId) throws NoSuchElementException, IllegalArgumentException {
        FraudDetails fraudDetails = fraudDetailsRepository.findOne(id);

        if (fraudDetails == null) {
            throw new NoSuchElementException(String.format("Couldn't find fraud details with id: %s", id));
        }

        if (!Objects.equals(fraudDetails.getUserId(), userId)) {
            throw new IllegalArgumentException(String.format("Fraud details with id: %s does not belong to user (%s).", id, userId));
        }
        return fraudDetails;
    }

    private Optional<Identity> getIdentityStateFromFraudDetails(List<FraudDetails> fraudDetailsList) {
        if (fraudDetailsList == null || fraudDetailsList.isEmpty()) {
            return Optional.empty();
        }

        Identity identity = new Identity();
        for (FraudDetails fraudDetails : fraudDetailsList) {
            IdentityEventUtils.handle(fraudDetails, identity);
        }
        return Optional.of(identity);
    }

}

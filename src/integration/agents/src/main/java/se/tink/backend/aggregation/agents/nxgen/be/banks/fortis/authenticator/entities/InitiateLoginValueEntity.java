package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class InitiateLoginValueEntity {
    private CardInfoEntity cardInfo;
    private List<ChannelAgreementsEntity> channelAgreements;
    private UcrChallengesEntity ucr;
    private EasyPinChallengeEntity easyPin;
}

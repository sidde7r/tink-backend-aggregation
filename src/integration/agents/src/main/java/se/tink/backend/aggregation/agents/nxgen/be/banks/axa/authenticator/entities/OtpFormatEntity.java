package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpFormatEntity {

    private Data data;
    private String type;

    public Data getData() {
        return data;
    }

    public String getType() {
        return type;
    }

    @JsonObject
    public static class Data {
        private String challenge;
        private String type;
        private List<Challenge> challengeList;

        public String getChallenge() {
            return challenge;
        }

        public String getType() {
            return type;
        }

        public List<Challenge> getChallengeList() {
            return challengeList;
        }
    }

    @JsonObject
    public static class Challenge {
        private String challenge;
        private String card;

        public String getChallenge() {
            return challenge;
        }

        public String getCard() {
            return card;
        }
    }
}

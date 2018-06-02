package se.tink.backend.categorization.rpc;

public class FastTextTrainRequest {
    private String targetModel;
    private String sourceTrainingSet;

    public FastTextTrainRequest(String targetModel, String sourceTrainingSet) {
        this.targetModel = targetModel;
        this.sourceTrainingSet = sourceTrainingSet;
    }

    public String getTargetModel() {
        return targetModel;
    }

    public String getSourceTrainingSet() {
        return sourceTrainingSet;
    }
}

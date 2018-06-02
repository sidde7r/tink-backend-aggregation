package se.tink.backend.system.workers.processor.formatting;

public interface DescriptionFormatter {
    String clean(String description);

    // Move extrapolation out of formatter? Is it an orthogonal use case?
    String extrapolate(String description);
}

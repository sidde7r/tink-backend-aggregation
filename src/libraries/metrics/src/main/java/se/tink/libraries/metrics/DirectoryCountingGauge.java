package se.tink.libraries.metrics;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileFilter;

/** A {@link Gauge} for the number of files in a directory. */
public class DirectoryCountingGauge extends Gauge {

    private File file;
    private FileFilter filter;

    public DirectoryCountingGauge(File file, FileFilter filter) {
        Preconditions.checkArgument(
                file.isDirectory(), String.format("%s is not a directory.", file));
        this.file = file;
        this.filter = filter;
    }

    @Override
    public double getValue() {
        File[] files = file.listFiles(filter);
        if (files == null) {
            return Double.NaN;
        }
        return Double.valueOf(files.length);
    }
}

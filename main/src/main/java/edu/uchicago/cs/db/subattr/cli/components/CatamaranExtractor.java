package edu.uchicago.cs.db.subattr.cli.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CatamaranExtractor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private String executable;

    private Consumer<File[]> fileProcessor;

    public CatamaranExtractor(String executable) {
        this.executable = executable;
    }

    public void extract(URI input) throws IOException, InterruptedException {
        File inputFile = new File(input);
        String inputPath = inputFile.getAbsolutePath();
        String outputPath = inputPath + ".catamaran";
        String logPath = inputPath + ".log";

//        logger.info("Executing Catamaran on input: "+ inputPath + " and output: " + outputPath);
        Process process = new ProcessBuilder().command(executable, inputPath, outputPath, logPath).start();
        process.waitFor(10, TimeUnit.MINUTES);
        if (process.isAlive()) {
            process.destroy();
            process.waitFor();
        }
        process.waitFor();

        String fileNamePrefix = inputFile.getName() + ".catamaran";
        // Look for files with catamaran suffix
        File[] catamaranFiles = new File(input).getParentFile().listFiles(
                (dir, name) -> name.startsWith(fileNamePrefix) && name.endsWith("tsv"));
        // Remove the large temp file it creates
        for (File cmFile : catamaranFiles) {
            Files.deleteIfExists(Paths.get(cmFile.getAbsolutePath().replaceFirst("tsv$", "cmbuffer")));
        }

        fileProcessor.accept(catamaranFiles);
    }

    public Consumer<File[]> getFileProcessor() {
        return fileProcessor;
    }

    public void setFileProcessor(Consumer<File[]> fileProcessor) {
        this.fileProcessor = fileProcessor;
    }
}

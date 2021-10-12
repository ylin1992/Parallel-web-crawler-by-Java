package com.udacity.webcrawler.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
    private final CrawlResult result;

    /**
     * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
     */
    public CrawlResultWriter(CrawlResult result) {
        this.result = Objects.requireNonNull(result);
    }

    /**
     * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
     *
     * <p>If a file already exists at the path, the existing file should not be deleted; new data
     * should be appended to it.
     *
     * @param path the file path where the crawl result data should be written.
     */
    public void write(Path path) throws IOException {
        // This is here to get rid of the unused variable warning.
        Objects.requireNonNull(path);
        // TODO: Fill in this method.
        // if file doesn't exists, create one
        File file = path.toFile();
        if (!file.exists()) {
            file.createNewFile();
        }

        try (FileWriter fileWriter = new FileWriter(file, true);
             Writer writer = new BufferedWriter(fileWriter)) {
            write(writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
     *
     * @param writer the destination where the crawl result data should be written.
     */
    public void write(Writer writer) throws IOException {
        // This is here to get rid of the unused variable warning.
        Objects.requireNonNull(writer);

        // TODO: Fill in this method.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        //String jsonData = objectMapper.writeValueAsString(result);
        objectMapper.writeValue(writer, result);
        writer.write("\n\n");
    }
}

package com.udacity.webcrawler.main;

import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;

import javax.inject.Inject;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;

public final class WebCrawlerMain {

    private final CrawlerConfiguration config;

    private WebCrawlerMain(CrawlerConfiguration config) {
        this.config = Objects.requireNonNull(config);
    }

    @Inject
    private WebCrawler crawler;

    @Inject
    private Profiler profiler;

    private void run() throws Exception {
        Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

        // print configuration for debugging
        System.out.println("Start crawling....");
        System.out.println("Configuration: " + config.toString());

        CrawlResult result = crawler.crawl(config.getStartPages());
        CrawlResultWriter resultWriter = new CrawlResultWriter(result);
        // TODO: Write the crawl results to a JSON file (or System.out if the file name is empty)
        String resultPath = config.getResultPath();
        Path path;
        if (resultPath == null || resultPath.length() == 0) {
            Writer resultStreamWriter = new OutputStreamWriter(System.out);
            resultWriter.write(resultStreamWriter);
            resultStreamWriter.flush();
        } else {
            path = Path.of(resultPath);
            resultWriter.write(path);
        }

        // TODO: Write the profile data to a text file (or System.out if the file name is empty)

        Path profileOutputPath = Path.of(config.getProfileOutputPath());
        if (profileOutputPath == null || profileOutputPath.toString().isEmpty()) {
            Writer streamWriter = new OutputStreamWriter(System.out);
            profiler.writeData(streamWriter);
            streamWriter.flush();
        } else {
            profiler.writeData(profileOutputPath);
        }

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: WebCrawlerMain [starting-url]");
            return;
        }

        CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
        new WebCrawlerMain(config).run();
    }
}

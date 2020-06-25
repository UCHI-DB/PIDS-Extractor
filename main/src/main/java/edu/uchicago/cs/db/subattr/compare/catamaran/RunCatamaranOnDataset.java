package edu.uchicago.cs.db.subattr.compare.catamaran;

import edu.uchicago.cs.db.subattr.cli.components.CatamaranExtractor;
import edu.uchicago.cs.db.subattr.compare.dataload.ColumnData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Run catamaran extractor on column files
 */
public class RunCatamaranOnDataset {

    static Logger logger = LoggerFactory.getLogger(RunCatamaranOnDataset.class);

    protected static List<ColumnData> getColumns(String fileName) throws IOException {
        List<ColumnData> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split(",");
            ColumnData col = new ColumnData();
            col.setId(Integer.valueOf(fields[0]));
            col.setFileUri(new File(fields[1].substring(15)).toURI());
            result.add(col);
        }
        reader.close();
        return result;
    }

    public static void main(String[] args) throws Exception {
        List<ColumnData> columns = getColumns(args[1]);
        columns.parallelStream().forEach(col -> processColumn(args[0], col));
    }

    protected static void processColumn(String cmexe, ColumnData column) {
        logger.info("Column " + column.getId() + ": Processing " + column.getFileUri().toString());
        CatamaranExtractor extractor = new CatamaranExtractor(cmexe);

        URI columnFile = column.getFileUri();

        extractor.setFileProcessor(files -> {
        });
        try {
            extractor.extract(columnFile);
            logger.info("Column " + column.getId() + ": Processed");
        } catch (IOException e) {
            logger.error("Column " + column.getId() + ": Unable to process file", e);
        } catch (InterruptedException e) {
            logger.error("Column " + column.getId() + ": Processing timeout", e);
        } catch (Exception e) {
            logger.error("Column " + column.getId() + ": Exception", e);
        }
    }

    static String printStacktrace(Throwable e) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(output);
        e.printStackTrace(writer);
        writer.close();
        String result = new String(output.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }
}

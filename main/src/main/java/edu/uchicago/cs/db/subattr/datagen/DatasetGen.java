package edu.uchicago.cs.db.subattr.datagen;

import org.apache.commons.cli.*;

import java.util.function.Consumer;

public class DatasetGen {

    public static void genPhone(int size, Consumer<String> output) {
        GenHelper helper = new GenHelper();
        for (int i = 0; i < size; i++) {
            output.accept(String.format("(%s)%s-%s", helper.genDigit(3), helper.genDigit(3), helper.genDigit(4)));
        }
    }

    public static void genIpv6(int size, Consumer<String> output) {
        GenHelper helper = new GenHelper();
        for (int i = 0; i < size; i++) {
            output.accept(String.format("%s:%s:%s:%s:%s:%s:%s:%s", helper.genHex(4), helper.genHex(4),
                    helper.genHex(4), helper.genHex(4), helper.genHex(4), helper.genHex(4),
                    helper.genHex(4), helper.genHex(4)));
        }
    }

    public static void genTimestamp(int size, Consumer<String> output) {
        GenHelper helper = new GenHelper();
        for (int i = 0; i < size; i++) {
            output.accept(String.format("%s-%s-%s %s:%s:%s %s.%s",
                    helper.genRangeDigit(1970, 50), helper.genRangeDigit(1, 12),
                    helper.genRangeDigit(1, 30), helper.genRangeDigit(1, 24),
                    helper.genRangeDigit(1, 60), helper.genRangeDigit(1, 60),
                    helper.genDigit(4), helper.genDigit(5)));
        }
    }

    public static void genAddress(int size, Consumer<String> output) {
        GenAddress ga = new GenAddress();
        for (int i = 0; i < size; i++) {
            output.accept(ga.generate());
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addRequiredOption("t", "type", true, "Dataset Type(ipv6/timestamp/phone/address)");
        options.addRequiredOption("n", "num", true, "Number of Rows");

        CommandLineParser parser = new DefaultParser();
        Consumer<String> consumer = (string) -> {
            System.out.println(string);
        };

        try {
            CommandLine cmd = parser.parse(options, args);
            String type = cmd.getOptionValue('t');
            int num = Integer.parseInt(cmd.getOptionValue('n'));
            switch (type) {
                case "address":
                    genAddress(num, consumer);
                    break;
                case "phone":
                    genPhone(num, consumer);
                    break;
                case "ipv6":
                    genIpv6(num, consumer);
                    break;
                case "timestamp":
                    genTimestamp(num, consumer);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("DatasetGen", options);
        }
    }
}

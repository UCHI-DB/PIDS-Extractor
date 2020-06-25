package edu.uchicago.cs.db.subattr.datagen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class GenMachineId {
    public static void main(String[] args) throws Exception {
        PrintWriter writer = new PrintWriter(new FileOutputStream(args[1]));
        int count = Integer.parseInt(args[0]);

        GenHelper helper = new GenHelper();

        String header = helper.genUpperChar(4);

        for (int i = 0; i < count; i++) {
            writer.println(String.format("%s-%s-%s-%s", header, helper.genHex(5), helper.genHex(5), helper.genDigit(4)));
        }

        writer.close();
    }
}

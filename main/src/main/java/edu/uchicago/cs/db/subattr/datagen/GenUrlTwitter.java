package edu.uchicago.cs.db.subattr.datagen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class GenUrlTwitter {

    public static void main(String[] args) throws Exception {
        PrintWriter writer = new PrintWriter(new FileOutputStream(args[1]));
        int count = Integer.parseInt(args[0]);

        GenHelper helper = new GenHelper();

        for (int i = 0; i < count; i++) {
            writer.println(String.format("https://twitter.com/%s/status/%s", helper.genLowerChar(5), helper.genDigit(18)));
        }

        writer.close();
    }
}

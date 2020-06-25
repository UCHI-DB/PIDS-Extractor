package edu.uchicago.cs.db.subattr.datagen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class GenUrl {

    public static void main(String[] args) throws Exception {
        PrintWriter writer = new PrintWriter(new FileOutputStream(args[1]));
        int count = Integer.parseInt(args[0]);

        GenHelper helper = new GenHelper();

        for (int i = 0; i < count; i++) {
            writer.println(String.format("http://www.twitter.com/genpicture?picid=%s&pageid=%s&good=%s",
                    helper.genDigit(4), helper.genDigit(5), helper.genDigit(4)
            ));
        }

        writer.close();
    }
}

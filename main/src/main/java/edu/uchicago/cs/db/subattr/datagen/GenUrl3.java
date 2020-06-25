package edu.uchicago.cs.db.subattr.datagen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class GenUrl3 {

    public static void main(String[] args) throws Exception {
        PrintWriter writer = new PrintWriter(new FileOutputStream(args[1]));
        int count = Integer.parseInt(args[0]);

        GenHelper helper = new GenHelper();

        for (int i = 0; i < count; i++) {
            writer.println(String.format("https://i.imgur.com/%s.png", helper.genEverything(10)));
        }

        writer.close();
    }
}

package edu.uchicago.cs.db.subattr.datagen;

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * This generator generates data records of 50 characters, consisting of the following pieces
 * <p>
 * <CONST CHAR*10><CHAR*5><DIGIT*5><HEX*10><CONST CHAR*10><DIGIT*10>
 */
public class GenEqlenField {

    public static void main(String[] args) throws Exception {
        PrintWriter writer = new PrintWriter(new FileOutputStream(args[1]));
        int count = Integer.parseInt(args[0]);

        GenHelper helper = new GenHelper();

        String constChar1 = helper.genUpperChar(10);
        String constChar2 = helper.genUpperChar(10);

        for (int i = 0; i < count; i++) {
            writer.println(String.format("%s%s%s%s%s%s", constChar1, helper.genUpperChar(5), helper.genDigit(5),
                    helper.genHex(10), constChar2, helper.genDigit(10)));
        }

        writer.close();
    }
}

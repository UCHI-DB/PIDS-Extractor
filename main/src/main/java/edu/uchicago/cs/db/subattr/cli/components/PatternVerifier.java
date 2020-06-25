package edu.uchicago.cs.db.subattr.cli.components;

import edu.uchicago.cs.db.subattr.ir.*;
import scala.Unit;
import scala.collection.JavaConverters;
import scala.collection.JavaConverters.*;

import java.util.List;

public class PatternVerifier {

    static int THRESHOLD = 16;

    public PatternVerifier() {

    }

    public boolean verify(Pattern pattern) {
        if (pattern instanceof PSeq) {
            PSeq seq = (PSeq) pattern;
            int numsub = seq.content().count(p -> p instanceof PUnion || p instanceof PAny);
            if (numsub > THRESHOLD) {
                return false;
            }
            // If there is no separator between any, and the variance is high, this is likely not a valid pattern
            //
//            if (invalidLenCombo(JavaConverters.seqAsJavaList(seq.content()))) {
//                return false;
//            }
            return true;
        }
        return false;
    }

    protected boolean invalidLenCombo(List<Pattern> patterns) {
        int state = 0;
        for (Pattern pattern : patterns) {
            switch (state) {
                case 0:
                    if (pattern instanceof PAny) {
                        PAny any = (PAny) pattern;
                        if (any.maxLength() == -1 || any.maxLength() - any.minLength() > 5) {
                            state = 1;
                        }
                    }
                    break;
                case 1:
                    if (pattern instanceof PToken) {
                        state = 0;
                    } else if (pattern instanceof PAny) {
                        PAny any = (PAny) pattern;
                        if (any.maxLength() == -1 || any.maxLength() - any.minLength() > 5) {
                            return true;
                        }
                    }
                    break;
            }
        }
        return false;
    }
}

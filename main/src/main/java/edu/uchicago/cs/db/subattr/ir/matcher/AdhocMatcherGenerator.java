/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.db.subattr.ir.matcher;

import edu.uchicago.cs.db.subattr.ir.PSeq;
import edu.uchicago.cs.db.subattr.ir.Pattern;
import edu.uchicago.cs.db.subattr.ir.matcher.adhoc.AdhocClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static edu.uchicago.cs.db.subattr.ir.matcher.TransitionBuilder.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * Generate ad-hoc class for pattern matching
 */
public class AdhocMatcherGenerator {

    private static final boolean debug = false;

    private AdhocClassLoader classLoader = new AdhocClassLoader(Thread.currentThread().getContextClassLoader());

    private final String CLASS_NAME = "edu/uchicago/cs/db/subattr/ir/matcher/adhoc/AdhocMatcher{0}";
    private final String JAVA_NAME = "edu.uchicago.cs.db.subattr.ir.matcher.adhoc.AdhocMatcher{0}";

    private String className;

    private TransitionBuilder tb;

    public ExactMatcher generate(Pattern pattern) {
        if (!(pattern instanceof PSeq)) {
//            throw new UnsupportedOperationException(pattern.getClass().getName());
            pattern = PSeq.apply(new Pattern[]{pattern});
        }
        PSeq seq = (PSeq) pattern;
        tb = new TransitionBuilder2().on(seq);
//        System.out.println(tb.endState);
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String className = MessageFormat.format(CLASS_NAME, uuid);
        String javaName = MessageFormat.format(JAVA_NAME, uuid);

        byte[] binaryClass = generateClass(className);
        if (debug) {
            // For debug only
            try {
                FileOutputStream fos = new FileOutputStream("/home/harper/AdhocMatcher.class");
                fos.write(binaryClass);
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        classLoader.getExtraClassDefs().put(javaName, binaryClass);
        try {
            Class<?> clazz = classLoader.loadClass(javaName);
            return (ExactMatcher) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static final String INTERFACE_NAME = Type.getType(ExactMatcher.class).getInternalName();

    protected byte[] generateClass(String name) {
        this.className = name;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_8, ACC_PUBLIC, name, null,
                "java/lang/Object", new String[]{INTERFACE_NAME});

        // Create Empty constructor
        MethodVisitor cvis = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        cvis.visitCode();
        cvis.visitIntInsn(ALOAD, 0);
        cvis.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        cvis.visitInsn(RETURN);

        cvis.visitMaxs(1, 1);
        cvis.visitEnd();

        MethodVisitor mvis = cw.visitMethod(ACC_PUBLIC, "match",
                "(Ljava/lang/String;)[Ljava/lang/CharSequence;",
                null, null);

        mvis.visitCode();

        mvis.visitInsn(ICONST_0);
        mvis.visitInsn(DUP);
        mvis.visitInsn(DUP);
        mvis.visitIntInsn(ISTORE, 2); // state
        mvis.visitIntInsn(ISTORE, 3); // matchStart
        mvis.visitIntInsn(ISTORE, 4); // i

        pushConstant(mvis, tb.numGroup);
        mvis.visitTypeInsn(ANEWARRAY, "java/lang/CharSequence");
        mvis.visitIntInsn(ASTORE, 5); // matched
        mvis.visitIntInsn(ALOAD, 1); // input
        mvis.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String",
                "length", "()I", false);
        mvis.visitIntInsn(ISTORE, 6); // input.length
        mvis.visitInsn(ICONST_0);
        mvis.visitIntInsn(ISTORE, 7); // Reserved for nextChar

        Label mainLoopLabel = new Label();
        Label mainLoopEndLabel = new Label();
        Label failReturnLabel = new Label();


        mvis.visitLabel(mainLoopLabel);
        visitFrame(mvis);

        mvis.visitIntInsn(ILOAD, 4); // i
        mvis.visitIntInsn(ILOAD, 6); // length
        mvis.visitJumpInsn(IF_ICMPGT, mainLoopEndLabel);

        Label eoiElseLabel = new Label();
        Label eoiEndLabel = new Label();

        // (i == length) ? EOI : input.charAt(i)
        mvis.visitIntInsn(ILOAD, 6); // length
        mvis.visitIntInsn(ILOAD, 4);
        mvis.visitJumpInsn(IF_ICMPNE, eoiElseLabel);
        mvis.visitIntInsn(SIPUSH, 256);
        mvis.visitIntInsn(ISTORE, 7);
        mvis.visitJumpInsn(GOTO, eoiEndLabel);

        mvis.visitLabel(eoiElseLabel);
        visitFrame(mvis);

        mvis.visitIntInsn(ALOAD, 1); // input
        mvis.visitIntInsn(ILOAD, 4); // i
        mvis.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String",
                "charAt", "(I)C", false);
        mvis.visitIntInsn(ISTORE, 7);

        mvis.visitLabel(eoiEndLabel);
        visitFrame(mvis);

        Label endSwitchLabel = new Label();
        Label[] stateLabels = new Label[tb.transitions.size() - 1];
        for (int i = 0; i < stateLabels.length; i++) {
            stateLabels[i] = new Label();
        }
        mvis.visitIntInsn(ILOAD, 2);
        mvis.visitTableSwitchInsn(0, tb.endState - 1, failReturnLabel, stateLabels);

        for (int i = 0; i < stateLabels.length; i++) {
            mvis.visitLabel(stateLabels[i]);
            visitFrame(mvis);

            boolean markNewStart = false;

            buildTransition(mvis, i, endSwitchLabel, failReturnLabel);
        }

        mvis.visitLabel(endSwitchLabel);
        visitFrame(mvis);

        mvis.visitIincInsn(4, 1);
        mvis.visitJumpInsn(GOTO, mainLoopLabel);

        mvis.visitLabel(mainLoopEndLabel);
        visitFrame(mvis);

        mvis.visitIntInsn(ALOAD, 5);
        mvis.visitInsn(ARETURN);

        mvis.visitLabel(failReturnLabel);
        visitFrame(mvis);
//        if (debug) {
//            mvis.visitIntInsn(ILOAD, 4);
//            printInt(mvis);
//            mvis.visitIntInsn(ILOAD, 2);
//            printInt(mvis);
//        }
        mvis.visitInsn(ACONST_NULL);
        mvis.visitInsn(ARETURN);

        mvis.visitMaxs(3, 8);
        mvis.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }

    protected void substring(MethodVisitor mvis, int index) {
        mvis.visitIntInsn(ALOAD, 5);
        pushConstant(mvis, index);
        mvis.visitIntInsn(ALOAD, 1);
        mvis.visitIntInsn(ILOAD, 3);
        mvis.visitIntInsn(ILOAD, 4);
        mvis.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring",
                "(II)Ljava/lang/String;", false);
        // java/util/List.add:(Ljava/lang/Object;)Z
        mvis.visitInsn(AASTORE);
    }

    protected void buildTransition(MethodVisitor mvis, int state,
                                   Label endSwitchLabel, Label failReturnLabel) {
//        if (tb.transitions.get(state).size() >= 5) {
//            buildSwitchTransition(mvis, state, endSwitchLabel, failReturnLabel);
//        } else {
        buildIfElseTransition(mvis, state, endSwitchLabel);
//        }
    }

    protected void buildIfElseTransition(MethodVisitor mvis, int state, Label endSwitchLabel) {
        Map<Integer, Integer> transition = tb.transitions.get(state);

        int counter = 0;

        Integer emptyState = transition.get(CHAR_EMPTY);

        Label[] labels = new Label[transition.size() - (emptyState == null ? 0 : 1)];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new Label();
        }
        for (Map.Entry<Integer, Integer> entry : transition.entrySet()) {
            if (entry.getKey() == CHAR_EMPTY) {
                continue;
            }
            if (counter > 0) {
                mvis.visitLabel(labels[counter - 1]);
                visitFrame(mvis);
            }
            mvis.visitIntInsn(ILOAD, 7);
            // Condition
            switch (entry.getKey()) {
                case CHAR_NUM:
                    mvis.visitMethodInsn(INVOKESTATIC, "edu/uchicago/cs/db/subattr/ir/matcher/MatcherHelper",
                            "isDigit", "(C)Z", false);
                    mvis.visitJumpInsn(IFEQ, labels[counter]);
                    break;
                case CHAR_HEX:
                    mvis.visitMethodInsn(INVOKESTATIC, "edu/uchicago/cs/db/subattr/ir/matcher/MatcherHelper",
                            "isHex", "(C)Z", false);
                    mvis.visitJumpInsn(IFEQ, labels[counter]);
                    break;
                case CHAR_LETTER:
                    mvis.visitMethodInsn(INVOKESTATIC, "edu/uchicago/cs/db/subattr/ir/matcher/MatcherHelper",
                            "isLetter", "(C)Z", false);
                    mvis.visitJumpInsn(IFEQ, labels[counter]);
                    break;
                case CHAR_LABEL:
                    mvis.visitMethodInsn(INVOKESTATIC, "edu/uchicago/cs/db/subattr/ir/matcher/MatcherHelper",
                            "isLabel", "(C)Z", false);
                    mvis.visitJumpInsn(IFEQ, labels[counter]);
                    break;
                case CHAR_WORD:
                    mvis.visitMethodInsn(INVOKESTATIC, "edu/uchicago/cs/db/subattr/ir/matcher/MatcherHelper",
                            "isWord", "(C)Z", false);
                    mvis.visitJumpInsn(IFEQ, labels[counter]);
                    break;
                default:
                    // Generate equality,
                    pushConstant(mvis, entry.getKey());
                    // Jump to next
                    mvis.visitJumpInsn(IF_ICMPNE, labels[counter]);
                    break;
            }

            // Do State transition
            transitToState(mvis, state, entry.getValue());
            mvis.visitJumpInsn(GOTO, endSwitchLabel);
            counter++;
        }

        // Final else, No match failure
        mvis.visitLabel(labels[labels.length - 1]);
        visitFrame(mvis);
        // No match
        if (emptyState == null) {
            // No empty state, failure
//            if (debug) {
//                // Print out current state and input index
//                mvis.visitIntInsn(ILOAD, 4);
//                printInt(mvis);
//                mvis.visitIntInsn(ILOAD, 2);
//                printInt(mvis);
//            }
            mvis.visitInsn(ACONST_NULL);
            mvis.visitInsn(ARETURN);
        } else {
            // Jump to empty state
            transitToState(mvis, state, emptyState);
            // Read nothing, move i back
            mvis.visitIincInsn(4, -1);
            mvis.visitJumpInsn(GOTO, endSwitchLabel);
        }
    }


    protected void transitToState(MethodVisitor mvis, int state, int toState) {
        pushConstant(mvis, toState);
        mvis.visitIntInsn(ISTORE, 2);

        int guideForComing = tb.groupGuideMap.getOrDefault(toState, 0);

        //
        // See TransitionBuilder2.java line 86-94
        if ((guideForComing & 2) != 0) {
            // State is a group end
            int groupIndex = guideForComing >> 2;
            if (groupIndex < 0) {
                groupIndex = -groupIndex - 2;
            }
            if ((guideForComing & 1) != 0) {
                // This state is also a group start, the matched string should belong to previous group
                groupIndex -= 1;
            }
            substring(mvis, groupIndex);
        }
        // Handle group things
        if (toState != state && (guideForComing & 1) != 0) {
            mvis.visitIntInsn(ILOAD, 4);
            mvis.visitIntInsn(ISTORE, 3);
        }
    }


    protected void buildSwitchTransition(MethodVisitor mvis, int currentState,
                                         Label endSwitchLabel, Label failReturnLabel) {
        Map<Integer, Integer> transition = tb.transitions.get(currentState);
        Map<Integer, Label> choiceLabels = new HashMap<>();
        int total = 0;
        for (Map.Entry<Integer, Integer> e : transition.entrySet()) {
            switch (e.getKey()) {
                case CHAR_NUM:
                    Label numLabel = new Label();
                    for (int j = 0; j < 10; j++) {
                        choiceLabels.put('0' + j, numLabel);
                    }
                    total += 10;
                    choiceLabels.put(CHAR_NUM, numLabel);
                    break;
                case CHAR_LETTER:
                    Label letterLabel = new Label();
                    for (int j = 0; j < 26; j++) {
                        choiceLabels.put('a' + j, letterLabel);
                        choiceLabels.put('A' + j, letterLabel);
                    }
                    total += 52;
                    choiceLabels.put(CHAR_LETTER, letterLabel);
                    break;
                case CHAR_HEX:
                    Label hexLabel = new Label();
                    for (int j = 0; j < 10; j++) {
                        choiceLabels.put('0' + j, hexLabel);
                    }
                    for (int j = 0; j < 6; j++) {
                        choiceLabels.put('a' + j, hexLabel);
                        choiceLabels.put('A' + j, hexLabel);
                    }
                    total += 22;
                    choiceLabels.put(CHAR_HEX, hexLabel);
                    break;
                default:
                    Label symbolLabel = new Label();
                    choiceLabels.put(e.getKey(), symbolLabel);
                    total += 1;
                    break;
            }
        }
        Label[] transitionLabels = new Label[total];
        int[] transitionChars = new int[total];

        int pointer = 0;
        for (Map.Entry<Integer, Label> choiceEntry : choiceLabels.entrySet()) {
            if (choiceEntry.getKey() <= 256) {
                transitionChars[pointer] = choiceEntry.getKey();
                transitionLabels[pointer++] = choiceEntry.getValue();
            }
        }
        mvis.visitIntInsn(ILOAD, 7); // char
        mvis.visitLookupSwitchInsn(failReturnLabel, transitionChars, transitionLabels);
        for (Map.Entry<Integer, Integer> entry : transition.entrySet()) {
            Label label = choiceLabels.get(entry.getKey());
            int state = entry.getValue();

            mvis.visitLabel(label);
            visitFrame(mvis);

            transitToState(mvis, currentState, state);
        }
        mvis.visitLabel(endSwitchLabel);
    }

    public void visitFrame(MethodVisitor mvis) {
        mvis.visitFrame(F_NEW, 8,
                new Object[]{className, "java/lang/String", INTEGER, INTEGER, INTEGER,
                        "[Ljava/lang/CharSequence;", INTEGER, INTEGER},
                0, new Object[]{});
    }

    public static void pushConstant(MethodVisitor mvis, int constant) {
        switch (constant) {
            case 0:
                mvis.visitInsn(ICONST_0);
                break;
            case 1:
                mvis.visitInsn(ICONST_1);
                break;
            case 2:
                mvis.visitInsn(ICONST_2);
                break;
            case 3:
                mvis.visitInsn(ICONST_3);
                break;
            case 4:
                mvis.visitInsn(ICONST_4);
                break;
            case 5:
                mvis.visitInsn(ICONST_5);
                break;
            default:
                if (constant >= 32768) {
                    mvis.visitLdcInsn(constant);
                } else if (constant >= 128) {
                    mvis.visitIntInsn(SIPUSH, constant);
                } else {
                    mvis.visitIntInsn(BIPUSH, constant);
                }
                break;
        }
    }

//    protected static void printInt(MethodVisitor mvis) {
////        2: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
////        5: iload_1
////        6: invokevirtual #3                  // Method java/io/PrintStream.println:(I)V
//        mvis.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mvis.visitInsn(SWAP);
//        mvis.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
//    }
}

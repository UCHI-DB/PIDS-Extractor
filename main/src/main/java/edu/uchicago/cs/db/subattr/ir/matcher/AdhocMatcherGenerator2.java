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
 * Generate ad-hoc class for pattern matching, generating StringPiece instead of string
 */
public class AdhocMatcherGenerator2 extends AdhocMatcherGenerator {

    static final String STRINGPIECE = "edu/uchicago/cs/db/subattr/ir/matcher/StringPiece";

    @Override
    protected void substring(MethodVisitor mvis, int index) {
        mvis.visitIntInsn(ALOAD, 5);
        pushConstant(mvis, index);
        mvis.visitTypeInsn(NEW, STRINGPIECE);
        mvis.visitInsn(DUP);
        mvis.visitIntInsn(ALOAD, 1);
        mvis.visitIntInsn(ILOAD, 3);
        mvis.visitIntInsn(ILOAD, 4);
        mvis.visitMethodInsn(INVOKESPECIAL, STRINGPIECE, "<init>", "(Ljava/lang/String;II)V", false);
        // java/util/List.add:(Ljava/lang/Object;)Z
        mvis.visitInsn(AASTORE);
    }

}

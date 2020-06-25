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

package edu.uchicago.cs.db.subattr.ir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class PatternSerializer {

    public static String serialize(Pattern pattern) {
        try {
            ByteArrayOutputStream res = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(res);
            oos.writeObject(pattern);
            oos.close();
            return Base64.getEncoder().encodeToString(res.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Pattern deserialize(String data) {
        try {
            byte[] raw = Base64.getDecoder().decode(data);
            ByteArrayInputStream from = new ByteArrayInputStream(raw);
            ObjectInputStream ois = new ObjectInputStream(from);
            return (Pattern) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

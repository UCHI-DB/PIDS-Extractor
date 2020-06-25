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

import edu.uchicago.cs.db.subattr.ir.Pattern;
import edu.uchicago.cs.db.subattr.ir.matcher.regex.GenRegexVisitor;

import java.util.regex.Matcher;

public class RegexMatcher extends AbstractMatcher implements ExactMatcher {

    public RegexMatcher(Pattern pattern) {
        super(pattern);

        pattern.visit(new NamingVisitor());
        GenRegexVisitor visitor = new GenRegexVisitor();
        pattern.visit(visitor);
        regexPattern = java.util.regex.Pattern.compile(visitor.get());
    }

    private java.util.regex.Pattern regexPattern = null;

    @Override
    public String[] match(String input) {
        Matcher matcher = regexPattern.matcher(input);
        if (matcher.matches()) {
            String[] result = new String[matcher.groupCount()];
            for (int i = 0; i < matcher.groupCount(); i++) {
                result[i] = matcher.group(i + 1);
            }
            return result;
        }
        return null;
    }
}

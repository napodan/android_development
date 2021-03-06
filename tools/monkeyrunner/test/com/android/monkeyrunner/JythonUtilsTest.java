/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.monkeyrunner;

import com.google.common.base.Preconditions;

import com.android.monkeyrunner.doc.MonkeyRunnerExported;

import junit.framework.TestCase;

import org.python.core.ArgParser;
import org.python.core.PyException;
import org.python.core.PyObject;

import java.util.List;
import java.util.Map;

/**
 * Unit tests for the JythonUtils class.
 */
public class JythonUtilsTest extends TestCase {
    private static final String PACKAGE_NAME = JythonUtilsTest.class.getPackage().getName();
    private static final String CLASS_NAME = JythonUtilsTest.class.getSimpleName();

    private static boolean called = false;
    private static double floatValue = 0.0;
    private static List<Object> listValue = null;
    private static Map<String, Object> mapValue;

    @MonkeyRunnerExported(doc = "", args = {"value"})
    public static void floatTest(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);
        called = true;

        floatValue = JythonUtils.getFloat(ap, 0);
    }

    @MonkeyRunnerExported(doc = "", args = {"value"})
    public static void listTest(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);
        called = true;

        listValue = JythonUtils.getList(ap, 0);
    }

    @MonkeyRunnerExported(doc = "", args = {"value"})
    public static void mapTest(PyObject[] args, String[] kws) {
        ArgParser ap = JythonUtils.createArgParser(args, kws);
        Preconditions.checkNotNull(ap);
        called = true;

        mapValue = JythonUtils.getMap(ap, 0);
    }

    @Override
    protected void setUp() throws Exception {
        called = false;
        floatValue = 0.0;
    }

    private static void call(String method) {
        call(method, "");
    }
    private static void call(String method, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("from ").append(PACKAGE_NAME);
        sb.append(" import ").append(CLASS_NAME).append("\n");
        sb.append(CLASS_NAME).append(".").append(method);
        sb.append("(");
        for (String arg : args) {
            sb.append(arg).append(",");
        }
        sb.append(")");

        ScriptRunner.runString(sb.toString());
    }

    public void testSimpleCall() {
        call("floatTest", "0.0");
        assertTrue(called);
    }

    public void testMissingFloatArg() {
        try {
            call("floatTest");
        } catch(PyException e) {
            return;
        }
        fail("Should have thrown exception");
    }

    public void testBadFloatArgType() {
        try {
            call("floatTest", "\'foo\'");
        } catch(PyException e) {
            return;
        }
        fail("Should have thrown exception");
    }

    public void testFloatParse() {
        call("floatTest", "103.2");
        assertTrue(called);
        assertEquals(floatValue, 103.2);
    }

    public void testFloatParseInteger() {
        call("floatTest", "103");
        assertTrue(called);
        assertEquals(floatValue, 103.0);
    }

    public void testParseStringList() {
        call("listTest", "['a', 'b', 'c']");
        assertTrue(called);
        assertEquals(3, listValue.size());
        assertEquals("a", listValue.get(0));
        assertEquals("b", listValue.get(1));
        assertEquals("c", listValue.get(2));
    }

    public void testParseIntList() {
        call("listTest", "[1, 2, 3]");
        assertTrue(called);
        assertEquals(3, listValue.size());
        assertEquals(new Integer(1), listValue.get(0));
        assertEquals(new Integer(2), listValue.get(1));
        assertEquals(new Integer(3), listValue.get(2));
    }

    public void testParseMixedList() {
        call("listTest", "['a', 1, 3.14]");
        assertTrue(called);
        assertEquals(3, listValue.size());
        assertEquals("a", listValue.get(0));
        assertEquals(new Integer(1), listValue.get(1));
        assertEquals(new Double(3.14), listValue.get(2));
    }

    public void testParsingNotAList() {
        try {
            call("listTest", "1.0");
        } catch (PyException e) {
            return;
        }
        fail("Should have thrown an exception");
    }

    public void testParseMap() {
        call("mapTest", "{'a': 0, 'b': 'bee', 3: 'cee'}");
        assertTrue(called);
        assertEquals(3, mapValue.size());
        assertEquals(new Integer(0), mapValue.get("a"));
        assertEquals("bee", mapValue.get("b"));
        // note: coerced key type
        assertEquals("cee", mapValue.get("3"));
    }

    public void testParsingNotAMap() {
        try {
            call("mapTest", "1.0");
        } catch (PyException e) {
            return;
        }
        fail("Should have thrown an exception");
    }
}

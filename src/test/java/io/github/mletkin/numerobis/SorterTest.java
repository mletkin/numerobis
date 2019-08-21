/**
 * (c) 2019 by Ullrich Rieger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.mletkin.numerobis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Sorter;

/**
 * Tests rearranging of class members.
 */
class SorterTest {

    @Test
    void test() throws IOException {
        CompilationUnit cu = StaticJavaParser.parseResource("SortTest.java");
        new Sorter().sort(cu);

        assertThat(cu.toString().replace("\r\n", "")).isEqualTo(//
                "/**" + //
                        " * File comment." + //
                        " */" + //
                        "package foo.bar.baz;" + //
                        "import di.da.dum;" + //
                        "/**" + //
                        " * class" + //
                        " */" + //
                        "public class SortTest {" + //
                        "    static int bla = 10;" + //
                        "    private TestClass product = new TestClass();" + //
                        "    /* Foo bekommt */" + //
                        "    /* Wert 10 */" + //
                        "    int foo = 10;" + //
                        "    /**" + //
                        "     * constructor comment." + //
                        "     */" + //
                        "    SortTest() {" + //
                        "    // nothing to do" + //
                        "    }" + //
                        "    public TestClassBuilder withX(int x) {" + //
                        "        // sue me" + //
                        "        product.x = x;" + //
                        "        return this;" + //
                        "    }" + //
                        "    public TestClass build() {" + //
                        "        return product;" + //
                        "    }" + //
                        "    /**" + //
                        "     * Method comment" + //
                        "     *" + //
                        "     * @param x" + //
                        "     * @return" + //
                        "     */" + //
                        "    public Object foo(int x) {" + //
                        "        return null;" + //
                        "    }" + //
                        "    public static class Foo {" + //
                        "    }" + //
                        "}");

    }

}

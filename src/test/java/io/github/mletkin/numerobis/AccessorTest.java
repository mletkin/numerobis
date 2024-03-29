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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Accessor generation in the product class.
 */
class AccessorTest {

    private TestFacade testFacade = new TestFacade(false);

    @Test
    void accessMethodGenerated() {
        Assertions.assertThat(testFacade.generateAccessors("Access")).isEqualTo(//
                "@GenerateAccessors" //
                        + "public class Access {" //
                        + "    int foo;" //
                        + "    public int foo() {" //
                        + "        return foo;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void accessMethodForListGeneratesStream() {
        Assertions.assertThat(testFacade.generateAccessors("WithList")).isEqualTo(//
                "import java.util.List;" //
                        + "import java.util.stream.Stream;" //
                        + "public class WithList {" //
                        + "    List<String> x = new ArrayList<>();" //
                        + "    public Stream<String> x() {" //
                        + "        return x.stream();" //
                        + "    }" //
                        + "}");
    }

    @Test
    void accessMethodWithPrefixGenerated() {
        Assertions.assertThat(testFacade.generateAccessors("AccessWithPrefix")).isEqualTo(//
                "@GenerateAccessors(prefix = \"get\")" //
                        + "public class AccessWithPrefix {" //
                        + "    int foo;" //
                        + "    public int getFoo() {" //
                        + "        return foo;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void retainsArrayAccessor() {
        Assertions.assertThat(testFacade.generateAccessors("ArrayFieldWithAccessor")).isEqualTo(//
                "class ArrayFieldWithAccessor {" //
                        + "    String[] foo;" //
                        + "    String[] foo() {" //
                        + "    }" //
                        + "}");
    }

    @Test
    void retainsIntAccessor() {
        Assertions.assertThat(testFacade.generateAccessors("IntFieldWithAccessor")).isEqualTo(//
                "class IntFieldWithAccessor {" //
                        + "    int foo;" //
                        + "    int foo() {" //
                        + "    }" //
                        + "}");
    }

    /**
     * Maybe there's a bug in JavaParser.<br>
     * In this test int[] and int[] are not considered equal
     */
    @Disabled
    @Test
    void retainsIntAndArrayAccessor() {
        Assertions.assertThat(testFacade.generateAccessors("IntAndArrayFieldWithAccessor")).isEqualTo(//
                "class IntAndArrayFieldWithAccessor {" //
                        + "    int foo, bar[];" //
                        + "    int foo() {" //
                        + "    }" //
                        + "    int[] bar() {" //
                        + "    }" //
                        + "}");
    }

}

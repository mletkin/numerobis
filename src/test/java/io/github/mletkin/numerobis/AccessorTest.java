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

import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.product;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Accessor generation in the product class.
 */
class AccessorTest {

    private Facade facade = new Facade();

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String content) {
        var order = mkOrder(product);
        var result = facade.withAccessors(order);
        assertThat(product(result, product)).as(desc).isEqualTo(content);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("accessMethodGenerated", "Access", //
                        "@GenerateAccessors" //
                                + "public class Access {" //
                                + "    int foo;" //
                                + "    public int foo() {" //
                                + "        return foo;" //
                                + "    }" //
                                + "}"),

                Arguments.of("accessMethodWithPrefixGenerated", "AccessWithPrefix", //
                        "@GenerateAccessors(prefix = \"get\")" //
                                + "public class AccessWithPrefix {" //
                                + "    int foo;" //
                                + "    public int getFoo() {" //
                                + "        return foo;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsArrayAccessor", "ArrayFieldWithAccessor", //
                        "class ArrayFieldWithAccessor {" //
                                + "    String[] foo;" //
                                + "    String[] foo() {" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsIntAccessor", "IntFieldWithAccessor", //
                        "class IntFieldWithAccessor {" //
                                + "    int foo;" //
                                + "    int foo() {" //
                                + "    }" //
                                + "}")

        );
    }

    /**
     * Maybe there's a bug in JavaParser.<br>
     * In this test int[] bar and int bar[] are not considered equal
     */
    @Disabled
    @Test
    void retainsIntAndArrayAccessor() {
        var product = "IntAndArrayFieldWithAccessor";
        var order = mkOrder(product);
        var result = facade.withAccessors(order);

        assertThat(product(result, product)).isEqualTo( //
                "class IntAndArrayFieldWithAccessor {" //
                        + "    int foo, bar[];" //
                        + "    int foo() {" //
                        + "    }" //
                        + "    int[] bar() {" //
                        + "    }" //
                        + "}");
    }

    @Test
    void accessMethodForListGeneratesStream() {
        var product = "WithList";
        var order = mkOrder(product);
        var result = facade.withAccessors(order);

        assertThat(Fixture.asString(result)).isEqualTo(//
                "import java.util.List;" //
                        + "import java.util.stream.Stream;" //
                        + "public class WithList {" //
                        + "    List<String> x = new ArrayList<>();" //
                        + "    public Stream<String> x() {" //
                        + "        return x.stream();" //
                        + "    }" //
                        + "}");
    }

}

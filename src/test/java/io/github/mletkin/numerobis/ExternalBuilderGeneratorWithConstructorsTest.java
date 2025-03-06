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
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.common.ClassUtil;

/**
 * Builder generation with constructors without existing builder class.
 */
class ExternalBuilderGeneratorWithConstructorsTest {

    private Facade facade = new Facade(false);

    public static String builder(CompilationUnit unit, String product) {
        return Fixture.asString(ClassUtil.findClass(unit, product + "Builder").orElse(null));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String builder) {
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();
        assertThat(builder(result, product)).as(desc).isEqualTo(builder);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("productClassWithoutConstructor", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    private Empty product;" //
                                + "    public EmptyBuilder() {" //
                                + "        product = new Empty();" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithCustomConstructor", "EmptyWithCustomConstructor", //
                        "public class EmptyWithCustomConstructorBuilder {" //
                                + "    private EmptyWithCustomConstructor product;" //
                                + "    public EmptyWithCustomConstructorBuilder(int n) {" //
                                + "        product = new EmptyWithCustomConstructor(n);" //
                                + "    }" //
                                + "    public EmptyWithCustomConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithDefaultConstructor", "EmptyWithDefaultConstructor", //
                        "public class EmptyWithDefaultConstructorBuilder {" //
                                + "    private EmptyWithDefaultConstructor product;" //
                                + "    public EmptyWithDefaultConstructorBuilder() {" //
                                + "        product = new EmptyWithDefaultConstructor();" //
                                + "    }" //
                                + "    public EmptyWithDefaultConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("constructorWithAnnotationIsIgnored", "EmptyWithIgnoredConstructor", //
                        "public class EmptyWithIgnoredConstructorBuilder {" //
                                + "    private EmptyWithIgnoredConstructor product;" //
                                + "    public EmptyWithIgnoredConstructorBuilder(int n) {" //
                                + "        product = new EmptyWithIgnoredConstructor(n);" //
                                + "    }" //
                                + "    public EmptyWithIgnoredConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("privateConstructorIsIgnored", "EmptyWithPrivateAndPublicConstructor", //
                        "public class EmptyWithPrivateAndPublicConstructorBuilder {" //
                                + "    private EmptyWithPrivateAndPublicConstructor product;" //
                                + "    public EmptyWithPrivateAndPublicConstructorBuilder(int n) {" //
                                + "        product = new EmptyWithPrivateAndPublicConstructor(n);" //
                                + "    }" //
                                + "    public EmptyWithPrivateAndPublicConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

}

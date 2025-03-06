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

import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Inner Builder generation with Factory Methods.
 */
class InternalBuilderGeneratorWithFactoryMethodsTest {

    private Facade facade = new Facade();

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String builder) {
        var order = mkOrder(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();
        assertThat(builder(result, product)).as(desc).isEqualTo(builder);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("productClassWithoutConstructor", "Empty", //
                        "public static class Builder {" //
                                + "    private Empty product;" //
                                + "    private Builder(Empty product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static Builder of() {" //
                                + "        return new Builder(new Empty());" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithCustomConstructor", "EmptyWithCustomConstructor", //
                        "public static class Builder {" //
                                + "    private EmptyWithCustomConstructor product;" //
                                + "    private Builder(EmptyWithCustomConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static Builder of(int n) {" //
                                + "        return new Builder(new EmptyWithCustomConstructor(n));" //
                                + "    }" //
                                + "    public EmptyWithCustomConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithDefaultConstructor", "EmptyWithDefaultConstructor", //
                        "public static class Builder {" //
                                + "    private EmptyWithDefaultConstructor product;" //
                                + "    private Builder(EmptyWithDefaultConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static Builder of() {" //
                                + "        return new Builder(new EmptyWithDefaultConstructor());" //
                                + "    }" //
                                + "    public EmptyWithDefaultConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("constructorWithAnnotationIsIgnored", "EmptyWithIgnoredConstructor", //
                        "public static class Builder {" //
                                + "    private EmptyWithIgnoredConstructor product;" //
                                + "    private Builder(EmptyWithIgnoredConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static Builder of(int n) {" //
                                + "        return new Builder(new EmptyWithIgnoredConstructor(n));" //
                                + "    }" //
                                + "    public EmptyWithIgnoredConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("privateConstructorIsProcessed", "EmptyWithPrivateAndPublicConstructor", //
                        "public static class Builder {" //
                                + "    private EmptyWithPrivateAndPublicConstructor product;" //
                                + "    private Builder(EmptyWithPrivateAndPublicConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static Builder of(int n) {" //
                                + "        return new Builder(new EmptyWithPrivateAndPublicConstructor(n));" //
                                + "    }" //
                                + "    public static Builder of(String s) {" //
                                + "        return new Builder(new EmptyWithPrivateAndPublicConstructor(s));" //
                                + "    }" //
                                + "    public EmptyWithPrivateAndPublicConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

}

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
import static io.github.mletkin.numerobis.Fixture.parseString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.common.ClassUtil;

/**
 * Builder generation with Factory Methods.
 */
class ExternalBuilderGeneratorWithFactoryMethodsTest {

    private Facade facade = new Facade();

    public static String builder(CompilationUnit unit, String product) {
        return Fixture.asString(ClassUtil.findClass(unit, product + "Builder").orElse(null));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String builder) {
        var order = mkOrder(product);
        var result = facade.separateWithFactoryMethods(order).execute();
        assertThat(builder(result, product)).as(desc).isEqualTo(builder);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("productClassWithoutConstructor", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    private Empty product;" //
                                + "    private EmptyBuilder(Empty product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return new EmptyBuilder(new Empty());" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithCustomConstructor", "EmptyWithCustomConstructor", //
                        "public class EmptyWithCustomConstructorBuilder {" //
                                + "    private EmptyWithCustomConstructor product;" //
                                + "    private EmptyWithCustomConstructorBuilder(EmptyWithCustomConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyWithCustomConstructorBuilder of(int n) {" //
                                + "        return new EmptyWithCustomConstructorBuilder(new EmptyWithCustomConstructor(n));" //
                                + "    }" //
                                + "    public EmptyWithCustomConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("productClassWithDefaultConstructor", "EmptyWithDefaultConstructor", //
                        "public class EmptyWithDefaultConstructorBuilder {" //
                                + "    private EmptyWithDefaultConstructor product;" //
                                + "    private EmptyWithDefaultConstructorBuilder(EmptyWithDefaultConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyWithDefaultConstructorBuilder of() {" //
                                + "        return new EmptyWithDefaultConstructorBuilder(new EmptyWithDefaultConstructor());" //
                                + "    }" //
                                + "    public EmptyWithDefaultConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("constructorWithAnnotationIsIgnored", "EmptyWithIgnoredConstructor", //
                        "public class EmptyWithIgnoredConstructorBuilder {" //
                                + "    private EmptyWithIgnoredConstructor product;" //
                                + "    private EmptyWithIgnoredConstructorBuilder(EmptyWithIgnoredConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyWithIgnoredConstructorBuilder of(int n) {" //
                                + "        return new EmptyWithIgnoredConstructorBuilder(new EmptyWithIgnoredConstructor(n));" //
                                + "    }" //
                                + "    public EmptyWithIgnoredConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("privateConstructorIsIgnored", "EmptyWithPrivateAndPublicConstructor", //
                        "public class EmptyWithPrivateAndPublicConstructorBuilder {" //
                                + "    private EmptyWithPrivateAndPublicConstructor product;" //
                                + "    private EmptyWithPrivateAndPublicConstructorBuilder(EmptyWithPrivateAndPublicConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyWithPrivateAndPublicConstructorBuilder of(int n) {" //
                                + "        return new EmptyWithPrivateAndPublicConstructorBuilder(new EmptyWithPrivateAndPublicConstructor(n));" //
                                + "    }" //
                                + "    public EmptyWithPrivateAndPublicConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

    @ParameterizedTest
    @MethodSource("retainerCases")
    void test(String desc, String product, String builder, String expected) {
        var order = mkOrder(product).useBuildUnit(parseString(builder));
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).as(desc).isEqualTo(expected);
    }

    static Stream<Arguments> retainerCases() {
        return Stream.of( //
                Arguments.of("productClassWithoutConstructor", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    protected EmptyBuilder() {" //
                                + "        product = null;" //
                                + "    }" //
                                + "}", //
                        "public class EmptyBuilder {" //
                                + "    protected EmptyBuilder() {" //
                                + "        product = null;" //
                                + "    }" //
                                + "    private Empty product;" //
                                + "    private EmptyBuilder(Empty product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return new EmptyBuilder(new Empty());" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsProductConstructor", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    private EmptyBuilder(Empty p) {" //
                                + "        this.product = p;" //
                                + "    }" //
                                + "}", //
                        "public class EmptyBuilder {" //
                                + "    private EmptyBuilder(Empty p) {" //
                                + "        this.product = p;" //
                                + "    }" //
                                + "    private Empty product;" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return new EmptyBuilder(new Empty());" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsDefaultFactoryMethod", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class EmptyBuilder {" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private Empty product;" //
                                + "    private EmptyBuilder(Empty product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainCustomFactoryMethod", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    public static EmptyBuilder of(String foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class EmptyBuilder {" //
                                + "    public static EmptyBuilder of(String foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private Empty product;" //
                                + "    private EmptyBuilder(Empty product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public static EmptyBuilder of() {" //
                                + "        return new EmptyBuilder(new Empty());" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainFactoryMethodForNonDefaultConstructor", "EmptyWithCustomConstructor", //
                        "public class EmptyWithCustomConstructorBuilder {" //
                                + "    public static EmptyWithCustomConstructorBuilder of(int m) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class EmptyWithCustomConstructorBuilder {" //
                                + "    public static EmptyWithCustomConstructorBuilder of(int m) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private EmptyWithCustomConstructor product;" //
                                + "    private EmptyWithCustomConstructorBuilder(EmptyWithCustomConstructor product) {" //
                                + "        this.product = product;" //
                                + "    }" //
                                + "    public EmptyWithCustomConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

}

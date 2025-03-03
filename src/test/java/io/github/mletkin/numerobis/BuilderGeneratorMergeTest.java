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

import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.parse;
import static io.github.mletkin.numerobis.Fixture.parseString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Builder generation with existing builder class.
 */
class BuilderGeneratorMergeTest {

    private Facade facade = new Facade(false);

    @ParameterizedTest
    @MethodSource("testCases")
    void mergeTest(String desc, String product, String builder, String expected) {
        var actual = facade //
                .withConstructors(parse(product), product, parseString(builder)) //
                .execute();

        assertThat(asString(actual)).as(desc).isEqualTo(expected);

    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("usesTargetClass", "TestClass", //
                        "public class TestClassBuilder {}", //
                        "public class TestClassBuilder {" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsProductField", "TestClass", //
                        "public class TestClassBuilder {TestClass product = null;}", //
                        "public class TestClassBuilder {" //
                                + "    TestClass product = null;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsBuildMethod", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClass build() {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClass build() {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsDefaultConstructor", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder() {" //
                                + "        product = null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder() {" //
                                + "        product = null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsMutator", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("retainsMutator", "WithList", //
                        "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(String y) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "import java.util.List;" //
                                + "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(String y) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private WithList product;" //
                                + "    public WithListBuilder() {" //
                                + "        product = new WithList();" //
                                + "    }" //
                                + "    public WithListBuilder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public WithList build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("usesWithMethodWithDifferentParameterName", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int ypsilon) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int ypsilon) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                // FIXME: The resulting class will not compile

                Arguments.of("ignoresWithMethodWithDifferentReturnType", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public Object withX(int x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public Object withX(int x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("ignoresWithMethodWithDifferentParameterType", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(String x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(String x) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("ignoresWithMethodWithAdditionalParameter", "TestClass", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int x, String z) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public class TestClassBuilder {" //
                                + "    public TestClassBuilder withX(int x, String z) {" //
                                + "        return null;" //
                                + "    }" //
                                + "    private TestClass product;" //
                                + "    public TestClassBuilder() {" //
                                + "        product = new TestClass();" //
                                + "    }" //
                                + "    public TestClassBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClass build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("existingImportIsNotDuplicated", "TestClassWithImport", //
                        "import foo.bar.baz;" + //
                                "public class TestClassWithImportBuilder {" + //
                                "}", //
                        "import foo.bar.baz;" + //
                                "public class TestClassWithImportBuilder {" //
                                + "    private TestClassWithImport product;" //
                                + "    public TestClassWithImportBuilder() {" //
                                + "        product = new TestClassWithImport();" //
                                + "    }" //
                                + "    public TestClassWithImport build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("builderRetainsPackage", "TestClassWithPackage", //
                        "package bim.bam.bum;", //
                        "package bim.bam.bum;" //
                                + "public class TestClassWithPackageBuilder {" //
                                + "    private TestClassWithPackage product;" //
                                + "    public TestClassWithPackageBuilder() {" //
                                + "        product = new TestClassWithPackage();" //
                                + "    }" //
                                + "    public TestClassWithPackageBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClassWithPackage build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("ignoresExistingConstructor", "TestClassWithConstructor", //
                        "public class TestClassWithConstructorBuilder {" //
                                + "    public TestClassWithConstructorBuilder(int n) {" //
                                + "    }" //
                                + "}", //
                        "public class TestClassWithConstructorBuilder {" //
                                + "    public TestClassWithConstructorBuilder(int n) {" //
                                + "    }" //
                                + "    private TestClassWithConstructor product;" //
                                + "    public TestClassWithConstructorBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public TestClassWithConstructor build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("generatesDefaultConstructor", "Empty", //
                        "public class EmptyBuilder { }", //
                        "public class EmptyBuilder {" //
                                + "    private Empty product;" //
                                + "    public EmptyBuilder() {" //
                                + "        product = new Empty();" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("generateDefaultConstructorIgnoreExistingNonDefaultConstructor", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    EmptyBuilder(int n) {" //
                                + "    }" //
                                + " }", //
                        "public class EmptyBuilder {" //
                                + "    EmptyBuilder(int n) {" //
                                + "    }" //
                                + "    private Empty product;" //
                                + "    public EmptyBuilder() {" //
                                + "        product = new Empty();" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("ignoresConstructorOfInnerClass", "Empty", //
                        "public class EmptyBuilder {" //
                                + "    public static class Foo {" //
                                + "        Foo() {" //
                                + "        }" //
                                + "    }" //
                                + "}", //
                        "public class EmptyBuilder {" //
                                + "    public static class Foo {" //
                                + "        Foo() {" //
                                + "        }" //
                                + "    }" // <
                                + "    private Empty product;" //
                                + "    public EmptyBuilder() {" //
                                + "        product = new Empty();" //
                                + "    }" //
                                + "    public Empty build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

}

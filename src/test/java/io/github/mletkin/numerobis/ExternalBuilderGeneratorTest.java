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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;

/**
 * Builder generation without existing builder class.
 * <p>
 * FIXME: two fields with one Annotation sucks
 */
class ExternalBuilderGeneratorTest {

    private Facade facade = new Facade(false);

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String builder) {
        var result = facade.withConstructors(parse(product), product, new CompilationUnit()).execute();
        assertThat(asString(result)).as(desc).isEqualTo(builder);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("builderIsInSamePackage", "TestClassWithPackage", //
                        "package foo.bar.baz;" //
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

                Arguments.of("copiesImport", "TestClassWithImport", //
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

                Arguments.of("ignoresBuilderImport", "TestClassWithBuilderImport", //
                        "public class TestClassWithBuilderImportBuilder {" //
                                + "    private TestClassWithBuilderImport product;" //
                                + "    public TestClassWithBuilderImportBuilder() {" //
                                + "        product = new TestClassWithBuilderImport();" //
                                + "    }" //
                                + "    public TestClassWithBuilderImport build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

    @Test
    void notContainedClassThrowsException() throws IOException {
        var generator = facade //
                .withConstructors(parse("TestClass"), "Foo", new CompilationUnit());

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute) //
                .withMessage("Product class Foo not found in compilation unit.");
    }

    @Test
    void nullClassThrowsException() {
        var generator = facade //
                .withConstructors(parse("TestClass"), null, new CompilationUnit());

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute) //
                .withMessage("Product class not found in compilation unit.");
    }

    @Test
    void classWithoutUsableConstructorThrowsException() {
        var product = "TestClassWithoutConstructor";
        var generator = facade //
                .withConstructors(parse(product), product, new CompilationUnit());

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute) //
                .withMessage("No suitable constructor found.");
    }

}

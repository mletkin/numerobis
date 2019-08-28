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

import static io.github.mletkin.numerobis.Util.asString;
import static io.github.mletkin.numerobis.Util.uncheckExceptions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;

/**
 * Builder generation without existing builder class.
 */
class ExternalBuilderGeneratorTest {

    // FIXME two fields with one Annotation sucks

    @Test
    void builderIsInSamePackage() {
        assertThat(generateFromResource("TestClassWithPackage")).isEqualTo(//
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
                        + "}");
    }

    @Test
    void copiesImport() {
        assertThat(generateFromResource("TestClassWithImport")).isEqualTo(//
                "import foo.bar.baz;" + //
                        "public class TestClassWithImportBuilder {" //
                        + "    private TestClassWithImport product;" //
                        + "    public TestClassWithImportBuilder() {" //
                        + "        product = new TestClassWithImport();" //
                        + "    }" //
                        + "    public TestClassWithImport build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void ignoresBuilderImport() {
        assertThat(generateFromResource("TestClassWithBuilderImport")).isEqualTo(//
                "public class TestClassWithBuilderImportBuilder {" //
                        + "    private TestClassWithBuilderImport product;" //
                        + "    public TestClassWithBuilderImportBuilder() {" //
                        + "        product = new TestClassWithBuilderImport();" //
                        + "    }" //
                        + "    public TestClassWithBuilderImport build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void notContainedClassProducesNothing() {
        assertThatExceptionOfType(GeneratorException.class).isThrownBy( //
                () -> Facade.withConstructors(StaticJavaParser.parseResource("TestClass.java"), "Foo",
                        new CompilationUnit()))
                .withMessage("Product class not found in compilation unit.");
    }

    @Test
    void nullClassProducesNothing() {
        assertThatExceptionOfType(GeneratorException.class).isThrownBy( //
                () -> Facade.withConstructors(StaticJavaParser.parseResource("TestClass.java"), "",
                        new CompilationUnit()))
                .withMessage("Product class not found in compilation unit.");
    }

    @Test
    void classWithoutUsableConstructorThrowsException() {
        assertThatExceptionOfType(GeneratorException.class).isThrownBy( //
                () -> generateFromResource("TestClassWithoutConstructor"))
                .withMessage("No suitable constructor found.");
    }

    private String generateFromResource(String className) {
        return uncheckExceptions(
                () -> asString(Facade.withConstructors(StaticJavaParser.parseResource(className + ".java"), className,
                        new CompilationUnit()).builderUnit));
    }

}

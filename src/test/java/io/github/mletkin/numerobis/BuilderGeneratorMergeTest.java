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

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;

/**
 * Builder generation with existing builder class.
 */
class BuilderGeneratorMergeTest {

    @Test
    void usesTargetClass() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainsProductField() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {TestClass product = null;}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void productFieldWithWrongTypeThrowsException() {
        assertThatExceptionOfType(GeneratorException.class).isThrownBy(//
                () -> generateFromResource("Empty", //
                        "public class EmptyBuilder {" //
                                + "    private String product;" //
                                + "}")//
        ).withMessage("The product field has the wrong type String.");
    }

    @Test
    void retainsBuildMethod() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClass build() {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainsDefaultConstructor() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClassBuilder() {" //
                        + "        product = null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void usesWithMethod() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClassBuilder withX(int x) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void usesWithMethodWithDifferentParameterName() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClassBuilder withX(int ypsilon) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    // FIXME: The resulting class will not compile

    @Test
    void ignoresWithMethodWithDifferentReturnType() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public Object withX(int x) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void ignoresWithMethodWithDifferentParameterType() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClassBuilder withX(String x) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void ignoresWithMethodWithAdditionalParameter() {
        assertThat(generateFromResource("TestClass", //
                "public class TestClassBuilder {" //
                        + "    public TestClassBuilder withX(int x, String z) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void existingImportIsNotDuplicated() {
        assertThat(generateFromResource("TestClassWithImport", //
                "import foo.bar.baz;" + //
                        "public class TestClassWithImportBuilder {" + //
                        "}") //
        ).isEqualTo(//
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
    void builderRetainsPackage() {
        assertThat(generateFromResource("TestClassWithPackage", //
                "package bim.bam.bum;")//
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void ignoresExistingConstructor() {
        assertThat(generateFromResource("TestClassWithConstructor", //
                "public class TestClassWithConstructorBuilder {" //
                        + "    public TestClassWithConstructorBuilder(int n) {" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void generateDefaultConstructor() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder { }") //
        ).isEqualTo(//
                "public class EmptyBuilder {" //
                        + "    private Empty product;" //
                        + "    public EmptyBuilder() {" //
                        + "        product = new Empty();" //
                        + "    }" //
                        + "    public Empty build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void generateDefaultConstructorIgnoreExistingNonDefaultConstructor() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    EmptyBuilder(int n) {" //
                        + "    }" //
                        + " }") //
        ).isEqualTo(//
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
                        + "}");
    }

    private String generateFromResource(String className, String builderClass) {
        return uncheckExceptions(() -> {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return asString(Facade.withConstructors(source, className, target).builderUnit);
        });
    }

    /**
     * No real test, just for fooling around with features.
     */
    @Disabled
    @Test
    void test2() throws IOException {
        System.out.println(Facade.withConstructors(StaticJavaParser.parseResource("TestClassWithConstructor.java"),
                "TestClassWithConstructor", new CompilationUnit()).toString());
    }

    @Test
    void ignoresConstructorOfInnerClass() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    public static class Foo {" //
                        + "        Foo() {" //
                        + "        }" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
                "public class EmptyBuilder {" //
                        + "    public static class Foo {" //
                        + "        Foo() {" //
                        + "        }" //
                        + "    }" //
                        + "    private Empty product;" //
                        + "    public EmptyBuilder() {" //
                        + "        product = new Empty();" //
                        + "    }" //
                        + "    public Empty build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}

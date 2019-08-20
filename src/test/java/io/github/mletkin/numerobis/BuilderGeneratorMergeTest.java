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

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.BuilderGenerator;

/**
 * Builder generation with existing builder class.
 */
class BuilderGeneratorMergeTest {

    @Test
    void usesTargetClass() {
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions
                .assertThat(
                        generateFromResource("TestClass", "public class TestClassBuilder {TestClass product = null;}"))
                .isEqualTo(//
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
    void retainsBuildMethod() {
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClass", //
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
        Assertions.assertThat(generateFromResource("TestClassWithImport", //
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
        Assertions.assertThat(generateFromResource("TestClassWithPackage", //
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
        Assertions.assertThat(generateFromResource("TestClassWithConstructor", //
                "public class TestClassWithConstructorBuilder {" + //
                        "    public TestClassWithConstructorBuilder(int n) {" + //
                        "    }" + //
                        "}") //
        ).isEqualTo(//
                "public class TestClassWithConstructorBuilder {" + //
                        "    public TestClassWithConstructorBuilder(int n) {" + //
                        "    }" + //
                        "    private TestClassWithConstructor product;" + //
                        "    public TestClassWithConstructorBuilder withX(int x) {" + //
                        "        product.x = x;" + //
                        "        return this;" + //
                        "    }" + //
                        "    public TestClassWithConstructor build() {" + //
                        "        return product;" + //
                        "    }" + //
                        "}");
    }

    private String generateFromResource(String className, String builderClass) {
        try {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return BuilderGenerator.generate(source, className, target).toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * No real test, just for fooling around with features.
     */
    @Disabled
    @Test
    void test2() throws IOException {
        System.out.println(BuilderGenerator
                .generate(StaticJavaParser.parseResource("TestClassWithConstructor.java"), "TestClassWithConstructor")
                .toString());
    }

}

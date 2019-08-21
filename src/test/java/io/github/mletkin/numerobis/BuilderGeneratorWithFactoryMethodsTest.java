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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Builder generation with Factory Methods.
 */
class BuilderGeneratorWithFactoryMethodsTest {

    @Test
    void productClassWithoutConstructor() {
        assertThat(generateFromResource("Empty")).isEqualTo(//
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
                        + "}");
    }

    @Test
    void productClassWithCustomConstructor() {
        assertThat(generateFromResource("EmptyWithCustomConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void productClassWithDefaultConstructor() {
        assertThat(generateFromResource("EmptyWithDefaultConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void ignoreConstructorWithAnnotation() {
        assertThat(generateFromResource("EmptyWithIgnoredConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void retainsDefaultConstructor() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    protected EmptyBuilder() {" //
                        + "        product = null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainsProductConstructor() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    private EmptyBuilder(Empty p) {" //
                        + "        this.product = p;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainsDefaultFactoryMethod() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    public static EmptyBuilder of() {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainCustomFactoryMethod() {
        assertThat(generateFromResource("Empty", //
                "public class EmptyBuilder {" //
                        + "    public static EmptyBuilder of(String foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    @Test
    void retainFactoryMethodForNonDefaultConstructor() {
        assertThat(generateFromResource("EmptyWithCustomConstructor", //
                "public class EmptyWithCustomConstructorBuilder {" //
                        + "    public static EmptyWithCustomConstructorBuilder of(int m) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
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
                        + "}");
    }

    private String generateFromResource(String className) {
        try {
            return Facade.withFactoryMethods(StaticJavaParser.parseResource(className + ".java"), className,
                    new CompilationUnit()).toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateFromResource(String className, String builderClass) {
        try {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return Facade.withFactoryMethods(source, className, target).toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

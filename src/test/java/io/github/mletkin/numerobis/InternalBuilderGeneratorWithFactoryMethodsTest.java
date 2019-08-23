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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import io.github.mletkin.numerobis.generator.ClassUtil;
import io.github.mletkin.numerobis.generator.Facade;

/**
 * Inner Builder generation with Factory Methods.
 */
class InternalBuilderGeneratorWithFactoryMethodsTest {

    @Test
    void productClassWithoutConstructor() {
        assertThat(generateFromResource("Empty")).isEqualTo(//
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
                        + "}");
    }

    @Test
    void productClassWithCustomConstructor() {
        assertThat(generateFromResource("EmptyWithCustomConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void productClassWithDefaultConstructor() {
        assertThat(generateFromResource("EmptyWithDefaultConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void constructorWithAnnotationIsIgnored() {
        assertThat(generateFromResource("EmptyWithIgnoredConstructor")).isEqualTo( //
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
                        + "}");
    }

    @Test
    void privateConstructorIsProcessed() {
        assertThat(generateFromResource("EmptyWithPrivateAndPublicConstructor")).isEqualTo( //
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
                        + "}");
    }

    /*
     * @Test void retainsDefaultConstructor() {
     * assertThat(generateFromResource("Empty", //
     * "public static class EmptyBuilder {" // + "    protected EmptyBuilder() {" //
     * + "        product = null;" // + "    }" // + "}") // ).isEqualTo(//
     * "public static class EmptyBuilder {" // + "    protected EmptyBuilder() {" //
     * + "        product = null;" // + "    }" // + "    private Empty product;" //
     * + "    private EmptyBuilder(Empty product) {" // +
     * "        this.product = product;" // + "    }" // +
     * "    public static EmptyBuilder of() {" // +
     * "        return new EmptyBuilder(new Empty());" // + "    }" // +
     * "    public Empty build() {" // + "        return product;" // + "    }" // +
     * "}"); }
     *
     * @Test void retainsProductConstructor() {
     * assertThat(generateFromResource("Empty", //
     * "public static class EmptyBuilder {" // +
     * "    private EmptyBuilder(Empty p) {" // + "        this.product = p;" // +
     * "    }" // + "}") // ).isEqualTo(// "public static class EmptyBuilder {" // +
     * "    private EmptyBuilder(Empty p) {" // + "        this.product = p;" // +
     * "    }" // + "    private Empty product;" // +
     * "    public static EmptyBuilder of() {" // +
     * "        return new EmptyBuilder(new Empty());" // + "    }" // +
     * "    public Empty build() {" // + "        return product;" // + "    }" // +
     * "}"); }
     *
     * @Test void retainsDefaultFactoryMethod() {
     * assertThat(generateFromResource("Empty", //
     * "public static class EmptyBuilder {" // +
     * "    public static EmptyBuilder of() {" // + "        return null;" // +
     * "    }" // + "}") // ).isEqualTo(// "public static class EmptyBuilder {" // +
     * "    public static EmptyBuilder of() {" // + "        return null;" // +
     * "    }" // + "    private Empty product;" // +
     * "    private EmptyBuilder(Empty product) {" // +
     * "        this.product = product;" // + "    }" // +
     * "    public Empty build() {" // + "        return product;" // + "    }" // +
     * "}"); }
     *
     * @Test void retainCustomFactoryMethod() {
     * assertThat(generateFromResource("Empty", //
     * "public static class EmptyBuilder {" // +
     * "    public static EmptyBuilder of(String foo) {" // + "        return null;"
     * // + "    }" // + "}") // ).isEqualTo(// "public static class EmptyBuilder {"
     * // + "    public static EmptyBuilder of(String foo) {" // +
     * "        return null;" // + "    }" // + "    private Empty product;" // +
     * "    private EmptyBuilder(Empty product) {" // +
     * "        this.product = product;" // + "    }" // +
     * "    public static EmptyBuilder of() {" // +
     * "        return new EmptyBuilder(new Empty());" // + "    }" // +
     * "    public Empty build() {" // + "        return product;" // + "    }" // +
     * "}"); }
     *
     * @Test void retainFactoryMethodForNonDefaultConstructor() {
     * assertThat(generateFromResource("EmptyWithCustomConstructor", //
     * "public static class EmptyWithCustomConstructorBuilder {" // +
     * "    public static EmptyWithCustomConstructorBuilder of(int m) {" // +
     * "        return null;" // + "    }" // + "}") // ).isEqualTo(//
     * "public static class EmptyWithCustomConstructorBuilder {" // +
     * "    public static EmptyWithCustomConstructorBuilder of(int m) {" // +
     * "        return null;" // + "    }" // +
     * "    private EmptyWithCustomConstructor product;" // +
     * "    private EmptyWithCustomConstructorBuilder(EmptyWithCustomConstructor product) {"
     * // + "        this.product = product;" // + "    }" // +
     * "    public EmptyWithCustomConstructor build() {" // +
     * "        return product;" // + "    }" // + "}"); }
     */

    private String generateFromResource(String className) {
        try {
            return extractBuilder(
                    Facade.withFactoryMethods(StaticJavaParser.parseResource(className + ".java"), className).productUnit,
                    className).toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClassOrInterfaceDeclaration extractBuilder(CompilationUnit cu, String className) {
        return ClassUtil.findClass(cu, className) //
                .map(c -> ClassUtil.allMember(c, ClassOrInterfaceDeclaration.class)) //
                .orElseGet(Stream::empty) //
                .findFirst().get();
    }

}

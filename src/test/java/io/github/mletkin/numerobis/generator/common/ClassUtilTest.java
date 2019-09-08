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
package io.github.mletkin.numerobis.generator.common;

import static io.github.mletkin.numerobis.generator.common.ClassUtil.hasSingleParameter;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;

class ClassUtilTest {

    @Test
    void singleParameterMethodForInt() {
        String clazz = "class Foo {" //
                + "void x(int x) {}" //
                + "}";

        assertThat(hasSingleParameter(PrimitiveType.intType()).test(firstMethod(clazz))).isTrue();
    }

    @Test
    void singleParameterMethodForClass() {
        String clazz = "class Foo {" //
                + "void x(Bar x) {}" //
                + "}";

        assertThat(hasSingleParameter(new ClassOrInterfaceType("Bar")).test(firstMethod(clazz))).isTrue();
    }

    @Test
    void singleParameterMethodForArray() {
        String clazz = "class Foo {" //
                + "void x(int[] x) {}" //
                + "}";

        assertThat(hasSingleParameter(new ArrayType(PrimitiveType.intType())).test(firstMethod(clazz))).isTrue();
    }

    @Test
    void multiParameterMethodForInt() {
        String clazz = "class Foo {" //
                + "void x(int x, int y) {}" //
                + "}";

        assertThat(hasSingleParameter(PrimitiveType.intType()).test(firstMethod(clazz))).isFalse();
    }

    @Test
    void singleParameterMethodNotMatchingType() {
        String clazz = "class Foo {" //
                + "void x(char x) {}" //
                + "}";

        assertThat(hasSingleParameter(PrimitiveType.intType()).test(firstMethod(clazz))).isFalse();
    }

    MethodDeclaration firstMethod(String clazz) {
        return ClassUtil.allMember(//
                ClassUtil.findClass(//
                        StaticJavaParser.parse(clazz), "Foo").orElse(null),
                MethodDeclaration.class).findAny().orElse(null);
    }

    MethodDeclaration firstMethod(CompilationUnit unit) {
        MethodDeclaration md = ClassUtil
                .allMember(ClassUtil.findClass(unit, "Foo").orElse(null), MethodDeclaration.class).findAny()
                .orElse(null);
        return md;
    }

}

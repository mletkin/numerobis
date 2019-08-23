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
 * List field.
 */
class ListFieldTest {

    @Test
    void addMethodForListField() {
        assertThat(generateFromResource("WithList")).isEqualTo(//
                "import java.util.List;" //
                        + "public class WithListBuilder {" //
                        + "    private WithList product;" //
                        + "    public WithListBuilder() {" //
                        + "        product = new WithList();" //
                        + "    }" //
                        + "    public WithListBuilder withX(List<String> x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithListBuilder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithList build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void addMethodForSetField() {
        assertThat(generateFromResource("WithSet")).isEqualTo(//
                "import java.util.Set;" //
                        + "public class WithSetBuilder {" //
                        + "    private WithSet product;" //
                        + "    public WithSetBuilder() {" //
                        + "        product = new WithSet();" //
                        + "    }" //
                        + "    public WithSetBuilder withX(Set<String> x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithSetBuilder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithSet build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void retainsAddMethod() {
        assertThat(generateFromResource("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder addX(String foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).isEqualTo(//
                "import java.util.List;" //
                        + "public class WithListBuilder {" // 1
                        + "    public WithListBuilder addX(String foo) {" //
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
                        + "}");
    }

    private String generateFromResource(String className) {
        try {
            return Facade.withConstructors(StaticJavaParser.parseResource(className + ".java"), className,
                    new CompilationUnit()).builderUnit.toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateFromResource(String className, String builderClass) {
        try {
            CompilationUnit source = StaticJavaParser.parseResource(className + ".java");
            CompilationUnit target = StaticJavaParser.parse(builderClass);

            return Facade.withConstructors(source, className, target).builderUnit.toString().replace("\r\n", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

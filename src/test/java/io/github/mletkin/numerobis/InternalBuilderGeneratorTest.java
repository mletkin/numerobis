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

import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;

/**
 * Builder generation without existing builder class.
 */
class InternalBuilderGeneratorTest {

    private Facade facade = new Facade(false);

    @Test
    void convertsClassWithField() {
        var product = "TestClass";
        var result = facade.withConstructors(parse(product), product).execute();

        assertThat(builder(result, product)).isEqualTo( //
                "public static class Builder {" //
                        + "    private TestClass product;" //
                        + "    public Builder() {" //
                        + "        product = new TestClass();" //
                        + "    }" //
                        + "    public Builder withX(int x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }" //
                        + "    public TestClass build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void notContainedClassProducesNothing() {
        var generator = facade //
                .withConstructors(parse("TestClass"), "Foo", new CompilationUnit());

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute).withMessage("Product class Foo not found in compilation unit.");
    }

    @Test
    void nullClassProducesNothing() {
        var generator = facade //
                .withConstructors(parse("TestClass"), "", new CompilationUnit());

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute) //
                .withMessage("Product class not found in compilation unit.");
    }

    @Disabled
    @Test
    void classWithoutUsableConstructorThrowsException() {
        // The member class builder can use any constructor
        // Although using a private constructor is not encouraged
    }

}

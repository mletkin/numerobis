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
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;

/**
 * Builder generation without existing builder class.
 */
class InternalBuilderGeneratorTest {

    private Facade facade = new Facade();

    @Test
    void convertsClassWithField() {
        var product = "TestClass";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

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
    void classWithoutUsableConstructorThrowsException() {
        var product = "TestClassWithoutConstructor";
        var order = mkOrder(product);
        var generator = facade.embeddedWithConstructors(order);

        assertThatExceptionOfType(GeneratorException.class) //
                .isThrownBy(generator::execute) //
                .withMessage("No suitable constructor found.");
    }

}

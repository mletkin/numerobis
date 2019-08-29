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

import static io.github.mletkin.numerobis.Util.externalWithConstructors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Builder generation with constructors without existing builder class.
 */
class ExternalBuilderGeneratorWithConstructorsTest {

    @Test
    void productClassWithoutConstructor() {
        assertThat(externalWithConstructors("Empty")).isEqualTo(//
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
    void productClassWithCustomConstructor() {
        assertThat(externalWithConstructors("EmptyWithCustomConstructor")).isEqualTo( //
                "public class EmptyWithCustomConstructorBuilder {" //
                        + "    private EmptyWithCustomConstructor product;" //
                        + "    public EmptyWithCustomConstructorBuilder(int n) {" //
                        + "        product = new EmptyWithCustomConstructor(n);" //
                        + "    }" //
                        + "    public EmptyWithCustomConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void productClassWithDefaultConstructor() {
        assertThat(externalWithConstructors("EmptyWithDefaultConstructor")).isEqualTo( //
                "public class EmptyWithDefaultConstructorBuilder {" //
                        + "    private EmptyWithDefaultConstructor product;" //
                        + "    public EmptyWithDefaultConstructorBuilder() {" //
                        + "        product = new EmptyWithDefaultConstructor();" //
                        + "    }" //
                        + "    public EmptyWithDefaultConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void constructorWithAnnotationIsIgnored() {
        assertThat(externalWithConstructors("EmptyWithIgnoredConstructor")).isEqualTo( //
                "public class EmptyWithIgnoredConstructorBuilder {" //
                        + "    private EmptyWithIgnoredConstructor product;" //
                        + "    public EmptyWithIgnoredConstructorBuilder(int n) {" //
                        + "        product = new EmptyWithIgnoredConstructor(n);" //
                        + "    }" //
                        + "    public EmptyWithIgnoredConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void privateConstructorIsIgnored() {
        assertThat(externalWithConstructors("EmptyWithPrivateAndPublicConstructor")).isEqualTo( //
                "public class EmptyWithPrivateAndPublicConstructorBuilder {" //
                        + "    private EmptyWithPrivateAndPublicConstructor product;" //
                        + "    public EmptyWithPrivateAndPublicConstructorBuilder(int n) {" //
                        + "        product = new EmptyWithPrivateAndPublicConstructor(n);" //
                        + "    }" //
                        + "    public EmptyWithPrivateAndPublicConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}

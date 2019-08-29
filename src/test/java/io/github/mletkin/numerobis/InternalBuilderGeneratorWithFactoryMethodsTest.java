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

import static io.github.mletkin.numerobis.Util.internalWithFactories;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Inner Builder generation with Factory Methods.
 */
class InternalBuilderGeneratorWithFactoryMethodsTest {

    @Test
    void productClassWithoutConstructor() {
        assertThat(internalWithFactories("Empty")).isEqualTo(//
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
        assertThat(internalWithFactories("EmptyWithCustomConstructor")).isEqualTo( //
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
        assertThat(internalWithFactories("EmptyWithDefaultConstructor")).isEqualTo( //
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
        assertThat(internalWithFactories("EmptyWithIgnoredConstructor")).isEqualTo( //
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
        assertThat(internalWithFactories("EmptyWithPrivateAndPublicConstructor")).isEqualTo( //
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

}

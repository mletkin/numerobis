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

import static io.github.mletkin.numerobis.Util.internalWithConstructors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Builder generation of internal builder class.
 */
class InternalBuilderGeneratorWithConstructorsTest {

    @Test
    void productClassWithoutConstructor() {
        assertThat(internalWithConstructors("Empty")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Empty product;" //
                        + "    public Builder() {" //
                        + "        product = new Empty();" //
                        + "    }" //
                        + "    public Empty build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void productClassWithCustomConstructor() {
        assertThat(internalWithConstructors("EmptyWithCustomConstructor")).isEqualTo(//
                "public static class Builder {" //
                        + "    private EmptyWithCustomConstructor product;" //
                        + "    public Builder(int n) {" //
                        + "        product = new EmptyWithCustomConstructor(n);" //
                        + "    }" //
                        + "    public EmptyWithCustomConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void productClassWithDefaultConstructor() {
        assertThat(internalWithConstructors("EmptyWithDefaultConstructor")).isEqualTo(//
                "public static class Builder {" //
                        + "    private EmptyWithDefaultConstructor product;" //
                        + "    public Builder() {" //
                        + "        product = new EmptyWithDefaultConstructor();" //
                        + "    }" //
                        + "    public EmptyWithDefaultConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void constructorWithAnnotationIsIgnored() {
        assertThat(internalWithConstructors("EmptyWithIgnoredConstructor")).isEqualTo( //
                "public static class Builder {" //
                        + "    private EmptyWithIgnoredConstructor product;" //
                        + "    public Builder(int n) {" //
                        + "        product = new EmptyWithIgnoredConstructor(n);" //
                        + "    }" //
                        + "    public EmptyWithIgnoredConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void privateConstructorIsProcessed() {
        assertThat(internalWithConstructors("EmptyWithPrivateAndPublicConstructor")).isEqualTo( //
                "public static class Builder {" //
                        + "    private EmptyWithPrivateAndPublicConstructor product;" //
                        + "    public Builder(int n) {" //
                        + "        product = new EmptyWithPrivateAndPublicConstructor(n);" //
                        + "    }" //
                        + "    public Builder(String s) {" //
                        + "        product = new EmptyWithPrivateAndPublicConstructor(s);" //
                        + "    }" //
                        + "    public EmptyWithPrivateAndPublicConstructor build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void usesExistingMemberBuilderClass() {
        assertThat(internalWithConstructors("WithBuilder")).isEqualTo(//
                "public static class Builder {" //
                        + "    private int x;" //
                        + "    private WithBuilder product;" //
                        + "    public Builder() {" //
                        + "        product = new WithBuilder();" //
                        + "    }" //
                        + "    public WithBuilder build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}

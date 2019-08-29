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

import org.junit.jupiter.api.Test;

public class MutableByDefaultTest {

    TestFacade facade = new TestFacade(true);

    @Test
    void withoutAnnotationCreatesManipulationConstructorInExternalBuilder() {
        assertThat(facade.externalWithConstructors("Empty")).contains(//
                "    public EmptyBuilder(Empty product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationConstructorInInternalBuilder() {
        assertThat(facade.internalWithConstructors("Empty")).contains(//
                "public Builder(Empty product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationFactoryInExternalBuilder() {
        assertThat(facade.externalWithFactories("Empty")).contains(//
                "public static EmptyBuilder of(Empty product) {" //
                        + "        return new EmptyBuilder(product);" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationFactoryInInternalBuilder() {
        assertThat(facade.internalWithFactories("Empty")).contains(//
                "public static Builder of(Empty product) {" //
                        + "        return new Builder(product);" //
                        + "    }");
    }


    @Test
    void mutableAnnotationCreatesManipulationConstructorInExternalBuilder() {
        assertThat(facade.externalWithConstructors("Mutable")).contains(//
                "    public MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationConstructorInInternalBuilder() {
        assertThat(facade.internalWithConstructors("Mutable")).contains(//
                "public Builder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryInExternalBuilder() {
        assertThat(facade.externalWithFactories("Mutable")).contains(//
                "public static MutableBuilder of(Mutable product) {" //
                        + "        return new MutableBuilder(product);" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryInInternalBuilder() {
        assertThat(facade.internalWithFactories("Mutable")).contains(//
                "public static Builder of(Mutable product) {" //
                        + "        return new Builder(product);" //
                        + "    }");
    }

    @Test
    void immutablePreventsManipulationFactoryInInternalBuilder() {
        assertThat(facade.internalWithFactories("Immutable")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Immutable product;" //
                        + "    private Builder(Immutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static Builder of() {" //
                        + "        return new Builder(new Immutable());" //
                        + "    }" //
                        + "    public Immutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void immutablePreventsManipulationFactoryInExternalBuilder() {
        assertThat(facade.externalWithFactories("Immutable")).isEqualTo(//
                "public class ImmutableBuilder {" //
                        + "    private Immutable product;" //
                        + "    private ImmutableBuilder(Immutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static ImmutableBuilder of() {" //
                        + "        return new ImmutableBuilder(new Immutable());" //
                        + "    }" //
                        + "    public Immutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void immutablePreventsManipulationConstructorInInternalBuilder() {
        assertThat(facade.internalWithConstructors("Immutable")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Immutable product;" //
                        + "    public Builder() {" //
                        + "        product = new Immutable();" //
                        + "    }" //
                        + "    public Immutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }


    @Test
    void immutablePreventsManipulationConstructorInExternalBuilder() {
        assertThat(facade.externalWithConstructors("Immutable")).isEqualTo(//
                "public class ImmutableBuilder {" //
                        + "    private Immutable product;" //
                        + "    public ImmutableBuilder() {" //
                        + "        product = new Immutable();" //
                        + "    }" //
                        + "    public Immutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

}

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

import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;

public class ImmutableByDefaultTest {

    private Facade facade = new Facade();

    @Test
    void mutableAnnotationCreatesManipulationConstructorInSeparateBuilder() {
        var product = "Mutable";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains(//
                "public MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationConstructorInEmbeddedBuilder() {
        var product = "Mutable";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
                "public Builder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryInSeparateBuilder() {
        var product = "Mutable";
        var order = mkOrder(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
                "public static MutableBuilder of(Mutable product) {" //
                        + "        return new MutableBuilder(product);" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryinInternalBuilder() {
        var product = "Mutable";
        var order = mkOrder(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "public static Builder of(Mutable product) {" //
                        + "        return new Builder(product);" //
                        + "    }");
    }

    @Test
    void immmutableAnnotationPreventsManipulationFactoryInInternalBuilder() {
        var product = "Immutable";
        var order = mkOrder(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).isEqualTo( //
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
    void immmutableAnnotationPreventsManipulationFactoryInExternalBuilder() {
        var product = "Immutable";
        var order = mkOrder(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).isEqualTo( //
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
    void immmutableAnnotationPreventsManipulationConstructorInInternalBuilder() {
        var product = "Immutable";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).isEqualTo( //
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
    void immmutableAnnotationPreventsManipulationConstructorInExternalBuilder() {
        var product = "Immutable";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).isEqualTo( //
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

    @Test
    void withoutAnnotationNoManipulationFactoryInInternalBuilder() {
        var product = "Empty";
        var order = mkOrder(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).isEqualTo( //
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
    void withoutAnnotationNoManipulationFactoryInExternalBuilder() {
        var product = "Empty";
        var order = mkOrder(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).isEqualTo( //
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
    void withoutAnnotationNoManipulationConstructorInInternalBuilder() {
        var product = "Empty";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).isEqualTo( //
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
    void withoutAnnotationNoManipulationConstructorInExternalBuilder() {
        var product = "Empty";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).isEqualTo( //
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

}

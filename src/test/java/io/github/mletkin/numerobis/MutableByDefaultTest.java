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
import static io.github.mletkin.numerobis.Fixture.mkOrderMutableProduct;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;

public class MutableByDefaultTest {

    private Facade facade = new Facade();

    @Test
    void withoutAnnotationCreatesManipulationConstructorInExternalBuilder() {
        var product = "Empty";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains( //
                "    public EmptyBuilder(Empty product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationConstructorInInternalBuilder() {
        var product = "Empty";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
                "public Builder(Empty product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationFactoryInExternalBuilder() {
        var product = "Empty";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
                "public static EmptyBuilder of(Empty product) {" //
                        + "        return new EmptyBuilder(product);" //
                        + "    }");
    }

    @Test
    void withoutAnnotationCreatesManipulationFactoryInInternalBuilder() {
        var product = "Empty";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "public static Builder of(Empty product) {" //
                        + "        return new Builder(product);" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationConstructorInExternalBuilder() {
        var product = "Mutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains( //
                "    public MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationConstructorInInternalBuilder() {
        var product = "Mutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
                "public Builder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryInExternalBuilder() {
        var product = "Mutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
                "public static MutableBuilder of(Mutable product) {" //
                        + "        return new MutableBuilder(product);" //
                        + "    }");
    }

    @Test
    void mutableAnnotationCreatesManipulationFactoryInInternalBuilder() {
        var product = "Mutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "public static Builder of(Mutable product) {" //
                        + "        return new Builder(product);" //
                        + "    }");
    }

    @Test
    void immutablePreventsManipulationFactoryInInternalBuilder() {
        var product = "Immutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
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
        var product = "Immutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
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
        var product = "Immutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
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
        var product = "Immutable";
        var order = mkOrderMutableProduct(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains( //
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

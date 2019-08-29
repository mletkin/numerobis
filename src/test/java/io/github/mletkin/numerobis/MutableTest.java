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
import static io.github.mletkin.numerobis.Util.externalWithFactories;
import static io.github.mletkin.numerobis.Util.internalWithConstructors;
import static io.github.mletkin.numerobis.Util.internalWithFactories;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.javaparser.StaticJavaParser;

import io.github.mletkin.numerobis.generator.Facade;

public class MutableTest {

    @Test
    void mutableCreatesManipulationConstructorInExternalBuilder() {
        assertThat(externalWithConstructors("Mutable")).isEqualTo(//
                "public class MutableBuilder {" //
                        + "    private Mutable product;" //
                        + "    public MutableBuilder() {" //
                        + "        product = new Mutable();" //
                        + "    }" //
                        + "    public MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void mutableCreatesManipulationConstructorInInternalBuilder() {
        assertThat(internalWithConstructors("Mutable")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Mutable product;" //
                        + "    public Builder() {" //
                        + "        product = new Mutable();" //
                        + "    }" //
                        + "    public Builder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void mutableCreatesManipulationFactoryinExternalBuilder() {
        assertThat(externalWithFactories("Mutable")).isEqualTo(//
                "public class MutableBuilder {" //
                        + "    private Mutable product;" //
                        + "    private MutableBuilder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static MutableBuilder of() {" //
                        + "        return new MutableBuilder(new Mutable());" //
                        + "    }" //
                        + "    public static MutableBuilder of(Mutable product) {" //
                        + "        return new MutableBuilder(product);" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void mutableCreatesManipulationFactoryinInternalBuilder() {
        assertThat(internalWithFactories("Mutable")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Mutable product;" //
                        + "    private Builder(Mutable product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static Builder of() {" //
                        + "        return new Builder(new Mutable());" //
                        + "    }" //
                        + "    public static Builder of(Mutable product) {" //
                        + "        return new Builder(product);" //
                        + "    }" //
                        + "    public Mutable build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void immmutablePreventsManipulationFactoryinInternalBuilder() {
        assertThat(mutableTrueInternalWithFactories("Immutable")).isEqualTo(//
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
    void mutableByDefaultcreatedManipulationFactoryInInternalBuilderWithoutAnnotation() {
        assertThat(mutableTrueInternalWithFactories("Empty")).isEqualTo(//
                "public static class Builder {" //
                        + "    private Empty product;" //
                        + "    private Builder(Empty product) {" //
                        + "        this.product = product;" //
                        + "    }" //
                        + "    public static Builder of() {" //
                        + "        return new Builder(new Empty());" //
                        + "    }" //
                        + "    public static Builder of(Empty product) {" //
                        + "        return new Builder(product);" //
                        + "    }" //
                        + "    public Empty build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    static String mutableTrueInternalWithFactories(String className) {
        return Util.uncheckExceptions(
                () -> Util.asString(
                        Util.extractBuilder(
                                new Facade(true).withFactoryMethods(
                                        StaticJavaParser.parseResource(className + ".java"), className).productUnit,
                                className)));
    }

}

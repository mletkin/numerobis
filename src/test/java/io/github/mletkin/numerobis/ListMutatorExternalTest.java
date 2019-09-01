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

import io.github.mletkin.numerobis.annotation.GenerateAdder.Variant;
import io.github.mletkin.numerobis.generator.Facade;

/**
 * Mutator generation for generated external builder.
 */
class ListMutatorExternalTest {

    @Test
    void addsObjectMutatorForList() {
        Variant[] variants = { Variant.OBJECT };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void addsObjectMutatorForSet() {
        Variant[] variants = { Variant.OBJECT };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithSet"))
                .contains(//
                        "public WithSetBuilder withX(Set<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsObjectMutatorForList() {
        Variant[] variants = { Variant.OBJECT };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList",
                "public class WithListBuilder {" //
                        + "    public WithListBuilder withX(List<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).contains(//
                "public WithListBuilder withX(List<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
        ).doesNotContain("public WithListBuilder withX(List<String> x)");
    }

    @Test
    void addsStreamMutatorForList() {
        Variant[] variants = { Variant.STREAM };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void addsStreamMutatorForSet() {
        Variant[] variants = { Variant.STREAM };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithSet"))
                .contains(//
                        "public WithSetBuilder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsStreamMutator() {
        Variant[] variants = { Variant.STREAM };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder withX(Stream<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).contains(//
                "public WithListBuilder withX(Stream<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
        ).doesNotContain("public WithListBuilder withX(Stream<String> items)");
    }

    @Test
    void addsCollectionMutatorForList() {
        Variant[] variants = { Variant.COLLECTION };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void addsCollectionMutatorForSet() {
        Variant[] variants = { Variant.COLLECTION };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithSet"))
                .contains(//
                        "public WithSetBuilder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsCollectionMutator() {
        Variant[] variants = { Variant.COLLECTION };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder withX(Collection<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).contains(//
                "public WithListBuilder withX(Collection<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
        ).doesNotContain("public WithListBuilder withX(Collection<String> items)");
    }

    @Test
    void addsVarArgMutatorForList() {
        Variant[] variants = { Variant.VARARG };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void addsVarArgMutatorForSet() {
        Variant[] variants = { Variant.VARARG };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithSet"))
                .contains(//
                        "public WithSetBuilder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsVarArgMutator() {
        Variant[] variants = { Variant.VARARG };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder withX(String... foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).contains(//
                "public WithListBuilder withX(String... foo) {" //
                        + "        return null;" //
                        + "    }" //
        ).doesNotContain("public WithListBuilder withX(String... items)");
    }

}

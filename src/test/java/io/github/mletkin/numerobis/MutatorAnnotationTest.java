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
import static io.github.mletkin.numerobis.Util.internalWithConstructors;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

class MutatorAnnotationTest {

    @Test
    void adderAnnoForListFieldInExternalBuilder() {
        assertThat(externalWithConstructors("AdderAnno")).contains(//
                "public AdderAnnoBuilder addProduct(String item) {" //
                        + "        product.products.add(item);" //
                        + "        return this;" //
                        + "    }" //
        ).contains( //
                "public AdderAnnoBuilder addProduct(Stream<String> items) {" //
                        + "        items.forEach(product.products::add);" //
                        + "        return this;" //
                        + "    }" //
        );
    }

    @Test
    void mutatorAnnoForListFieldInInternalBuilder() {
        assertThat(internalWithConstructors("AdderAnno")).contains(//
                "public Builder withProducts(List<String> products) {" //
                        + "        product.products = products;" //
                        + "        return this;" //
                        + "    }" //
        ).contains( //
                "public Builder withProducts(Stream<String> items) {" //
                        + "        product.products = items.collect(Collectors.toList());" //
                        + "        return this;" //
                        + "    }" //
        );
    }

    @Test
    void mutatorAnnoForListFieldInExternalBuilder() {
        assertThat(externalWithConstructors("AdderAnno")).contains(//
                "public AdderAnnoBuilder withProducts(List<String> products) {" //
                        + "        product.products = products;" //
                        + "        return this;" //
                        + "    }" //
        ).contains( //
                "public AdderAnnoBuilder withProducts(Stream<String> items) {" //
                        + "        product.products = items.collect(Collectors.toList());" //
                        + "        return this;" //
                        + "    }" //
        );
    }

    @Test
    void adderAnnoForListFieldInInternalBuilder() {
        assertThat(internalWithConstructors("AdderAnno")).contains(//
                "public Builder addProduct(String item) {" //
                        + "        product.products.add(item);" //
                        + "        return this;" //
                        + "    }" //
        ).contains( //
                "public Builder addProduct(Stream<String> items) {" //
                        + "        items.forEach(product.products::add);" //
                        + "        return this;" //
                        + "    }" //
        );
    }

    @Test
    void adderAnnotationOverridesPomInInternalBuilder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(
                new TestFacade(new Facade(false).withAdderVariants(variants)).internalWithConstructors("AdderAnnoNone"))
                        .doesNotContain(//
                                "addProduct");
    }

    @Test
    void adderAnnotationOverridesPomInExternalBuilder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(
                new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("AdderAnnoNone"))
                        .doesNotContain(//
                                "addProduct");
    }

    @Test
    void mutatorAnnotationOverridesPomInInternalBuilder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants))
                .internalWithConstructors("AdderAnnoNone")).doesNotContain(//
                        "addProduct");
    }

    @Test
    void mutatorAnnotationOverridesPomInExternalBuilder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants))
                .externalWithConstructors("AdderAnnoNone")).doesNotContain(//
                        "addProduct");
    }

}

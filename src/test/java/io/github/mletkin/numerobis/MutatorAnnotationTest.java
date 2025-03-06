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

import static io.github.mletkin.numerobis.Fixture.asArray;
import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

class MutatorAnnotationTest {

    private Facade facade = new Facade(false);

    void adderAnnoForListFieldInExternalBuilder() {
        var product = "AdderAnno";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains( //
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
        var product = "AdderAnno";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
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
        var product = "AdderAnno";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).contains( //
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
        var product = "AdderAnno";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
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
    void adderAnnotationOverridesDefaultInInternalBuilder() {
        var product = "AdderAnnoNone";
        var order = mkOrder(product);
        var result = facade //
                .withAdderVariants(asArray(ListMutatorVariant.STREAM)) //
                .embeddedWithConstructors(order) //
                .execute();

        assertThat(asString(result)).doesNotContain( //
                "addProduct");
    }

    @Test
    void mutatorAnnotationOverridesDefaultInInternalBuilder() {
        var product = "AdderAnnoNone";
        var order = mkOrder(product);
        var result = facade //
                .withMutatorVariants(asArray(ListMutatorVariant.STREAM)) //
                .embeddedWithConstructors(order) //
                .execute();

        assertThat(asString(result)).doesNotContain( //
                "addProduct");
    }

    @Test
    void mutatorAnnotationOverridesPomInExternalBuilder() {
        var product = "AdderAnnoNone";
        var order = mkOrder(product);
        var result = facade //
                .withMutatorVariants(asArray(ListMutatorVariant.STREAM)) //
                .separateWithConstructors(order) //
                .execute();

        assertThat(asString(result)).doesNotContain( //
                "addProduct");
    }

}

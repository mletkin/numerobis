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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Adder generation for generated external builder.
 */
class AdderExternalTest {

    @Test
    void adderForListField() {
        assertThat(externalWithConstructors("WithList")).contains(//
                "    public WithListBuilder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForListFieldWithPostfixS() {
        assertThat(externalWithConstructors("WithListWithPostfix")).contains(//
                "    public WithListWithPostfixBuilder addProduct(String item) {" //
                        + "        product.products.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Disabled
    @Test
    void adderForListFieldWithPostfixEn() {
        assertThat(externalWithConstructors("WithListWithPostfix")).contains(//
                "    public WithListWithPostfixBuilder addPerson(String item) {" //
                        + "        product.personen.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Disabled
    @Test
    void adderForListFieldWithPostfixE() {
        assertThat(externalWithConstructors("WithListWithPostfix")).contains(//
                "    public WithListWithPostfixBuilder addBrief(String item) {" //
                        + "        product.briefe.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForSetField() {
        assertThat(externalWithConstructors("WithSet")).isEqualTo(//
                "import java.util.Set;" //
                        + "public class WithSetBuilder {" //
                        + "    private WithSet product;" //
                        + "    public WithSetBuilder() {" //
                        + "        product = new WithSet();" //
                        + "    }" //
                        + "    public WithSetBuilder withX(Set<String> x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithSetBuilder addX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }" //
                        + "    public WithSet build() {" //
                        + "        return product;" //
                        + "    }" //
                        + "}");
    }

    @Test
    void addsItemAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.ITEM };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void addsStreamAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder addX(Stream<String> items) {" //
                                + "        items.forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsStreamAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder addX(Stream<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).doesNotContain(//
                "public WithListBuilder addX(Stream<String> stream)");
    }

    @Test
    void addCollectionAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.COLLECTION };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder addX(Collection<String> items) {" //
                                + "        product.x.addAll(items);" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsCollectionAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.COLLECTION };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder addX(Collection<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).doesNotContain(//
                "public WithListBuilder addX(Collection<String> collection)" //
        );
    }

    @Test
    void addsVarArgAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.VARARG };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList"))
                .contains(//
                        "public WithListBuilder addX(String... items) {" //
                                + "        Stream.of(items).forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }");
    }

    @Test
    void retainsVarArgAdder() {
        ListMutatorVariant[] variants = { ListMutatorVariant.VARARG };
        assertThat(new TestFacade(new Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList", //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder addX(String... foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}") //
        ).doesNotContain(//
                "public WithListBuilder addX(String... items)" //
        );
    }

}

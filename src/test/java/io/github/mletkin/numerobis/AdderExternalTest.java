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
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.product;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Adder generation for generated external builder.
 */
class AdderExternalTest {
    private Facade facade = new Facade();

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String method) {
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(product(result, product + "Builder")).as(desc).contains(method);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("adderForListField", "WithList", //
                        "    public WithListBuilder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("adderForListFieldWithPostfixS", "WithListWithPostfix", //
                        "    public WithListWithPostfixBuilder addProduct(String item) {" //
                                + "        product.products.add(item);" //
                                + "        return this;" //
                                + "    }")

        );
    }

    // Only suffix "s" works right now
    @Disabled
    @Test
    void adderForListFieldWithPostfixEn() {
        var product = "WithListWithPostfix";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(product(result, product + "Builder")).contains( //
                "    public WithListWithPostfixBuilder addPerson(String item) {" //
                        + "        product.personen.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    // Only suffix "s" works right now
    @Disabled
    @Test
    void adderForListFieldWithPostfixE() {
        var product = "WithListWithPostfix";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();
        assertThat(product(result, product + "Builder")).contains( //
                "    public WithListWithPostfixBuilder addBrief(String item) {" //
                        + "        product.briefe.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderForSetField() {
        var product = "WithSet";
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();

        assertThat(asString(result)).isEqualTo( //
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

    @ParameterizedTest
    @MethodSource("adderCases")
    void addsAdder(String desc, ListMutatorVariant variant, String method) {
        var product = "WithList";
        var order = mkOrder(product);
        var result = facade.withAdderVariants(asArray(variant)).separateWithConstructors(order).execute();

        assertThat(asString(result)).contains(method);
    }

    static Stream<Arguments> adderCases() {
        return Stream.of( //
                Arguments.of("addsItemAdder", ListMutatorVariant.ITEM, //
                        "public WithListBuilder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsStreamAdder", ListMutatorVariant.STREAM, //
                        "public WithListBuilder addX(Stream<String> items) {" //
                                + "        items.forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionAdder", ListMutatorVariant.COLLECTION, //
                        "public WithListBuilder addX(Collection<String> items) {" //
                                + "        product.x.addAll(items);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsVarArgAdder", ListMutatorVariant.VARARG, //
                        "public WithListBuilder addX(String... items) {" //
                                + "        Stream.of(items).forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }")

        );

    }

    @Disabled
    @ParameterizedTest
    @MethodSource("retainCases")
    void retainsAdder(ListMutatorVariant variant, String clazz, String method) {
        var product = "WithList";
        var order = mkOrder(product);
        var result = facade.withAdderVariants(asArray(variant)).separateWithConstructors(order).execute();

        assertThat(asString(result)).doesNotContain(method);
    }

    static Stream<Arguments> retainCases() {
        return Stream.of( //
                Arguments.of(ListMutatorVariant.ITEM, //
                        "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(String foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public WithListBuilder addX(String item"), //

                Arguments.of(ListMutatorVariant.STREAM, //
                        "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(Stream<String> foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public WithListBuilder addX(Stream<String> stream)"), //

                Arguments.of(ListMutatorVariant.COLLECTION, //
                        "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(Collection<String> foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public WithListBuilder addX(Collection<String> collection)"), //

                Arguments.of(ListMutatorVariant.VARARG, //
                        "public class WithListBuilder {" //
                                + "    public WithListBuilder addX(String... foo) {" //
                                + "        return null;" //
                                + "    }" //
                                + "}", //
                        "public WithListBuilder addX(String... items)") //
        );
    }

}

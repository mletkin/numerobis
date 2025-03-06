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
import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static io.github.mletkin.numerobis.Fixture.parse;
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
 * Adder generation for generated internal builder.
 */
class AdderInternalTest {

    private Facade facade = new Facade();

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String method) {
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).as(desc).contains(method);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("adderForListField", "WithList", //
                        "public Builder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("adderForListFieldWithPostfixS", "WithListWithPostfix", //
                        "public Builder addProduct(String item) {" //
                                + "        product.products.add(item);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("adderForSetField", "WithSet", //
                        "public Builder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }")

        );
    }

    @Disabled
    @Test
    void adderForListFieldWithPostfixEn() {
        var product = "WithListWithPostfix";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
                "public Builder addPerson(String item) {" //
                        + "        product.personen.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Disabled
    @Test
    void adderForListFieldWithPostfixE() {
        var product = "WithListWithPostfix";
        var order = mkOrder(product);
        var result = facade.embeddedWithConstructors(order).execute();

        assertThat(builder(result, product)).contains( //
                "public Builder addBrief(String item) {" //
                        + "        product.briefe.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @ParameterizedTest
    @MethodSource("adderCases")
    void addsAdder(ListMutatorVariant variant, String method) {
        var product = "WithList";
        var order = mkOrder(product);
        var result = facade //
                .withAdderVariants(asArray(variant)) //
                .embeddedWithConstructors(order) //
                .execute();

        assertThat(builder(result, product)).contains(method);
    }

    static Stream<Arguments> adderCases() {
        return Stream.of( //
                Arguments.of(ListMutatorVariant.ITEM, //
                        "public Builder addX(String item) {" //
                                + "        product.x.add(item);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of(ListMutatorVariant.STREAM, //
                        "public Builder addX(Stream<String> items) {" //
                                + "        items.forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of(ListMutatorVariant.COLLECTION, //
                        "public Builder addX(Collection<String> items) {" //
                                + "        product.x.addAll(items);" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of(ListMutatorVariant.VARARG, //
                        "public Builder addX(String... items) {" //
                                + "        Stream.of(items).forEach(product.x::add);" //
                                + "        return this;" //
                                + "    }")

        );
    }

}

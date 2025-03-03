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
import static io.github.mletkin.numerobis.Fixture.parse;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Mutator generation for generated internal builder.
 */
class ListMutatorInternalTest {

    private Facade facade = new Facade(false);

    @ParameterizedTest
    @MethodSource("listCases")
    void addsMutatorForList(String desc, ListMutatorVariant variant, String method) {
        var product = "WithList";
        var result = facade //
                .withMutatorVariants(asArray(variant)) //
                .withConstructors(parse(product), product) //
                .execute();

        assertThat(builder(result, product)).as(desc).contains(method);
    }

    static Stream<Arguments> listCases() {
        return Stream.of( //
                Arguments.of("defaultMutatorIsObject", null, //
                        "public Builder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsObjectMutator", ListMutatorVariant.OBJECT, //
                        "public Builder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsStreamMutator", ListMutatorVariant.STREAM, //
                        "public Builder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionMutator", ListMutatorVariant.COLLECTION, //
                        "public Builder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsVarargMutator", ListMutatorVariant.VARARG, //
                        "public Builder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }")

        );
    }

    @ParameterizedTest
    @MethodSource("setCases")
    void addsMutatorForSet(String desc, ListMutatorVariant variant, String method) {
        var product = "WithSet";
        var result = facade //
                .withMutatorVariants(asArray(variant)) //
                .withConstructors(parse(product), product) //
                .execute();

        assertThat(builder(result, product)).as(desc).contains(method);
    }

    static Stream<Arguments> setCases() {
        return Stream.of( //
                Arguments.of("defaultMutatorIsObject", null, //
                        "public Builder withX(Set<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsObjectMutator", ListMutatorVariant.OBJECT, //
                        "public Builder withX(Set<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsStreamMutator", ListMutatorVariant.STREAM, //
                        "public Builder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionMutator", ListMutatorVariant.COLLECTION, //
                        "public Builder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsVarargMutator", ListMutatorVariant.VARARG, //
                        "public Builder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }")

        );
    }

    // @Test
    // void retainsObjectMutatorForList() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.OBJECT };
    // assertThat(new TestFacade(new
    // Facade(false).withMutatorVariants(variants)).internalWithConstructors("WithList",
    // "public class Builder {" //
    // + " public Builder withX(List<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public Builder withX(List<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public Builder withX(List<String> x)");
    // }

    // @Test
    // void retainsStreamMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
    // assertThat(new TestFacade(new
    // Facade(false).withMutatorVariants(variants)).internalWithConstructors("WithList",
    // //
    // "public class Builder {" //
    // + " public Builder withX(Stream<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public Builder withX(Stream<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public Builder withX(Stream<String> items)");
    // }

    // @Test
    // void retainsCollectionMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.COLLECTION };
    // assertThat(new TestFacade(new
    // Facade(false).withAdderVariants(variants)).internalWithConstructors("WithList",
    // //
    // "public class Builder {" //
    // + " public Builder withX(Collection<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public Builder withX(Collection<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public Builder withX(Collection<String> items)");
    // }

    // @Test
    // void retainsVarArgMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.VARARG };
    // assertThat(new TestFacade(new Facade(false).withMutatorVariants(variants))
    // .internalWithConstructors("WithListWithVarargMutator")).contains(//
    // "Builder withX(String... foo) { }");
    // }

}

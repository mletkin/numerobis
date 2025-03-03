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
import static io.github.mletkin.numerobis.Fixture.parse;
import static io.github.mletkin.numerobis.Fixture.parseString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Mutator generation for generated external builder.
 */
class ListMutatorExternalTest {

    private Facade facade = new Facade(false);

    @ParameterizedTest
    @MethodSource("listCases")
    void addsMutatorForList(String desc, ListMutatorVariant variant, String method) {
        var product = "WithList";
        var result = facade //
                .withMutatorVariants(asArray(variant)) //
                .withConstructors(parse(product), product, new CompilationUnit()) //
                .execute();

        assertThat(asString(result)).as(desc).contains(method);
    }

    static Stream<Arguments> listCases() {
        return Stream.of( //
                Arguments.of("defaultMutatorIsObject", null, //
                        "public WithListBuilder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsObjectMutator", ListMutatorVariant.OBJECT, //
                        "public WithListBuilder withX(List<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsStreamMutator", ListMutatorVariant.STREAM, //
                        "public WithListBuilder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionMutator", ListMutatorVariant.COLLECTION, //
                        "public WithListBuilder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsVarargMutator", ListMutatorVariant.VARARG, //
                        "public WithListBuilder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toList());" //
                                + "        return this;" //
                                + "    }")

        );
    }

    @Test
    void addsMutatorForListWithCustomName() {
        var product = "WithListWithCustomName";
        var result = facade //
                .withMutatorVariants(asArray(ListMutatorVariant.OBJECT)) //
                .withConstructors(parse(product), product, new CompilationUnit()) //
                .execute();

        assertThat(asString(result)).contains( //
                "public WithListWithCustomNameBuilder foo(List<String> x) {" //
                        + "        product.x = x;" //
                        + "        return this;" //
                        + "    }");
    }

    @ParameterizedTest
    @MethodSource("setCases")
    void addsMutatorForSet(String desc, ListMutatorVariant variant, String method) {
        var product = "WithSet";
        var result = facade //
                .withMutatorVariants(asArray(variant)) //
                .withConstructors(parse(product), product, new CompilationUnit()) //
                .execute();

        assertThat(asString(result)).as(desc).contains(method);
    }

    static Stream<Arguments> setCases() {
        return Stream.of( //
                Arguments.of("defaultMutatorIsObject", null, //
                        "public WithSetBuilder withX(Set<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsObjectMutator", ListMutatorVariant.OBJECT, //
                        "public WithSetBuilder withX(Set<String> x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsStreamMutator", ListMutatorVariant.STREAM, //
                        "public WithSetBuilder withX(Stream<String> items) {" //
                                + "        product.x = items.collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionMutator", ListMutatorVariant.COLLECTION, //
                        "public WithSetBuilder withX(Collection<String> items) {" //
                                + "        product.x = items.stream().collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }"),

                Arguments.of("addsCollectionMutator", ListMutatorVariant.VARARG, //
                        "public WithSetBuilder withX(String... items) {" //
                                + "        product.x = Stream.of(items).collect(Collectors.toSet());" //
                                + "        return this;" //
                                + "    }")

        );
    }

    @Test
    void retainsObjectMutatorForList() {
        var product = "WithList";
        var builder = parseString( //
                "public class WithListBuilder {" //
                        + "    public WithListBuilder withX(List<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
                        + "}");

        var result = facade //
                .withMutatorVariants(asArray(ListMutatorVariant.OBJECT)) //
                .withConstructors(parse(product), product, builder) //
                .execute();

        assertThat(asString(result)).contains( //
                "public WithListBuilder withX(List<String> foo) {" //
                        + "        return null;" //
                        + "    }" //
        ).doesNotContain("public WithListBuilder withX(List<String> x)");

    }

    // @Test
    // void retainsStreamMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.STREAM };
    // assertThat(new TestFacade(new
    // Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList",
    // //
    // "public class WithListBuilder {" //
    // + " public WithListBuilder withX(Stream<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public WithListBuilder withX(Stream<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public WithListBuilder withX(Stream<String> items)");
    // }
    //
    // @Test
    // void retainsCollectionMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.COLLECTION };
    // assertThat(new TestFacade(new
    // Facade(false).withAdderVariants(variants)).externalWithConstructors("WithList",
    // //
    // "public class WithListBuilder {" //
    // + " public WithListBuilder withX(Collection<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public WithListBuilder withX(Collection<String> foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public WithListBuilder withX(Collection<String> items)");
    // }
    //
    // @Test
    // void retainsVarArgMutator() {
    // ListMutatorVariant[] variants = { ListMutatorVariant.VARARG };
    // assertThat(new TestFacade(new
    // Facade(false).withMutatorVariants(variants)).externalWithConstructors("WithList",
    // //
    // "public class WithListBuilder {" //
    // + " public WithListBuilder withX(String... foo) {" //
    // + " return null;" //
    // + " }" //
    // + "}") //
    // ).contains(//
    // "public WithListBuilder withX(String... foo) {" //
    // + " return null;" //
    // + " }" //
    // ).doesNotContain("public WithListBuilder withX(String... items)");
    // }

}

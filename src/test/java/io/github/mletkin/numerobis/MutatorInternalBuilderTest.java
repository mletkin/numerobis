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

import static io.github.mletkin.numerobis.Fixture.builder;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Mutator generation for generated internal builder.
 */
class MutatorInternalBuilderTest {

    private Facade facade = new Facade(false);

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String expected) {
        var result = facade.withConstructors(Fixture.parse(product), product).execute();
        assertThat(builder(result, product)).as(desc).isEqualTo(expected);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("mutatorForNonListField", "Mutator", //
                        "public static class Builder {" //
                                + "    private Mutator product;" //
                                + "    public Builder() {" //
                                + "        product = new Mutator();" //
                                + "    }" //
                                + "    public Builder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public Mutator build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorsFortwoFieldsInOneDeclaration", "MutatorTwoFields", //
                        "public static class Builder {" //
                                + "    private MutatorTwoFields product;" //
                                + "    public Builder() {" //
                                + "        product = new MutatorTwoFields();" //
                                + "    }" //
                                + "    public Builder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public Builder withY(int y) {" //
                                + "        product.y = y;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorTwoFields build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorForFieldWithCustomNameAnnotation", "MutatorWithCustomName", //
                        "public static class Builder {" //
                                + "    private MutatorWithCustomName product;" //
                                + "    public Builder() {" //
                                + "        product = new MutatorWithCustomName();" //
                                + "    }" //
                                + "    public Builder fillX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorWithCustomName build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorForFieldWithAnnotationWithoutCustomMethodName", "FieldAnnoNoCustomName", //
                        "public static class Builder {" //
                                + "    private FieldAnnoNoCustomName product;" //
                                + "    public Builder() {" //
                                + "        product = new FieldAnnoNoCustomName();" //
                                + "    }" //
                                + "    public Builder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public FieldAnnoNoCustomName build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("noMutatorForFieldWithIgnoreAnnotation", "MutatorIgnore", //
                        "public static class Builder {" //
                                + "    private MutatorIgnore product;" //
                                + "    public Builder() {" //
                                + "        product = new MutatorIgnore();" //
                                + "    }" //
                                + "    public MutatorIgnore build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorForPrivateField", "MutatorPrivateField", //
                        "public static class Builder {" //
                                + "    private MutatorPrivateField product;" //
                                + "    public Builder() {" //
                                + "        product = new MutatorPrivateField();" //
                                + "    }" //
                                + "    public Builder withY(int y) {" //
                                + "        product.y = y;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorPrivateField build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

    @Test
    void mutatorForSimpleAndArrayField() {
        var product = "IntAndArray";
        var result = facade.withConstructors(Fixture.parse(product), product).execute();

        assertThat(builder(result, product)) //
                .contains( //
                        "public Builder withA(int a) {" //
                                + "        product.a = a;" //
                                + "        return this;" //
                                + "    }") //
                .contains( //
                        "public Builder withB(int[] b) {" //
                                + "        product.b = b;" //
                                + "        return this;" //
                                + "    }" //
                );
    }
}

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

import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.common.ClassUtil;

/**
 * Mutator generation for generated external builder.
 */
class MutatorExternalBuilderTest {

    private Facade facade = new Facade(false);

    public static String builder(CompilationUnit unit, String product) {
        return Fixture.asString(ClassUtil.findClass(unit, product + "Builder").orElse(null));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void test(String desc, String product, String expected) {
        var order = mkOrder(product);
        var result = facade.separateWithConstructors(order).execute();
        assertThat(asString(result)).as(desc).isEqualTo(expected);
    }

    static Stream<Arguments> testCases() {
        return Stream.of( //
                Arguments.of("mutatorForNonListField", "Mutator", //
                        "public class MutatorBuilder {" //
                                + "    private Mutator product;" //
                                + "    public MutatorBuilder() {" //
                                + "        product = new Mutator();" //
                                + "    }" //
                                + "    public MutatorBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public Mutator build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorsForTwoFieldsInOneDeclaration", "MutatorTwoFields", //
                        "public class MutatorTwoFieldsBuilder {" //
                                + "    private MutatorTwoFields product;" //
                                + "    public MutatorTwoFieldsBuilder() {" //
                                + "        product = new MutatorTwoFields();" //
                                + "    }" //
                                + "    public MutatorTwoFieldsBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorTwoFieldsBuilder withY(int y) {" //
                                + "        product.y = y;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorTwoFields build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorForFieldWithCustomNameAnnotation", "MutatorWithCustomName", //
                        "public class MutatorWithCustomNameBuilder {" //
                                + "    private MutatorWithCustomName product;" //
                                + "    public MutatorWithCustomNameBuilder() {" //
                                + "        product = new MutatorWithCustomName();" //
                                + "    }" //
                                + "    public MutatorWithCustomNameBuilder fillX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public MutatorWithCustomName build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("mutatorForFieldWithAnnotationWithoutCustomMethodName", "FieldAnnoNoCustomName", //
                        "public class FieldAnnoNoCustomNameBuilder {" //
                                + "    private FieldAnnoNoCustomName product;" //
                                + "    public FieldAnnoNoCustomNameBuilder() {" //
                                + "        product = new FieldAnnoNoCustomName();" //
                                + "    }" //
                                + "    public FieldAnnoNoCustomNameBuilder withX(int x) {" //
                                + "        product.x = x;" //
                                + "        return this;" //
                                + "    }" //
                                + "    public FieldAnnoNoCustomName build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("noMutatorForFieldWithIgnoreAnnotation", "MutatorIgnore", //
                        "public class MutatorIgnoreBuilder {" //
                                + "    private MutatorIgnore product;" //
                                + "    public MutatorIgnoreBuilder() {" //
                                + "        product = new MutatorIgnore();" //
                                + "    }" //
                                + "    public MutatorIgnore build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}"),

                Arguments.of("noMutatorForPrivateField", "MutatorPrivateField", //
                        "public class MutatorPrivateFieldBuilder {" //
                                + "    private MutatorPrivateField product;" //
                                + "    public MutatorPrivateFieldBuilder() {" //
                                + "        product = new MutatorPrivateField();" //
                                + "    }" //
                                + "    public MutatorPrivateField build() {" //
                                + "        return product;" //
                                + "    }" //
                                + "}")

        );
    }

}

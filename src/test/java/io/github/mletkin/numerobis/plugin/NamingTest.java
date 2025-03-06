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
package io.github.mletkin.numerobis.plugin;

import static io.github.mletkin.numerobis.Fixture.asString;
import static io.github.mletkin.numerobis.Fixture.builder;
import static io.github.mletkin.numerobis.Fixture.mkOrder;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.generator.Facade;

class NamingTest {

    private Facade facade = new Facade(false);

    @Test
    void buildNameConfiguration() {
        var naming = Naming.Builder.of().withBuildMethod("foo").build();
        var product = "Empty";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains(//
                "      public Empty foo() {" //
                        + "            return product;" //
                        + "        }");
    }

    @Test
    void factoryNameConfiguration() {
        var naming = Naming.Builder.of().withFactoryMethod("foo").build();
        var product = "Empty";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
                "public static Builder foo() {" //
                        + "            return new Builder(new Empty());" //
                        + "        }");
    }

    @Test
    void mutatorPrefixConfiguration() {
        var naming = Naming.Builder.of().withMutatorPrefix("foo").build();
        var product = "IntFieldWithAccessor";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "    public Builder fooFoo(int foo) {" //
                        + "        product.foo = foo;" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderPrefixConfiguration() {
        var naming = Naming.Builder.of().withAdderPrefix("foo").build();
        var product = "WithList";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "    public Builder fooX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void productFieldConfiguration() {
        var naming = Naming.Builder.of().withProductField("foo").build();
        var product = "Empty";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "private Empty foo;");
    }

    @Test
    void internalBuilderClassNameConfiguration() {
        var naming = Naming.Builder.of().withBuilderClassPostfix("Foo").build();
        var product = "WithList";
        var order = mkOrder(product, naming);

        var result = facade.embeddedWithFactoryMethods(order).execute();

        assertThat(builder(result, product)).contains( //
                "public static class Foo {");
    }

    @Test
    void externalBuilderClassNameConfiguration() {
        var naming = Naming.Builder.of().withBuilderClassPostfix("Foo").build();
        var product = "WithList";
        var order = mkOrder(product, naming);

        var result = facade.separateWithFactoryMethods(order).execute();

        assertThat(asString(result)).contains( //
                "public class WithListFoo {");
    }

}

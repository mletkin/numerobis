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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.mletkin.numerobis.plugin.Naming;

class NamingTest {

    @Test
    void buildNameConfiguration() {
        Naming naming = Naming.Builder.of().withBuildMethod("foo").build();
        assertThat(internalWithFactories("Empty", naming)).contains(//
                "    public Empty foo() {" //
                        + "        return product;" //
                        + "    }");
    }

    @Test
    void factoryNameConfiguration() {
        Naming naming = Naming.Builder.of().withFactoryMethod("foo").build();
        assertThat(internalWithFactories("Empty", naming)).contains(//
                "    public static Builder foo() {" //
                        + "        return new Builder(new Empty());" //
                        + "    }");
    }

    @Test
    void mutatorPrefixConfiguration() {
        Naming naming = Naming.Builder.of().withMutatorPrefix("foo").build();
        assertThat(internalWithFactories(("IntFieldWithAccessor"), naming)).contains(//
                "    public Builder fooFoo(int foo) {" //
                        + "        product.foo = foo;" //
                        + "        return this;" //
                        + "    }");
    }

    @Test
    void adderPrefixConfiguration() {
        Naming naming = Naming.Builder.of().withAdderPrefix("foo").build();
        assertThat(internalWithFactories(("WithList"), naming)).contains(//
                "    public Builder fooX(String item) {" //
                        + "        product.x.add(item);" //
                        + "        return this;" //
                        + "    }");
    }

    static String internalWithFactories(String className, Naming naming) {
        return new TestFacade(false, naming).internalWithFactories(className);
    }

}

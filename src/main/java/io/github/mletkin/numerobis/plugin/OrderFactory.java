/**
 * (c) 2025 by Ullrich Rieger
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

import static java.util.Optional.of;

import java.nio.file.Path;
import java.util.stream.Stream;

import io.github.mletkin.numerobis.common.VisibleForTesting;
import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;

/**
 * Generates {@link Order} objects for product classes.
 * <p>
 * uses the settings from
 * <ul>
 * <li>the MojoSettings from the maven configuration
 * <li>The name settings from the maven configuration
 * <li>The product class annotations (future feature)
 * </ul>
 */
public class OrderFactory {
    private static final ListMutatorVariant[] EMTPY = {};

    private Path destinationPath;
    private boolean useFactoryMethods;
    private boolean makeEmbeddedBuilders;
    private boolean productsAreMutable;
    private Naming naming;
    private ListMutatorVariant[] adderVariants;
    private ListMutatorVariant[] mutatorVariants;

    /**
     * Creates a Factory for the global configuration.
     *
     * @param settings configuration from the Maven configuration
     */
    public OrderFactory(MojoSettings settings) {
        this.destinationPath = settings.targetDirectory();
        this.useFactoryMethods = settings.builderCreation().flag();
        this.makeEmbeddedBuilders = settings.builderLocation().flag();
        this.productsAreMutable = settings.productsAreMutable();
        this.naming = settings.naming();

        this.adderVariants = of(settings).map(MojoSettings::listAdderVariants).map(this::toVariants).orElse(EMTPY);
        this.mutatorVariants = of(settings).map(MojoSettings::listMutatorVariants).map(this::toVariants).orElse(EMTPY);
    }

    /**
     * Maps variant lists for generator use.
     * <p>
     * The generator uses one enum for adder and list mutator methods.<br>
     * Constants are identified by name.
     *
     * @param  list List of enum constants
     * @return      array of {@code ListMutatorVariant} constants
     */
    private ListMutatorVariant[] toVariants(Enum<?>[] list) {
        return Stream.of(list) //
                .map(Enum::name) //
                .map(ListMutatorVariant::valueOf) //
                .toArray(ListMutatorVariant[]::new);
    }

    /**
     * Produces an Order from the (potential) product file.
     *
     * @param  productFile locator of the java file containing the product class
     * @return             an object describing the builder generation context
     */
    public Order makeOrder(Path productFile) {
        var order = new Order(productFile, naming, makeEmbeddedBuilders, useFactoryMethods, productsAreMutable);
        if (order.generateBuilder()) {
            order.setBuilderPath(builderPath(order));
        }
        return order;
    }

    /**
     * TODO: move to Order class.<br>
     * Settings can be changed by the product classes annotations.
     */
    public Facade makeFacade() {
        return new Facade() //
                .withAdderVariants(adderVariants) //
                .withMutatorVariants(mutatorVariants);
    }

    private Path builderPath(Order order) {
        return builderPath(order.productPath(), order.unitPackageName());
    }

    @VisibleForTesting
    Path builderPath(Path productPath, String packagePath) {
        var path = destinationPath != null //
                ? destinationPath.resolve(packageToPath(packagePath))
                : productPath.getParent();

        return path.resolve(builderFileName(productPath));
    }

    private Path packageToPath(String packagePath) {
        return Path.of("", packagePath.split("\\."));
    }

    private String builderFileName(Path productPath) {
        return productPath.getFileName().toString().replace(".java", naming.builderClassPostfix() + ".java");
    }

}

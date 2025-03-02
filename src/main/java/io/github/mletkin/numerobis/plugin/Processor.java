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

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.common.Generator;
import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.common.VisibleForTesting;
import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;
import io.github.mletkin.numerobis.generator.Sorter;

/**
 * Processor for single java files to generate builder classes.
 * <ul>
 * <li>called by the mojo
 * <li>created with a setup for the generator
 * <li>{@link #process(Path)} is called for each java file
 * <li>maps mojo settings to generator settings
 * </ul>
 */
public class Processor {

    private Path destinationPath;
    private boolean useFactoryMethods;
    private boolean embeddedBuilder;
    private Facade facade;
    private Naming naming;

    /**
     * Creates a processor for the given configuration.
     *
     * @param settings configuration from the mojo
     */
    public Processor(MojoSettings settings) {
        this.destinationPath = settings.targetDirectory();
        this.useFactoryMethods = settings.builderCreation().flag();
        this.embeddedBuilder = settings.builderLocation().flag();
        this.naming = settings.naming();
        this.facade = new Facade(settings.productsAreMutable(), settings.naming());

        ofNullable(settings.listAdderVariants()).map(this::toVariants).ifPresent(facade::withAdderVariants);
        ofNullable(settings.listMutatorVariants()).map(this::toVariants).ifPresent(facade::withMutatorVariants);

        StaticJavaParser.getParserConfiguration().setLanguageLevel(settings.javaVersion());
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
     * Parses the java file, generates and stores the class files if desired.
     *
     * @param file location of the product class definition
     */
    public void process(Path file) {
        var order = new Order(file);
        if (order.generateBuilder()) {
            order.setBuilderPath(builderPath(order));
        }
        if (order.needsProcessing()) {
            generate(order);
            sort(order);
            write(order);
        }
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

    private void generate(Order order) {
        var productTypeName = order.productTypeName().orElseThrow(GeneratorException::productClassNotFound);

        if (order.generateBuilder()) {
            generator(order).execute();
        }

        if (order.generateAccessors()) {
            facade.withAccessors(order.productUnit(), productTypeName);
        }
    }

    private Generator generator(Order order) {
        var type = order.productTypeName().get();

        if (order.isRecord()) {
            return embeddedBuilder //
                    ? facade.forRecordEmbedded(order.productUnit(), type)
                    : facade.forRecordSeparate(order.productUnit(), type, order.builderUnit());
        }
        if (embeddedBuilder) {
            return useFactoryMethods //
                    ? facade.withFactoryMethods(order.productUnit(), type)
                    : facade.withConstructors(order.productUnit(), type);
        }
        return useFactoryMethods //
                ? facade.withFactoryMethods(order.productUnit(), type, order.builderUnit())
                : facade.withConstructors(order.productUnit(), type, order.builderUnit());
    }

    private void sort(Order order) {
        var sorter = new Sorter(naming);
        of(order).map(Order::builderUnit).ifPresent(sorter::sort);
        of(order).map(Order::productUnit).ifPresent(sorter::sort);
    }

    private void write(Order order) {
        if (!embeddedBuilder) {
            of(order).map(Order::builderUnit).ifPresent(u -> writeUnit(order.builderPath(), u));
        }
        of(order).map(Order::productUnit).ifPresent(u -> writeUnit(order.productPath(), u));
    }

    private void writeUnit(Path path, CompilationUnit unit) {
        try {
            Util.createParentPath(path);
            Files.write(path, unit.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

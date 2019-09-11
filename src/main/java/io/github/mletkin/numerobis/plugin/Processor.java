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

import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;
import io.github.mletkin.numerobis.generator.ListMutatorVariant;
import io.github.mletkin.numerobis.generator.Sorter;

/**
 * Processes a single java files to generate a builder class.
 * <ul>
 * <li>called by the mojo
 * <li>created with a setup for the generator
 * <li>{@code process} is called for each java file
 * <li>maps mojo settings to generator settings
 * </ul>
 */
public class Processor {
    private String destinationPath;
    private boolean useFactoryMethods;
    private boolean embeddedBuilder;
    private Facade facade;
    private Naming naming;

    /**
     * Creates a processor for the given configuration.
     *
     * @param settings
     *            configuration from the mojo
     */
    public Processor(MojoSettings settings) {
        this.destinationPath = ofNullable(settings.targetDirectory()).map(String::trim).orElse("");
        this.useFactoryMethods = settings.builderCreation().flag();
        this.embeddedBuilder = settings.builderLocation().flag();
        this.naming = settings.naming();
        this.facade = new Facade(settings.productsAreMutable(), settings.naming());

        ofNullable(settings.listAdderVariants()).map(this::toVariants).ifPresent(facade::withAdderVariants);
        ofNullable(settings.listMutatorVariants()).map(this::toVariants).ifPresent(facade::withMutatorVariants);
    }

    private ListMutatorVariant[] toVariants(Enum<?>[] liste) {
        return Stream.of(liste).map(v -> ListMutatorVariant.valueOf(v.name())).toArray(ListMutatorVariant[]::new);
    }

    /**
     * Parse the java file, generates and stores the class files if desired.
     *
     * @param file
     *            location of the Product class definition
     */
    public void process(File file) {
        Order order = new Order(file);
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
        return dest(order.productFile(),
                order.productUnit().getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse(null));
    }

    private void generate(Order order) {
        String productTypeName = order.productTypeName().orElseThrow(GeneratorException::productClassNotFound);

        if (order.generateBuilder()) {
            generator(order, productTypeName).execute();
        }

        if (order.generateAccessors()) {
            facade.withAccessors(order.productUnit(), productTypeName);
        }
    }

    @FunctionalInterface
    private interface Executor {
        void execute();
    }

    private Executor generator(Order order, String type) {
        if (embeddedBuilder) {
            return useFactoryMethods //
                    ? () -> facade.withFactoryMethods(order.productUnit(), type)
                    : () -> facade.withConstructors(order.productUnit(), type);
        }
        return useFactoryMethods //
                ? () -> facade.withFactoryMethods(order.productUnit(), type, order.builderUnit())
                : () -> facade.withConstructors(order.productUnit(), type, order.builderUnit());
    }

    private void sort(Order order) {
        Sorter sorter = new Sorter(naming);
        ofNullable(order.builderUnit()).ifPresent(sorter::sort);
        ofNullable(order.productUnit()).ifPresent(sorter::sort);
    }

    private void write(Order order) {
        if (!embeddedBuilder) {
            ofNullable(order.builderUnit()).ifPresent(u -> writeUnit(order.builderPath(), u));
        }
        ofNullable(order.productUnit()).ifPresent(u -> writeUnit(order.productPath(), u));
    }

    private void writeUnit(Path path, CompilationUnit unit) {
        try {
            Util.createParentPath(path);
            Files.write(path, unit.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path dest(File src, String packagePath) {
        String path = "".equals(destinationPath) //
                ? src.getParent()
                : destinationPath + File.separator + packagePath.replace(".", File.separator);
        String fileName = src.getName().replace(".java", naming.builderClassPostfix() + ".java");
        return new File(path, fileName).toPath();
    }

}

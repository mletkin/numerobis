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

import static io.github.mletkin.numerobis.common.Util.createParentPath;
import static java.util.Optional.of;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.common.Generator;
import io.github.mletkin.numerobis.generator.GeneratorException;
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

    private OrderFactory orderFactory;

    /**
     * Creates a processor for the given configuration.
     *
     * @param settings configuration from the mojo
     */
    public Processor(MojoSettings settings) {
        orderFactory = new OrderFactory(settings);

        StaticJavaParser.getParserConfiguration().setLanguageLevel(settings.javaVersion());
    }

    /**
     * Parses the java file, generates and stores the class files if desired.
     *
     * @param file location of the product class definition
     */
    public void process(Path file) {
        var order = orderFactory.makeOrder(file);
        if (order.needsProcessing()) {
            generate(order);
            sort(order);
            write(order);
        }
    }

    private void generate(Order order) {
        var productTypeName = order.productTypeName().orElseThrow(GeneratorException::productClassNotFound);

        if (order.generateBuilder()) {
            generator(order).execute();
        }

        if (order.generateAccessors()) {
            orderFactory.makeFacade().withAccessors(order.productUnit(), productTypeName);
        }
    }

    private Generator generator(Order order) {
        var facade = orderFactory.makeFacade();

        if (order.isRecord()) {
            return order.embeddedBuilder() //
                    ? facade.forRecordEmbedded(order)
                    : facade.forRecordSeparate(order);
        }
        if (order.embeddedBuilder()) {
            return order.useFactoryMethods() //
                    ? facade.embeddedWithFactoryMethods(order)
                    : facade.embeddedWithConstructors(order);
        }
        return order.useFactoryMethods() //
                ? facade.separateWithFactoryMethods(order)
                : facade.separateWithConstructors(order);
    }

    private void sort(Order order) {
        var sorter = new Sorter(order.naming());
        of(order).map(Order::builderUnit).ifPresent(sorter::sort);
        of(order).map(Order::productUnit).ifPresent(sorter::sort);
    }

    private void write(Order order) {
        if (order.separateBuilder()) {
            of(order).map(Order::builderUnit).ifPresent(u -> writeUnit(order.builderPath(), u));
        }
        of(order).map(Order::productUnit).ifPresent(u -> writeUnit(order.productPath(), u));
    }

    private void writeUnit(Path path, CompilationUnit unit) {
        try {
            createParentPath(path);
            Files.write(path, unit.toString().getBytes());
        } catch (IOException e) {
            throw new MojoFileIOException(e);
        }
    }

}

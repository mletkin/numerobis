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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import io.github.mletkin.numerobis.common.Util;
import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.Facade.Result;
import io.github.mletkin.numerobis.generator.GeneratorException;
import io.github.mletkin.numerobis.generator.Sorter;

/**
 * Processes a single java files to generate a builder class.
 */
public class Processor {

    private String destinationPath;
    private boolean useFactoryMethods;
    private boolean embeddedBuilder;
    private Facade facade;


    /**
     * Creates a processor for the given configuration.
     *
     * @param destinationPath
     *            Where external generated builder classes are stored.
     * @param creation
     *            generate factory methods or constructors
     * @param location
     *            use an embedded or a separate builder class
     * @param productsAreMutable
     *            consider products objects as mutable by default
     */
    public Processor(String destinationPath, BuilderMojo.Creation creation, BuilderMojo.Location location,
            boolean productsAreMutable) {
        this.destinationPath = destinationPath == null ? "" : destinationPath.trim();
        this.useFactoryMethods = creation.flag();
        this.embeddedBuilder = location.flag();
        facade = new Facade(productsAreMutable);
    }

    /**
     * Parse the java file, generates and stores the builder if desired.
     *
     * @param file
     *            location of the Product class definition
     */
    public void process(File file) {
        try {
            CompilationUnit productUnit = parse(file);
            if (Facade.isBuilderWanted(productUnit)) {
                Destination dest = builder(file, productUnit) //
                        .withProductUnit(productUnit) //
                        .withProductPath(file.toPath());
                write(dest, sort(generate(dest)));
            }
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    private Result generate(Destination dest) throws FileNotFoundException {
        Result result = dest.product.getPrimaryTypeName() //
                .map(generator(dest)) //
                .orElseThrow(GeneratorException::productClassNotFound);
        if (Facade.areAccessorsWanted(dest.product)) {
            dest.product.getPrimaryTypeName().ifPresent(type -> facade.withAccessMethods(result.productUnit, type));
        }
        return result;
    }

    private Function<String, Result> generator(Destination dest) {
        if (embeddedBuilder) {
            return useFactoryMethods //
                    ? type -> facade.withFactoryMethods(dest.product, type)
                    : type -> facade.withConstructors(dest.product, type);
        }
        return useFactoryMethods //
                ? type -> facade.withFactoryMethods(dest.product, type, dest.builder)
                : type -> facade.withConstructors(dest.product, type, dest.builder);
    }

    private Result sort(Result result) {
        Sorter sorter = new Sorter();
        ofNullable(result.builderUnit).ifPresent(sorter::sort);
        ofNullable(result.productUnit).ifPresent(sorter::sort);
        return result;
    }

    private static class Destination {
        private CompilationUnit builder;
        private Path builderPath;

        private CompilationUnit product;
        private Path productPath;

        public Destination(CompilationUnit unit, Path path) {
            this.builder = unit;
            this.builderPath = path;
        }

        Destination withProductUnit(CompilationUnit product) {
            this.product = product;
            return this;
        }

        Destination withProductPath(Path productPath) {
            this.productPath = productPath;
            return this;
        }

        Path builderPath() {
            return builderPath;
        }

    }

    private Destination builder(File productFile, CompilationUnit productClass) throws FileNotFoundException {
        Path destinationPath = dest(productFile,
                productClass.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse(null));

        if (destinationPath.toFile().exists()) {
            return new Destination(parse(destinationPath.toFile()), destinationPath);
        }
        return new Destination(new CompilationUnit(), destinationPath);
    }

    private void write(Destination dest, Result result) {
        ofNullable(result.builderUnit).ifPresent(u -> writeResult(dest.builderPath(), u));
        ofNullable(result.productUnit).ifPresent(u -> writeResult(dest.productPath, u));
    }

    private void writeResult(Path path, CompilationUnit unit) {
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
        String fileName = src.getName().replace(".java", "Builder.java");
        return new File(path, fileName).toPath();
    }

    private CompilationUnit parse(File file) throws FileNotFoundException {
        return StaticJavaParser.parse(file);
    }

}

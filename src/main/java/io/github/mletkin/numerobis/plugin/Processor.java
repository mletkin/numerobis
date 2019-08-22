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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import io.github.mletkin.numerobis.generator.Facade;
import io.github.mletkin.numerobis.generator.GeneratorException;
import io.github.mletkin.numerobis.generator.Sorter;

/**
 * Processes single java files to generate builder classes in separate files.
 */
public class Processor {

    private String destinationPath;
    private boolean useFactoryMethods;

    /**
     * Produces a new instance for the given configuration.x
     *
     * @param destinationPath
     *            Where the generated builder classes are stored.
     * @param b
     */
    public Processor(String destinationPath, boolean useFactoryMethods) {
        this.destinationPath = destinationPath == null ? "" : destinationPath.trim();
        this.useFactoryMethods = useFactoryMethods;
    }

    /**
     * Parse a single java file, generates and stores a builder if desired.
     *
     * @param file
     *            location of the Product class definition
     */
    public void process(File file) {
        try {
            CompilationUnit productClass = parse(file);
            if (Facade.isBuilderWanted(productClass)) {
                Destination builder = builder(file, productClass);
                process(productClass, builder);
                writeDestinationUnit(builder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(CompilationUnit productClass, Destination builder) throws FileNotFoundException {
        new Sorter().sort(generate(productClass, builder.unit));
    }

    private CompilationUnit generate(CompilationUnit productClass, CompilationUnit builderClass)
            throws FileNotFoundException {

        Function<String, CompilationUnit> generator = useFactoryMethods //
                ? type -> Facade.withFactoryMethods(productClass, type, builderClass)
                : type -> Facade.withConstructors(productClass, type, builderClass);

        return productClass.getPrimaryTypeName() //
                .map(generator) //
                .orElseThrow(GeneratorException::productClassNotFound);
    }

    private static class Destination {
        private CompilationUnit unit;
        private Path path;

        public Destination(CompilationUnit unit, Path path) {
            this.unit = unit;
            this.path = path;
        }

        Path path() {
            return path;
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

    private Path writeDestinationUnit(Destination dest) throws IOException {
        createPath(dest.path());
        return Files.write(dest.path(), dest.unit.toString().getBytes());
    }

    private void createPath(Path destinationFile) {
        File parent = destinationFile.getParent().toFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
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

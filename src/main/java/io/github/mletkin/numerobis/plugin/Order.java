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
import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Class describing the processing of a single java file.
 */
class Order {

    private boolean generateBuilder;
    private boolean generateAccessors;

    private CompilationUnit builderUnit;
    private Path builderPath;

    private CompilationUnit productUnit;
    private Path productPath;

    /**
     * Creates an order object for a given product class file.
     *
     * @param productClassFile
     *            descriptor of the file with the product class
     */
    public Order(File productClassFile) {
        productPath = productClassFile.toPath();
        productUnit = parse(productClassFile);

        generateBuilder = Facade.isBuilderWanted(productUnit);
        generateAccessors = Facade.areAccessorsWanted(productUnit);
    }

    /**
     * Sets the path descriptor of the builder file.
     * <p>
     * This indicates, that the builder class is generated as a separate file.<br>
     * Parses the builder class or generates a new compilation unit.
     *
     * @param builderPath
     *            object describing the builder file
     */
    void setBuilderPath(Path builderPath) {
        this.builderPath = builderPath;
        if (builderPath.toFile().exists()) {
            builderUnit = parse(builderPath.toFile());
        } else {
            builderUnit = new CompilationUnit();
        }
    }

    File productFile() {
        return productPath.toFile();
    }

    Path productPath() {
        return productPath;
    }

    CompilationUnit builderUnit() {
        return builderUnit;
    }

    CompilationUnit productUnit() {
        return productUnit;
    }

    Optional<String> productTypeName() {
        return productUnit.getPrimaryTypeName();
    }

    Path builderPath() {
        return builderPath;
    }

    boolean generateAccessors() {
        return generateAccessors;
    }

    boolean generateBuilder() {
        return generateBuilder;
    }

    boolean needsProcessing() {
        return generateAccessors || generateBuilder;
    }

    public String unitPackageName() {
        return productUnit.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse(null);
    }

    private CompilationUnit parse(File file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new MojoFileNotFoundException(e);
        }
    }
}

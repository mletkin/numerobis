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

import io.github.mletkin.numerobis.generator.Facade;

/**
 * Class describing the processing of a single java file.
 */
class Order {

    private boolean generateBuilder = false;
    private boolean generateAccessors = false;

    private CompilationUnit builder;
    private Path builderPath;

    private CompilationUnit product;
    private Path productPath;

    public Order(File productClassFile) {
        productPath = productClassFile.toPath();
        product = parse(productClassFile);

        generateBuilder = Facade.isBuilderWanted(product);
        generateAccessors = Facade.areAccessorsWanted(product);
    }

    void setBuilderPath(Path builderPath) {
        this.builderPath = builderPath;
        if (builderPath.toFile().exists()) {
            builder = parse(builderPath.toFile());
        } else {
            builder = new CompilationUnit();
        }
    }

    File productFile() {
        return productPath.toFile();
    }

    Path productPath() {
        return productPath;
    }

    CompilationUnit builderUnit() {
        return builder;
    }

    CompilationUnit productUnit() {
        return product;
    }

    Optional<String> productTypeName() {
        return product.getPrimaryTypeName();
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

    static CompilationUnit parse(File file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new MojoFileNotFoundException(e);
        }
    }
}

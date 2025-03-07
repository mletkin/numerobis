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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import io.github.mletkin.numerobis.annotation.GenerateAccessors;
import io.github.mletkin.numerobis.annotation.GenerateBuilder;

/**
 * Represents the order to process a single java file.
 * <p>
 * The product class for which the builder is made must be the top class of the
 * file. It's not possible to generate a builder for a inner or member class.
 * <p>
 * Maybe the parsing should not be performed in the {@code Order} class.
 */
public class Order {

    private boolean generateBuilder;
    private boolean embeddedBuilder;
    private boolean useFactoryMethods;
    private boolean generateAccessors;
    private boolean productsAreMutable;

    private Naming naming;

    private CompilationUnit builderUnit = new CompilationUnit();
    private Path builderPath;

    private CompilationUnit productUnit;
    private Path productPath;

    /**
     * Creates an order object for a given product class file.
     *
     * @param productClassFile descriptor of the file with the product class
     */
    public Order(Path productClassFile, Naming naming, boolean embedded, boolean useFactoryMethods,
            boolean productsAreMutable) {
        productPath = productClassFile;
        productUnit = parse(productPath);
        this.naming = naming;
        this.embeddedBuilder = embedded;
        this.useFactoryMethods = useFactoryMethods;
        this.productsAreMutable = productsAreMutable;

        generateBuilder = isBuilderWanted(productUnit);
        generateAccessors = areAccessorsWanted(productUnit);
    }

    public Order useBuildUnit(CompilationUnit bcu) {
        this.builderUnit = bcu;
        return this;
    }

    /**
     * Tests whether a class wants a builder.
     *
     * @param  sourceUnit compilation unit with the potential product class
     * @return            {@code true} when a builder class shall be built
     */
    private static boolean isBuilderWanted(CompilationUnit sourceUnit) {
        return sourceUnit.findAll(TypeDeclaration.class).stream() //
                .filter(t -> t instanceof ClassOrInterfaceDeclaration || t instanceof RecordDeclaration) //
                .anyMatch(c -> c.isAnnotationPresent(GenerateBuilder.class));
    }

    /**
     * Test whether a class wants accessors
     *
     * @param  sourceUnit compilation unit with the potential product class
     * @return            {@code true} when accessurs should be generated
     */
    private static boolean areAccessorsWanted(CompilationUnit sourceUnit) {
        return sourceUnit.findAll(ClassOrInterfaceDeclaration.class).stream() //
                .anyMatch(c -> c.isAnnotationPresent(GenerateAccessors.class));
    }

    /**
     * Checks whether the product is a record.
     *
     * @return {@code true} if the product is a record
     */
    public boolean isRecord() {
        return productUnit.findAll(RecordDeclaration.class).stream() //
                .anyMatch(c -> c.isAnnotationPresent(GenerateBuilder.class));
    }

    /**
     * Sets the path descriptor of the builder file.
     * <p>
     * This indicates, that the builder class is generated as a separate file.<br>
     * Parses the builder class or generates a new compilation unit.
     *
     * @param builderPath object describing the builder file
     */
    public void setBuilderPath(Path builderPath) {
        this.builderPath = builderPath;
        this.builderUnit = Files.exists(builderPath) //
                ? parse(builderPath)
                : new CompilationUnit();
    }

    /**
     * Returns the locator of the product class file.
     *
     * @return a {@lonk Path}-Object
     */
    public Path productPath() {
        return productPath;
    }

    public CompilationUnit builderUnit() {
        return builderUnit;
    }

    public CompilationUnit productUnit() {
        return productUnit;
    }

    public Optional<String> productTypeName() {
        return productUnit.getPrimaryTypeName();
    }

    public String productType() {
        return productUnit.getPrimaryTypeName().orElse(null);
    }

    public Path builderPath() {
        return builderPath;
    }

    public boolean generateAccessors() {
        return generateAccessors;
    }

    public boolean generateBuilder() {
        return generateBuilder;
    }

    public boolean embeddedBuilder() {
        return embeddedBuilder;
    }

    public boolean separateBuilder() {
        return !embeddedBuilder;
    }

    public boolean useFactoryMethods() {
        return useFactoryMethods;
    }

    public boolean productsAreMutable() {
        return productsAreMutable;
    }

    public boolean needsProcessing() {
        return generateAccessors || generateBuilder;
    }

    public String unitPackageName() {
        return productUnit.getPackageDeclaration().map(PackageDeclaration::getNameAsString).orElse(null);
    }

    public Naming naming() {
        return naming;
    }

    private CompilationUnit parse(Path file) {
        try {
            return StaticJavaParser.parse(file);
        } catch (IOException e) {
            throw new MojoFileIOException(e);
        }
    }
}

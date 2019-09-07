package io.github.mletkin.numerobis.plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;

import io.github.mletkin.numerobis.generator.Facade;

class Order {
    private boolean generateBuilder = false;
    private boolean generateAccessors = false;

    private CompilationUnit builder;
    private Path builderPath;

    private CompilationUnit product;
    private Path productPath;

    public Order(File productClassFile) {
        productPath = productClassFile.toPath();
        product = PluginUtil.parse(productClassFile);

        generateBuilder = Facade.isBuilderWanted(product);
        generateAccessors = Facade.areAccessorsWanted(product);
    }

    void setBuilderPath(Path builderPath) {
        this.builderPath = builderPath;
        if (builderPath.toFile().exists()) {
            builder = PluginUtil.parse(builderPath.toFile());
        } else {
            builder = new CompilationUnit();
        }
        generateBuilder = true;
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
}
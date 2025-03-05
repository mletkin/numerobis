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

import static io.github.mletkin.numerobis.common.Util.stream;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import io.github.mletkin.numerobis.annotation.GenerateAdder;
import io.github.mletkin.numerobis.annotation.GenerateListMutator;

/**
 * Entry point for the generator plugin.
 * <ul>
 * <li>handles maven plugin specific stuff
 * <li>gathers configuration settings from the pom.xml
 * <li>walks through the source file tree
 * <li>calls the generator for each java file
 * <li>dumps configuration to the log
 * </ul>
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BuilderMojo extends AbstractMojo {

    /**
     * Possible variants for creation of builder instances
     */
    enum Creation {
        CONSTRUCTOR, FACTORY;

        boolean flag() {
            return this == FACTORY;
        }
    }

    /**
     * Possible location of the builder class
     */
    enum Location {
        EMBEDDED, SEPARATE;

        boolean flag() {
            return this == EMBEDDED;
        }
    }

    /**
     * The directories containing the sources to be processed.
     */
    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = false, required = true)
    private List<String> compileSourceRoots;

    /**
     * Where the generated builder classes are stored.
     * <p>
     * The packages are converted to file paths.
     */
    @Parameter
    private String targetDirectory;

    /**
     * How instances of the builder class shall be created
     */
    @Parameter(defaultValue = "FACTORY")
    private Creation builderCreation;

    /**
     * Where the builder class shall be created.
     */
    @Parameter(defaultValue = "EMBEDDED")
    private Location builderLocation;

    /**
     * Whether product instances may be changes after creation.
     */
    @Parameter(defaultValue = "false")
    private boolean productsAreMutable;

    /**
     * Java version to be recognized by the parser.
     */
    @Parameter(defaultValue = "JAVA_17")
    private LanguageLevel javaVersion;

    /**
     * Naming of builder components.
     */
    @Parameter
    private Naming naming = Naming.DEFAULT;

    /**
     * Variants of adder methods to create in the builder.
     */
    @Parameter
    private List<GenerateAdder.Variant> listAdderVariants;

    /**
     * Variants of list mutator methods to create in the builder.
     */
    @Parameter
    private List<GenerateListMutator.Variant> listMutatorVariants;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        logConfiguration();
        compileSourceRoots.forEach(this::walk);
    }

    private void logConfiguration() {
        getLog().info("target directory: " + targetDirectory);
        getLog().info("source directories: ");
        stream(compileSourceRoots).forEach(getLog()::info);
        getLog().info("builder creation: " + builderCreation);
        getLog().info("builder location: " + builderLocation);
        getLog().info("products are " + (productsAreMutable ? "mutable" : "immutable") + " by default");
        getLog().info("list adder variants: ");
        stream(listAdderVariants).map(GenerateAdder.Variant::name).forEach(getLog()::info);
        getLog().info("list mutator variants: ");
        stream(listMutatorVariants).map(GenerateListMutator.Variant::name).forEach(getLog()::info);
        getLog().info("naming settings");
        ofNullable(naming).map(Object::toString).ifPresent(getLog()::info);
    }

    /**
     * Recursivly walks through the directory and processes all java files.
     *
     * @param directory directory to traverse
     */
    private void walk(String directory) {
        try (var paths = Files.walk(Paths.get(directory))) {
            paths //
//                    .peek(f -> getLog().info(f.toString())) //
                    .filter(Files::exists) //
                    .filter(f -> f.getFileName().toString().endsWith(".java")) //
                    .peek(f -> getLog().info(f.toString())) //
                    .forEach(new Processor(processorSettings())::process);
        } catch (IOException e) {
            throw new MojoFileIOException(e);
        }
    }

    /**
     * Collect the processor configuration.
     *
     * @return Settings object
     */
    private MojoSettings processorSettings() {
        return new MojoSettings.Builder() //
                .withTargetDirectory(targetDirectory) //
                .withBuilderCreation(builderCreation) //
                .withBuilderLocation(builderLocation) //
                .withProductsAreMutable(productsAreMutable) //
                .withJavaVersion(javaVersion) //
                .withListAdderVariants(stream(listAdderVariants).toArray(GenerateAdder.Variant[]::new)) //
                .withListMutatorVariants(stream(listMutatorVariants).toArray(GenerateListMutator.Variant[]::new)) //
                .withNamingSettings(naming) //
                .build();
    }

}

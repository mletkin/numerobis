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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class BuilderMojo extends AbstractMojo {

    enum Creation {
        CONSTRUCTOR, FACTORY;
        boolean flag() {
            return this == FACTORY;
        }
    }

    enum Location {
        EMBEDDED, SEPARATE;
        boolean flag() {
            return this == EMBEDDED;
        }
    }

    /**
     * The directories containing the sources to be processed.
     */
    @Parameter(defaultValue = "${project.compileSourceRoots}", readonly = true, required = true)
    private List<String> compileSourceRoots;

    /**
     * Where the generated builder classes are stored.
     * <p>
     * The packages are converted to file paths.
     */
    @Parameter(defaultValue = " ", readonly = true, required = true)
    private String targetDirectory;

    @Parameter(defaultValue = "FACTORY")
    private Creation builderCreation;

    @Parameter(defaultValue = "EMBEDDED")
    private Location builderLocation;

    @Parameter
    private boolean productsAreMutable;

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
    }

    /**
     * Recursivly walks through the directory and processes all java files.
     *
     * @param directory
     *            directory to traverse
     */
    private void walk(String directory) {
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths //
                    .map(Path::toFile) //
                    .filter(File::isFile) //
                    .filter(f -> f.getName().endsWith(".java")) //
                    .forEach(new Processor(targetDirectory, //
                            builderCreation, //
                            builderLocation,
                            productsAreMutable//
                    )::process);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

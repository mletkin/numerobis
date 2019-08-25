# Maven Builder Plugin
A simple maven plugin for the generation of builder classes.

## Nomenclature
There is no standard for the wording of builder components.   
The following terms are used throughout the code and the documentation.

The (generated) class that builds the object instances is the **builder**.
The object it builds is the **product**.
The class which defines the products is the **product class**.
The builder methods of the builder that set the values for the product
are the **with methods** (for lack of a better word).
The method in the builder that provides the **product** is the **build method**. 

## Usage
Add the following to the plugin section of your pom.xml
```v
<plugin>
    <groupId>io.github.mletkin</groupId>
    <artifactId>builder-maven-plugin</artifactId>
    <version>${version.builder.plugin}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
Add the annotation `@WithBuilder`to each class for which you want to create a builder.
Builder code is generated in the `generate-sources` phase of the maven build. 

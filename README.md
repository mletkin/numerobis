# Maven Builder Generator Plugin
A simple maven plugin for the generation of builder classes.

## Nomenclature
There is no standard for the wording of builder components.   
The following terms are used throughout the code and the documentation.

The (generated) class that builds the object instances is the **builder**.
The object it builds is the **product**.
The class which defines the product is the **product class**.
A method of the builder that sets the value of a product field is a **mutator**.
The method in the builder that provides the **product** is the **build method**.
A method of the product class that returns the content of a field is an **accessor**.

I avoided the terms **getter** and **setter** because of the special meaning they have in the JavaBeans context.

## Usage
Add the following to the plugin section of your pom.xml
```v
<plugin>
    <groupId>io.github.mletkin</groupId>
    <artifactId>builder-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
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

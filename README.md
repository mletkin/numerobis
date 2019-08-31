# Maven Builder Generator Plugin
This is a simple maven plugin that generates builder classes for classes that have the appropriate annotation.The builders are generated during the generate-sources phase of the maven build. The current version is restricted to the use of default settings. Customization is currently in a proof of concept state.

The generator creates the builders as a fully functional skeleton. It can be used "as is" without manual changes. On the other hand is it possible to change and extend the generated builder. Methods can be added and implementation  can be changed. When the generator runs a second time, missing methods will be created existing methods will not be changeed. When the builder class was deleted or renamed a new one will be created.

## Nomenclature
There is no standard for the wording of builder components.   
The following terms are used throughout the code and the documentation.

The (generated) class that builds the object instances is the **builder**.
The object it builds is the **product**.
The class which defines the product is the **product class**.
A method of the builder that sets the value of a product field is a **mutator**.
A mutator that adds something (e.g. an Object to a Collection) is an **adder**.
The method in the builder that provides the **product** is the **build method**.
A method of the product class that returns the content of a field is an **accessor**.

I avoided the terms **getter** and **setter** because of the special meaning they have in the JavaBeans context.

## Usage
Add the following to the plugin section of your pom.xml
```
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
Add the annotation `@GenerateBuilder`to each class for which you want to create a builder.
Builder code is generated in the `generate-sources` phase of the maven build. 

## Configuration
The following settings may be customized through Maven configuration
### builder creation
To use the generated builder it is necessary to create builder instances. The generator provides two alternatives.
The builder creates the object of the product class during builder creation. For this reason the builder generator must provide a constructor or factory method for each product constrcutor.  
#### Constructor
For each constructor in the production class a constructor in the builder class is created.
```
<configuration>
    <builderCreation>CONSTRUCTOR</builderCreation>
<configuration>
```
#### Factory
For each constructor in the production class a static factory method the builder class is created.
This is the default.
```
<configuration>
    <builderCreation>FACTORY</builderCreation>
<configuration>
```
### builder location
The builder can be generated as separate class or as member class of the product class.
#### embedded
The builder class is generated as member class (sometimes called "static inner class") of the product class.
This is the default.
```
<configuration>
    <builderLocation>EMBEDDED</builderLocation>
<configuration>
```
#### separate
The builder class is generated as a separate class. It is located logically in the same package as the product class an physically in the same directory in the file system.   
```
<configuration>
    <builderLocation>SEPARATE</builderLocation>
<configuration>
```
### compileSourceRoots
A list property with the directories that contain the production classes. The directories will be searched recursively.
The default is ```${project.compileSourceRoots}```
### targetDirectory
The directory in which the generated builder classes are stored. File paths are created for the packages. The parameter is only relevant if the builders are created as separate classes.
The default is the generation in the same directory as the product class.
### products are mutable by default
If set to true product classes are considered mutable. For the modification of product class instances a constructor (or factory method) will be created in the builder class. This modificaton method accepts a product class instance as parameter.
The default value is **false**. This means that the product objetcs are considered immutable and no method for modification is generated.
```
<configuration>
    <productsAreMutable>true</productsAreMutable>
<configuration>
```
## Annotations
Most of the behavior of the builder generator is controlled through annotations.
The generator will stick to the annotation concept. The names might change and options may be set via annotation parameters. 
All annotations are located in the package ```io.github.mletkin.numerobis.annotation```
### GenerateBuilder
Used on product classes.
This is the most important annotation. Only classes annotated with ```@GenerateBuilder``` will be processed.
### GenerateAccessors
Used on product classes.
For every field in the product class an accessor will be generated. Accessors have the same name as the field.
### GenerateMutator
Mutator generation is the default. This annotation is a means to specify a custom name for the mutator.
### Ignore
Used on product fields.
Field annotated with ```@Ignore``` are ignored by the generator, no mutators are generated.
### Mutable and Immutable
Used on product classes.
Overrides the default setting for "product classes are immutable/mutable by default".



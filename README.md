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

# Creational Patterns:

Typical object creation in programming languages could be deficient for several use cases, Creational patterns are software design patterns that were invented to overcome these limitations

## Typical disadvantages with object creation in java:

- Code Bloat:

  Imagine a scenario where you create different types of toy objects in your software, this would mean initializing 10 different objects in java.

  Instead if you were to use a Factory pattern, you could create a toy factory that creates the ten different toys for you, this would still need ten lines of code - even if I use a factory I would still need ten lines of code - so TODO: think about why a factory is better here
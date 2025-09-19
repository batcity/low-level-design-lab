# low-level-design-lab  

A personal collection of **low-level design explorations, UML diagrams, and code skeletons** as I practice building clean, extensible, and maintainable systems.  

Each folder focuses on a specific problem (e.g., Parking Lot, BookMyShow, Chess, Ride-Sharing), documenting the **requirements, object modeling, class interactions, design choices, and key learnings**.  

📌 **Goal:** Build a structured knowledge base of **OOP principles, design patterns, and code-level reasoning** that I can reuse for interviews and real-world development.  

⚡ **Note:** These designs are for **learning and discussion** purposes. I’ve refined them through practice, reading, and AI-assisted reasoning.  

---

## 📂 Problem Write-up Template 

Each problem write-up follows the same template for consistency:  

1. **Problem statement** – What we’re designing and the scope.  
2. **Requirements** – Functional and non-functional needs.  
3. **Key entities & responsibilities** – Core classes/objects and their roles.  
4. **Class diagram (UML)** – Relationships between entities.  
5. **Core workflows** – Step-by-step object interactions.  
6. **Code skeleton (Java/Python/etc.)** – Core classes and methods.  
7. **Design principles & patterns** – SOLID, GRASP, and relevant design patterns used.  
8. **Trade-offs & alternatives** – Other possible designs and pros/cons.  
9. **Extensions / next steps** – Features or improvements beyond v1.  

---

## 🔍 Optional Add-ons for Advanced Practice  

These are not typically required in interviews, but useful for deepening understanding and building a richer reference library:  

10. **Use-case Diagram** – Map actors (e.g., Member, Librarian) to top-level actions like search, checkout, reserve.  
11. **Activity or Sequence Diagram** – Show the detailed steps of a core workflow (e.g., how a book checkout flows across objects).  
12. **CRC Cards (Brainstorming Tool)** – For each class:  
   - **Name**: Class name  
   - **Responsibilities**: What it does  
   - **Collaborators**: Other classes it interacts with  

---

## 🚀 Implementations of Common Design Patterns

This section contains **Java implementations of widely used object-oriented design patterns**, organized by category. Each pattern includes an example Java file and a README for notes.  

### Polymorphism
- **[Method Overriding](./implementations/polymorphism/MethodOverriding.java)** – Demonstrates Java method overriding with `Car` and `Mercedes`.

### Creational Patterns
- **[Singleton](./implementations/creational_patterns/Singleton/Singleton.java)** – Ensures a single instance of a class.  
- **[Factory](./implementations/creational_patterns/Factory/Factory.java)** – Creates objects without exposing the instantiation logic.  
- **[Abstract Factory](./implementations/creational_patterns/AbstractFactory/AbstractFactory.java)** – Produces families of related objects.  
- **[Builder](./implementations/creational_patterns/Builder/Builder.java)** – Step-by-step construction of complex objects.  
- **[Prototype](./implementations/creational_patterns/Prototype/Prototype.java)** – Clones existing objects efficiently.  

### Structural Patterns
- **[Adapter](./implementations/structural_patterns/Adapter/Adapter.java)** – Converts one interface to another.  
- **[Decorator](./implementations/structural_patterns/Decorator/Decorator.java)** – Adds responsibilities to objects dynamically.  
- **[Composite](./implementations/structural_patterns/Composite/Composite.java)** – Treats individual objects and compositions uniformly.  
- **[Facade](./implementations/structural_patterns/Facade/Facade.java)** – Simplifies access to complex subsystems.  
- **[Proxy](./implementations/structural_patterns/Proxy/Proxy.java)** – Controls access to objects.  
- **[Bridge](./implementations/structural_patterns/Bridge/Bridge.java)** – Decouples abstraction from implementation.  

### Behavioral Patterns
- **[Observer](./implementations/behavioral_patterns/Observer/Observer.java)** – Implements publish-subscribe pattern.  
- **[Strategy](./implementations/behavioral_patterns/Strategy/Strategy.java)** – Enables interchangeable algorithms.  
- **[Command](./implementations/behavioral_patterns/Command/Command.java)** – Encapsulates requests as objects.  
- **[State](./implementations/behavioral_patterns/State/State.java)** – Alters object behavior based on state.  
- **[Template Method](./implementations/behavioral_patterns/TemplateMethod/TemplateMethod.java)** – Defines algorithm skeleton with customizable steps.  
- **[Iterator](./implementations/behavioral_patterns/Iterator/Iterator.java)** – Sequential access to elements without exposing structure.  
- **[Chain of Responsibility](./implementations/behavioral_patterns/ChainOfResponsibility/ChainOfResponsibility.java)** – Passes request along a chain of handlers.  
- **[Mediator](./implementations/behavioral_patterns/Mediator/Mediator.java)** – Centralizes communication between objects.  
- **[Memento](./implementations/behavioral_patterns/Memento/Memento.java)** – Captures and restores object state.  

---

## 🚀 Why this repo?  

- Strengthen **object-oriented design intuition**.  
- Practice **clean, reusable, and testable code structures**.  
- Build a **reusable library** of design blueprints and patterns for interviews and real-world coding.

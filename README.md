# low-level-design-lab  

A personal collection of **low-level design explorations, UML diagrams, and code skeletons** as I practice building clean, extensible, and maintainable systems.  

Each folder focuses on a specific problem (e.g., Parking Lot, BookMyShow, Chess, Ride-Sharing), documenting the **problem context, object modeling, class interactions, design choices, and key learnings**.  

📌 **Goal:** Build a structured knowledge base of **OOP principles, design patterns, and code-level reasoning** for interviews and real-world development.  

**Note:** These designs are for **learning and discussion** purposes. They are refined through practice, reading, and AI-assisted reasoning.  

---

## 📂 Problem Write-up Template 

Each problem follows the same template for consistency:

### 1. Problem Statement & Requirements
- **Description:** What we’re designing and the scope.  
- **Functional Requirements:** Core behaviors the system must support.  
- **Non-Functional Requirements:** Scalability, concurrency, reliability, and extensibility expectations.  

### 2. Key Entities & Responsibilities
- Core classes/objects and their roles in the system.  

### 3. Class Diagram (UML)
- Relationships between entities.

### 4. Core Workflows
- Step-by-step object interactions for primary operations.

### 5. Code Skeleton (Java/Python/etc.)
- Core classes and methods.

### 6. Design Principles & Patterns
- SOLID, GRASP, and relevant design patterns applied.  

### 7. Trade-offs & Alternatives
- Other possible designs and pros/cons.  

### 8. Extensions / Next Steps
- Features or improvements beyond the first version.  

---

## 🔍 Optional Add-ons for Advanced Practice
These are not typically required in interviews but deepen understanding:

- **Use-case Diagram:** Map actors (e.g., Member, Librarian) to top-level actions like search, checkout, reserve.
- **Activity or Sequence Diagram:** Show the detailed steps of a core workflow (e.g., how a book checkout flows across objects).
- **CRC Cards (Brainstorming Tool):** For each class, list:  
  - **Name:** Class name  
  - **Responsibilities:** What it does
  - **Collaborators:** Other classes it interacts with

---

## Implementations of Common Design Patterns

This section contains **Java implementations of widely used object-oriented design patterns**, organized by category. Each pattern includes an example file and notes.

### Polymorphism
- **[Method Overriding](./implementations/ood_concepts/polymorphism/MethodOverriding.java)** – Demonstrates method overriding.

### Creational Patterns
- **[Singleton](./implementations/design_patterns/creational_patterns/Singleton/Singleton.java)** – Ensures a single instance of a class.  
- **[Factory](./implementations/design_patterns/creational_patterns/Factory/Factory.java)** – Creates objects without exposing instantiation logic.  
- **[Abstract Factory](./implementations/design_patterns/creational_patterns/AbstractFactory/AbstractFactory.java)** – Produces families of related objects.  
- **[Builder](./implementations/design_patterns/creational_patterns/Builder/Builder.java)** – Step-by-step construction of complex objects.  
- **[Prototype](./implementations/design_patterns/creational_patterns/Prototype/Prototype.java)** – Clones existing objects efficiently.  

### Structural Patterns
- **[Adapter](./implementations/design_patterns/structural_patterns/Adapter/AdapterDemo.java)** – Converts one interface to another.  
- **[Decorator](./implementations/design_patterns/structural_patterns/Decorator/Decorator.java)** – Adds responsibilities to objects dynamically.  
- **[Composite](./implementations/design_patterns/structural_patterns/Composite/Composite.java)** – Treats individual objects and compositions uniformly.  
- **[Facade](./implementations/design_patterns/structural_patterns/Facade/Facade.java)** – Simplifies access to complex subsystems.  
- **[Proxy](./implementations/design_patterns/structural_patterns/Proxy/Proxy.java)** – Controls access to objects.  
- **[Bridge](./implementations/design_patterns/structural_patterns/Bridge/Bridge.java)** – Decouples abstraction from implementation.  

### Behavioral Patterns
- **[Observer](./implementations/design_patterns/behavioral_patterns/Observer/Observer.java)** – Implements publish-subscribe pattern.  
- **[Strategy](./implementations/design_patterns/behavioral_patterns/Strategy/Strategy.java)** – Enables interchangeable algorithms.  
- **[Command](./implementations/design_patterns/behavioral_patterns/Command/Command.java)** – Encapsulates requests as objects.  
- **[State](./implementations/design_patterns/behavioral_patterns/State/State.java)** – Alters object behavior based on state.  
- **[Template Method](./implementations/design_patterns/behavioral_patterns/TemplateMethod/TemplateMethod.java)** – Defines algorithm skeleton with customizable steps.  
- **[Iterator](./implementations/design_patterns/behavioral_patterns/Iterator/Iterator.java)** – Sequential access to elements without exposing structure.  
- **[Chain of Responsibility](./implementations/design_patterns/behavioral_patterns/ChainOfResponsibility/ChainOfResponsibility.java)** – Passes requests along a chain of handlers.  
- **[Mediator](./implementations/design_patterns/behavioral_patterns/Mediator/Mediator.java)** – Centralizes communication between objects.  
- **[Memento](./implementations/design_patterns/behavioral_patterns/Memento/Memento.java)** – Captures and restores object state.

---

## Why this Repo?

- Strengthen **object-oriented design intuition**.  
- Practice **clean, reusable, and testable code structures**.  
- Build a **reusable library** of design blueprints and patterns for interviews and real-world coding.

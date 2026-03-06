# Design principles and patterns used:

## Design principles:

I'm using the following principles in the parking lot implementation

- [Single Responsibility Principle](../implementations/design_principles/SOLID/single-responsibility-principle/README.md)

Each class in the system has a clear responsibility.

Examples:

- `ParkingLot` manages parking spot availability.
- `ParkingService` manages the lifecycle of parking sessions.
- `ParkingSession` represents a single parking event.

## Design patterns

I'm not using any classic Design patterns
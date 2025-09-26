# Key Entities & Responsibilities

- User → Represents the person parking the vehicle; initiates and ends parking sessions.
- Vehicle → Represents the vehicle being parked; linked to a user.
- ParkingSpot → Represents an individual parking space; tracks availability (occupied or free).
-  ParkingSession → Represents the lifecycle of a parking event; begins when a vehicle is parked and ends when it exits the lot; used for billing/ticketing.
- ParkingLot → Container for parking spots, possibly organized into multiple floors/sections; responsible for assigning and managing available spots.
# Design a Parking Lot System

## Description
Design a parking lot management system that allows vehicles to park, leave, and pay for parking. The system should track parking spot availability and calculate fees based on parking duration.

## Functional Requirements

### Vehicle Parking
- Park a vehicle in an available spot assigned by the system.
- Remove a vehicle from the lot when it leaves.

### Availability Tracking
- Notify if the parking lot is full.
- Track which spots are occupied and which are available.

### Payment Calculation
- Calculate parking fee based on time parked.

## Non-Functional Requirements
- **Scalability:** The system should support multiple floors or sections.
- **Concurrency:** Multiple vehicles may arrive/leave simultaneously.
- **Reliability:** Prevent double-booking of the same parking spot.
- **Extensibility:** Easy to add new vehicle types or payment strategies.

## Optional Features / Extensions
- Reserve a parking spot in advance.
- Support monthly passes or subscriptions.
- Generate reports on parking lot usage.
- Integration with sensors or mobile apps.

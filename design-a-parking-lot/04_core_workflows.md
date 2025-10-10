# Parking System – Core Workflows (Concurrent Version) - WIP - TODO: Refine this:

## Overview

This parking system supports **high concurrency**, ensuring:

- **Atomic reservation** of parking spots.
- **Single active session per user**.
- Thread-safe operations for starting, ending, and querying sessions.
- Scalable to multiple threads or distributed nodes.

Key components:

| Component        | Responsibility                                     |
| ---------------- | ------------------------------------------------- |
| `ParkingService` | Manages user sessions (`currentParkingSessions`)  |
| `ParkingLot`     | Tracks parking spots (`parkingSpotMap`)           |
| `ParkingSession` | Records user, vehicle, spot, start/end times      |

Thread-safe data structures used:

- `ConcurrentHashMap` – for `currentParkingSessions` and `parkingSpotMap`.
- `AtomicBoolean` – for atomic spot reservation and release.
- Optional `ConcurrentLinkedQueue` – for fast reads of available spots.

---

## 1. Start a Parking Session

**Description:** Allocates a parking spot to a user and starts a parking session.

**Workflow:**

1. **Check for existing session**
   - Atomically verify that the user does not already have an active session using `ConcurrentHashMap.compute`.
   - If a session exists, return an error: `User already has an active parking session`.

2. **Find and reserve an available spot**
   - Iterate over `parkingSpotMap` (or a cached available-spots queue).
   - Use `AtomicBoolean.compareAndSet(true, false)` to **atomically reserve** a spot.
   - If no spot is available, return an error: `No parking spots available`.

3. **Create and store the parking session**
   - Generate a `ParkingSession` with user, vehicle, spot ID, and start time.
   - Store the session in `currentParkingSessions` **atomically**.

4. **Return the session to the client**

**Concurrency Guarantees:**

- Only one session per user is allowed.
- Only one thread can reserve each parking spot.
- No global locks; high throughput even with many users.

---

## 2. End a Parking Session

**Description:** Ends the parking session and frees the allocated parking spot.

**Workflow:**

1. **Retrieve the current session**
   - Atomically fetch the session for the user using `ConcurrentHashMap.computeIfPresent`.
   - If no session exists, return an error: `No active parking session`.

2. **Mark the session as ended**
   - Set `endTime` to the current timestamp.

3. **Release the parking spot**
   - Atomically set the `AtomicBoolean` in `parkingSpotMap` to `true`.

4. **Remove the session from active sessions**
   - Returning `null` in `computeIfPresent` removes the session atomically.

5. **Return success to the client**

**Concurrency Guarantees:**

- No two threads can end the same session simultaneously.
- Spot release is atomic, preventing double-booking.

---

## 3. Get Current Parking Session

**Description:** Retrieves the user’s active parking session.

**Workflow:**

1. Fetch the session from `currentParkingSessions` using `ConcurrentHashMap.get(userId)`.
2. If no session exists, return `null` or an appropriate response.

**Concurrency Guarantees:**

- Lock-free, thread-safe read.
- Immediate consistency for active sessions.

---

## 4. Get Available Parking Spots

**Description:** Retrieves a list of currently available spots.

**Options:**

1. **Iterate `parkingSpotMap`:**
   ```java
   for (Map.Entry<SpotId, AtomicBoolean> entry : parkingSpotMap.entrySet()) {
       if (entry.getValue().get()) {
           availableSpots.add(entry.getKey());
       }
   }
    ````

* Simple, fully thread-safe.
* May be slightly slower if the parking lot is very large.

2. **Cached list of available spots (optional optimization):**

   * Maintain a `ConcurrentLinkedQueue` of available spots.
   * On reservation/release, atomically update the queue.
   * Enables very fast reads without iterating the map.

**Concurrency Guarantees:**

* Spot availability always reflects atomic reservations/releases.
* Cached queue may be slightly stale under extreme concurrency but is acceptable for most queries.

---

## 5. Additional High-Concurrency Recommendations

1. **Thread-safe structures:**

   * `ConcurrentHashMap` for sessions and spots.
   * `AtomicBoolean` for spot reservation.

2. **Optimistic concurrency / retries:**

   * If spot reservation fails due to concurrent allocation, retry the next available spot.

3. **Distributed coordination (for multi-node systems):**

   * Use distributed locks (Redis SETNX, Zookeeper) to reserve spots across nodes.
   * Store `currentParkingSessions` in a distributed cache to synchronize sessions.

4. **Scalability:**

   * Lock-free reads and atomic updates allow high throughput.
   * Minimal contention per user and per spot.


✅ **Benefits of this design:**

* Prevents **double-booking** of spots.
* Guarantees **single active session per user**.
* Thread-safe, lock-minimized design for **highly concurrent environments**.
* Optionally supports **fast queries for available spots**.
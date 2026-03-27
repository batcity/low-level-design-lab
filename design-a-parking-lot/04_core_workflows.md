# Parking System – Core Workflows (Concurrent Version):

## Overview

This parking system supports **high concurrency**, ensuring:

- **Atomic reservation** of parking spots — supports optimistic locking (CAS/version checks) under normal load and switches to pessimistic locking (ReentrantLocks) under high per-spot contention, ensuring conflict-free reservations.
- Contention is tracked per spot by measuring **failed CAS attempts** (optimistic path) or **threads blocked on locks** (pessimistic path), allowing adaptive locking strategies.

TODO: I'm only using optimistic locking in the parking lot implementation, gotta change the code to switch to pessimistic locks when there's high contention

- **Single active session per user**.
- Thread-safe operations for starting, ending, and querying sessions.
- Scalable to multiple threads or distributed nodes.

Key components:

| Component        | Responsibility                                     |
| ---------------- | ------------------------------------------------- |
| `ParkingService` | Manages user sessions |
| `ParkingLot`     | Tracks parking spots |
| `ParkingSession` | Records user, vehicle, spot, start/end times      |

---

## Workflows:

## 1. Start a Parking Session

**Description:** Allocates a parking spot to a user and starts a parking session.

**Workflow:**

1. **Check for existing session**
   - Atomically verify that the user does not already have an active session
   - If a session exists, return an error: `User already has an active parking session`.

2. **Find and reserve an available spot**

3. **Create and store the parking session**
   - Generate a `ParkingSession` with user, vehicle, spot ID, and start time.
   - Store the session in `currentParkingSessionsByUserId` **atomically**.

4. **Return the session ID to the client**

**Concurrency Guarantees:**

- Only one session per user is allowed.
- Only one thread can reserve each parking spot.
- No global locks; high throughput even with many users.

---

## 2. End a Parking Session

**Description:** Ends the parking session and frees the allocated parking spot.

**Workflow:**

1. **Retrieve the current session**
   - Atomically fetch the session for the user.
   - If no session exists, return an error: `No active parking session`.

2. **Mark the session as ended**
   - Set `endTime` to the current timestamp.

3. **Release the parking spot**

4. **Remove the session from active sessions**

5. **Return success to the client**

**Concurrency Guarantees:**

- No two threads can end the same session simultaneously.
- Spot release is atomic, preventing double-booking.

---

## Workflows 3 and 4 are Future Improvements:

## 3. Get Current Parking Session

**Description:** Retrieves the user’s active parking session.

**Workflow:**

1. Fetch the user's current session.
2. If no session exists, return `null` or an appropriate response.

**Concurrency Guarantees:**

- Lock-free, thread-safe read.
- Immediate consistency for active sessions.

---

## 4. Get Available Parking Spots

**Description:** Retrieves a list of currently available spots.

1. This is fairly straightforward, the list of available parking spots from the ParkingLot class needs to be returned to the user

**Concurrency Guarantees:**

* Spot availability always reflects atomic reservations/releases.

---

✅ **Benefits of this design:**

* Prevents **double-booking** of spots.
* Guarantees **single active session per user**.
* Thread-safe, lock-minimized design for **highly concurrent environments**.
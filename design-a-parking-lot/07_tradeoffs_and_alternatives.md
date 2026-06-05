# Tradeoffs and alternatives:

## 1. Lock-Free Concurrent Collections vs Explicit Locks

In the current design, I use Java's thread-safe concurrent collections (`ConcurrentLinkedQueue` and `ConcurrentHashMap`) rather than explicit locking.

### Benefits

- Simple implementation.
- High throughput under normal workloads.
- No risk of threads blocking while waiting for locks.
- Atomic operations such as `poll()` and `putIfAbsent()` naturally prevent double-booking of parking spots and duplicate parking sessions.

### Tradeoffs

- During periods of heavy contention (for example, a large number of vehicles arriving simultaneously), many requests may compete for the same limited pool of parking spots.
- The system favors fast failure over fairness; some threads may repeatedly lose the race for available spots.
- It provides less control over scheduling and fairness compared to explicit lock-based approaches

> **ℹ️ Note on Fairness: Why Locks are Fairer than Lock-Free**
>
> **Q: Don't two cars still race for a lock if they arrive at the same time?**
>
> **A:** Yes, the initial race is the exact same. The difference is what happens to the **loser** of that race:
> * **With Explicit Locks (Fair):** Losers are placed in a strict first-in, first-out (FIFO) queue by the operating system and put to sleep. They are guaranteed to get the spot in the exact order they arrived.
> * **With Lock-Free (CAS):** Losers do not wait in line. Instead, they immediately loop back and try again ("spin"). Under heavy traffic, a brand-new arrival can "cut the line" and win the spot on its very first try, while an older thread keeps losing the race and starves indefinitely. 
> 
> *Conclusion:* Lock-free operations create a **free-for-all scramble** where the luckiest/fastest thread wins; explicit fair locks act as a structured ticket counter. For low-contention parking lots, this scramble rarely happens anyway, making lock-free the faster, simpler choice.

### Alternative

A hybrid approach could be used:

- Use lock-free operations during normal traffic conditions.
- Switch to per-spot or per-lot pessimistic locking when contention exceeds a threshold.
- This can improve fairness and reduce contention-related failures during peak traffic periods, at the cost of additional complexity and lock-management overhead.

### Why I Chose This Approach

My assumption is that parking lots generally experience low to moderate contention most of the time. Under these conditions, lock-free concurrent collections provide a good balance of simplicity, correctness, and performance while avoiding the overhead associated with explicit locking.
# Tradeoffs and alternatives (TODO: Refine this)

1. I used optimistic locking vs pessimistic locks

    In my current design, I've used optimistic locking for Concurrency control. My assumption here is that contention would be low, this is due to my guess that each parking lot would have a limited number of parking spots (perhaps high hundreds at the max).

    Tradeoffs:

    - If there's high contention during peak traffic (perhaps during the 9 AM office stretch), then it's possible that multiple users might compete for the same spot which would mean that there could potentially be a large amount of retries -> this would cause increased latency for users

    Alternative:

    - Pessimistic locking or a hybrid strategy where pessimistic locking takes over if there are too many retries for each thread are viable alternatives


# Unique ID generator:

These are essentially built to solve the problem of generating unique Ids that are unique across a Distributed system (no central coordination)


here's the java implementation: https://docs.oracle.com/javase/8/docs/api/java/util/UUID.html

Popular alternatives for this are  the:

ULID spec: https://github.com/ulid/spec


A previously popular version of a unique ID generator was twitter's unique ID generator which is now archived: https://github.com/twitter-archive/snowflake/tree/b3f6a3c6ca8e1b6847baa6ff42bf72201e2c2231


They primarily built the snowflake generator for the following reasons:

- snowflake Id was 48 bits and UUIDs are 128 bits, so they'd save significantly on storage since there's tons of tweets
- previous versions of UUID were not sortable which was bad for twitter, since they wanted an easy way to sort tweets on their timeline
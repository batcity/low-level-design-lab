# How to run tests:

Run the following command to run tests and cleanup afterwards

```
(javac -d out $(find . -name "*.java") && for t in ParkingServiceScenarioStressTest ParkingServiceConcurrencyInvariantTest ParkingServiceThroughputBenchmark; do echo "Running $t"; java -cp out $t; done); rm -rf out
```
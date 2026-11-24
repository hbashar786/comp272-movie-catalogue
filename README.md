# COMP 272/400C — Team Project — Application B: Movie Catalogue Search Backend

**Team:** Hamza Bashar  · Brendan Gallagher 

| | Design | Source |
|---|---|---|
| **Solution 1** | `HashMap` by ID + `ArrayList` kept sorted by (year, ID) | Team (binary search, shifting, `topRated`) + `java.util` |
| **Solution 2** | `HashMap` by ID + `TreeMap` (Red-Black tree) by (year, ID) | Team adapter around `java.util.TreeMap` |

Both use a binary heap of size k (`PriorityQueue`) for `topRated`.

## Repository layout

```
src/main/java/movies/          MovieCatalogue, Movie, DatasetReader, Solution1, Solution2
src/main/java/movies/bench/    Benchmark.java (Stage 4 benchmark harness)
src/test/java/movies/          JUnit tests (shared tests + Solution1Test, Solution2Test)
results/                       Raw benchmark results: raw_timing.csv, counters.csv
docs/proposal/                 Week 10 proposal
docs/BenchmarkResults/         Stage 4 results and plots (PDF)
data/                          MovieLens CSVs (not in the repo)
```

## Dataset

Download MovieLens from https://grouplens.org/datasets/movielens/ and unzip:
**ml-latest-small** into `data/` (tests) and **ml-32m** into `data/ml-32m/` (benchmarks).
The dataset is not committed: its license does not allow redistribution.

## Run the tests

```
mvn test
```

## Run the benchmarks

Java 17 or later. From the project folder:

1. Set `COUNTERS = false` in `Solution1.java`, then run the timing experiments:
   ```
   mvn -q compile && java -Xmx4g -cp target/classes movies.bench.Benchmark timing data/ml-32m results
   ```
2. Set `COUNTERS = true`, then record the instrumentation counters:
   ```
   mvn -q compile && java -Xmx4g -cp target/classes movies.bench.Benchmark counters data/ml-32m results
   ```

Add `quick` at the end of either command for a short test run. Every data point gets 5 warm-up runs and
7 measured runs; the plots and tables use the median. The plots in `docs/BenchmarkResults/` were made in
Excel from the CSV files in `results/`.

## Deliverables

| Due | Deliverable | Status |
|---|---|---|
| Week 10 | Proposal | ☑ |
| Week 12 | `DatasetReader`, both solutions, extended tests, progress note | ☑ |
| Week 13 | Benchmark harness, raw CSVs, plots | ☑ |
| Week 14 | Report (PDF) + final code `.zip` | ☐ |
| Week 15 | Recorded presentation (MP4) + slides (PDF) | ☐ |

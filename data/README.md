# data/

Put the MovieLens CSV files here. **They are git-ignored and must not be committed or submitted.**

| Dataset | Use | Download |
|---|---|---|
| `ml-latest-small` | Development and unit tests | https://grouplens.org/datasets/movielens/ |
| `ml-32m` | Experiments (Stage 4) | https://grouplens.org/datasets/movielens/ |

Suggested layout (keeps both sizes side by side):

```
data/
├── movies.csv        ← from ml-latest-small (the tests read data/movies.csv)
├── ratings.csv       ← from ml-latest-small
└── ml-32m/
    ├── movies.csv
    └── ratings.csv   (~1 GB)
```

Cite the dataset in the report: F. Maxwell Harper and Joseph A. Konstan. 2015.
*The MovieLens Datasets: History and Context.* ACM Transactions on Interactive
Intelligent Systems (TiiS) 5, 4, Article 19. https://doi.org/10.1145/2827872
(check the README inside each download for the exact license and citation text).

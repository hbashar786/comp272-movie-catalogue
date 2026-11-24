package movies.bench;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import movies.*;

public class Benchmark {

    static int warmup, runs;
    static List<Movie> movies, order;
    static List<DatasetReader.Rating> ratings;
    static int[] ids;
    static int[][] ranges;
    static MovieCatalogue cat;
    static PrintWriter out;
    static double aux;

    public static void main(String[] args) throws IOException
    {
        boolean timing = args[0].equals("timing");
        warmup = args.length > 3 ? 2 : 5;
        runs = args.length > 3 ? 3 : 7;
        out = new PrintWriter(args[2] + (timing ? "/raw_timing.csv" : "/counters.csv"));
        out.println(timing ? "Expirement, Solution, Workload, n, Run, Ops, Elasped_ns, ns_per,op,aux, checksum" 
        : "Solution, Workload, n, SearchSteps, Shifts");

        long start = System.nanoTime();
        movies = DatasetReader.readMovies(Path.of(args[1], "movies.csv"));
        out.printf(timing ? "Load, Both, read_Movies_csv, %d, 0, %d, %d, 0, 0, 0%n" : "", movies.size(), movies.size(),
        System.nanoTime() - start);
        ratings = DatasetReader.readRatings(Path.of(args[1], "ratings.csv"), args.length > 3 ? 2000000 : Integer.MAX_VALUE);

        Comparator<Movie> byYear = Comparator.comparingInt(Movie::year).thenComparingInt(Movie::id);

        for(String s : new String[] { "Solution 1", "Solution 2"})
        {
            for(int n : new int[] {100, 1000, 10000, 30000, movies.size()})
            {
                for(String o : new String[] { "File_order", "Year_Ascending", "Year_Descending", "Random_Order"})
                {
                    order = new ArrayList<>(movies.subList(0, n));

                    if(o.startsWith("Year"))
                    {
                        order.sort(o.equals("Year_Ascending") ? byYear : byYear.reversed());
                    }
                    if(o.equals("Random_Order"))
                    {
                        Collections.shuffle(order, new Random(42));
                    }
                    if(timing)
                    {
                        measure(s, "build", o, n, n);
                    }
                    else
                    {
                        MovieCatalogue c = build(s,0,0);
                        body(c, "build", n);
                        out.println(s + "," + o + "," + n + "," + (c instanceof Solution1 ? ((Solution1) c).getSearchSteps() + "," + 
                        ((Solution1) c).getShifts() : ","));
                    }
                }

                cat = null;
                System.gc();
                long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                cat = build(s, n, 0);
                System.gc();
                long bytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - before;
                out.printf(timing ? "memory,%s,index,%d,0,%d,%d,%.2f,0,%d%n" : "", s, n, n, bytes, (double) bytes / n, cat.size());
                cat = build(s, n, timing ? ratings.size() : 0);

                for (int i = 0; i < 3 && timing; i++)
                {
                    Random rand = new Random(42);
                    ids = new int[runs == 3 ? 100000 : 1000000];
                    for (int j = 0; j < ids.length; j++)
                    {
                        int id = movies.get(i == 1 ? j % n : rand.nextInt(n)).id();
                        ids[j] = i == 2 ? id + movies.get(n - 1).id() : id;
                    }
                    measure(s, "getById", i == 0 ? "random_ids" : i == 1 ? "increasing_ids" : "missing_ids", n, ids.length);
                }
                for (int w : timing ? new int[] {1, 10, 50, 0} : new int[0])
                {
                    Random rand = new Random(42 + w);
                    ranges = new int[w == 0 ? 50 : 500][2];
                    for (int j = 0; j < ranges.length; j++)
                    {
                        int year = movies.get(rand.nextInt(movies.size())).year();
                        ranges[j][0] = w == 0 ? 0 : Math.max(1874, Math.min(year - rand.nextInt(w), 2024 - w));
                        ranges[j][1] = w == 0 ? 9999 : ranges[j][0] + w - 1;
                    }
                    measure(s, "range", w == 0 ? "all_years" : w + "y", n, ranges.length);
                    measure(s, "topRated", w == 0 ? "all_years" : w + "y", n, ranges.length);
                }
            }

            for (int r : timing ? new int[] {10000, 100000, 1000000, 10000000, ratings.size()} : new int[0])
            {
                measure(s, "addRating", "file_order", movies.size(), Math.min(r, ratings.size()));
            }
        }
        out.close();
    }

    static void measure(String s, String exp, String wl, int n, int ops)
    {
        double last = 0;

        for (int run = 0; run < warmup + runs; run++)
        {
            MovieCatalogue c = exp.equals("build") ? build(s, 0, 0) : exp.equals("addRating") ? build(s, n, 0) : cat;
            aux = 0;
            long start = System.nanoTime();
            long checksum = body(c, exp, ops);
            long elapsed = System.nanoTime() - start;
            if(run >= warmup)
            {
                last = (double) elapsed / ops;
                out.printf("%s,%s,%s,%d,%d,%d,%d,%.3f,%.2f,%d%n", exp, s, wl, n, run - warmup, ops, elapsed, (double) elapsed / ops, aux, checksum);
            }
        }

        System.out.printf("%-10s %-9s %-16s n=%-6d ops=%-9d %.1f ns/op%n", s, exp, wl, n, ops, last);
    }

    static long body(MovieCatalogue c, String exp, int ops)
    {
        long sum = 0;

        for (int i = 0; i < ops; i++)
        {
            if(exp.equals("build"))
            {
                c.insertMovie(order.get(i));
            }
            else if(exp.equals("getById") || exp.equals("addRating"))
            {
                sum += exp.equals("getById") ? (c.getById(ids[i]) == null ? 0 : ids[i]) : (c.addRating(ratings.get(i).movieId(), ratings.get(i).rating()) ? 1 : 0);
            }
            else
            {
                List<Movie> res = exp.equals("range") ? c.moviesInYears(ranges[i][0], ranges[i][1]) : c.topRated(ranges[i][0], ranges[i][1], 10, 50);
                aux += (double) res.size() / ops;
                for (int j = 0; j < res.size(); j++)
                {
                    sum += (long) res.get(j).id() * (j + 1);
                }
            }
        }

        return sum + c.size();
    }

    static MovieCatalogue build(String s, int n, int count)
    {
        MovieCatalogue c = s.equals("Solution 1") ? new Solution1() : new Solution2();

        for (int i = 0; i < n; i++)
        {
            c.insertMovie(movies.get(i));
        }
        for (int i = 0; i < count; i++)
        {
            c.addRating(ratings.get(i).movieId(), ratings.get(i).rating());
        }
        return c;
    }
}

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Qualification Round 2017
 */
public class Qualification {
    private static final String EXAMPLE_IN = "files/qualification_2017/example.in";
    private static final String EXAMPLE_OUT = "files/qualification_2017/example.out";

    private static final String KITTEN_IN = "files/qualification_2017/kittens.in";
    private static final String KITTEN_OUT = "files/qualification_2017/kittens.out";

    private static final String ZOO_IN = "files/qualification_2017/me_at_the_zoo.in";
    private static final String ZOO_OUT = "files/qualification_2017/me_at_the_zoo.out";

    private static final String TRENDING_IN = "files/qualification_2017/trending_today.in";
    private static final String TRENDING_OUT = "files/qualification_2017/trending_today.out";

    private static final String WORTH_SPREADING_IN = "files/qualification_2017/videos_worth_spreading.in";
    private static final String WORTH_SPREADING_OUT = "files/qualification_2017/videos_worth_spreading.out";


    public static void main(String[] args) {
        System.out.println("Welcome to the qualification round!");

        Problem problem = parseProblem(EXAMPLE_IN);

        // fake solution
        problem.caches.get(0).videos.add(problem.videos.get(2));
        problem.caches.get(1).videos.add(problem.videos.get(3));
        problem.caches.get(1).videos.add(problem.videos.get(1));
        problem.caches.get(2).videos.add(problem.videos.get(0));
        problem.caches.get(2).videos.add(problem.videos.get(1));

        write_solution(EXAMPLE_OUT, problem.caches);

        System.out.println("Bye bye to the qualification round!");
    }

    public static void write_solution(String fileName, List<Cache> caches) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            int numberOfCaches = caches.size();

            writer.println(numberOfCaches);
            for (int i = 0; i < numberOfCaches; i++) {
                Cache cache = caches.get(i);
                writer.print(cache.id);
                int numberOfVideos = cache.videos.size();
                for (int j = 0; j < numberOfVideos; j++) {
                    Video video = cache.videos.get(j);
                    writer.print(' ');
                    writer.print(video.id);
                }
                writer.println();
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("error writing file " + e.getMessage());
        }
    }

    public static Problem parseProblem(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            line = br.readLine();

            String[] firstLineParams = line.split(" ");
            int numberOfVideos = Integer.parseInt(firstLineParams[0]);
            int numberOfEndPoints = Integer.parseInt(firstLineParams[1]);
            int numberOfRequests = Integer.parseInt(firstLineParams[2]);
            int numberOfCaches = Integer.parseInt(firstLineParams[3]);
            int cacheCapacity = Integer.parseInt(firstLineParams[4]);

            ArrayList<Cache> caches = new ArrayList<Cache>(numberOfCaches);
            for (int i = 0; i < numberOfCaches; i++) {
                Cache cache = new Cache(i, cacheCapacity);
                caches.add(cache);
            }

            ArrayList<Video> videos = new ArrayList<Video>(numberOfVideos);
            line = br.readLine();
            String[] videosSize = line.split(" ");
            for (int i = 0; i < numberOfVideos; i++) {
                int videoSize = Integer.parseInt(videosSize[i]);
                Video video = new Video(i, videoSize);
                videos.add(video);
            }

            ArrayList<EndPoint> endPoints = new ArrayList<EndPoint>(numberOfEndPoints);
            for (int i = 0; i < numberOfEndPoints; i++) {
                line = br.readLine();
                String[] endPointParams = line.split(" ");
                int latencyFromDataCenter = Integer.parseInt(endPointParams[0]);
                int numberOfConnectedCache = Integer.parseInt(endPointParams[1]);
                EndPoint endPoint = new EndPoint(i, latencyFromDataCenter);
                for (int j = 0; j < numberOfConnectedCache; j++) {
                    line = br.readLine();
                    String[] latencyParams = line.split(" ");
                    int cacheId = Integer.parseInt(latencyParams[0]);
                    int latencyValue = Integer.parseInt(latencyParams[1]);
                    Cache cache = caches.get(cacheId);

                    Latency latency = new Latency(cache, latencyValue);
                    endPoint.latencies.add(latency);
                }
                endPoints.add(endPoint);
            }

            ArrayList<Request> requests = new ArrayList<Request>(numberOfRequests);
            for (int i = 0; i < numberOfRequests; i++) {
                line = br.readLine();
                String[] requestParams = line.split(" ");
                int videoId = Integer.parseInt(requestParams[0]);
                int endPointId = Integer.parseInt(requestParams[1]);
                int number = Integer.parseInt(requestParams[2]);

                Video video = videos.get(videoId);
                EndPoint endPoint = endPoints.get(endPointId);

                Request request = new Request(video, endPoint, number);
                requests.add(request);
            }

            return new Problem(videos, caches, requests, endPoints);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Problem {
        public final ArrayList<Video> videos;
        public final ArrayList<Cache> caches;
        public final ArrayList<Request> requests;
        public final ArrayList<EndPoint> endPoints;

        public Problem(ArrayList<Video> videos,
                       ArrayList<Cache> caches,
                       ArrayList<Request> requests,
                       ArrayList<EndPoint> endPoints) {
            this.videos = videos;
            this.caches = caches;
            this.requests = requests;
            this.endPoints = endPoints;
        }
    }


    public static class Video {
        public final int id;
        public final int size;

        public Video(int id, int size) {
            this.id = id;
            this.size = size;
        }
    }

    public static class Cache {
        public final int id;
        public final int initialCapacity;
        public final ArrayList<Video> videos;
        public int currentCapacity;

        public Cache(int id, int initialCapacity) {
            this.id = id;
            this.initialCapacity = initialCapacity;
            this.currentCapacity = initialCapacity;
            this.videos = new ArrayList<Video>();
        }
    }

    public static class Latency {
        public final Cache cache;
        public final int value;

        public Latency(Cache cache, int value) {
            this.cache = cache;
            this.value = value;
        }
    }

    public static class EndPoint {
        public final int id;
        public final int latencyFromDataCenter;
        public final ArrayList<Latency> latencies;

        public EndPoint(int id, int latencyFromDataCenter) {
            this.id = id;
            this.latencyFromDataCenter = latencyFromDataCenter;
            this.latencies = new ArrayList<Latency>();
        }
    }

    public static class Request {
        public final Video video;
        public final EndPoint endPoint;
        public final int number;

        public Request(Video video, EndPoint endPoint, int number) {
            this.video = video;
            this.endPoint = endPoint;
            this.number = number;
        }
    }
}

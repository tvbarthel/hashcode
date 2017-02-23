import java.io.*;
import java.util.*;

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

//        solve(EXAMPLE_IN, EXAMPLE_OUT);
        solve(ZOO_IN, ZOO_OUT);
        solve(WORTH_SPREADING_IN, WORTH_SPREADING_OUT);
        solve(TRENDING_IN, TRENDING_OUT);
        solve(KITTEN_IN, KITTEN_OUT);

        System.out.println("Bye bye to the qualification round!");
    }

    private List<Request> getRequestsFromEndPoint(EndPoint endPoint, List<Request> requests) {
        ArrayList<Request> requestsFromEndPoint = new ArrayList<Request>();
        for (Request request : requests) {
            if (request.endPoint.id == endPoint.id) {
                requestsFromEndPoint.add(request);
            }
        }
        return requestsFromEndPoint;
    }

    private List<RequestFromEndPointToCache> getRequestToCache(Cache cache, List<Request> requests) {
        List<RequestFromEndPointToCache> requestsFromEndPointToCache = new ArrayList<RequestFromEndPointToCache>();

        for (Request request : requests) {
            ArrayList<Latency> latencies = request.endPoint.latencies;
            for (Latency latency : latencies) {
                if (latency.cache.id == cache.id) {
                    RequestFromEndPointToCache requestFromEndPointToCache =
                            new RequestFromEndPointToCache(request.video, request.endPoint,
                                    cache, request.number, latency.value);

                    requestsFromEndPointToCache.add(requestFromEndPointToCache);
                }
            }
        }

        return requestsFromEndPointToCache;
    }

    private static void solve(String in, String out) {
        Problem problem = parseProblem(in);

        // fill target cache id.
        for (Request request : problem.requests) {
            for (Latency latency : request.endPoint.latencies) {
                if (!request.video.targetCacheIds.contains(latency.cache.id)) {
                    request.video.targetCacheIds.add(latency.cache.id);
                }
            }
        }

        long maxScore = 0;
        ArrayList<Cache> maxCache = new ArrayList<Cache>();

        System.out.println("============================");
        System.out.println(in);
        System.out.println("============================");

//        System.out.println("cacheMostVideosAsPossible: ");
//        cacheMostVideosAsPossible(problem.caches, problem.requests);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
//        System.out.println("cacheVideoFromMostRequestedFirst: ");
//        cacheVideoFromMostRequestedFirst(problem.caches, problem.requests);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
//        System.out.println("cacheVideoByMostRequested: ");
//        cacheVideoByMostRequested(problem.caches, problem.requests);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
////        System.out.println("cacheVideoByMostSaving: ");
////        cacheVideoByMostSaving(problem.caches, problem.requests, problem.videos);
////        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
//        System.out.println("cacheMostLightVideosFirst: ");
//        cacheMostLightVideosFirst(problem.caches, problem.requests, problem.videos);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
//        System.out.println("cacheMostRequestedVideosFirst: ");
//        cacheMostRequestedVideosFirst(problem.caches, problem.requests, problem.videos);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);
//
//        System.out.println("cacheMostRequestedVideosFirst: ");
//        cacheMostTimeSaveVideosFirst(problem.caches, problem.requests, problem.videos);
//        maxScore = keepHighestScore(problem, maxScore, maxCache);

        System.out.println("cacheMostTimeSaveNormalizedBySizeFirst: ");
        cacheMostTimeSaveNormalizedBySizeFirst(problem.caches, problem.requests, problem.videos);
        maxScore = keepHighestScore(problem, maxScore, maxCache);


        write_solution(out, maxCache);

        System.out.println("============================");
        System.out.println("============================");
        System.out.println("");
    }

    private static long keepHighestScore(Problem problem, long maxScore, ArrayList<Cache> maxCache) {
        long score = computeScore(problem.requests);
        System.out.println("New score: " + score);
        if (score > maxScore) {
            maxCache.clear();
            maxCache.addAll(problem.caches);
            System.out.println("NEW HIGHEST SCORE: " + score);
            return score;
        }
        return maxScore;
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

    public static void cacheVideoByMostSaving(ArrayList<Cache> caches, ArrayList<Request> requests, ArrayList<Video> videos) {

        ArrayList<Long> timeSavedOverall = new ArrayList<Long>(videos.size());
        for (Video video : videos) {
            timeSavedOverall.add(video.id, computeSavingTime(video, requests));
        }

        VideoEndPoint videoEndPoint = null;
        final HashMap<VideoEndPoint, Long> videoEndPointIntegerHashMap = new HashMap<VideoEndPoint, Long>();
        for (Request request : requests) {
            videoEndPoint = new VideoEndPoint(request.video.id, request.endPoint.id);
            Long integer = videoEndPointIntegerHashMap.get(videoEndPoint);

            Long timeSaved = timeSavedOverall.get(request.video.id);

            if (integer == null) {
                videoEndPointIntegerHashMap.put(videoEndPoint, timeSaved);
            } else {
                videoEndPointIntegerHashMap.put(videoEndPoint, timeSaved);
            }
        }

        Collections.sort(requests, new Comparator<Request>() {

            VideoEndPoint v1 = new VideoEndPoint();
            VideoEndPoint v2 = new VideoEndPoint();

            public int compare(Request o1, Request o2) {
                v1.endPointId = o1.endPoint.id;
                v1.videoId = o1.video.id;
                v2.endPointId = o2.endPoint.id;
                v2.videoId = o2.video.id;

                return (int) (videoEndPointIntegerHashMap.get(v2) - videoEndPointIntegerHashMap.get(v1));
            }

        });

        resetCache(caches);
        for (Request request : requests) {
            for (Cache cache : caches) {
                if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                    if (!cache.videos.contains(request.video)) {
                        cache.videos.add(request.video);
                        cache.currentCapacity -= request.video.size;
                    }
                }
            }
        }
    }

    private static long computeSavingTime(Video video, ArrayList<Request> requests) {
        long timeSaved = 0;
        long requestNumber = 0;
        for (Request request : requests) {
            long lowestLatency = request.endPoint.latencyFromDataCenter;
            for (Latency latency : request.endPoint.latencies) {
                if (latency.value < lowestLatency) {
                    if (video.id == request.video.id) {
                        lowestLatency = latency.value; // lower latency
                    }
                }
            }

            requestNumber += request.number;
            timeSaved += request.number * (request.endPoint.latencyFromDataCenter - lowestLatency);
        }
        return timeSaved * 1000 / requestNumber;
    }

    public static void cacheVideoByMostRequested(ArrayList<Cache> caches, ArrayList<Request> requests) {

        VideoEndPoint videoEndPoint = null;
        final HashMap<VideoEndPoint, Integer> videoEndPointIntegerHashMap = new HashMap<VideoEndPoint, Integer>();
        for (Request request : requests) {
            videoEndPoint = new VideoEndPoint(request.video.id, request.endPoint.id);
            Integer integer = videoEndPointIntegerHashMap.get(videoEndPoint);
            if (integer == null) {
                videoEndPointIntegerHashMap.put(videoEndPoint, request.number);
            } else {
                videoEndPointIntegerHashMap.put(videoEndPoint, integer + request.number);
            }
        }

        Collections.sort(requests, new Comparator<Request>() {

            VideoEndPoint v1 = new VideoEndPoint();
            VideoEndPoint v2 = new VideoEndPoint();

            public int compare(Request o1, Request o2) {
                v1.endPointId = o1.endPoint.id;
                v1.videoId = o1.video.id;
                v2.endPointId = o2.endPoint.id;
                v2.videoId = o2.video.id;

                return videoEndPointIntegerHashMap.get(v2) - videoEndPointIntegerHashMap.get(v1);
            }

        });

        resetCache(caches);
        for (Request request : requests) {
            for (Cache cache : caches) {
                if (request.video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                        if (!cache.videos.contains(request.video)) {
                            cache.videos.add(request.video);
                            cache.currentCapacity -= request.video.size;
                        }
                    }
                }
            }
        }
    }

    public static void cacheVideoFromMostRequestedFirst(ArrayList<Cache> caches, ArrayList<Request> requests) {

        Collections.sort(requests, new Comparator<Request>() {

            public int compare(Request o1, Request o2) {
                return o2.number - o1.number;
            }

        });

        resetCache(caches);
        for (Request request : requests) {
            for (Cache cache : caches) {
                if (request.video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                        if (!cache.videos.contains(request.video)) {
                            cache.videos.add(request.video);
                            cache.currentCapacity -= request.video.size;
                        }
                    }
                }
            }
        }
    }


    public static void cacheMostVideosAsPossible(ArrayList<Cache> caches, ArrayList<Request> requests) {
        resetCache(caches);
        for (Request request : requests) {
            for (Cache cache : caches) {
                if (request.video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                        if (!cache.videos.contains(request.video)) {
                            cache.videos.add(request.video);
                            cache.currentCapacity -= request.video.size;
                        }
                    }
                }
            }
        }
    }

    public static void cacheMostRequestedVideosFirst(ArrayList<Cache> caches, ArrayList<Request> requests, ArrayList<Video> videos) {
        resetCache(caches);

        Collections.sort(videos, new Comparator<Video>() {
            public int compare(Video o1, Video o2) {
                return o2.requestedTime - o1.requestedTime;
            }
        });
        for (Video video : videos) {
            for (Cache cache : caches) {
                if (video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - video.size > 0) {
                        if (!cache.videos.contains(video)) {
                            cache.videos.add(video);
                            cache.currentCapacity -= video.size;
                        }
                    }
                }
            }
        }
    }

    public static void cacheMostTimeSaveNormalizedBySizeFirst(ArrayList<Cache> caches, ArrayList<Request> requests, ArrayList<Video> videos) {
        resetCache(caches);


        for (final Cache cache : caches) {
            Collections.sort(videos, new Comparator<Video>() {
                public int compare(Video o1, Video o2) {
                    Integer i1 = (int) (o1.averageTimeSavedPerCache.get(cache.id) / (float) o1.size);
                    Integer i2 = (int) (o2.averageTimeSavedPerCache.get(cache.id) / (float) o2.size);
                    return i2 - i1;
                }
            });
            for (Video video : videos) {
                if (video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - video.size > 0) {
                        if (!cache.videos.contains(video)) {
                            cache.videos.add(video);
                            cache.currentCapacity -= video.size;
                        }
                    }
                }
            }
        }

    }

    public static void cacheMostTimeSaveVideosFirst(ArrayList<Cache> caches, ArrayList<Request> requests, ArrayList<Video> videos) {
        resetCache(caches);


        for (final Cache cache : caches) {
            Collections.sort(videos, new Comparator<Video>() {
                public int compare(Video o1, Video o2) {
                    Integer i1 = o1.averageTimeSavedPerCache.get(cache.id);
                    Integer i2 = o2.averageTimeSavedPerCache.get(cache.id);
                    return i2 - i1;
                }
            });
            for (Video video : videos) {
                if (video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - video.size > 0) {
                        if (!cache.videos.contains(video)) {
                            cache.videos.add(video);
                            cache.currentCapacity -= video.size;
                        }
                    }
                }
            }
        }

    }

    public static void cacheMostLightVideosFirst(ArrayList<Cache> caches, ArrayList<Request> requests, ArrayList<Video> videos) {
        resetCache(caches);

        Collections.sort(videos, new Comparator<Video>() {
            public int compare(Video o1, Video o2) {
                return o1.size - o2.size;
            }
        });
        for (Video video : videos) {
            for (Cache cache : caches) {
                if (video.targetCacheIds.contains(cache.id)) {
                    if (cache.currentCapacity > 0 && cache.currentCapacity - video.size > 0) {
                        if (!cache.videos.contains(video)) {
                            cache.videos.add(video);
                            cache.currentCapacity -= video.size;
                        }
                    }
                }
            }
        }
    }

    private static void resetCache(ArrayList<Cache> caches) {
        for (Cache cache : caches) {
            cache.videos.clear();
            cache.currentCapacity = cache.initialCapacity;
        }
    }

    public static long computeScore(ArrayList<Request> requests) {
        long timeSaved = 0;
        long requestNumber = 0;
        for (Request request : requests) {
            long lowestLatency = request.endPoint.latencyFromDataCenter;
            for (Latency latency : request.endPoint.latencies) {
                if (latency.value < lowestLatency) {
                    for (Video video : latency.cache.videos) {
                        if (video.id == request.video.id) {
                            lowestLatency = latency.value; // lower latency
                        }
                    }
                }
            }

            requestNumber += request.number;
            timeSaved += request.number * (request.endPoint.latencyFromDataCenter - lowestLatency);
        }
        return timeSaved * 1000 / requestNumber;
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
                for (Cache cache : caches) {
                    video.averageTimeSavedPerCache.put(cache.id, 0);
                }
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

                video.requestedTime += request.number;
                int time;
                for (Latency latency : endPoint.latencies) {
                    time = request.number * (endPoint.latencyFromDataCenter - latency.value) / 100;
                    Integer currentTimeSaved = video.averageTimeSavedPerCache.get(latency.cache.id);
                    if (currentTimeSaved != null) {
                        time += currentTimeSaved;
                    }
                    video.averageTimeSavedPerCache.put(latency.cache.id, time);
                }
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
        public final ArrayList<Integer> targetCacheIds;
        public int requestedTime;
        public final HashMap<Integer, Integer> averageTimeSavedPerCache;

        public Video(int id, int size) {
            this.id = id;
            this.size = size;
            targetCacheIds = new ArrayList<Integer>();
            requestedTime = 0;
            averageTimeSavedPerCache = new HashMap<Integer, Integer>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Video video = (Video) o;

            return id == video.id;

        }

        @Override
        public int hashCode() {
            return id;
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

    public static class RequestFromEndPointToCache {
        public final Video video;
        public final EndPoint endPoint;
        public final Cache cache;
        public final int number;
        public final int latencyValue;

        public RequestFromEndPointToCache(Video video, EndPoint endPoint, Cache cache, int number, int latencyValue) {
            this.video = video;
            this.endPoint = endPoint;
            this.cache = cache;
            this.number = number;
            this.latencyValue = latencyValue;
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

    public static class VideoEndPoint {
        public int videoId;
        public int endPointId;

        public VideoEndPoint() {

        }

        public VideoEndPoint(int videoId, int endPointId) {
            this.videoId = videoId;
            this.endPointId = endPointId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VideoEndPoint that = (VideoEndPoint) o;

            if (videoId != that.videoId) return false;
            return endPointId == that.endPointId;

        }

        @Override
        public int hashCode() {
            int result = videoId;
            result = 31 * result + endPointId;
            return result;
        }
    }
}

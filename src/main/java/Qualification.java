import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private static final int BUCKET_INITIAL_SIZE = 5_000;
    private static final int BUCKET_MAX_SIZE = 20_000;
    private static final int MAX_GAIN_ESPERANCES_SIZE = 1_500_000;


    public static void main(String[] args) {
        System.out.println("Welcome to the qualification round!");

        solve(KITTEN_IN, KITTEN_OUT);
        // solve(ZOO_IN, ZOO_OUT);
        // solve(TRENDING_IN, TRENDING_OUT);
        // solve(WORTH_SPREADING_IN, WORTH_SPREADING_OUT);

        System.out.println("Bye bye to the qualification round!");
    }

    private static List<Request> getRequestsFromEndPoint(EndPoint endPoint, List<Request> requests) {
        ArrayList<Request> requestsFromEndPoint = new ArrayList<Request>();
        for (Request request : requests) {
            if (request.endPoint.id == endPoint.id) {
                requestsFromEndPoint.add(request);
            }
        }
        return requestsFromEndPoint;
    }

    private static List<RequestFromEndPointToCache> getRequestToCache(Cache cache, List<Request> requests) {
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

        double avg = 0;
        int t = 1;
        double numberOfRequestInTotal = 0;

        for (Request request : problem.requests) {
            avg += (request.number - avg) / t;
            numberOfRequestInTotal += request.number;
        }
        System.out.println("Average request number: " + avg + " for " + problem.requests.size() + " requests ");
        System.out.println("Number of request in total " + numberOfRequestInTotal);

        List<RequestFromEndPointToCache> allRequestsFromEndPointToCache =
                createRequestFromEndPointToCaches(in, problem, problem.requests);
        List<GainEsperance> allGainEsperances = createGainEsperancesInParallel(allRequestsFromEndPointToCache);
        recomputeGainEsperancesGain(allGainEsperances);
        orderByMostGain(allGainEsperances);

        if (allGainEsperances.size() > MAX_GAIN_ESPERANCES_SIZE) {
            System.out.println("That's a lot of gain esperances: " + allGainEsperances.size());
            System.out.println("Only take the first " + MAX_GAIN_ESPERANCES_SIZE);
            allGainEsperances = allGainEsperances.subList(0, MAX_GAIN_ESPERANCES_SIZE);
        }

        List<List<GainEsperance>> buckets = new ArrayList<>();
        fillBuckets(allGainEsperances, buckets);

        GainEsperance appliedEsperancesFromBucket = null;
        int appliedEsperances = 0;
        List<GainEsperance> currentBestBucket = buckets.remove(0);

        do {
            currentBestBucket = recomputeBestBucket(currentBestBucket, buckets);

            appliedEsperancesFromBucket = applyingFirstEsperance(currentBestBucket);

            removeUselessEsperances(currentBestBucket, true);
            // recomputeGainEsperancesGain(currentBestBucket);
            orderByMostGain(currentBestBucket);

            if (appliedEsperances % 40 == 0) {
                int numberOfEsperanceInTotal = buckets.stream().mapToInt(List::size).sum();
                System.out.println("Applied esperances " + appliedEsperances +
                        " still " + currentBestBucket.size() + " gain esperances in current buckets for " +
                        numberOfEsperanceInTotal + " esperances in total");
                printCacheCapacities(problem);
            }
            appliedEsperances++;
        } while (appliedEsperancesFromBucket != null || !buckets.isEmpty());

        printCacheCapacities(problem);

        long score = computeScore(problem.requests);
        System.out.println("New score: " + score + " for " + appliedEsperances + " applied esperances");

        write_solution(out, problem.caches);
    }

    private static void fillBuckets(List<GainEsperance> esperances, List<List<GainEsperance>> buckets) {
        int numberOfGainEsperances = esperances.size();
        int numberOfBuckets = 1 + numberOfGainEsperances / BUCKET_INITIAL_SIZE;
        for (int i = 0; i < numberOfBuckets; i++) {
            int startIndex = i * BUCKET_INITIAL_SIZE;
            int stopIndex = Math.min(numberOfGainEsperances, startIndex + BUCKET_INITIAL_SIZE);
            buckets.add(new ArrayList<>(esperances.subList(startIndex, stopIndex)));
        }
    }

    private static List<GainEsperance> recomputeBestBucket(List<GainEsperance> currentBestBucket, List<List<GainEsperance>> buckets) {
        boolean changed = false;
        while (shouldRecomputeBestBucket(currentBestBucket, buckets)) {
            List<GainEsperance> nextBestBucket = buckets.remove(0);

            ArrayList<GainEsperance> mergedBuckets = new ArrayList<>(currentBestBucket);
            mergedBuckets.addAll(nextBestBucket);

            removeUselessEsperances(mergedBuckets, true);
            // recomputeGainEsperancesGain(mergedBuckets);
            orderByMostGain(mergedBuckets);

            currentBestBucket = mergedBuckets;
            changed = true;
        }

        if (changed && currentBestBucket.size() > BUCKET_MAX_SIZE) {
            System.out.println("Too big buckets: " + currentBestBucket.size() + " recomputing all buckets");
            List<GainEsperance> gainEsperances = buckets.stream().flatMap(List::stream).collect(Collectors.toList());

            gainEsperances.addAll(currentBestBucket);
            removeUselessEsperances(gainEsperances, true);
            // recomputeGainEsperancesGain(gainEsperances);
            orderByMostGain(gainEsperances);

            buckets.clear();
            fillBuckets(gainEsperances, buckets);
            currentBestBucket = buckets.remove(0);
            String toto = "toto";
        }

        if (changed) {
            double bestGain = currentBestBucket.isEmpty() ? 0 : currentBestBucket.get(0).gain;
            System.out.println("New best bucket " + currentBestBucket.size() + " with best gain " + bestGain);
        }

        return currentBestBucket;
    }

    private static boolean shouldRecomputeBestBucket(List<GainEsperance> currentBestBucket, List<List<GainEsperance>> buckets) {
        if (buckets.isEmpty()) {
            return false;
        }

        if (currentBestBucket.isEmpty()) {
            return true;
        }

        List<GainEsperance> nextBestBucket = buckets.get(0);
        double nextBucketBestGain = nextBestBucket.isEmpty() ? 0 : nextBestBucket.get(0).gain;
        return Double.compare(currentBestBucket.get(0).gain, nextBucketBestGain) < 0;
    }

    private static List<RequestFromEndPointToCache> createRequestFromEndPointToCaches(String in, Problem problem, List<Request> subListRequests) {
        List<RequestFromEndPointToCache> allRequestsFromEndPointToCache = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger(problem.caches.size());

        problem.caches.parallelStream().forEach(cache -> {
            int remainingCaches = atomicInteger.decrementAndGet();
            System.out.println("Computing request from end point to cache " + cache.id + " remaining " + remainingCaches + " on thread " + Thread.currentThread().getName());
            List<RequestFromEndPointToCache> requestList = getRequestToCache(cache, subListRequests);
            synchronized (allRequestsFromEndPointToCache) {
                allRequestsFromEndPointToCache.addAll(requestList);
            }
        });

        return allRequestsFromEndPointToCache;
    }

    private static void printCacheCapacities(Problem problem) {
        int minCapacity = Integer.MAX_VALUE;
        int maxCapacity = 0;
        double averageCapacity = 0;
        for (Cache cache : problem.caches) {
            averageCapacity += cache.currentCapacity;
            if (cache.currentCapacity > maxCapacity) {
                maxCapacity = cache.currentCapacity;
            } else if (cache.currentCapacity < minCapacity) {
                minCapacity = cache.currentCapacity;
            }
        }
        averageCapacity /= problem.caches.size();
        System.out.println("Cache status:  min: " + minCapacity + " max: " + maxCapacity + " avg " + averageCapacity);
    }

    private static void removeUselessEsperances(List<GainEsperance> gainEsperances, boolean remove) {
        gainEsperances.parallelStream()
                .forEach(gainEsperance -> {
                    if (gainEsperance.cache.currentCapacity >= gainEsperance.video.size
                            && !gainEsperance.cache.videos.contains(gainEsperance.video)) {
                        ArrayList<RequestFromEndPointToCache> associatedRequests = gainEsperance.associatedRequests;
                        for (int requestIndex = associatedRequests.size() - 1; requestIndex >= 0; requestIndex--) {
                            RequestFromEndPointToCache candidateRequest = associatedRequests.get(requestIndex);
                            boolean isAlreadySatisfied = false;
                            for (Latency latency : candidateRequest.endPoint.latencies) {
                                if (latency.cache.videos.contains(candidateRequest.video) && latency.value <= candidateRequest.latencyValue) {
                                    isAlreadySatisfied = true;
                                    break;
                                }
                            }

                            if (isAlreadySatisfied) {
                                gainEsperance.gain -= candidateRequest.gain;
                                associatedRequests.remove(requestIndex);
                            }
                        }
                    }
                });

        if (remove) {
            for (int esperanceIndex = gainEsperances.size() - 1; esperanceIndex >= 0; esperanceIndex--) {
                GainEsperance candidateGainEsperance = gainEsperances.get(esperanceIndex);
                if (candidateGainEsperance.cache.currentCapacity < candidateGainEsperance.video.size
                        || candidateGainEsperance.associatedRequests.isEmpty()
                        || candidateGainEsperance.cache.videos.contains(candidateGainEsperance.video)) {
                    // Too big to fit or already there or no associated request
                    gainEsperances.remove(esperanceIndex);
                }
            }
        }
    }

    private static GainEsperance applyingFirstEsperance(List<GainEsperance> gainEsperances) {
        if (gainEsperances.isEmpty()) {
            return null;
        }

        final GainEsperance gainEsperance = gainEsperances.remove(0);
        gainEsperance.cache.currentCapacity -= gainEsperance.video.size;
        gainEsperance.cache.videos.add(gainEsperance.video);
        return gainEsperance;
    }

    private static void applyingEsperances(List<GainEsperance> gainEsperances) {
        System.out.println("applyingEsperances");
        for (GainEsperance gainEsperance : gainEsperances) {
            if (gainEsperance.cache.currentCapacity >= gainEsperance.video.size
                    && !gainEsperance.cache.videos.contains(gainEsperance.video)) {
                gainEsperance.cache.currentCapacity -= gainEsperance.video.size;
                gainEsperance.cache.videos.add(gainEsperance.video);
            }
        }
    }

    private static void orderByMostGain(List<GainEsperance> gainEsperances) {
        gainEsperances.sort((o1, o2) -> Double.compare(o2.gain, o1.gain));
    }

    private static int removeRequestAlreadySatisfiedAndGainWithNoRequest(List<GainEsperance> gainEsperances) {
        int totalNumberOfRequestLeft = 0;
        for (int esperanceIndex = gainEsperances.size() - 1; esperanceIndex >= 0; esperanceIndex--) {
            GainEsperance gainEsperance = gainEsperances.get(esperanceIndex);
            ArrayList<RequestFromEndPointToCache> associatedRequests = gainEsperance.associatedRequests;
            for (int requestIndex = associatedRequests.size() - 1; requestIndex >= 0; requestIndex--) {
                RequestFromEndPointToCache candidate = associatedRequests.get(requestIndex);
                boolean isAlreadySatisfied = false;
                for (Latency latency : candidate.endPoint.latencies) {
                    if (latency.cache.videos.contains(candidate.video) && latency.value < candidate.latencyValue) {
                        isAlreadySatisfied = true;
                        break;
                    }
                }

                if (isAlreadySatisfied) {
                    associatedRequests.remove(requestIndex);
                }
            }

            int numberOfRequestLeft = associatedRequests.size();
            totalNumberOfRequestLeft += numberOfRequestLeft;
            if (numberOfRequestLeft == 0) {
                gainEsperances.remove(esperanceIndex);
            }
        }

        // System.out.println("removeRequestAlreadySatisfiedAndGainWithNoRequest "
        //        + gainEsperances.size() + " gain esperances left for "
        //        + totalNumberOfRequestLeft + " total request left");
        return totalNumberOfRequestLeft;
    }

    private static void recomputeGainEsperancesGain(List<GainEsperance> gainEsperances) {
        gainEsperances.parallelStream()
                .forEach(gainEsperance -> {
                    gainEsperance.gain = 0;
                    for (RequestFromEndPointToCache request : gainEsperance.associatedRequests) {
                        gainEsperance.gain += request.gain;
                    }
                });
    }

    private static List<GainEsperance> createGainEsperances(List<RequestFromEndPointToCache> allRequests) {
        int numberOfRequests = allRequests.size();
        System.out.println("creating GainEsperances for " + numberOfRequests + " requests");

        ConcurrentMap<Pair<Video, Cache>, List<RequestFromEndPointToCache>> map =
                allRequests.parallelStream().collect(Collectors.groupingByConcurrent(requestFromEndPointToCache
                        -> new Pair<>(requestFromEndPointToCache.video, requestFromEndPointToCache.cache)));

        ArrayList<GainEsperance> gainEsperances = new ArrayList<>();
        map.forEach((videoCachePair, requestFromEndPointToCaches) -> {
            GainEsperance gainEsperance = new GainEsperance(videoCachePair.key, videoCachePair.value);
            gainEsperance.associatedRequests.addAll(requestFromEndPointToCaches);
            gainEsperances.add(gainEsperance);
        });

        System.out.println(gainEsperances.size() + " gain esperances created");
        return new ArrayList<>(gainEsperances);
    }


    private static List<GainEsperance> createGainEsperancesInParallel(List<RequestFromEndPointToCache> allRequests) {
        int numberOfRequests = allRequests.size();
        System.out.println("creating GainEsperances for " + numberOfRequests + " requests");

        List<GainEsperance> gainEsperances = allRequests.stream()
                .collect(Collectors.groupingBy(request -> request.cache))
                .entrySet()
                .parallelStream()
                .map(cacheListEntry -> {
                    final Cache cache = cacheListEntry.getKey();
                    List<RequestFromEndPointToCache> requests = cacheListEntry.getValue();
                    System.out.println("creating GainEsperances for cache " + cache.id + " and " + requests.size() + " requests");

                    Map<Video, List<RequestFromEndPointToCache>> requestGroupByVideo = requests.stream()
                            .collect(Collectors.groupingBy(request -> request.video));

                    int numberOfVideos = requestGroupByVideo.size();
                    List<GainEsperance> gainEsperancesForCache = new ArrayList<>(numberOfVideos);

                    requestGroupByVideo.forEach((video, requestFromEndPointToCaches) -> {
                        GainEsperance gainEsperance = new GainEsperance(video, cache);
                        gainEsperance.associatedRequests.addAll(requestFromEndPointToCaches);
                        gainEsperancesForCache.add(gainEsperance);
                    });

                    return gainEsperancesForCache;
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        System.out.println(gainEsperances.size() + " gain esperances created");
        return gainEsperances;
    }


    private static List<GainEsperance> getGainEsperances(List<RequestFromEndPointToCache> allRequests, double numberOfRequestInTotal) {
        int size = allRequests.size();
        System.out.println("getGainEsperances for " + size);
        Map<Pair<Video, Cache>, GainEsperance> esperanceHashMap = new HashMap<Pair<Video, Cache>, GainEsperance>();

        int i = 0;
        Iterator<RequestFromEndPointToCache> iterator = allRequests.iterator();
        while (iterator.hasNext()) {
            RequestFromEndPointToCache request = iterator.next();
            boolean requestAlreadyServedBetter = false;
            ArrayList<Latency> latencies = request.endPoint.latencies;
            for (Latency latency : latencies) {
                if (latency.cache.videos.contains(request.video) && latency.value < request.latencyValue) {
                    requestAlreadyServedBetter = true;
                    break;
                }
            }

            if (!requestAlreadyServedBetter) {
                GainEsperance gainEsperance = getGainEsperanceForVideoAndCache(esperanceHashMap, request.video, request.cache);
                float timeSaved = request.number * (float) (request.endPoint.latencyFromDataCenter - request.latencyValue);
                gainEsperance.gain += timeSaved / request.video.size / numberOfRequestInTotal;
                gainEsperance.associatedRequests.add(request);
            } else {
                iterator.remove();
            }

            i++;
            if (i % (size / 10) == 0) {
                // System.out.println("getGainEsperances " + i + " / " + size);
            }
        }

        return new ArrayList<GainEsperance>(esperanceHashMap.values());
    }

    private static GainEsperance getGainEsperanceForVideoAndCache(Map<Pair<Video, Cache>, GainEsperance> gainEsperances, Video video, Cache cache) {
        Pair<Video, Cache> videoCachePair = new Pair<>(video, cache);
        GainEsperance gainEsperance = gainEsperances.get(videoCachePair);

        if (gainEsperance == null) {
            gainEsperance = new GainEsperance(video, cache);
            gainEsperances.put(videoCachePair, gainEsperance);
        }

        return gainEsperance;
    }

    private static GainEsperance getGainEsperanceForVideoAndCache(List<GainEsperance> gainEsperances, Video video, Cache cache) {
        for (GainEsperance gainEsperance : gainEsperances) {
            if (gainEsperance.cache.id == cache.id && gainEsperance.video.id == video.id) {
                return gainEsperance;
            }
        }
        GainEsperance gainEsperance = new GainEsperance(video, cache);
        gainEsperances.add(gainEsperance);
        return gainEsperance;
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
                if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                    if (!cache.videos.contains(request.video)) {
                        cache.videos.add(request.video);
                        cache.currentCapacity -= request.video.size;
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
                if (cache.currentCapacity > 0 && cache.currentCapacity - request.video.size > 0) {
                    if (!cache.videos.contains(request.video)) {
                        cache.videos.add(request.video);
                        cache.currentCapacity -= request.video.size;
                    }
                }
            }
        }
    }


    public static void cacheMostVideosAsPossible(ArrayList<Cache> caches, ArrayList<Request> requests) {
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

    private static void resetCache(ArrayList<Cache> caches) {
        for (Cache cache : caches) {
            cache.videos.clear();
            cache.currentCapacity = cache.initialCapacity;
        }
    }

    public static long computeScore(List<Request> requests) {
        System.out.println("computing score");
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
        public final List<Video> videos;
        public final List<Cache> caches;
        public final List<Request> requests;
        public final List<EndPoint> endPoints;

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

    public static final class Cache {
        public final int id;
        public final int initialCapacity;
        public final ArrayList<Video> videos;
        public int currentCapacity;

        public Cache(int id, int initialCapacity) {
            this.id = id;
            this.initialCapacity = initialCapacity;
            this.currentCapacity = initialCapacity;
            this.videos = new ArrayList<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cache cache = (Cache) o;

            return id == cache.id;
        }

        @Override
        public int hashCode() {
            return id;
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
        public final double gain;

        public RequestFromEndPointToCache(Video video, EndPoint endPoint, Cache cache, int number, int latencyValue) {
            this.video = video;
            this.endPoint = endPoint;
            this.cache = cache;
            this.number = number;
            this.latencyValue = latencyValue;
            double timeSaved = number * (double) (endPoint.latencyFromDataCenter - latencyValue);
            this.gain = timeSaved / video.size;
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

    public static class GainEsperance {
        public final Video video;
        public final Cache cache;
        public final ArrayList<RequestFromEndPointToCache> associatedRequests;
        public double gain;

        public GainEsperance(Video video, Cache cache) {
            this.video = video;
            this.cache = cache;
            this.gain = 0;
            this.associatedRequests = new ArrayList<RequestFromEndPointToCache>();
        }
    }


    private static void computeRequestFromEndPointToCache(String fileNameIn) {
        final Problem problem = parseProblem(fileNameIn);
        ArrayList<List<RequestFromEndPointToCache>> allRequests = new ArrayList<List<RequestFromEndPointToCache>>();
        for (Cache cache : problem.caches) {
            System.out.println("Computing request from end point to cache " + cache.id);
            List<RequestFromEndPointToCache> requestList = getRequestToCache(cache, problem.requests);
            allRequests.add(requestList);
        }

        writeRequestFromEndPointToCache(fileNameIn + "requests_from_endpoint_to_cache", allRequests);
    }

    private static void writeRequestFromEndPointToCache(String fileName, List<List<RequestFromEndPointToCache>> allRequests) {
        try {
            PrintWriter writer = new PrintWriter(fileName);

            int totalSize = 0;
            for (List<RequestFromEndPointToCache> requests : allRequests) {
                totalSize += requests.size();
            }
            System.out.println("writeRequestFromEndPointToCache " + fileName + "(entries: " + totalSize + ")");

            writer.println(totalSize);

            for (List<RequestFromEndPointToCache> requests : allRequests) {
                for (RequestFromEndPointToCache request : requests) {
                    writer.println(request.endPoint.id + " " + request.cache.id + " " + request.video.id +
                            " " + request.video.size + " " + request.number + " " + request.latencyValue);
                }
            }

            writer.close();
        } catch (IOException e) {
            System.out.println("error writing file " + e.getMessage());
        }
    }


    /**
     * Pair from javafx utils
     * <p>
     * javafx.util.Pair
     *
     * @param <K>
     * @param <V>
     */
    private static class Pair<K, V> implements Serializable {

        /**
         * Key of this <code>Pair</code>.
         */
        private K key;

        /**
         * Gets the key for this pair.
         *
         * @return key for this pair
         */
        public K getKey() {
            return key;
        }

        /**
         * Value of this this <code>Pair</code>.
         */
        private V value;

        /**
         * Gets the value for this pair.
         *
         * @return value for this pair
         */
        public V getValue() {
            return value;
        }

        /**
         * Creates a new pair
         *
         * @param key   The key for this pair
         * @param value The value to use for this pair
         */
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        /**
         * <p><code>String</code> representation of this
         * <code>Pair</code>.</p>
         * <p>
         * <p>The default name/value delimiter '=' is always used.</p>
         *
         * @return <code>String</code> representation of this <code>Pair</code>
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }

        /**
         * <p>Generate a hash code for this <code>Pair</code>.</p>
         * <p>
         * <p>The hash code is calculated using both the name and
         * the value of the <code>Pair</code>.</p>
         *
         * @return hash code for this <code>Pair</code>
         */
        @Override
        public int hashCode() {
            // name's hashCode is multiplied by an arbitrary prime number (13)
            // in order to make sure there is a difference in the hashCode between
            // these two parameters:
            //  name: a  value: aa
            //  name: aa value: a
            return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
        }

        /**
         * <p>Test this <code>Pair</code> for equality with another
         * <code>Object</code>.</p>
         * <p>
         * <p>If the <code>Object</code> to be tested is not a
         * <code>Pair</code> or is <code>null</code>, then this method
         * returns <code>false</code>.</p>
         * <p>
         * <p>Two <code>Pair</code>s are considered equal if and only if
         * both the names and values are equal.</p>
         *
         * @param o the <code>Object</code> to test for
         *          equality with this <code>Pair</code>
         * @return <code>true</code> if the given <code>Object</code> is
         * equal to this <code>Pair</code> else <code>false</code>
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof Pair) {
                Pair pair = (Pair) o;
                if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
                if (value != null ? !value.equals(pair.value) : pair.value != null) return false;
                return true;
            }
            return false;
        }
    }
}

import java.util.ArrayList;
import java.util.List;

/**
 * Qualification Round 2017
 */
public class Qualification {
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
        public final int latencyFromDataCenter;
        public final ArrayList<Latency> latencies;

        public EndPoint(int latencyFromDataCenter) {
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

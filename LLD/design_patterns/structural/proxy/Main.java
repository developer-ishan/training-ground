package structural.proxy;


public class Main {
    public static void main(String[] args) {
        RealVideoService realService = new RealVideoService();
        ProxyVideoService proxyService = new ProxyVideoService(realService);
        // Free user trying to watch a video
        proxyService.playVideo("free", "Free Video 1");
        // Premium user trying to watch a video
        proxyService.playVideo("premium", "Premium Video 1");
        // Unauthorized user
        proxyService.playVideo("guest", "Video 1");
        // Too many requests
        for (int i = 0; i < 6; i++) {
            proxyService.playVideo("free", "Free Video 2");
        }
    }
}

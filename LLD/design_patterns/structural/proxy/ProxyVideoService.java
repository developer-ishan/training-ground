package structural.proxy;

import java.util.HashMap;
import java.util.Map;

public class ProxyVideoService implements VideoServiceInterface {
  private RealVideoService realVideoService;
  private Map<String, String> cachedVideos = new HashMap<>();
  private Map<String, Integer> requestCounts = new HashMap<>();
  public ProxyVideoService(RealVideoService realVideoService) {
    this.realVideoService = realVideoService;
  }

  @Override
  public void playVideo(String userType, String videoId) {
    // Check user permissions
    if (!userType.equals("premium") && videoId.startsWith("Premium")) {
      System.out.println(
          "Access denied: Premium video requires a premium account.");
      return;
    }

    // Limit requests
    requestCounts.put(userType, requestCounts.getOrDefault(userType, 0) + 1);
    if (requestCounts.get(userType) > 5) {
      System.out.println("Access denied: Too many requests.");
      return;
    }

    // Caching logic
    if (cachedVideos.containsKey(videoId)) {
      System.out.println("Streaming cached video: " + videoId);
    } else {
      realVideoService.playVideo(userType, videoId);
      cachedVideos.put(videoId, videoId);
    }
  }
}


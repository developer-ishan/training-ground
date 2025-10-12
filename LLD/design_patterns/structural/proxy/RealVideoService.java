package structural.proxy;

public class RealVideoService implements VideoServiceInterface {

  @Override
  public void playVideo(String userType, String videoId) {
    System.out.println("Streaming video: " + videoId);  
  }
  
}

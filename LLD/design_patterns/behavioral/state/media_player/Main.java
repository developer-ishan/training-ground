package behavioral.state.media_player;

import behavioral.state.media_player.context.MediaPlayer;

public class Main {
    public static void main(String[] args) {
        MediaPlayer player = new MediaPlayer();

        player.play();
        player.pause();
        player.stop();

        player.pause();
    }
}

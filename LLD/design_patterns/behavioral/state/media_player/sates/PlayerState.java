package behavioral.state.media_player.sates;

import behavioral.state.media_player.context.MediaPlayer;

public interface PlayerState {
    public void play(MediaPlayer mediaPlayer);
    public void pause(MediaPlayer mediaPlayer);
    public void stop(MediaPlayer mediaPlayer);

    public String getState();
}

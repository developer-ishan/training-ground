package behavioral.state.media_player.sates;

import behavioral.state.media_player.context.MediaPlayer;

public class PlayingState implements PlayerState {
    @Override
    public void play(MediaPlayer mediaPlayer) {
        System.out.println("Already playing media!");
    }

    @Override
    public void pause(MediaPlayer mediaPlayer) {
        System.out.println("Paused playing media!");
        mediaPlayer.setCurrentState(new PausedState());
    }

    @Override
    public void stop(MediaPlayer mediaPlayer) {
        System.out.println("Stopped playing media!");
        mediaPlayer.setCurrentState(new StoppedState());
    }

    @Override
    public String getState() {
        return "PLAYING";
    }
}

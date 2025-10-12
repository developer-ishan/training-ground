package behavioral.state.media_player.sates;

import behavioral.state.media_player.context.MediaPlayer;

public class PausedState implements PlayerState {
    @Override
    public void play(MediaPlayer mediaPlayer) {
        System.out.println("Playing paused media!");
        mediaPlayer.setCurrentState(new PlayingState());
    }

    @Override
    public void pause(MediaPlayer mediaPlayer) {
        System.out.println("Already Paused!");
    }

    @Override
    public void stop(MediaPlayer mediaPlayer) {
        System.out.println("Stopping paused media!");
        mediaPlayer.setCurrentState(new StoppedState());
    }

    @Override
    public String getState() {
        return "PAUSED";
    }
}

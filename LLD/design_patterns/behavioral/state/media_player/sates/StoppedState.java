package behavioral.state.media_player.sates;

import behavioral.state.media_player.context.MediaPlayer;

public class StoppedState implements PlayerState {
    @Override
    public void play(MediaPlayer mediaPlayer) {
        System.out.println("Replaying media from start!");
        mediaPlayer.setCurrentState(new PlayingState());
    }

    @Override
    public void pause(MediaPlayer mediaPlayer) {
        System.out.println("Cannot pause stopped media!");
    }

    @Override
    public void stop(MediaPlayer mediaPlayer) {
        System.out.println("Already stopped!");
    }

    @Override
    public String getState() {
        return "STOPPED";
    }
}

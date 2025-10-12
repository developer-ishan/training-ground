package behavioral.state.media_player.context;

import behavioral.state.media_player.sates.PlayerState;
import behavioral.state.media_player.sates.StoppedState;

public class MediaPlayer {
    PlayerState currentState;

    public MediaPlayer() {
        currentState = new StoppedState();
    }

    public void setCurrentState(PlayerState currentState) {
        this.currentState = currentState;
    }

    public void display(){
        System.out.println(currentState.getState());
    }

    public void play(){
        currentState.play(this);
    }

    public void pause(){
        currentState.pause(this);
    }

    public void stop(){
        currentState.stop(this);
    }
}

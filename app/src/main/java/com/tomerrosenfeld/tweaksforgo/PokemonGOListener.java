package com.tomerrosenfeld.tweaksforgo;

public interface PokemonGOListener {
    long REFRESH_INTERVAL = 1000;

    void onStart();

    void onStop();

    boolean isGoRunning();
}

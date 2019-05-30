package nl.avans.wordcrex.controller.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.Controller;
import nl.avans.wordcrex.model.Character;
import nl.avans.wordcrex.model.Game;
import nl.avans.wordcrex.model.Tile;
import nl.avans.wordcrex.model.User;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.view.impl.GameView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class GameController extends Controller<Game> {
    public GameController(Main main, Function<User, Game> fn) {
        super(main, fn);
    }

    @Override
    public View<? extends Controller<Game>> createView() {
        return new GameView(this);
    }

    public String getScore() {
        return this.getModel().getHostScore() + " - " + this.getModel().getOpponentScore();
    }

    public String getHostName() {
        return this.getModel().host.username;
    }

    public String getOpponentName() {
        return this.getModel().opponent.username;
    }

    public List<Tile> getTiles() {
        return this.getModel().tiles;
    }

    public int getPoolSize() {
        return this.getModel().pool.size();
    }

    public void getNewHand() {

        this.getModel().startNewRound();
    }
}

package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.impl.ObserveController;
import nl.avans.wordcrex.model.Game;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.Fonts;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.ListWidget;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ObserveView extends View<ObserveController> {
    private final ListWidget<Game> list;

    public ObserveView(ObserveController controller) {
        super(controller);
        this.list = new ListWidget<>(
            0,
            96,
            "Geen spellen",
            (game) -> String.valueOf(game.id),
            (previous, next) -> {
                var label = this.controller.getLabel(next);

                if (previous != null && this.controller.getLabel(previous).equals(label)) {
                    return null;
                }

                return label;
            },
            (g, game) -> {
                var winner = game.winner != null ? game.winner : "";
                var round = game.getLastRound();
                var score = round == null ? " 0 - 0 " : (" " + round.hostScore + " - " + round.opponentScore + " ");
                var metrics = g.getFontMetrics();
                var width = metrics.stringWidth(score);

                g.setFont(Fonts.NORMAL);
                g.setColor(winner.equals(game.host) ? Colors.DARK_YELLOW : Color.WHITE);
                g.drawString(game.host, Main.TASKBAR_SIZE, 36);
                g.setFont(Fonts.SMALL);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString("tegen", Main.TASKBAR_SIZE, 52);
                g.setFont(Fonts.NORMAL);
                g.setColor(winner.equals(game.opponent) ? Colors.DARK_YELLOW : Color.WHITE);
                g.drawString(game.opponent, Main.TASKBAR_SIZE, 70);
                g.setColor(Colors.DARK_BLUE);
                g.fillRect(450 - width, 34, width, 28);
                g.setColor(Color.WHITE);
                g.drawString(score, 450 - width, 54);
            },
            this.controller::canClick,
            this.controller::clickGame
        );
    }

    @Override
    public void draw(Graphics2D g) {
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        this.list.setItems(this.controller.getGames());
    }

    @Override
    public List<Widget> children() {
        return List.of(
            this.list
        );
    }
}

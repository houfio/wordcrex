package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.impl.ApproveController;
import nl.avans.wordcrex.model.Word;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.Fonts;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.DialogWidget;
import nl.avans.wordcrex.widget.impl.ListWidget;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ApproveView extends View<ApproveController> {
    private final ListWidget<Word> list;
    private final DialogWidget dialog = new DialogWidget();

    public ApproveView(ApproveController controller) {
        super(controller);
        this.list = new ListWidget<>(
            0,
            96,
            "Geen suggesties",
            (word) -> word.word,
            (previous, next) -> null,
            (g, word) -> {
                var metrics = g.getFontMetrics();
                var dictionary = " " + word.dictionary.id + " ";
                var width = metrics.stringWidth(dictionary);

                g.setColor(Color.WHITE);
                g.drawString(word.word, Main.TASKBAR_SIZE, 44);
                g.setFont(Fonts.SMALL);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(word.username, Main.TASKBAR_SIZE, 60);
                g.setFont(Fonts.NORMAL);
                g.setColor(Colors.DARK_BLUE);
                g.fillRect(450 - width, 34, width, 28);
                g.setColor(Color.WHITE);
                g.drawString(dictionary, 450 - width, 54);
            },
            (word) -> true,
            (word) -> this.dialog.show("Accepteren?", "JA", "NEE", (positive) -> {
                if (positive) {
                    this.controller.accept(word);
                } else {
                    this.controller.decline(word);
                }
            })
        );
    }

    @Override
    public void draw(Graphics2D g) {
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        this.list.setItems(this.controller.getWords());
    }

    @Override
    public List<Widget> children() {
        return List.of(
            this.list,
            this.dialog
        );
    }
}

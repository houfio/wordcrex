package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.impl.RegisterController;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.particle.impl.TileParticle;
import nl.avans.wordcrex.util.Assets;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.StringUtil;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.ButtonWidget;
import nl.avans.wordcrex.widget.impl.InputWidget;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class RegisterView extends View<RegisterController> {
    private final ButtonWidget submitButton = new ButtonWidget("REGISTREER", 112, 312, 336, 32, this.controller::register);

    private int update;

    public RegisterView(RegisterController controller) {
        super(controller);
    }

    @Override
    public void draw(Graphics2D g) {
        this.submitButton.setEnabled(this.controller.isValid());

        if (this.controller.hasFailed()) {
            g.setColor(Colors.DARK_RED);
            g.fillRect(112, 344, 336, 32);
            g.setColor(Color.WHITE);
            StringUtil.drawCenteredString(g, 112, 344, 336, 32, "ongeldig");
        }
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        if (this.update++ % 5 != 0) {
            return;
        }

        addParticle.accept(new TileParticle(Main.RANDOM.nextInt(Main.FRAME_SIZE - 24), Main.RANDOM.nextFloat() - 0.5f, 10.0f + Main.RANDOM.nextFloat() * 5.0f));
    }

    @Override
    public List<Widget> children() {
        return List.of(
            new InputWidget("GEBRUIKERSNAAM", 64, 184, 384, 48, this.controller::setUsername),
            new InputWidget("WACHTWOORD", '*', 64, 248, 384, 48, this.controller::setPassword),
            new ButtonWidget(Assets.read("back"), null, 64, 312, 32, 32, this.controller::navigateBack),
            this.submitButton
        );
    }
}

package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.controller.impl.RegisterController;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.StringUtil;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.ButtonWidget;
import nl.avans.wordcrex.widget.impl.InputWidget;

import java.awt.*;
import java.util.List;

public class RegisterView extends View<RegisterController> {
    private final ButtonWidget submitButton = new ButtonWidget("REGISTER", 144, 312, 304, 48, this::register);
    private boolean invalid;

    public RegisterView(RegisterController controller) {
        super(controller);
    }

    @Override
    public void draw(Graphics2D g) {
        this.submitButton.setEnabled(this.controller.isValid());

        if (this.invalid) {
            g.setColor(Colors.DARK_RED);
            g.fillRect(144, 360, 304, 32);
            g.setColor(Color.WHITE);
            StringUtil.drawCenteredString(g, 144, 360, 304, 32, "invalid");
        }
    }

    @Override
    public void update() {
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(
            new InputWidget("USERNAME", 64, 184, 384, 48, this.controller::setUsername),
            new InputWidget("PASSWORD", '*', 64, 248, 384, 48, this.controller::setPassword),
            new ButtonWidget("<", 64, 312, 64, 48, this.controller::navigateLogin),
            this.submitButton
        );
    }

    private void register() {
        this.invalid = !this.controller.register();
    }
}
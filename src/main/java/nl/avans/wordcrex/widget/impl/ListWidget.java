package nl.avans.wordcrex.widget.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.widget.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ListWidget<T> extends Widget {
    private final ScrollbarWidget scrollbar = new ScrollbarWidget((scroll) -> this.scroll = scroll);
    private final int y;
    private final int height;
    private final BiConsumer<Graphics2D, T> draw;
    private final BiFunction<T, T, String> header;
    private final Function<T, String> getId;
    private final Function<T, Boolean> canClick;
    private final Consumer<T> click;

    private List<T> items = new ArrayList<>();
    private int scroll;
    private String hover;
    private String selected;
    private int selectedId;
    public ListWidget(int y, int height, BiConsumer<Graphics2D, T> draw, BiFunction<T, T, String> header, Function<T, String> getId, Function<T, Boolean> canClick, Consumer<T> click) {
        this.y = y;
        this.height = height;
        this.draw = draw;
        this.header = header;
        this.getId = getId;
        this.canClick = canClick;
        this.click = click;
    }

    @Override
    public void draw(Graphics2D g) {
        var offset = this.y;
        var count = 0;

        for (var i = 0; i < this.items.size(); i++) {
            var item = this.items.get(i);
            var position = this.height * i + offset - this.scroll + Main.TASKBAR_SIZE;
            var header = this.getHeader(i);

            if (header != null) {
                g.setColor(Colors.DARK_BLUE);
                g.fillRect(0, position, Main.FRAME_SIZE - Main.TASKBAR_SIZE, 64);
                g.setColor(Colors.DARK_YELLOW);
                g.drawString(header, Main.TASKBAR_SIZE, position + 38);

                offset += 64;
                position += 64;
            }

            if (this.getId.apply(item).equals(this.hover)) {
                g.setColor(Colors.DARKERER_BLUE);
                g.fillRect(0, position, Main.FRAME_SIZE - Main.TASKBAR_SIZE, this.height);
            }

            g.translate(0, position);
            this.draw.accept(g, item);
            g.translate(0, -position);

            if (i < this.items.size() - 1) {
                g.setColor(Colors.DARKERER_BLUE);
                g.fillRect(Main.TASKBAR_SIZE * 2 + 42, position + this.height - 2, 268, 4);
            }

            count++;
        }

        this.scrollbar.setHeight(count * this.height + offset);
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
    }

    @Override
    public void mouseMove(int x, int y) {
        this.hover = null;

        if (x > Main.FRAME_SIZE - Main.TASKBAR_SIZE || y < Main.TASKBAR_SIZE) {
            return;
        }

        var offset = this.y;

        for (var i = 0; i < this.items.size(); i++) {
            var item = this.items.get(i);
            var position = this.height * i + offset - this.scroll + Main.TASKBAR_SIZE;

            if (this.getHeader(i) != null) {
                offset += 64;
                position += 64;
            }

            if (y > position && y < position + this.height && this.canClick.apply(item)) {
                this.hover = this.getId.apply(item);

                break;
            }
        }
    }

    @Override
    public void mouseClick(int x, int y) {
        if (this.hover == null) {
            return;
        }

        this.click.accept(this.items.stream()
            .filter((item) -> this.getId.apply(item).equals(this.hover))
            .findFirst()
            .orElse(null));
    }

    @Override
    public List<Widget> children() {
        return List.of(
            this.scrollbar
        );
    }

    @Override
    public void keyPress(int code, int modifiers) {
        if(code == KeyEvent.VK_UP){

           if(selected == null){
               selected = this.getId.apply(items.get(0));
           }else{
               if((selectedId-1) >= 0){
                   selectedId--;
               }
               selected = this.getId.apply(items.get(selectedId));
           }
            System.out.println(selected);
        }

        if(code == KeyEvent.VK_DOWN){
            if(selected == null){
                System.out.println("selected is empty");
                selected = this.getId.apply(items.get(0));
            }else{
                if((selectedId+1) < items.size()){
                    selectedId++;
                }
                selected = this.getId.apply(items.get(selectedId));
            }
            System.out.println(selected);
        }

        if(code == KeyEvent.VK_ENTER){
            this.click.accept(this.items.stream()
                    .filter((item) -> this.getId.apply(item).equals(this.selected))
                    .findFirst()
                    .orElse(null));
        }
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    private String getHeader(int i) {
        var previous = i > 0 ? this.items.get(i - 1) : null;

        return this.header.apply(previous, this.items.get(i));
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}

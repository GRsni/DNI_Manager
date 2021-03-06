package uca.esi.dni.ui;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;
import processing.event.MouseEvent;

public abstract class BaseElement {

    protected final PApplet parent;
    protected PVector pos;
    protected int w;
    protected int h;

    protected boolean clicked = false;
    protected boolean hover = false;
    protected boolean isVisible = true;

    protected PFont font;
    protected int fontSize = 10;

    public static class Rectangle {
        public final float x;
        public final float y;
        public final int w;
        public final int h;

        public Rectangle(float x, float y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

    }


    protected BaseElement(PApplet parent, Rectangle rectangle) {
        this.parent = parent;
        this.pos = new PVector(rectangle.x, rectangle.y);
        this.w = rectangle.w;
        this.h = rectangle.h;
    }

    public PFont getFont() {
        return font;
    }

    public void setFont(PFont font) {
        this.font = font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public abstract void display();

    public boolean inside(int x, int y) {
        return x > pos.x && x < pos.x + w && y > pos.y && y < pos.y + h;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void isClicked(boolean val) {
        clicked = val;
    }

    public boolean isHover() {
        return hover;
    }

    public void isHover(boolean val) {
        hover = val;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public void handleInput(MouseEvent e) {
    }

    public void resize(PVector newPos, int newWidth, int newHeight) {
        this.pos = newPos;
        this.w = newWidth;
        this.h = newHeight;
    }

}

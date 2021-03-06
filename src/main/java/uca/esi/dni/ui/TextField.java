package uca.esi.dni.ui;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import uca.esi.dni.views.View;

public class TextField extends BaseElement {

    private int backgroundColor;
    private boolean hasBackground = false;
    private int contentColor = View.COLORS.BLACK;
    private int hintColor = View.COLORS.ACCENT_DARK;
    private String content;
    private final String hint;

    private boolean hasMaxLength = false;
    private int maxLength;
    private static final int PADDING = 4;
    private boolean isClickable = false;
    private boolean isFocused = false;
    private boolean isHeader = false;
    private boolean isCentered = false;
    private boolean isScrollable = false;
    private boolean isCuttable = false;


    public TextField(PApplet parent, Rectangle rectangle, String content, String hint) {
        super(parent, rectangle);
        this.content = content;
        this.hint = hint;
    }

    public PVector getPos() {
        return pos;
    }

    public void setPos(float x, float y) {
        this.pos = new PVector(x, y);
    }

    public float getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public float getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getContentColor() {
        return contentColor;
    }

    public void setContentColor(int contentColor) {
        this.contentColor = contentColor;
    }

    public int getHintColor() {
        return hintColor;
    }

    public void setHintColor(int hintColor) {
        this.hintColor = hintColor;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        hasBackground = true;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        hasMaxLength = true;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean header) {
        isHeader = header;
    }

    public boolean isCentered() {
        return isCentered;
    }

    public void setCentered(boolean centered) {
        isCentered = centered;
    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean scrollable) {
        isScrollable = scrollable;
    }

    public boolean isCuttable() {
        return isCuttable;
    }

    public void setCuttable(boolean cuttable) {
        isCuttable = cuttable;
    }

    public void display() {
        renderBackground();
        renderContent();
        renderCursor();
    }

    private void renderBackground() {
        parent.push();
        if (hasBackground) {
            parent.noStroke();
            parent.fill(backgroundColor);
            if (isHeader) {
                parent.rect(pos.x, pos.y, w, h, 4, 4, 0, 0);
            } else {
                parent.rect(pos.x, pos.y, w, h);
            }
        }
        parent.pop();
    }

    private void renderContent() {
        parent.push();
        parent.textSize(fontSize);
        parent.textFont(font);
        if (!isCentered) {
            parent.textAlign(PConstants.LEFT, PConstants.CENTER);
        } else {
            parent.textAlign(PConstants.CENTER, PConstants.CENTER);
        }
        String text = getContentTextAndFill();
        text = getFinalCutScrollText(text);
        parent.text(text, pos.x + getXOffset(), pos.y, w, h);
        parent.pop();
    }

    private String getContentTextAndFill() {
        String text;
        if (!content.isEmpty() || isFocused) {
            parent.fill(contentColor);
            text = content;
        } else {
            parent.fill(hintColor);
            text = hint;
        }
        return text;
    }

    private String getFinalCutScrollText(String text) {
        if (parent.textWidth(text) > w) {
            if (isCuttable) {
                text = text.substring(0, maxNumCharacters());
                text += "...";
            } else if (isScrollable) {
                text = text.substring(text.length() - maxNumCharacters());
            }
        }
        return text;
    }

    private float getXOffset() {
        if (isCentered) {
            return 0;
        } else {
            return PADDING;
        }
    }

    private int maxNumCharacters() {
        return w / (font.getSize() / 2);
    }

    private void renderCursor() {
        parent.push();
        if (isFocused && (parent.frameCount >> 5 & 1) == 0) {
            parent.stroke(View.COLORS.BLACK);
            parent.strokeWeight(3);
            parent.strokeCap(PConstants.SQUARE);
            parent.textFont(font);
            float xCursorPos = getCursorXPos();
            float xOffset = pos.x + PADDING + xCursorPos;
            parent.line(xOffset, pos.y + PADDING, xOffset, pos.y + h - PADDING);
        }
        parent.pop();
    }

    private float getCursorXPos() {
        return Math.max(0, Math.min(w, parent.textWidth(content)));
    }


    public void addCharToContent(char k) {
        if (hasMaxLength) {
            if (content.length() < maxLength) {
                content += k;
            }
        } else {
            content += k;
        }
    }

    public void removeCharacter() {
        content = content.substring(0, Math.max(0, content.length() - 1));
    }

    public void modifyCounter(int number) {
        modifyCounter(Integer.toString(number));
    }

    public void modifyCounter(float number) {
        modifyCounter(Float.toString(number));
    }

    public void modifyCounter(String end) {
        content = content.substring(0, content.lastIndexOf(':') + 2) + end;
    }
}

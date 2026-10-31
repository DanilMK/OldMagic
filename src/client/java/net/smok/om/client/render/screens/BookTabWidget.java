package net.smok.om.client.render.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.smok.om.Values;

public class BookTabWidget extends Button {
    
    private final Direction direction;
    private final Renderable overLayer;
    private final int color;



    public static BookTabWidget left(int x, int y, int color, Component message, OnPress onPress, Renderable overLayer) {
        return new BookTabWidget(x, y, color, message, onPress, overLayer, Direction.LEFT);
    }
    
    public static BookTabWidget right(int x, int y, int color, Component message, OnPress onPress, Renderable overLayer) {
        return new BookTabWidget(x, y, color, message, onPress, overLayer, Direction.RIGHT);
    }
    
    public static BookTabWidget top(int x, int y, int color, Component message, OnPress onPress, Renderable overLayer) {
        return new BookTabWidget(x, y, color, message, onPress, overLayer, Direction.TOP);
    }
    
    public static BookTabWidget down(int x, int y, int color, Component message, OnPress onPress, Renderable overLayer) {
        return new BookTabWidget(x, y, color, message, onPress, overLayer, Direction.DOWN);
    }

    protected BookTabWidget(int x, int y, int color, Component message, OnPress onPress, Renderable overLayer, Direction direction) {
        super(x, y, direction.width, direction.height, message, onPress, DEFAULT_NARRATION);
        this.direction = direction;
        this.overLayer = overLayer;
        this.color = color;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, direction.identifier, getX(), getY(), direction.width, direction.height, color);
        overLayer.extractRenderState(graphics, mouseX, mouseY, a);
    }
    
    public enum Direction {
        TOP(Values.id("widget/spell_book_button_top"), 24, 32),
        DOWN(Values.id("widget/spell_book_button_down"), 24, 32),
        LEFT(Values.id("widget/spell_book_button_left"), 32, 24),
        RIGHT(Values.id("widget/spell_book_button_right"), 32, 24);
        
        public final Identifier identifier;
        public final int width;
        public final int height;

        Direction(Identifier identifier, int width, int height) {
            this.identifier = identifier;
            this.width = width;
            this.height = height;
        }
    }
}

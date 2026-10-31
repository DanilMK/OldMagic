package net.smok.om.spell;

import java.util.List;

public class SlotButton {

    public static final int RM = 4;
    public static final List<Integer> EXTRA = List.of(0, 1, 2, 3);


    public static boolean isShown(int button) {
        return EXTRA.contains(button);
    }
}

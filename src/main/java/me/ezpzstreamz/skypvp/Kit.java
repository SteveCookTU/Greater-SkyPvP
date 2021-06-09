package me.ezpzstreamz.skypvp;

public class Kit {

    private final String[] items;
    private final boolean replace;
    private final boolean equip;

    public Kit(String[] i, boolean r, boolean e) {
        items = i;
        replace = r;
        equip = e;
    }

    public String[] getItems() {
        return items;
    }

    public boolean shouldReplace() {
        return replace;
    }

    public boolean shouldEquip() {
        return equip;
    }

}

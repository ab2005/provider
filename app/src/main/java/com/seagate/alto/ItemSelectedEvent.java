package com.seagate.alto;

// add a class header comment here

public class ItemSelectedEvent {

    private int mPosition;

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public ItemSelectedEvent(int position) {
        mPosition = position;
    }
}

package com.example.vb.sticklightpump;

public class Balloon {
    private int fillLevel;
    private final static int maxFillLevel = 100;
    private int fillStepSize = 1;

    public void pump() {
        fillLevel+=fillStepSize;
        if (fillLevel > maxFillLevel) fillLevel = maxFillLevel;
    }

    public int getFillLevel() {
        return fillLevel;
    }

    public boolean isFull() {
        return fillLevel == maxFillLevel;
    }

    public void reset() {
        fillLevel = 0;
    }

    public int getFillStepSize() {
        return fillStepSize;
    }

    public void setFillStepSize(int fillStepSize) {
        this.fillStepSize = fillStepSize;
    }
}

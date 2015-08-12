package com.example.vb.sticklightpump;

public class Led {
    private byte[] rgb;
    public Led() {
        rgb = new byte[3];
    }
    public void setRgb(byte r, byte g, byte b) {
        rgb[0] = r;
        rgb[1] = g;
        rgb[2] = b;
    }
    public byte[] bytes() {
        return rgb;
    }
}

package com.example.vb.sticklightpump;

public class Stick {
    private final static int HEADER_LEN = 4;
    private final static int NUMBER_LEDS = 60;
    private Led[] leds;
    private String address;
    public Stick(String id) {
        this.leds = new Led[NUMBER_LEDS];
        for(int i = 0; i < this.leds.length;++i)
        {
            this.leds[i] = new Led();
        }
        setId(id);
    }
    public void setId(String id) {
        StringBuilder builder = new StringBuilder();
        builder.append("192.168.23.");
        builder.append(id);
        this.address = builder.toString();
    }

    public void setLedRgb(int ledIndex, byte r, byte g, byte b) {
        this.leds[ledIndex].setRgb(r, g, b);
    }

    public void setLedRgbRange(int r, int g, int b, int indexFrom, int indexTo)
    {
        for (int i = indexFrom; i <= indexTo; ++i) {
            this.leds[i].setRgb((byte)r, (byte)g, (byte)b);
        }
    }

    public void setAllRgb(byte r, byte g, byte b) {
        for (Led led : this.leds) {
            led.setRgb(r, g, b);
        }
    }

    byte[] bytes() {
        byte[] result = new byte[HEADER_LEN + 3 * leds.length];
        for(int i = 0; i < this.leds.length;++i) {
            System.arraycopy(this.leds[i].bytes(), 0, result, HEADER_LEN + (i * 3), 3);
        }
        return result;
    }

    public String getAddress() {
        return address;
    }
}

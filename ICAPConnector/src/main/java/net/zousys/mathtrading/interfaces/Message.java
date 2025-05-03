package net.zousys.mathtrading.interfaces;

public interface Message {
    public <T> T message();

    public String getType();

    public String getId();
}

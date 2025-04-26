package net.zousys.mathtrading.interfaces;

public interface Source {
    public void startDeamon();
    public void checkSession();
    public void pollingPush(Message message);
}

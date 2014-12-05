package kr.kdev.dg1s.cards;

public interface CardViewStatusNotifier {

    public static final int FAILURE = 0x01;
    public static final int SUCCESS = 0x00;

    public void notifyCompletion(Object object, int status);
}

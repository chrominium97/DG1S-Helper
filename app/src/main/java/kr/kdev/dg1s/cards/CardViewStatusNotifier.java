package kr.kdev.dg1s.cards;

public interface CardViewStatusNotifier {

    public static final int FAILURE = 1;
    public static final int SUCCESS = 0;

    public void notifyCompletion(int status);
}

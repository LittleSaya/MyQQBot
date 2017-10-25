package qqbot_botsaya.executors.core_threads;

public class BaseThread extends Thread
{
    // thread control
    public static final Object invokeObj = new Object();
    protected boolean suspend;
    protected boolean quit;

    public void setSuspend()
    {   suspend = true; }

    public void unsetSuspend()
    {   suspend = false; }

    public void setQuit()
    {   quit = true; }

    public void unsetQuit()
    {   quit = false; }

    protected BaseThread(String name)
    {   super(name); }
}

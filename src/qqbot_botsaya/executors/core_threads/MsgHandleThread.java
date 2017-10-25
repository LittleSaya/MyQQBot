package qqbot_botsaya.executors.core_threads;

import application.classes.TargetedMessage;
import application.modules.Mod_TimeAndDate;
import qqbot_botsaya.executors.AuxiliaryFunctions;
import qqbot_botsaya.value_store.IntegerValue;

import java.util.concurrent.LinkedBlockingQueue;

public class MsgHandleThread extends BaseThread
{
    public static LinkedBlockingQueue<TargetedMessage> inMsgQueue = new LinkedBlockingQueue<>();

    private long leisureTime = 0;
    public static final long LEISURE_TIME_LIMIT = 600 * 1000;// 10min
    public static final long DETECT_INTERVAL = 300;// 0.3s

    private static int instanceCount = 0;

    private MsgHandleThread()
    {   super("MsgHandleThread"); }

    public static MsgHandleThread createMsgHandleThread()
    {
        if (0 == instanceCount)
        {
            ++instanceCount;
            return new MsgHandleThread();
        }
        else
        {   return null; }
    }

    @Override
    public void run()
    {
        synchronized (invokeObj)
        {
            while (suspend)
            {
                try
                {   invokeObj.wait(); }
                catch (InterruptedException e)
                {   e.printStackTrace(); }
            }
        }
        run_internal();
    }

    private void run_internal()
    {
        AuxiliaryFunctions.log("MsgHandleThread is running...");
        while (!quit)
        {
            waitForInvoking();
            AuxiliaryFunctions.log("thread been invoked");
            leisureTime = 0;
            while (!quit)
            {
                if (inMsgQueue.isEmpty())
                {
                    if (leisureTime > LEISURE_TIME_LIMIT)
                    {   break; }
                    leisureTime += waitForTime();
                }
                else
                {
                    leisureTime = 0;
                    guideMsg();
                }
            }
        }
        AuxiliaryFunctions.log("MsgHandleThread is exiting...");
    }

    private void guideMsg()
    {
        TargetedMessage inMsg = null;
        while ((inMsg = inMsgQueue.poll()) != null)
        {
            /*  消息分流逻辑
                目前不把模块写成多线程
                模块位于 /src/application/modules
             */
            int idx = 0;
            if ((idx = AuxiliaryFunctions.findStrInStrs(inMsg.content, Mod_TimeAndDate.dict)) >= 0)
            {
                AuxiliaryFunctions.log("guide msg to Mod_TimeAndDate");
                sendMsgToResponder(new TargetedMessage(
                    IntegerValue.uin,
                    TargetedMessage.TARGET_GROUP,
                    inMsg.targetId,
                    Mod_TimeAndDate.respond(idx)));
            }
            else
            {
                // do nothing
            }
        }
    }

    private void sendMsgToResponder(TargetedMessage outMsg)
    {
        RespondingThread.outMsgQueue.offer(outMsg);
        synchronized (RespondingThread.invokeObj)
        {   RespondingThread.invokeObj.notifyAll(); }
    }

    private void waitForInvoking()
    {
        synchronized (invokeObj)
        {
            try
            {   invokeObj.wait(); }
            catch (InterruptedException e)
            {   e.printStackTrace(); }
        }
    }

    private long waitForTime()
    {
        long timeStamp = System.currentTimeMillis();
        synchronized (invokeObj)
        {
            try
            {   invokeObj.wait(DETECT_INTERVAL); }
            catch (InterruptedException e)
            {   e.printStackTrace(); }
        }
        return System.currentTimeMillis() - timeStamp;
    }
}

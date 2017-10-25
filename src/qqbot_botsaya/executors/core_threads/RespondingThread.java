package qqbot_botsaya.executors.core_threads;

import application.classes.TargetedMessage;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import qqbot_botsaya.constants.StringConstants;
import qqbot_botsaya.executors.AuxiliaryFunctions;
import qqbot_botsaya.requests.name_value_pairs.Cookies;
import qqbot_botsaya.requests.uris.FixedURIs;
import qqbot_botsaya.value_store.StringValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class RespondingThread extends BaseThread
{
    public static LinkedBlockingQueue<TargetedMessage> outMsgQueue = new LinkedBlockingQueue<>();

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private HttpPost request;

    private long leisureTime = 0;
    public static final long LEISURE_TIME_LIMIT = 600 * 1000;// 10min
    public static final long DETECT_INTERVAL = 300;// 0.3s

    private static int instanceCount = 0;

    private RespondingThread()
    {
        super("RespondingThread");
        httpClient = null;
        response = null;
        request = null;
    }

    public static RespondingThread createRespondingThread()
    {
        if (0 == instanceCount)
        {
            ++instanceCount;
            return new RespondingThread();
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
        AuxiliaryFunctions.log("RespondingThread is running...");
        httpClient = HttpClients.createDefault();
        while (!quit)
        {
            // wait for invoking
            waitForInvoking();
            AuxiliaryFunctions.log("thread been invoked");
            leisureTime = 0;
            while (!quit)
            {
                if (outMsgQueue.isEmpty())
                {
                    if (leisureTime > LEISURE_TIME_LIMIT)
                    {   break; }
                    leisureTime += waitForTime();
                }
                else
                {
                    leisureTime = 0;
                    sendQueuedMessage();
                }
            }
        }
        AuxiliaryFunctions.closeClient(httpClient);
        AuxiliaryFunctions.log("RespondingThread is exiting...");
    }

    private void sendQueuedMessage()
    {
        TargetedMessage msg = null;
        while (null != (msg = outMsgQueue.poll()))
        {
            request = buildRequest(msg);
            if (!sendRequest())
            {   AuxiliaryFunctions.log("fail to send message"); }
            else
            {   AuxiliaryFunctions.log("msg sent: " + msg.content); }
        }
    }

    private HttpPost buildRequest(TargetedMessage msg)
    {
        HttpPost post = new HttpPost(FixedURIs.sendGroupMsg);
        // headers
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Cookie",
                new StringBuilder()
                        .append("RK=").append(Cookies.RK).append(';')
                        .append("p_skey=").append(Cookies.p_skey).append(';')
                        .append("p_uin=").append(Cookies.p_uin).append(';')
                        .append("pt2gguin=").append(Cookies.pt2gguin).append(';')
                        .append("pt4_token=").append(Cookies.pt4_token).append(';')
                        .append("ptcz=").append(Cookies.ptcz).append(';')
                        .append("ptisp=").append(Cookies.ptisp).append(';')
                        .append("skey=").append(Cookies.skey).append(';')
                        .append("uin=").append(Cookies.uin).append(';')
                        .toString()
        );
        post.addHeader("Origin", FixedURIs.sendGroupMsg_origin.toString());
        post.addHeader("Referer", FixedURIs.sendGroupMsg_referer.toString());
        post.addHeader("User-Agent", StringConstants.UserAgent);
        // JSONObject
        JSONObject jsonObject = new JSONObject()
                .put("group_uin", msg.targetId)
                .put("content", new JSONArray()
                        .put(msg.content)
                        .put(new JSONArray()
                                .put("font")
                                .put(new JSONObject()
                                        .put("name", "宋体")
                                        .put("size", 10)
                                        .put("style", new JSONArray().put(0).put(0).put(0))
                                        .put("color", "000000")
                                )
                        ).toString()
                )
                .put("face", 399)
                .put("clientid", 53999199)
                .put("msg_id", System.currentTimeMillis() / 1000)
                .put("psessionid", StringValue.psessionid);
        // entity
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("r", jsonObject.toString()));
        try
        {
            HttpEntity entity = new UrlEncodedFormEntity(params, "utf-8");
            post.setEntity(entity);
        }
        catch (UnsupportedEncodingException e)
        {   e.printStackTrace(); }
        return post;
    }

    private boolean sendRequest()
    {
        // send request
        try
        {   response = httpClient.execute(request); }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        // process response
        if (200 != response.getStatusLine().getStatusCode())
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response);
            return false;
        }
        // retcode
        int retcode = 0;
        try
        {   retcode = new JSONObject(EntityUtils.toString(response.getEntity())).getInt("retcode"); }
        catch (IOException|JSONException e)
        {
            e.printStackTrace();
            return false;
        }
        return (0 == retcode);
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

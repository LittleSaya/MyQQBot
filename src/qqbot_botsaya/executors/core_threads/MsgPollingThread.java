package qqbot_botsaya.executors.core_threads;

import application.classes.TargetedMessage;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
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
import qqbot_botsaya.value_store.IntegerValue;
import qqbot_botsaya.value_store.StringValue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MsgPollingThread extends BaseThread
{
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private TargetedMessage msg;
    private HttpPost request;

    // singleton pattern
    private static int instanceCount = 0;

    private MsgPollingThread()
    {
        super("MsgPollingThread");
        httpClient = null;
        response = null;
        suspend = true;
        quit = false;
        msg = null;
        request = null;
    }

    public static MsgPollingThread createMsgPollingThread()
    {
        if (0 == instanceCount)
        {
            instanceCount = 1;
            return new MsgPollingThread();
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
                AuxiliaryFunctions.log("thread is entering suspend state...");
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
        AuxiliaryFunctions.log("thread is running...");
        httpClient = HttpClients.createDefault();
        request = buildRequest();
        while (!quit)
        {
            // send request, receive response
            if (!longPoll())
            {
                AuxiliaryFunctions.log("false == longPoll()");
                continue;
            }
            // try to get a message from the response
            if (!extractMessage())
            {
                AuxiliaryFunctions.log("false == extractMessage()");
                continue;
            }
            // process
//            logMsg();
//            repeatMsg();
            sendMsgToHandler();
        }
        closeClient();
        AuxiliaryFunctions.log("thread was commanded to exit");
    }

    private HttpPost buildRequest()
    {
        HttpPost post = new HttpPost(FixedURIs.poll2);
        // config
        post.setConfig(RequestConfig.custom()
//                .setSocketTimeout(60000)
//                .setConnectTimeout(60000)
//                .setConnectionRequestTimeout(60000)
                .build());
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
        post.addHeader("Origin", FixedURIs.poll2_origin.toString());
        post.addHeader("Referer", FixedURIs.poll2_referer.toString());
        post.addHeader("User-Agent", StringConstants.UserAgent);
        // entity
        String paramName = "r";
        String paramValue = new JSONObject()
                .put("ptwebqq", "")
                .put("clientid", 53999199)
                .put("psessionid", StringValue.psessionid)
                .put("key", "").toString();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(paramName, paramValue));
        HttpEntity entity = null;
        try
        {   entity = new UrlEncodedFormEntity(params, "utf-8"); }
        catch (UnsupportedEncodingException e)
        {   e.printStackTrace(); }
        post.setEntity(entity);
        return post;
    }

    private boolean longPoll()
    {
        // send request
        try
        {   response = httpClient.execute(request); }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        // check response
        if (200 != response.getStatusLine().getStatusCode())
        {
            AuxiliaryFunctions.log("unexpected status code: " + response.getStatusLine().getStatusCode());
            AuxiliaryFunctions.consumeUnexpectedResponse(response);
            response = null;
        }
        return true;
    }

    private boolean extractMessage()
    {
        // extract the json obj from entity
        HttpEntity entity = response.getEntity();
        JSONObject jsonObject = null;
        String str = null;
        try
        {
            str = EntityUtils.toString(entity);
            AuxiliaryFunctions.log("rsp=" + str);
            jsonObject = new JSONObject(str);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            AuxiliaryFunctions.consumeEntityAndCloseResponse(entity, response);
            response = null;
        }
        // check if there is a msg
        JSONArray result = null;
        try
        {   result = jsonObject.getJSONArray("result"); }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return false;
        }
        // get poll_type, group_code, content
        try
        {
            String poll_type = result.getJSONObject(0).getString("poll_type");
            long group_code = result.getJSONObject(0).getJSONObject("value").getNumber("group_code").longValue();
            String content = result.getJSONObject(0).getJSONObject("value").getJSONArray("content").getString(1);
            long send_uin = result.getJSONObject(0).getJSONObject("value").getNumber("send_uin").longValue();
            msg = new TargetedMessage(send_uin, TargetedMessage.TARGET_GROUP, group_code, content);
            logMsg();
        }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean logMsg()
    {
        AuxiliaryFunctions.log("send_uin: " + msg.sourceId);
        AuxiliaryFunctions.log("poll_type: " + msg.targetType);
        AuxiliaryFunctions.log("group_code: " + msg.targetId);
        AuxiliaryFunctions.log("content: " + msg.content);
        return true;
    }

    private boolean repeatMsg()
    {
        if (IntegerValue.uin == msg.sourceId)
        {   return true; }
        RespondingThread.outMsgQueue.offer(msg);
        synchronized (RespondingThread.invokeObj)
        {   RespondingThread.invokeObj.notifyAll(); }
        return true;
    }

    private void sendMsgToHandler()
    {
        if (IntegerValue.uin == msg.sourceId)
        {   return; }
        MsgHandleThread.inMsgQueue.offer(msg);
        synchronized (MsgHandleThread.invokeObj)
        {   MsgHandleThread.invokeObj.notifyAll(); }
    }

    private void closeClient()
    {
        try
        { httpClient.close(); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("I/O Exception when httpClient.close()");
            e.printStackTrace();
        }
    }
}

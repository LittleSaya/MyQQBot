package qqbot_botsaya.executors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.*;
import qqbot_botsaya.constants.ErrorCodes;
import qqbot_botsaya.constants.IntegerConstants;
import qqbot_botsaya.constants.QRStateCode;
import qqbot_botsaya.constants.StringConstants;
import qqbot_botsaya.executors.core_threads.MsgHandleThread;
import qqbot_botsaya.executors.core_threads.MsgPollingThread;
import qqbot_botsaya.executors.core_threads.RespondingThread;
import qqbot_botsaya.requests.name_value_pairs.Cookies;
import qqbot_botsaya.requests.name_value_pairs.ReqParas;
import qqbot_botsaya.requests.uris.AlterableURIs;
import qqbot_botsaya.requests.uris.FixedURIs;
import qqbot_botsaya.value_store.IntegerValue;
import qqbot_botsaya.value_store.StringValue;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ClientManager
{
    private CloseableHttpClient httpClient;

    public ClientManager()
    {   httpClient = HttpClients.createDefault(); }

    public void launch()
    {
        AuxiliaryFunctions.log("ClientManager launched...");
        // login
        int err = 0;
        err = login();
        login_printErr(err);
        AuxiliaryFunctions.closeClient(httpClient);
        if (ErrorCodes.nLOGIN_ERR_NOERR != err)
        {   return; }
        // todo: get friends info
        // invoke MsgPollingThread, MsgGuidingThread, RespondingThread
        MsgPollingThread msgPollingThread = MsgPollingThread.createMsgPollingThread();
        MsgHandleThread msgHandleThread = MsgHandleThread.createMsgHandleThread();
        RespondingThread respondingThread = RespondingThread.createRespondingThread();
        if (null != msgPollingThread && null != respondingThread && null != msgHandleThread)
        {
            msgPollingThread.start();
            msgHandleThread.start();
            respondingThread.start();

            respondingThread.unsetSuspend();
            msgHandleThread.unsetSuspend();
            msgPollingThread.unsetSuspend();
        }
        // invoke ModuleThread_Time
    }

    private int login()
    {
        AuxiliaryFunctions.log("logining...");
        int err = 0;
        // login step #1: request for the QRCode Image
        // save qrsig and ptqrtoken
        err = queryQRCodeImg();
        queryQRCodeImg_printErr(err);
        if (ErrorCodes.nQRCODE_IMG_ERR_NOERR != err)
        {
            AuxiliaryFunctions.closeClient(httpClient);
            return ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_QRCODE_IMG;
        }
        // login step #2: request for the state of QRCode (loop)
        int URIFailRetryCount = 0;
        while (true)
        {
            err = queryQRCodeState();
            queryQRCodeState_printErr(err);
            if (ErrorCodes.nQRCODE_STATE_ERR_NULL_URI == err || ErrorCodes.nQRCODE_STATE_ERR_FAIL_TO_SET_URI == err)
            {
                // the state code shows that we have loged in successfully
                // but we cannot get a proper auth uri to continue the process
                AuxiliaryFunctions.log("fail to get a proper URI, URIFailRetryCount=" + (++URIFailRetryCount));
                if (URIFailRetryCount > IntegerConstants.AUTH_URI_FAIL_RETRY)
                {
                    AuxiliaryFunctions.closeClient(httpClient);
                    return ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE;
                }
                AuxiliaryFunctions.waitFor(IntegerConstants.QRCODE_STATE_QUERY_INTERVAL);
            }
            else if (ErrorCodes.nQRCODE_STATE_ERR_NOERR == err)
            {
                // we get the state code successfully (and even the auth uri)
                if (QRStateCode.BEFORE_SCAN_EFFECTIVE == IntegerValue.QRCodeState ||// waiting for scan
                        QRStateCode.AFTER_SCAN_BEFORE_IDENTITY == IntegerValue.QRCodeState)// waiting for identification
                {   AuxiliaryFunctions.waitFor(IntegerConstants.QRCODE_STATE_QUERY_INTERVAL); }
                else if (QRStateCode.BEFORE_SCAN_EFFECTIVELESS == IntegerValue.QRCodeState)
                {
                    // fail to login, QRCode timeout
                    AuxiliaryFunctions.closeClient(httpClient);
                    return ErrorCodes.nLOGIN_ERR_QRCODE_TIMEOUT;
                }
                else if (QRStateCode.AFTER_SCAN_AFTER_IDENTITY == IntegerValue.QRCodeState)// congrats
                {   break; }
                else// this shouldn't happen
                {
                    AuxiliaryFunctions.closeClient(httpClient);
                    return ErrorCodes.nLOGIN_ERR_UNDEFINED_QRSTATE_CODE;
                }
            }
            else// any other error is fatal
            {
                AuxiliaryFunctions.closeClient(httpClient);
                return ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE;
            }
        }
        // login step #3: request for other auth info
        err = queryOtherAuth();
        queryOtherAuth_printErr(err);
        if (ErrorCodes.nOTHER_AUTH_ERR_NOERR != err)
        {
            AuxiliaryFunctions.closeClient(httpClient);
            return ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_OTHER_AUTH;
        }
        AuxiliaryFunctions.log("logining...complete");
        // login step #4: request online buddies
        requestOnlineBuddies();
        return ErrorCodes.nLOGIN_ERR_NOERR;
    }

    private int queryQRCodeImg()
    {
        AuxiliaryFunctions.log("querying the QRCode image...");
        // build request
        HttpGet request = new HttpGet(FixedURIs.QRCodeImg);
        request.addHeader("user-agent", StringConstants.UserAgent);
        request.addHeader("referer", FixedURIs.QRCodeImg_referer.toString());
        AuxiliaryFunctions.log("queryQRCode() request line: " + request.getRequestLine());
        // send request
        CloseableHttpResponse response = null;
        try
        { response = httpClient.execute(request); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_NET;
        }
        Header[] cookies = response.getHeaders("set-cookie");// save the cookies ASA we receive the response
        // process response
        AuxiliaryFunctions.log("queryQRCode() response status line: " + response.getStatusLine());
        if (response.getStatusLine().getStatusCode() != 200)// check the status code
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response);
            return ErrorCodes.nQRCODE_IMG_ERR_UNEXPECT_CODE;
        }
        // save cookie: qrsig
        String[] cookieValues = AuxiliaryFunctions.extractCookieValues(cookies,
                new String[]{ "qrsig;" + StringConstants.DOMAIN_PTLOGIN2_QQ_COM });
        String qrsig = cookieValues[0];
        if (qrsig == null)// doesn't exist
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response);
            return ErrorCodes.nQRCODE_IMG_ERR_NO_COOKIE;
        }
        Cookies.qrsig = qrsig;
        AuxiliaryFunctions.log("qrsig=" + Cookies.qrsig);
        // calculate ptqrtoken
        ReqParas.ptqrtoken = AuxiliaryFunctions.hash33(qrsig);
        // get the img data
        HttpEntity entity = response.getEntity();
        byte[] imgData = null;
        try
        { imgData = EntityUtils.toByteArray(entity); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_NET;
        }
        finally
        { AuxiliaryFunctions.consumeEntityAndCloseResponse(entity, response); }
        // save the img
        File fout = new File(StringConstants.QRCodeFilePath);
        if (!fout.exists())
        {
            // try to create a new file
            try
            { fout.createNewFile(); }
            catch (IOException e)
            {
                e.printStackTrace();
                return ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_FS;
            }
        }
        // create the FileOutputStream
        FileOutputStream fos = null;
        try
        { fos = new FileOutputStream(fout); }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return ErrorCodes.nQRCODE_IMG_ERR_NO_FILE;
        }
        // write data to file
        try
        { fos.write(imgData); }
        catch (IOException e)
        {
            e.printStackTrace();
            // try to release the resource of fos
            try
            { fos.close(); }
            catch (IOException e1)
            {
                AuxiliaryFunctions.log("warning: I/O Exception occurred when fos.close()");
                e1.printStackTrace();
            }
            return ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_FS;
        }
        // close fos
        try
        { fos.close(); }
        catch (IOException e)
        {
            AuxiliaryFunctions.log("warning: I/O Exception occurred when fos.close()");
            e.printStackTrace();
        }
        AuxiliaryFunctions.log("querying the QRCode image...complete");
        return ErrorCodes.nQRCODE_IMG_ERR_NOERR;
    }

    // when this function returned with NOERR, and the state code is 0
    // we know that the authURI has been set correctly
    private int queryQRCodeState()
    {
        AuxiliaryFunctions.log("querying QRCode state...");
        // create request
        // ptqrtoken
        if (ReqParas.ptqrtoken == null)
        { return ErrorCodes.nQRCODE_STATE_ERR_NULL_TOKEN; }
        AlterableURIs.setURI_QRCodeState(ReqParas.ptqrtoken);
        HttpGet request = new HttpGet(AlterableURIs.getURI_QRCodeState());
        // cookie qrsig
        if (Cookies.qrsig == null)
        { return ErrorCodes.nQRCODE_STATE_ERR_NULL_COOKIE; }
        request.addHeader("cookie", "qrsig=" + Cookies.qrsig + ";");
        request.addHeader("user-agent", StringConstants.UserAgent);
        request.addHeader("referer", FixedURIs.QRCodeImg_referer.toString());// the 2 requests use the same referer
        AuxiliaryFunctions.log("queryQRCodeState() request line: " + request.getRequestLine());
        // send request
        CloseableHttpResponse response = null;
        try
        { response = httpClient.execute(request); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nQRCODE_STATE_ERR_IOEXCEPT_NET;
        }
        Header[] cookies = response.getHeaders("set-cookie");// save the cookies ASA we receive the response
        // check status code
        AuxiliaryFunctions.log("queryQRCodeState() response status line: " + response.getStatusLine());
        if (response.getStatusLine().getStatusCode() != 200)
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response);
            return ErrorCodes.nQRCODE_STATE_ERR_UNEXPECT_CODE;
        }
        // extract cookie from response
        // though they may redundant, keep these things if they do not cause problems
        String[] cookieValues = AuxiliaryFunctions.extractCookieValues(cookies,
                new String[]
                        {
                                "RK;" + StringConstants.DOMAIN_QQ_COM,
                                "ptcz;" + StringConstants.DOMAIN_QQ_COM,
                                "ptisp;" + StringConstants.DOMAIN_QQ_COM,
                                "skey;" + StringConstants.DOMAIN_QQ_COM,
                                "pt2gguin;" + StringConstants.DOMAIN_QQ_COM,
                                "uin;" + StringConstants.DOMAIN_QQ_COM
                        });
        Cookies.RK = cookieValues[0];
        Cookies.ptcz = cookieValues[1];
        Cookies.ptisp = cookieValues[2];
        Cookies.skey = cookieValues[3];
        Cookies.pt2gguin = cookieValues[4];
        Cookies.uin = cookieValues[5];
        AuxiliaryFunctions.log("RK=" + Cookies.RK);
        AuxiliaryFunctions.log("ptcz=" + Cookies.ptcz);
        AuxiliaryFunctions.log("ptisp=" + Cookies.ptisp);
        AuxiliaryFunctions.log("skey=" + Cookies.skey);
        AuxiliaryFunctions.log("pt2gguin=" + Cookies.pt2gguin);
        AuxiliaryFunctions.log("uin=" + Cookies.uin);
        // extract ptuiCB from entity
        HttpEntity entity = response.getEntity();
        String ptuiCB = null;
        try
        { ptuiCB = EntityUtils.toString(entity); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nQRCODE_STATE_ERR_IOEXCEPT_NET;
        }
        finally
        { AuxiliaryFunctions.consumeEntityAndCloseResponse(entity, response); }
        AuxiliaryFunctions.log(ptuiCB);
        StringValue.ptuiCB = ptuiCB;
        // extract state code from ptuiCB
        int stateCode = AuxiliaryFunctions.extractStateCodeFromPtuiCB(ptuiCB);
        IntegerValue.QRCodeState = stateCode;
        AuxiliaryFunctions.log("QRCodeState=" + stateCode);
        if (stateCode == 0)// login success
        {
            // extract auth uri from ptuiCB
            String authURI = AuxiliaryFunctions.extractAuthURIFromPtuiCB(ptuiCB);
            AuxiliaryFunctions.log("authURI=" + authURI);
            if (null == authURI)
            { return ErrorCodes.nQRCODE_STATE_ERR_NULL_URI; }// the login is successful, but we cannot get an auth uri
            else if (!AlterableURIs.setURI_authURI(authURI))// we get a uri, but it is not correct
            { return ErrorCodes.nQRCODE_STATE_ERR_FAIL_TO_SET_URI; }
        }
        AuxiliaryFunctions.log("querying QRCode state...complete");
        return ErrorCodes.nQRCODE_STATE_ERR_NOERR;
    }

    private int queryOtherAuth()
    {
        AuxiliaryFunctions.log("querying other auth...");
        // step #1: query the authURI
        AuxiliaryFunctions.log("querying authURI...");
        // build request
        /*
            Difference note:
                1.cookie: pgv_pvi, pgv_si
                2.URI
         */
        if (AlterableURIs.isNull_authURI())// this shouldn't happen
        {   return ErrorCodes.nOTHER_AUTH_ERR_NULL_AUTHURI; }
        HttpGet request1 = new HttpGet(AlterableURIs.getURI_authURI());
        request1.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());// disable auto redirect
//        request1.addHeader("Connection", "keep-alive");
//        request1.addHeader("Host", "ptlogin2.web2.qq.com");
//        request1.addHeader("Upgrade-Insecure-Requests", "1");
//        request1.addHeader("User-Agent", StringConstants.UserAgent);
        AuxiliaryFunctions.log("queryOtherAuth() request line: " + request1.getRequestLine());
        // send request
        CloseableHttpResponse response1 = null;
        try
        { response1 = httpClient.execute(request1); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET;
        }
        AuxiliaryFunctions.log("queryOtherAuth() response status line: " + response1.getStatusLine());
        Header[] cookies = response1.getHeaders("Set-Cookie");
        // process response
        if (302 != response1.getStatusLine().getStatusCode())
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response1);
            return ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_CODE;
        }
        // extract cookie: p_skey, pt4_token, p_uin
        String[] cookieValues = AuxiliaryFunctions.extractCookieValues(cookies,
                new String[]
                        {
                                "p_skey;" + StringConstants.DOMAIN_WEB2_QQ_COM,
                                "pt4_token;" + StringConstants.DOMAIN_WEB2_QQ_COM,
                                "p_uin;" + StringConstants.DOMAIN_WEB2_QQ_COM
                        });
        Cookies.p_skey = cookieValues[0];
        Cookies.pt4_token = cookieValues[1];
        Cookies.p_uin = cookieValues[2];
        AuxiliaryFunctions.log("p_skey=" + Cookies.p_skey);
        AuxiliaryFunctions.log("pt4_token=" + Cookies.pt4_token);
        // release resources
        AuxiliaryFunctions.consumeUnexpectedResponse(response1);
        AuxiliaryFunctions.log("querying authURI...complete");
        // step #2: get vfwebqq
        AuxiliaryFunctions.log("querying vfwebqq...");
        // build a new request
        AlterableURIs.setURI_vfwebqq();
        HttpGet request2 = new HttpGet(AlterableURIs.getURI_vfwebqq());
        request2.addHeader("Referer", FixedURIs.vfwebqq_referer.toString());
        request2.addHeader("User-Agent", StringConstants.UserAgent);
        String cookie2 = new StringBuilder()
                .append("RK=").append(Cookies.RK).append(';')
                .append("p_skey=").append(Cookies.p_skey).append(';')
                .append("pt4_token=").append(Cookies.pt4_token).append(';')
                .append("ptcz=").append(Cookies.ptcz).append(';')
                .append("ptisp=").append(Cookies.ptisp).append(';')
                .append("skey=").append(Cookies.skey).append(';').toString();
        request2.addHeader("Cookie", cookie2);
        AuxiliaryFunctions.log("queryOtherAuth() request line: " + request2.getRequestLine());
        AuxiliaryFunctions.log("queryOtherAuth() request cookie: " + request2.getFirstHeader("Cookie").getValue());
        // send request 2
        CloseableHttpResponse response2 = null;
        try
        {   response2 = httpClient.execute(request2); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET;
        }
        // process response 2
        AuxiliaryFunctions.log("queryOtherAuth() response status line: " + response2.getStatusLine());
        if (200 != response2.getStatusLine().getStatusCode())
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response2);
            return ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_CODE;
        }
        // read the entity
        HttpEntity entity2 = response2.getEntity();
        String vfwebqqJSONstr = null;
        try
        { vfwebqqJSONstr = EntityUtils.toString(entity2); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET;
        }
        finally
        {   AuxiliaryFunctions.consumeEntityAndCloseResponse(entity2, response2); }
        AuxiliaryFunctions.log("JSON data: " + vfwebqqJSONstr);
        // convert string to JSON object
        JSONObject vfwebqqJSONobj = null;
        try
        {   vfwebqqJSONobj = new JSONObject(new JSONTokener(vfwebqqJSONstr)); }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
//            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        // get retcode2
        int retcode2 = 0;
        try
        {   retcode2 = vfwebqqJSONobj.getInt("retcode"); }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        AuxiliaryFunctions.log("retcode2=" + retcode2);
        if (0 != retcode2)
        {   return ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_RETCODE; }
        // get result.vfwebqq
        String vfwebqq = null;
        try
        {   vfwebqq = (String)vfwebqqJSONobj.query("/result/vfwebqq"); }
        catch (JSONPointerException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        AuxiliaryFunctions.log("vfwebqq=" + vfwebqq);
        StringValue.vfwebqq = vfwebqq;
        AuxiliaryFunctions.log("querying vfwebqq...complete");
        // step #3: get psessionid and uin
        AuxiliaryFunctions.log("querying psessionid and uin...");
        // build request
        HttpPost request3 = new HttpPost(FixedURIs.login2);
        request3.addHeader("Content-Type", "application/x-www-form-urlencoded");
        request3.addHeader("Origin", FixedURIs.login2_origin.toString());
        request3.addHeader("Referer", FixedURIs.login2_referer.toString());
        request3.addHeader("User-Agent", StringConstants.UserAgent);
        String cookie3 = new StringBuilder()
                .append("RK=").append(Cookies.RK).append(';')
                .append("p_skey=").append(Cookies.p_skey).append(';')
                .append("pt2gguin=").append(Cookies.pt2gguin).append(';')
                .append("pt4_token=").append(Cookies.pt4_token).append(';')
                .append("ptcz=").append(Cookies.ptcz).append(';')
                .append("ptisp=").append(Cookies.ptisp).append(';')
                .append("skey=").append(Cookies.skey).append(';')
                .append("uin=").append(Cookies.uin).append(';')
                .append("p_uin=").append(Cookies.p_uin).append(';')
                .toString();
        request3.addHeader("Cookie", cookie3);
        // build the json entity
        List<NameValuePair> formParas = new ArrayList<>();
        String login2JSONReqStr = new JSONObject()
                .put("ptwebqq", "").put("clientid", 53999199).put("psessionid", "").put("status", "online").toString();
        formParas.add(new BasicNameValuePair("r", login2JSONReqStr));
        try
        {   request3.setEntity(new UrlEncodedFormEntity(formParas, "utf-8")); }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_ENCODING_EXCEPT;
        }
        AuxiliaryFunctions.log("queryOtherAuth() request line: " + request3.getRequestLine());
        AuxiliaryFunctions.log("queryOtherAuth() request cookie: " + request3.getFirstHeader("Cookie").getValue());
        AuxiliaryFunctions.log("queryOtherAuth() request entity (login2JSONReqStr): " + login2JSONReqStr);
        // send request
        CloseableHttpResponse response3 = null;
        try
        {   response3 = httpClient.execute(request3); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET;
        }
        AuxiliaryFunctions.log("queryOtherAuth() response status line: " + response3.getStatusLine());
        // process response
        if (200 != response3.getStatusLine().getStatusCode())
        {
            AuxiliaryFunctions.consumeUnexpectedResponse(response3);
            return ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_CODE;
        }
        // get entity and convert it to string
        HttpEntity entity3 = response3.getEntity();
        String login2JSONRspStr = null;
        try
        {   login2JSONRspStr = EntityUtils.toString(entity3); }
        catch (IOException e)
        {
            e.printStackTrace();
            return ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET;
        }
        finally
        {   AuxiliaryFunctions.consumeEntityAndCloseResponse(entity3, response3); }
        AuxiliaryFunctions.log("login2JSONRspStr=" + login2JSONRspStr);
        // convert the string to a JSONObject
        JSONObject login2JSONRsp = null;
        try
        {   login2JSONRsp = new JSONObject(login2JSONRspStr); }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        // get the retcode3
        int retcode3 = 0;
        try
        {   retcode3 = login2JSONRsp.getInt("retcode"); }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        AuxiliaryFunctions.log("retcode3=" + retcode3);
        if (0 != retcode3)
        {   return ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_RETCODE; }
        // get result.psessionid and result.uin
        String psessionid = null;
        long uin = 0;
        try
        {
            psessionid = (String)login2JSONRsp.query("/result/psessionid");
            uin = ((Number)login2JSONRsp.query("/result/uin")).longValue();
        }
        catch (JSONException e)
        {
            AuxiliaryFunctions.log(e.getMessage());
            return ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT;
        }
        StringValue.psessionid = psessionid;
        IntegerValue.uin = uin;
        AuxiliaryFunctions.log("psessionid=" + StringValue.psessionid);
        AuxiliaryFunctions.log("uin=" + IntegerValue.uin);
        AuxiliaryFunctions.log("querying psessionid and uin...complete");
        AuxiliaryFunctions.log("querying other auth...complete");
        return ErrorCodes.nOTHER_AUTH_ERR_NOERR;
    }

    private void requestOnlineBuddies()
    {
        // build req
        AlterableURIs.setURI_onlineBuddies();
        HttpGet request = new HttpGet(AlterableURIs.getURI_onlineBuddies());
        request.addHeader("Cookie",
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
        request.addHeader("Referer", FixedURIs.onlineBuddies_referer.toString());
        request.addHeader("User-Agent", StringConstants.UserAgent);
        // send
        try
        {   httpClient.execute(request); }
        catch (IOException e)
        {   e.printStackTrace(); }
    }

    private void login_printErr(int code)
    {
        switch (code)
        {
            /* #0 */case ErrorCodes.nLOGIN_ERR_QRCODE_TIMEOUT:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_QRCODE_TIMEOUT);
            break;
            /* #1 */case ErrorCodes.nLOGIN_ERR_NOERR:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_NOERR);
            break;
            /* #2 */case ErrorCodes.nLOGIN_ERR_UNKNOWN:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_UNKNOWN);
            break;
            /* #3 */case ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_QRCODE_IMG:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_FAIL_TO_GET_QRCODE_IMG);
            break;
            /* #4 */case ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE);
            break;
            /* #5 */case ErrorCodes.nLOGIN_ERR_UNDEFINED_QRSTATE_CODE:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_UNDEFINED_QRSTATE_CODE);
            break;
            /* #6 */case ErrorCodes.nLOGIN_ERR_FAIL_TO_GET_OTHER_AUTH:
            AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_FAIL_TO_GET_OTHER_AUTH);
            break;
            default:
                AuxiliaryFunctions.log(ErrorCodes.sLOGIN_ERR_UDEFINED_ERRCODE);
        }
    }

    private void queryQRCodeImg_printErr(int code)
    {
        switch (code)
        {
            /* #0 */case ErrorCodes.nQRCODE_IMG_ERR_NOERR:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_NOERR);
            break;
            /* #1 */case ErrorCodes.nQRCODE_IMG_ERR_UNKNOWN:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_UNKNOWN);
            break;
            /* #2 */case ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_NET:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_IOEXCEPT_NET);
            break;
            /* #3 */case ErrorCodes.nQRCODE_IMG_ERR_IOEXCEPT_FS:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_IOEXCEPT_FS);
            break;
            /* #4 */case ErrorCodes.nQRCODE_IMG_ERR_UNEXPECT_CODE:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_UNEXPECT_CODE);
            break;
            /* #5 */case ErrorCodes.nQRCODE_IMG_ERR_NO_COOKIE:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_NO_COOKIE);
            break;
            /* #6 */case ErrorCodes.nQRCODE_IMG_ERR_NO_FILE:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_NO_FILE);
            break;
            default:
                AuxiliaryFunctions.log(ErrorCodes.sQRCODE_IMG_ERR_UDEFINED_ERRCODE);
        }
    }

    private void queryQRCodeState_printErr(int code)
    {
        switch (code)
        {
            /* #0 */case ErrorCodes.nQRCODE_STATE_ERR_NOERR:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_NOERR);
            break;
            /* #1 */case ErrorCodes.nQRCODE_STATE_ERR_UNKNOWN:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_UNKNOWN);
            break;
            /* #2 */case ErrorCodes.nQRCODE_STATE_ERR_NULL_TOKEN:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_NULL_TOKEN);
            break;
            /* #3 */case ErrorCodes.nQRCODE_STATE_ERR_NULL_COOKIE:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_NULL_COOKIE);
            break;
            /* #4 */case ErrorCodes.nQRCODE_STATE_ERR_IOEXCEPT_NET:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_IOEXCEPT_NET);
            break;
            /* #5 */case ErrorCodes.nQRCODE_STATE_ERR_UNEXPECT_CODE:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_UNEXPECT_CODE);
            break;
            /* #6 */case ErrorCodes.nQRCODE_STATE_ERR_NULL_URI:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_NULL_URI);
            break;
            /* #7 */case ErrorCodes.nQRCODE_STATE_ERR_FAIL_TO_SET_URI:
            AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_FAIL_TO_SET_URI);
            break;
            default:
                AuxiliaryFunctions.log(ErrorCodes.sQRCODE_STATE_ERR_UDEFINED_ERRCODE);
        }
    }

    private void queryOtherAuth_printErr(int code)
    {
        switch (code)
        {
            /* #0 */case ErrorCodes.nOTHER_AUTH_ERR_NOERR:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_NOERR);
                break;
            /* #1 */case ErrorCodes.nOTHER_AUTH_ERR_UNKNOWN:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_UNKNOWN);
                break;
            /* #2 */case ErrorCodes.nOTHER_AUTH_ERR_NULL_AUTHURI:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_NULL_AUTHURI);
                break;
            /* #3 */case ErrorCodes.nOTHER_AUTH_ERR_IOEXCEPT_NET:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_IOEXCEPT_NET);
                break;
            /* #4 */case ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_CODE:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_UNEXPECT_CODE);
                break;
            /* #5 */case ErrorCodes.nOTHER_AUTH_ERR_JSON_EXCEPT:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_JSON_EXCEPT);
                break;
            /* #6 */case ErrorCodes.nOTHER_AUTH_ERR_UNEXPECT_RETCODE:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_UNEXPECT_RETCODE);
                break;
            /* #7 */case ErrorCodes.nOTHER_AUTH_ERR_ENCODING_EXCEPT:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_ENCODING_EXCEPT);
                break;
            default:
                AuxiliaryFunctions.log(ErrorCodes.sOTHER_AUTH_ERR_UDEFINED_ERRCODE);
        }
    }
}

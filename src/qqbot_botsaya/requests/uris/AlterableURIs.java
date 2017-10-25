package qqbot_botsaya.requests.uris;

import org.apache.http.client.utils.URIBuilder;
import qqbot_botsaya.executors.AuxiliaryFunctions;
import qqbot_botsaya.constants.ExitStatus;
import qqbot_botsaya.value_store.StringValue;

import java.net.URI;
import java.net.URISyntaxException;

public class AlterableURIs
{
    // the uri used to query the state of the qrcode
    private static URI QRCodeState = null;
    public static boolean isNull_QRCodeState() { return QRCodeState == null; }
    public static URI getURI_QRCodeState() { return QRCodeState; }
    public static boolean setURI_QRCodeState(String ptqrtoken)
    {
        try
        {
            QRCodeState = new URIBuilder()
                    .setScheme("https")
                    .setHost("ssl.ptlogin2.qq.com")
                    .setPath("ptqrlogin")
                    .setParameter("u1", "http://w.qq.com/proxy.html")
                    .setParameter("ptqrtoken", ptqrtoken)
                    .setParameter("ptredirect", "0")
                    .setParameter("h", "1")
                    .setParameter("t", "1")
                    .setParameter("g", "1")
                    .setParameter("from_ui", "1")
                    .setParameter("ptlang", "2052")
                    .setParameter("action",
                            "0-0-" + String.valueOf(System.currentTimeMillis()))
                    .setParameter("js_ver", "10230")
                    .setParameter("js_type", "1")
                    .setParameter("login_sig", "")
                    .setParameter("pt_uistyle", "40")
                    .setParameter("aid", "501004106")
                    .setParameter("daid", "164")
                    .setParameter("mibao_css", "m_webqq")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in AlterableURIs.setURI_QRCodeState(String ptqrtoken)");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return true;
    }

    // the uri we should get after the scanning of qrcode
    private static URI authURI = null;
    public static boolean isNull_authURI() { return null == authURI; }
    public static URI getURI_authURI() { return authURI; }
    public static boolean setURI_authURI(String uri)// we receive the authURI from the server, we do not build it
    {
        try
        { authURI = new URI(uri); }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in AlterableURIs.setURI_authURI(String uri)");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return true;
    }

    // through this uri we get vfwebqq
    private static URI vfwebqq = null;
    public static boolean isNull_vfwebqq() { return null == vfwebqq; }
    public static URI getURI_vfwebqq() { return vfwebqq; }
    public static void setURI_vfwebqq()
    {
        try
        {
            vfwebqq = new URIBuilder()
                    .setScheme("http").setHost("s.web2.qq.com").setPath("/api/getvfwebqq")
                    .setParameter("ptwebqq", "")
                    .setParameter("clientid", "53999199")
                    .setParameter("psessionid", "")
                    .setParameter("t", String.valueOf(System.currentTimeMillis()))
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in AlterableURIs.setURI_vfwebqq()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
    }

    private static URI onlineBuddies = null;
    public static boolean isNull_onlineBuddies()
    {   return null == onlineBuddies; }
    public static URI getURI_onlineBuddies()
    {   return onlineBuddies; }
    public static void setURI_onlineBuddies()
    {
        try
        {
            onlineBuddies = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").setPath("/channel/get_online_buddies2")
                    .setParameter("vfwebqq", StringValue.vfwebqq)
                    .setParameter("clientid", "53999199")
                    .setParameter("psessionid", StringValue.psessionid)
                    .setParameter("t", String.valueOf(System.currentTimeMillis()))
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in AlterableURIs.setURI_onlineBuddies()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
    }
}

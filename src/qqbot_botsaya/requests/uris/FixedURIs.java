package qqbot_botsaya.requests.uris;

import org.apache.http.client.utils.URIBuilder;
import qqbot_botsaya.executors.AuxiliaryFunctions;
import qqbot_botsaya.constants.ExitStatus;

import java.net.URI;
import java.net.URISyntaxException;

public class FixedURIs
{
    public static final URI QRCodeImg = setQRCodeImg();
    public static final URI QRCodeImg_referer = setQRCodeImg_referer();
    public static final URI vfwebqq_referer = setVfwebqq_referer();
    public static final URI login2 = setLogin2();
    public static final URI login2_origin = setLogin2_origin();
    public static final URI login2_referer = setLogin2_referer();
    public static final URI onlineBuddies_referer = setOnlineBuddies_referer();
    public static final URI poll2 = setPoll2();
    public static final URI poll2_origin = setPoll2_origin();
    public static final URI poll2_referer = setPoll2_referer();
    public static final URI sendGroupMsg = setSendGroupMsg();
    public static final URI sendGroupMsg_origin = setSendGroupMsg_origin();
    public static final URI sendGroupMsg_referer = setSendGroupMsg_referer();

    private static URI setQRCodeImg()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("https").setHost("ssl.ptlogin2.qq.com").setPath("/ptqrshow")
                    .setParameter("appid", "501004106")
                    .setParameter("e", "2")
                    .setParameter("l", "M")
                    .setParameter("s", "3")
                    .setParameter("d", "72")
                    .setParameter("v", "4")
                    .setParameter("t", "0.1")
                    .setParameter("daid", "164")
                    .setParameter("pt_3rd_aid", "0")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setQRCodeImg()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setQRCodeImg_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("https").setHost("xui.ptlogin2.qq.com").setPath("/cgi-bin/xlogin")
                    .setParameter("daid", "164")
                    .setParameter("target", "self")
                    .setParameter("style", "40")
                    .setParameter("pt_disable_pwd", "1")
                    .setParameter("mibao_css", "m_webqq")
                    .setParameter("appid", "501004106")
                    .setParameter("enable_qlogin", "0")
                    .setParameter("no_verifyimg", "1")
                    .setParameter("s_url", "http://w.qq.com/proxy.html")
                    .setParameter("f_url", "loginerroralert")
                    .setParameter("strong_login", "1")
                    .setParameter("login_state", "10")
                    .setParameter("t", "20131024001")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setQRCodeImg_referer()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setVfwebqq_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("s.web2.qq.com").setPath("/proxy.html")
                    .setParameter("v", "20130916001")
                    .setParameter("callback", "1")
                    .setParameter("id", "1")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setVfwebqq_referer()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setLogin2()
    {
        URI uri = null;
        try
        {   uri = new URIBuilder().setScheme("http").setHost("d1.web2.qq.com").setPath("/channel/login2").build(); }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setLogin2()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setLogin2_origin()
    {
        URI uri = null;
        try
        {   uri = new URIBuilder().setScheme("http").setHost("d1.web2.qq.com").build(); }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setLogin2_origin()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setLogin2_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").setPath("/proxy.html")
                    .setParameter("v", "20151105001")
                    .setParameter("callback", "1")
                    .setParameter("id", "2")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setLogin2_referer()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setOnlineBuddies_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").setPath("/proxy.html")
                    .setParameter("v", "20151105001")
                    .setParameter("callback", "1")
                    .setParameter("id", "2")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setOnlineBuddies_referer()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setPoll2()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").setPath("/channel/poll2").build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setPoll2()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setPoll2_origin()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setPoll2_origin()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setPoll2_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder()
                    .setScheme("http").setHost("d1.web2.qq.com").setPath("/proxy.html")
                    .setParameter("v", "20151105001")
                    .setParameter("callback", "1")
                    .setParameter("id", "2")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setPoll2_referer()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setSendGroupMsg()
    {
        URI uri = null;
        try
        {   uri = new URIBuilder().setScheme("http").setHost("d1.web2.qq.com").setPath("/channel/send_qun_msg2").build(); }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setSendGroupMsg()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setSendGroupMsg_origin()
    {
        URI uri = null;
        try
        {   uri = new URIBuilder().setScheme("http").setHost("d1.web2.qq.com").build(); }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setSendGroupMsg_origin()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }

    private static URI setSendGroupMsg_referer()
    {
        URI uri = null;
        try
        {
            uri = new URIBuilder().setScheme("http").setHost("d1.web2.qq.com").setPath("/cfproxy.html")
                    .setParameter("v", "20151105001")
                    .setParameter("callback", "1")
                    .build();
        }
        catch (URISyntaxException e)
        {
            AuxiliaryFunctions.error("URISyntaxException in FixedURIs.setSendGroupMsg()");
            e.printStackTrace();
            System.exit(ExitStatus.FATAL_ERROR);
        }
        return uri;
    }
}

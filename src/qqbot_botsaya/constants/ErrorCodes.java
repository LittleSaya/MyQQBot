package qqbot_botsaya.constants;

public class ErrorCodes
{
    /* login */
    public static final int nLOGIN_ERR_QRCODE_TIMEOUT = 0x0000;
    public static final String sLOGIN_ERR_QRCODE_TIMEOUT = "login: QRCode timeout error";

    public static final int nLOGIN_ERR_NOERR = 0x0001;
    public static final String sLOGIN_ERR_NOERR = "login: no error";

    public static final int nLOGIN_ERR_UNKNOWN = 0x0002;
    public static final String sLOGIN_ERR_UNKNOWN = "login: unknown error";

    public static final int nLOGIN_ERR_FAIL_TO_GET_QRCODE_IMG = 0x0003;
    public static final String sLOGIN_ERR_FAIL_TO_GET_QRCODE_IMG = "login: fail to get QRCode";

    public static final int nLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE = 0x0004;
    public static final String sLOGIN_ERR_FAIL_TO_GET_QRCODE_STATE = "login: fail to get QRCode state";

    public static final int nLOGIN_ERR_UNDEFINED_QRSTATE_CODE = 0x0005;
    public static final String sLOGIN_ERR_UNDEFINED_QRSTATE_CODE = "login: undefined QRState code";

    public static final int nLOGIN_ERR_FAIL_TO_GET_OTHER_AUTH = 0x0006;
    public static final String sLOGIN_ERR_FAIL_TO_GET_OTHER_AUTH = "login: fail to get other auth info";

    public static final String sLOGIN_ERR_UDEFINED_ERRCODE = "login: undefined error code";

    /* query QRCode image */
    public static final int nQRCODE_IMG_ERR_NOERR = 0x0100;
    public static final String sQRCODE_IMG_ERR_NOERR = "QRCode image: no error";

    public static final int nQRCODE_IMG_ERR_UNKNOWN = 0x0101;
    public static final String sQRCODE_IMG_ERR_UNKNOWN = "QRCode image: unknown error";

    public static final int nQRCODE_IMG_ERR_IOEXCEPT_NET = 0x0102;
    public static final String sQRCODE_IMG_ERR_IOEXCEPT_NET = "QRCode image: I/O Exception (network)";

    public static final int nQRCODE_IMG_ERR_IOEXCEPT_FS = 0x0103;
    public static final String sQRCODE_IMG_ERR_IOEXCEPT_FS = "QRCode image: I/O Exception (file system)";

    public static final int nQRCODE_IMG_ERR_UNEXPECT_CODE = 0x0104;
    public static final String sQRCODE_IMG_ERR_UNEXPECT_CODE = "QRCode image: unexpected status code";

    public static final int nQRCODE_IMG_ERR_NO_COOKIE = 0x0105;
    public static final String sQRCODE_IMG_ERR_NO_COOKIE = "QRCode image: cannot find specified cookie";

    public static final int nQRCODE_IMG_ERR_NO_FILE = 0x0106;
    public static final String sQRCODE_IMG_ERR_NO_FILE = "QRCode image: file not found: \"" +StringConstants.QRCodeFilePath + "\"";

    public static final String sQRCODE_IMG_ERR_UDEFINED_ERRCODE = "QRCode image: undefined error code";

    /* query QRCode state */
    public static final int nQRCODE_STATE_ERR_NOERR = 0x0200;
    public static final String sQRCODE_STATE_ERR_NOERR = "QRCode state: no error";

    public static final int nQRCODE_STATE_ERR_UNKNOWN = 0x0201;
    public static final String sQRCODE_STATE_ERR_UNKNOWN = "QRCode state: unknown error";

    public static final int nQRCODE_STATE_ERR_NULL_TOKEN = 0x0202;
    public static final String sQRCODE_STATE_ERR_NULL_TOKEN = "QRCode state: (String)ptqrtoken is null";

    public static final int nQRCODE_STATE_ERR_NULL_COOKIE = 0x0203;
    public static final String sQRCODE_STATE_ERR_NULL_COOKIE = "QRCode state: (String)cookie is null";

    public static final int nQRCODE_STATE_ERR_IOEXCEPT_NET = 0x0204;
    public static final String sQRCODE_STATE_ERR_IOEXCEPT_NET = "QRCode state: I/O Exception (network)";

    public static final int nQRCODE_STATE_ERR_UNEXPECT_CODE = 0x0205;
    public static final String sQRCODE_STATE_ERR_UNEXPECT_CODE = "QRCode state: unexpected status code";

    public static final int nQRCODE_STATE_ERR_NULL_URI = 0x0206;
    public static final String sQRCODE_STATE_ERR_NULL_URI = "QRCode state: null authURI";

    public static final int nQRCODE_STATE_ERR_FAIL_TO_SET_URI = 0x0207;
    public static final String sQRCODE_STATE_ERR_FAIL_TO_SET_URI = "QRCode state: fail to set authURI";

    public static final String sQRCODE_STATE_ERR_UDEFINED_ERRCODE = "QRCode state: undefined error code";

    /* query other auth info */
    public static final int nOTHER_AUTH_ERR_NOERR = 0x0300;
    public static final String sOTHER_AUTH_ERR_NOERR = "Other auth: no error";

    public static final int nOTHER_AUTH_ERR_UNKNOWN = 0x0301;
    public static final String sOTHER_AUTH_ERR_UNKNOWN = "Other auth: unknown error";

    public static final int nOTHER_AUTH_ERR_NULL_AUTHURI = 0x0302;
    public static final String sOTHER_AUTH_ERR_NULL_AUTHURI = "Other auth: authURI == null";

    public static final int nOTHER_AUTH_ERR_IOEXCEPT_NET = 0x0303;
    public static final String sOTHER_AUTH_ERR_IOEXCEPT_NET = "Other auth: I/O Exception (network)";

    public static final int nOTHER_AUTH_ERR_UNEXPECT_CODE = 0x0304;
    public static final String sOTHER_AUTH_ERR_UNEXPECT_CODE = "Other auth: unexpected status code";

    public static final int nOTHER_AUTH_ERR_JSON_EXCEPT = 0x0305;
    public static final String sOTHER_AUTH_ERR_JSON_EXCEPT = "Other auth: json exception";

    public static final int nOTHER_AUTH_ERR_UNEXPECT_RETCODE = 0x0306;
    public static final String sOTHER_AUTH_ERR_UNEXPECT_RETCODE = "Other auth: unexpected retcode";

    public static final int nOTHER_AUTH_ERR_ENCODING_EXCEPT = 0x0307;
    public static final String sOTHER_AUTH_ERR_ENCODING_EXCEPT = "Other auth: unsupported encoding";

    public static final String sOTHER_AUTH_ERR_UDEFINED_ERRCODE = "Other auth: undefined error code";
}

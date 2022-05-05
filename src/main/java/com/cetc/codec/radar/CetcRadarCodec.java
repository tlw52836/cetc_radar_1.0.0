//package codec;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.cetc.cpix.framework.codec.CpixCodec;
//import com.cetc.cpix.framework.codec.CpixCrypterUtil;
//import com.cetc.cpix.framework.codec.ICpixCodec;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.List;
//
///**
// * @ClassName: ${file_name}
// * @Description: ${todo}(用一句话描述该文件做什么)
// * @author XuJialiang
// * @version V1.0.0, YYYY-MM-DD
// * @see            相关类/方法
// * @since 产品/模块版本
// * @date ${date} ${time}
// */
//@CpixCodec
//public class CetcRadarCodec implements ICpixCodec {
//
//    static Logger LOGGER = LoggerFactory.getLogger(CetcRadarCodec.class);
//
//    @Override
//    public void init() throws Exception {
//    }
//
//    @Override
//    public JSON preDecode(JSONObject var1) throws Exception {
//        JSONObject result = new JSONObject();
//
//        try {
//            byte[] data = var1.getBytes("source");
//
//            if (data[0] == 0x68 && data[data.length-1] == 0x16) {
//
//                byte[] snB = new byte[8];
//                System.arraycopy(data, 6, snB, 0, 8);
//
//                result.put("sn", "k" + byteToHexString(snB));
//                result.put("source", data);
//                result.put("type", "deviceReq");
//                result.put("errorCode", 0);
//
//                int func = data[27] & 0xFF;
//                if (func != 0x0A && (func & 0x80) != 0x80) {
//                    result.put("errorCode", -3);
//                }
//
//            } else {
//                result.put("source", var1.getBytes("source"));
//                result.put("type", "deviceReq");
//                result.put("errorCode", -1);
//            }
//        } catch (Throwable e) {
//            LOGGER.info("CetcGytCodec decodeSn error: {}", e);
//            result.put("source", var1.getBytes("source"));
//            result.put("type", "deviceReq");
//            result.put("errorCode", -1);
//        }
//
//        return result;
//    }
//
//    @Override
//    public JSON decode(JSONObject var1) throws Exception {
//        JSONObject result = null;
//
//        try {
//
//            byte[] data = var1.getBytes("source");
//            //JSONObject extObj = var1.getJSONObject("extend");
//
//            byte[] snB = new byte[8];
//            System.arraycopy(data, 6, snB, 0, 8);
//            String sn = byteToHexString(snB);
//
//            byte[] timeB = new byte[8];
//            System.arraycopy(data, 14, snB, 0, 8);
//            ByteBuffer bf1 = ByteBuffer.wrap(timeB);
//            long timestamp = bf1.order(ByteOrder.BIG_ENDIAN).getLong();
//
//            int serial = (data[23] & 0xFF) << 8 | (data[24] & 0xFF);
//
//            int func = data[27] & 0xFF;
//
//            int userLen = (data[28] & 0xFF) << 8 | (data[29] & 0xFF);
//            byte[] userData = new byte[userLen];
//            System.arraycopy(data, 30, userData, 0, userLen);
//            ByteBuffer bf = ByteBuffer.wrap(userData).order(ByteOrder.BIG_ENDIAN);
//
//            if (func == 0x0A) { // 数据上发
//                result = new JSONObject();
//                result.put("errorCode", 0);
//                result.put("identify", String.valueOf(serial));
//                result.put("type", "deviceReq");
//                result.put("sn", sn);
//
//                JSONObject dataObj = new JSONObject();
//                JSONObject historyObj = new JSONObject();
//                result.put("data", dataObj);
//                result.put("history", historyObj);
//
//                while (bf.hasRemaining()) {
//                    int tag = bf.get() & 0xFF;
//                    int tLen = bf.getShort();
//                    switch (tag) {
//                        case 1:
//                        case 2:
//                        case 3:
//                        case 7:
//                        {
//                            JSONObject svt = new JSONObject();
//                            svt.put("t", timestamp);
//                            svt.put("v", bf.get());
//                            dataObj.put("P" + tag, svt);
//                        }
//                        break;
//                        case 4:
//                        {
//                            int n = tLen / 4;
//                            for (int i = 0; i < n; i++) {
//                                JSONObject svt = new JSONObject();
//                                svt.put("t", timestamp);
//                                svt.put("v", bf.getInt() & 0xFFFFFFFF);
//                                dataObj.put("P" + tag + "_" + i, svt);
//                            }
//                        }
//                        break;
//                        case 5:
//                        case 10:
//                        case 11:
//                        {
//                            JSONObject svt = new JSONObject();
//                            svt.put("t", timestamp);
//                            svt.put("v", bf.getShort() & 0xFFFF);
//                            dataObj.put("P" + tag, svt);
//                        }
//                        break;
//                        case 6:
//                        {
//                            JSONObject svt = new JSONObject();
//                            svt.put("t", timestamp);
//                            svt.put("v", bf.getInt() & 0xFFFFFFFF);
//                            dataObj.put("P" + tag, svt);
//                        }
//                        break;
//                        case 8:
//                        case 9:
//                        {
//                            int n = tLen / 4;
//                            long[] valus = new long[n];
//                            for (int i = 0; i < n; i++) {
//                                valus[i] = bf.getInt() & 0xFFFFFFFF;
//                            }
//
//                            JSONObject svt = new JSONObject();
//                            svt.put("t", timestamp);
//                            svt.put("v", valus);
//                            svt.put("d", 10);
//                            historyObj.put("P" + tag, svt);
//                        }
//                        break;
//                        default:
//                        {
//                            bf.get(new byte[tLen]); // 忽略未识别的TLV
//                        }
//                        break;
//                    }
//                }
//            } else if ((func & 0x80) == 0x80) { // 指令响应
//                result = new JSONObject();
//                result.put("errorCode", 0);
//                result.put("identify", String.valueOf(serial));
//                result.put("type", "deviceAck");
//                result.put("sn", sn);
//
//                JSONArray resultDetail = new JSONArray();
//                result.put("resultDetail", resultDetail);
//
//                int resultCode = bf.get();
//                JSONObject resultObj = new JSONObject();
//                resultObj.put("s", "result");
//                resultObj.put("v", resultCode);
//                resultDetail.add(resultObj);
//
//                if (func == 0x90) {
//                    // 产品型号
//                    String pml = String.format("%02d-%02d-%02d-%02d", bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject pmlObj = new JSONObject();
//                    pmlObj.put("s", "pml");
//                    pmlObj.put("v", pml);
//                    resultDetail.add(pmlObj);
//
//                    // 系统软件版本
//                    String sw1 = String.format("%02d.%02d.%02d.%02d", bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject sw1Obj = new JSONObject();
//                    sw1Obj.put("s", "sw1");
//                    sw1Obj.put("v", sw1);
//                    resultDetail.add(sw1Obj);
//
//                    // DSP软件版本
//                    String sw2 = String.format("%02d.%02d.%02d.%02d", bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject sw2Obj = new JSONObject();
//                    sw2Obj.put("s", "sw2");
//                    sw2Obj.put("v", sw2);
//                    resultDetail.add(sw2Obj);
//
//                    // 硬件版本
//                    String hw = String.format("%02d.%02d.%02d.%02d", bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject hwObj = new JSONObject();
//                    hwObj.put("s", "hw");
//                    hwObj.put("v", hw);
//                    resultDetail.add(hwObj);
//
//                    // 设备IP
//                    String ip = String.format("%d.%d.%d.%d", bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject ipObj = new JSONObject();
//                    ipObj.put("s", "ip");
//                    ipObj.put("v", ip);
//                    resultDetail.add(ipObj);
//
//                    // 网卡地址
//                    String mac = String.format("%02x:%02x:%02x:%02x:%02x:%02x", bf.get()&0xFF, bf.get()&0xFF,
//                            bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF, bf.get()&0xFF);
//                    JSONObject macObj = new JSONObject();
//                    macObj.put("s", "mac");
//                    macObj.put("v", mac);
//                    resultDetail.add(macObj);
//
//                } else if (func == 0x8F) {
//                    int status = bf.get();  // 升级状态
//                    JSONObject statusObj = new JSONObject();
//                    statusObj.put("s", "status");
//                    statusObj.put("v", status);
//                    resultDetail.add(statusObj);
//                }
//            }
//        } catch (Throwable e) {
//            LOGGER.info("CetcGytCodec decode error: {}", e);
//
//            result = new JSONObject();
//            result.put("errorCode", -1);
//            result.put("type", "deviceReq");
//            result.put("identify", "123");
//        }
//
//        return result;
//    }
//
//    @Override
//    public JSON merge(JSONObject var1) throws Exception {
//        return null;
//    }
//
//    @Override
//    public List<byte[]> encode(JSONObject var1) throws Exception {
//        List<byte[]> resultList = new ArrayList<>();
//        String type = var1.getString("type");
//        String identity = var1.getString("identity");
//
//        if (type.contentEquals("platReq")) {
//            String sn = var1.getString("sn");
//            //String encrypt = var1.getString("encrypt");
//            //String secret = var1.getString("secret");
//            String method = var1.getString("method");
//            JSONObject params = var1.getJSONObject("params");
//
//            if (method.contentEquals("GET_INFO")) {
//                byte[] data = new byte[2];
//                data[0] = 0x00;
//                data[1] = 0x00;
//
//                byte[] result = getEncodeData(sn, Integer.parseInt(identity), 0x10, null, null, data);
//                resultList.add(result);
//            } else if (method.contentEquals("SET_TIME")) {
//                byte[] data =  new byte[10];
//                long time = Calendar.getInstance().getTimeInMillis();
//                data[0] = 0x00;
//                data[1] = 0x08;
//                data[2] = (byte) ((time >> 56) & 0xFF);
//                data[3] = (byte) ((time >> 48) & 0xFF);
//                data[4] = (byte) ((time >> 40) & 0xFF);
//                data[5] = (byte) ((time >> 32) & 0xFF);
//                data[6] = (byte) ((time >> 24) & 0xFF);
//                data[7] = (byte) ((time >> 16) & 0xFF);
//                data[8] = (byte) ((time >> 8) & 0xFF);
//                data[9] = (byte) (time & 0xFF);
//
//                byte[] result = getEncodeData(sn, Integer.parseInt(identity), 0x11, null, null, data);
//                resultList.add(result);
//            } else if (method.contentEquals("SET_PLATFORM")) {
//                int protocol = params.getInteger("protocol");
//                String[] ips = params.getString("ip").split("\\.");
//                int port = params.getInteger("port");
//                String url = params.getString("url");
//
//                byte[] data = new byte[73];
//                data[0] = 0x00;
//                data[1] = 0x47;
//                data[2] = (byte) (protocol & 0xFF);
//
//                if (protocol == 4) {
//                    System.arraycopy(url.getBytes(), 0, data, 9, url.length());
//                } else {
//                    data[3] = (byte) (Integer.parseInt(ips[0]) & 0xFF);
//                    data[4] = (byte) (Integer.parseInt(ips[1]) & 0xFF);
//                    data[5] = (byte) (Integer.parseInt(ips[2]) & 0xFF);
//                    data[6] = (byte) (Integer.parseInt(ips[3]) & 0xFF);
//                    data[7] = (byte) ((port >> 8) & 0xFF);
//                    data[8] = (byte) (port & 0xFF);
//                }
//
//                byte[] result = getEncodeData(sn, Integer.parseInt(identity), 0x12, null, null, data);
//                resultList.add(result);
//            } else if (method.contentEquals("UPGRADE")) {
//                int module = params.getInteger("module");
//                int download = params.getInteger("download");
//                String url = params.getString("url");
//                String token = params.getString("token");
//
//                byte[] data = new byte[84];
//                data[0] = 0x00;
//                data[1] = 0x52;
//                data[2] = (byte) (module & 0xFF);
//                data[3] = (byte) (download & 0xFF);
//
//                System.arraycopy(url.getBytes(), 0, data, 4, url.length());
//                System.arraycopy(token.getBytes(), 0, data, 68, token.length());
//
//                byte[] result = getEncodeData(sn, Integer.parseInt(identity), 0x0F, null, null, data);
//                resultList.add(result);
//            }
//        } else if (type.contentEquals("platAck")) {
//            int errorCode = var1.getInteger("errorCode");
//            byte[] source = var1.getBytes("source");
//
//            byte[] result = new byte[33];
//            result[0] = 0x68;
//            result[1] = 0x00;
//            result[2] = 0x21;
//
//            System.arraycopy(source, 3, result, 3, 11);
//
//            Calendar calendar = Calendar.getInstance();
//            long time = calendar.getTimeInMillis();
//            result[14] = (byte) ((time >> 56) & 0xFF);
//            result[15] = (byte) ((time >> 48) & 0xFF);
//            result[16] = (byte) ((time >> 40) & 0xFF);
//            result[17] = (byte) ((time >> 32) & 0xFF);
//            result[18] = (byte) ((time >> 24) & 0xFF);
//            result[19] = (byte) ((time >> 16) & 0xFF);
//            result[20] = (byte) ((time >> 8) & 0xFF);
//            result[21] = (byte) (time & 0xFF);
//
//            result[22] = (byte) 0xB3;
//            result[23] = source[23];
//            result[24] = source[24];
//            result[27] = (byte) (0x80 | source[27]);
//            result[28] = 0x00;
//            result[29] = 0x01;
//            result[30] = (byte) (errorCode & 0xFF);
//
//            int checkSum = 0;
//            for (int i = 1; i < result.length - 2; i++) {
//                checkSum += (0xFF & result[i]);
//            }
//
//            result[31] = (byte) (0xFF & checkSum);
//            result[32] = 0x16;
//
//            resultList.add(result);
//        }
//
//        return resultList;
//    }
//
//    private byte[] getEncodeData(String sn, int searial, int func, String encrypt, String secret, byte[] data) throws Exception {
//        boolean encryptFlag = false;
//        if (data != null && encrypt != null && encrypt.contentEquals("AES-128")) {
//            data = CpixCrypterUtil.encrypt2(data, secret);
//            encryptFlag = true;
//        }
//
//        int len = 30;
//        if (data != null) {
//            len += data.length;
//        }
//
//        byte[] cmd = new byte[len];
//
//        cmd[0] = 0x68;
//        cmd[1] = (byte) ((len >> 8) & 0xFF);
//        cmd[2] = (byte) (len & 0xFF);
//        cmd[3] = 0x01;
//        cmd[4] = 0x00;
//        cmd[5] = 0x00;
//
//        sn = StringUtils.leftPad(sn, 16, "0");
//        byte[] bSn = hexStringToByte(sn);
//        System.arraycopy(bSn, 0, cmd, 6, 8);
//
//        Calendar calendar = Calendar.getInstance();
//        long time = calendar.getTimeInMillis();
//        cmd[14] = (byte) ((time >> 56) & 0xFF);
//        cmd[15] = (byte) ((time >> 48) & 0xFF);
//        cmd[16] = (byte) ((time >> 40) & 0xFF);
//        cmd[17] = (byte) ((time >> 32) & 0xFF);
//        cmd[18] = (byte) ((time >> 24) & 0xFF);
//        cmd[19] = (byte) ((time >> 16) & 0xFF);
//        cmd[20] = (byte) ((time >> 8) & 0xFF);
//        cmd[21] = (byte) (time & 0xFF);
//
//        if (encryptFlag) {
//            cmd[22] = (byte) 0xFB;
//        } else {
//            cmd[22] = (byte) 0xF3;
//        }
//        cmd[23] = (byte) ((searial >> 8) & 0xFF);
//        cmd[24] = (byte) (searial & 0xFF);
//        cmd[27] = (byte) (func & 0xFF);
//
//        System.arraycopy(data, 0, cmd, 28, data.length);
//
//        int checkSum = 0;
//        for (int i = 1; i < cmd.length - 2; i++) {
//            checkSum += (0xFF & cmd[i]);
//        }
//
//        cmd[len - 2] = (byte) (0xFF & checkSum);
//        cmd[len - 1] = 0x16;
//
//        return cmd;
//    }
//
//    @Override
//    public String getFactoryID() throws Exception {
//        return "CETC";
//    }
//
//    @Override
//    public String getModel() throws Exception {
//        return "RADAR";
//    }
//
//    @Override
//    public JSONObject getDescription() throws Exception {
//        JSONObject obj = new JSONObject();
//        obj.put("version", "1.0.0");
//        obj.put("author", "XuJialiang");
//        obj.put("date", "2022-04-18");
//        obj.put("desc", "雷达睡眠编解码插件");
//
//        return obj;
//    }
//
//    @Override
//    public JSONArray getMethodList() throws Exception {
//        return (JSONArray) JSON.toJSON(METHODS);
//    }
//
//    @Override
//    public JSONArray getChannelList() throws Exception {
//        return (JSONArray) JSON.toJSON(CHANNELS);
//    }
//
//    private static final char[] TBL = new char[256 * 4];
//
//    private static final ChannelObj[] CHANNELS = new ChannelObj[11];
//
//    private static final MethodObj[] METHODS = new MethodObj[4];
//
//    static {
//
//        final char[] DIGITS = "0123456789ABCDEF".toCharArray();
//
//        for (int i = 0; i < 256; i++) {
//
//            TBL[i << 1] = DIGITS[i >>> 4 & 0x0F];
//            TBL[(i << 1) + 1] = DIGITS[i & 0x0F];
//        }
//
//        CHANNELS[0] = new ChannelObj("P1", "data", "跟踪状态");
//        CHANNELS[1] = new ChannelObj("P2", "data", "跟踪位置x坐标");
//        CHANNELS[2] = new ChannelObj("P3", "data", "跟踪位置y坐标");
//        CHANNELS[3] = new ChannelObj("P4_x", "data", "距离像(x = 0, 1, 2...35)");
//        CHANNELS[4] = new ChannelObj("P5", "data", "体动评分");
//        CHANNELS[5] = new ChannelObj("P6", "data", "体动幅度");
//        CHANNELS[6] = new ChannelObj("P7", "data", "体动等级");
//        CHANNELS[7] = new ChannelObj("P8", "data", "呼吸曲率");
//        CHANNELS[8] = new ChannelObj("P9", "data", "心跳曲率");
//        CHANNELS[9] = new ChannelObj("P10", "data", "呼吸率");
//        CHANNELS[10] = new ChannelObj("P11", "data", "心率");
//
//        final ParamObj[] SET_PLATFORM_PARAMS = {
//                new ParamObj("protocol", "协议类型: udp, tcp, mqtt, http"),
//                new ParamObj("ip", "设备IP地址"),
//                new ParamObj("port", "上报端口号"),
//                new ParamObj("url", "上报url地址(http协议使用)")
//        };
//
//        final ParamObj[] UPGRADE_PARAMS = {
//                new ParamObj("module", "升级模块: 0-系统固件升级，1-DSP固件升级"),
//                new ParamObj("download", "下载方式: 0-http"),
//                new ParamObj("url", "固件下载地址"),
//                new ParamObj("token", "固件下载请求鉴权")
//        };
//
//        METHODS[0] = new MethodObj("GET_INFO", null, "获取设备基本信息");
//        METHODS[1] = new MethodObj("SET_TIME", null, "设备校时");
//        METHODS[2] = new MethodObj("SET_PLATFORM", SET_PLATFORM_PARAMS, "云平台配置");
//        METHODS[3] = new MethodObj("UPGRADE", UPGRADE_PARAMS, "设备固件升级");
//    }
//
//    public static byte[] hexStringToByte(String hex) {
//        int length = hex.length();
//        byte[] buffer = new byte[length / 2];
//
//        for (int i = 0; i < length; i += 2) {
//            buffer[i / 2] = (byte) (Integer.parseInt(hex.substring(i, i + 2), 16));
//        }
//
//        return buffer;
//    }
//
//    public static String byteToHexString(byte[] binary) {
//        StringBuilder sb = new StringBuilder();
//
//        for (byte b : binary) {
//
//            int i = (b & 0xff) << 1;
//            sb.append(TBL[i]).append(TBL[i + 1]);
//        }
//
//        return sb.toString();
//    }
//
//    private static class ChannelObj {
//        public ChannelObj(String name, String type, String desc) {
//            this.name = name;
//            this.type = type;
//            this.desc = desc;
//        }
//
//        public String name;
//        public String type;
//        public String desc;
//    }
//
//    private static class MethodObj {
//        public MethodObj(String method, ParamObj[] params, String desc) {
//            this.method = method;
//            if (params != null) {
//                for (ParamObj paramObj : params) {
//                    this.params.add(paramObj);
//                }
//            }
//            this.desc = desc;
//        }
//
//        public String method;
//
//        public List<ParamObj> params = new ArrayList<>();
//
//        public String desc;
//    }
//
//    private static class ParamObj {
//        public ParamObj(String key, String value) {
//            this.key = key;
//            this.value = value;
//        }
//
//        public String key;
//        public String value;
//    }
//
//    public static void main(String[] args) {
//        try {
//            CetcRadarCodec codec = new CetcRadarCodec();
//
//            System.out.println(codec.getDescription());
//            System.out.println();
//            JSONArray channels = codec.getChannelList();
//            for (int i = 0; i < channels.size(); i++) {
//                System.out.println(channels.getJSONObject(i));
//            }
//            System.out.println();
//            JSONArray methods = codec.getMethodList();
//            for (int j = 0; j < methods.size(); j++) {
//                System.out.println(methods.getJSONObject(j));
//            }
//
//            System.out.println();
//            String source = "6800D901000000000102030405060000018040DF030A33007B00000A00B9010001000200010A0300010B0400900000000100000002000000030000000400000005000000060000000700000008000000090000000a0000000b0000000c0000000d0000000e0000000f000000100000001100000012000000130000001400000015000000160000001700000018000000190000001a0000001b0000001c0000001d0000001e0000001f0000002000000021000000220000002300000024050002000306000400000005070001010a000200030b000200040016";
//            byte[] data = hexStringToByte(source);
//            System.out.println("len = " + data.length);
//            int checkSum = 0;
//            for (int i = 1; i < data.length - 1; i++) {
//                checkSum += (0xFF & data[i]);
//            }
//            data[data.length-2] = (byte) (0xFF & checkSum);
//            System.out.println("deviceReq: " + byteToHexString(data));
//            JSONObject dataObj = new JSONObject();
//            dataObj.put("source", data);
//            long t = System.currentTimeMillis();
//            JSON resultObj = codec.decode(dataObj);
//            System.out.println("consumer: " + (System.currentTimeMillis() - t));
//            System.out.println(resultObj.toJSONString());
//
//            System.out.println();
//            dataObj.put("errorCode", 0);
//            dataObj.put("type", "platAck");
//            List<byte[]> bAck = codec.encode(dataObj);
//            System.out.println("platAck: " + byteToHexString(bAck.get(0)));
//
//            System.out.println();
//            JSONObject methodObj = new JSONObject();
//            methodObj.put("method", "GET_INFO");
//            methodObj.put("sn", "010203040506");
//            methodObj.put("identity", "123");
//            methodObj.put("type", "platReq");
//            System.out.println(methodObj);
//            List<byte[]> resultList = codec.encode(methodObj);
//            System.out.println(byteToHexString(resultList.get(0)));
//
//            System.out.println();
//            methodObj.clear();
//            methodObj.put("method", "SET_TIME");
//            methodObj.put("sn", "010203040506");
//            methodObj.put("identity", "123");
//            methodObj.put("type", "platReq");
//            System.out.println(methodObj);
//            resultList = codec.encode(methodObj);
//            System.out.println(byteToHexString(resultList.get(0)));
//
//            System.out.println();
//            methodObj.clear();
//            methodObj.put("method", "SET_PLATFORM");
//            methodObj.put("sn", "010203040506");
//            methodObj.put("identity", "123");
//            methodObj.put("type", "platReq");
//
//            JSONObject params = new JSONObject();
//            params.put("protocol", 3);
//            params.put("ip", "10.88.203.121");
//            params.put("port", 32183);
//            methodObj.put("params", params);
//            System.out.println(methodObj);
//            resultList = codec.encode(methodObj);
//            System.out.println(byteToHexString(resultList.get(0)));
//
//            System.out.println();
//            methodObj.clear();
//            methodObj.put("method", "UPGRADE");
//            methodObj.put("sn", "010203040506");
//            methodObj.put("identity", "123");
//            methodObj.put("type", "platReq");
//
//            params.clear();
//            params.put("module", 0);
//            params.put("download", "0");
//            params.put("url", "http://10.88.203.121:12000/upgrade");
//            params.put("token", "123");
//            methodObj.put("params", params);
//            System.out.println(methodObj);
//            resultList = codec.encode(methodObj);
//            System.out.println(byteToHexString(resultList.get(0)));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

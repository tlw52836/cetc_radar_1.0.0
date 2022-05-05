package com.cetc.codec.radar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@CpixCodec
public class CetcRadarCodec_v2 implements ICpixCodec{
    static Logger LOGGER = LoggerFactory.getLogger(CetcRadarCodec_v2.class);

    @Override
    public void init() throws Exception {

    }

    @Override
    public JSON preDecode(JSONObject data) throws Exception {

        return null;
    }

    @Override
    public JSON decode(JSONObject var1) throws Exception {
        JSONObject result = null;

        try {
            JSONObject data = var1.getJSONObject("data");
            short func = data.getShort("func");

            if (func == 0x0A) {  // 设备请求：数据上发1
                result = new JSONObject();
                result.put("identify", var1.getString("ser"));
                result.put("type", "deviceReq");
                result.put("errorCode", 0);
                result.put("sn", var1.getString("addr"));
                JSONObject dataObj = new JSONObject();
                result.put("data", dataObj);

                String timeString = var1.getString("timestamp");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                long timestamp = sdf.parse(timeString).getTime();

                JSONArray tlv = data.getJSONArray("tlv");

                for (int i = 0; i < tlv.size(); i++) {
                    JSONObject curTlv = tlv.getJSONObject(i);
                    short tag = curTlv.getShort("tag");
                    short len = curTlv.getShort("len");
                    switch (tag) {
                        case 1:
                        case 2:
                        case 3:
                        case 7:
                        {
                            JSONObject svt = new JSONObject();
                            svt.put("t", timestamp);
                            svt.put("v", curTlv.getShort("val"));
                            dataObj.put("P" + tag, svt);
                        }
                        break;
                        case 4:
                        case 8:
                        case 9:
                        {
                            JSONArray val = curTlv.getJSONArray("val");
                            for (int j = 0; j < len; j++) {
                                JSONObject svt = new JSONObject();
                                svt.put("t", timestamp);
                                svt.put("v", val.getLong(j));
                                dataObj.put("P" + tag + "_" + j, svt);
                            }
                        }
                        break;
                        case 5:
                        case 10:
                        case 11:
                        {
                            JSONObject svt = new JSONObject();
                            svt.put("t", timestamp);
                            svt.put("v", curTlv.getInteger("val"));
                            dataObj.put("P" + tag, svt);
                        }
                        break;
                        case 6:
                        {
                            JSONObject svt = new JSONObject();
                            svt.put("t", timestamp);
                            svt.put("v", curTlv.getLong("val"));
                            dataObj.put("P" + tag, svt);
                        }
                        break;
                    }
                }
            } else if (func == 0x8F || (func >= 0x90 && func <= 0x9F)) { // 设备响应：固件升级响应与指令下发响应
                result = new JSONObject();
                result.put("identify", var1.getString("ser"));
                result.put("type", "deviceAck");
                result.put("errorCode", 0);
                result.put("sn", var1.getString("addr"));
                result.put("status", data.getShort("resultCode"));

                if (func == 0x90) {
                    JSONArray resultDetail = new JSONArray();
                    result.put("resultDetail", resultDetail);
                    JSONObject interData = data.getJSONObject("Data");

                    // 产品型号
                    JSONObject pmlObj = new JSONObject();
                    pmlObj.put("s", "pml");
                    pmlObj.put("v", interData.getString("pml"));
                    resultDetail.add(pmlObj);

                    // 系统软件版本
                    JSONObject sw1Obj = new JSONObject();
                    sw1Obj.put("s", "sw1");
                    sw1Obj.put("v", interData.getString("sw1"));
                    resultDetail.add(sw1Obj);

                    // DSP软件版本
                    JSONObject sw2Obj = new JSONObject();
                    sw2Obj.put("s", "sw2");
                    sw2Obj.put("v", interData.getString("sw2"));
                    resultDetail.add(sw2Obj);

                    // 硬件版本
                    JSONObject hwObj = new JSONObject();
                    hwObj.put("s", "hw");
                    hwObj.put("v", interData.getString("hw"));
                    resultDetail.add(hwObj);

                    // 设备IP
                    JSONObject ipObj = new JSONObject();
                    ipObj.put("s", "ip");
                    ipObj.put("v", interData.getString("ip"));
                    resultDetail.add(ipObj);

                    // 网卡地址
                    JSONObject macObj = new JSONObject();
                    macObj.put("s", "mac");
                    macObj.put("v", interData.getString("mac"));
                    resultDetail.add(macObj);

                } else if (func == 0x8F) {
                    JSONArray resultDetail = new JSONArray();
                    result.put("resultDetail", resultDetail);
                    JSONObject interData = var1.getJSONObject("Data");

                    JSONObject statusObj = new JSONObject();
                    statusObj.put("s", "status");
                    statusObj.put("v", data.getShort("status"));
                    resultDetail.add(statusObj);
                }
            }
        } catch (Throwable e) {
            LOGGER.info("CetcGytCodec decode error: {}", e);

            result = new JSONObject();
            result.put("identify", "123");
            result.put("type", "deviceReq");
            result.put("errorCode", -1);
        }

        return result;
    }

    @Override
    public JSON merge(JSONObject data) throws Exception {
        return null;
    }

    @Override
    public JSON encode(JSONObject var1) throws Exception {
        JSONObject result = null;
        String type = var1.getString("type");

        if (type.contentEquals("platReq")) {  //平台请求
            result = new JSONObject();
            result.put("version", "1.0.0");
            result.put("addr", var1.getString("sn"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            result.put("timestamp", sdf.format(new Date()));
            result.put("se", var1.getString("identity"));
            result.put("dir", 1);
            JSONObject data = new JSONObject();
            result.put("data", data);

            String method = var1.getString("method");

            if (method.contentEquals("GET_INFO")) {  //用户指令下发：0x10~0x1F
                data.put("func", 0x10);

            } else if (method.contentEquals("SET_TIME")) {
                data.put("func", 0x11);
                JSONObject newParams = new JSONObject();
                data.put("param", newParams);
                newParams.put("time", Calendar.getInstance().getTimeInMillis());

            } else if (method.contentEquals("SET_PLATFORM")) {
                data.put("func", 0x12);
                data.put("param", var1.getJSONObject("params"));

            } else if (method.contentEquals("UPGRADE")) {  //固件升级：0x0f
                data.put("func", 0x0F);
                data.put("param", var1.getJSONObject("params"));

            }
        } else if (type.contentEquals("platAck")) {  //平台响应
            JSONObject source = var1.getJSONObject("source");

            result = new JSONObject();
            result.put("version", source.getString("version"));
            result.put("addr",source.getString("addr"));
            result.put("timestamp", Calendar.getInstance().getTimeInMillis());
            result.put("se", var1.getString("identity"));
            result.put("dir", 1);
            JSONObject data = new JSONObject();
            result.put("data", data);

            data.put("func", 0x80 | source.getJSONObject("data").getShort("func"));
            data.put("resultCode",var1.getShort("errorCode"));
        }

        return result;
    }


    @Override
    public String getFactoryID() throws Exception {
        return "CETC";
    }

    @Override
    public String getModel() throws Exception {
        return "RADAR";
    }

    @Override
    public JSONObject getDescription() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("version", "1.0.0");
        obj.put("author", "TaoLiwei");
        obj.put("date", "2022-04-28");
        obj.put("desc", "雷达睡眠编解码插件");
        return obj;
    }


    @Override
    public JSONArray getMethodList() throws Exception {
        return (JSONArray) JSON.toJSON(METHODS);
    }

    @Override
    public JSONArray getChannelList() throws Exception {
        return (JSONArray) JSON.toJSON(CHANNELS);
    }

    private static final char[] TBL = new char[256 * 4];

    private static final CetcRadarCodec_v2.ChannelObj[] CHANNELS = new CetcRadarCodec_v2.ChannelObj[11];

    private static final CetcRadarCodec_v2.MethodObj[] METHODS = new CetcRadarCodec_v2.MethodObj[4];

    static {

        final char[] DIGITS = "0123456789ABCDEF".toCharArray();

        for (int i = 0; i < 256; i++) {

            TBL[i << 1] = DIGITS[i >>> 4 & 0x0F];
            TBL[(i << 1) + 1] = DIGITS[i & 0x0F];
        }

        CHANNELS[0] = new CetcRadarCodec_v2.ChannelObj("P1", "data", "跟踪状态");
        CHANNELS[1] = new CetcRadarCodec_v2.ChannelObj("P2", "data", "跟踪位置x坐标");
        CHANNELS[2] = new CetcRadarCodec_v2.ChannelObj("P3", "data", "跟踪位置y坐标");
        CHANNELS[3] = new CetcRadarCodec_v2.ChannelObj("P4_x", "data", "距离像(x = 0, 1, 2...35)");
        CHANNELS[4] = new CetcRadarCodec_v2.ChannelObj("P5", "data", "体动评分");
        CHANNELS[5] = new CetcRadarCodec_v2.ChannelObj("P6", "data", "体动幅度");
        CHANNELS[6] = new CetcRadarCodec_v2.ChannelObj("P7", "data", "体动等级");
        CHANNELS[7] = new CetcRadarCodec_v2.ChannelObj("P8", "data", "呼吸曲率");
        CHANNELS[8] = new CetcRadarCodec_v2.ChannelObj("P9", "data", "心跳曲率");
        CHANNELS[9] = new CetcRadarCodec_v2.ChannelObj("P10", "data", "呼吸率");
        CHANNELS[10] = new CetcRadarCodec_v2.ChannelObj("P11", "data", "心率");

        final CetcRadarCodec_v2.ParamObj[] SET_PLATFORM_PARAMS = {
                new CetcRadarCodec_v2.ParamObj("protocol", "协议类型: udp, tcp, mqtt, http"),
                new CetcRadarCodec_v2.ParamObj("ip", "设备IP地址"),
                new CetcRadarCodec_v2.ParamObj("port", "上报端口号"),
                new CetcRadarCodec_v2.ParamObj("url", "上报url地址(http协议使用)")
        };

        final CetcRadarCodec_v2.ParamObj[] UPGRADE_PARAMS = {
                new CetcRadarCodec_v2.ParamObj("module", "升级模块: 0-系统固件升级，1-DSP固件升级"),
                new CetcRadarCodec_v2.ParamObj("download", "下载方式: 0-http"),
                new CetcRadarCodec_v2.ParamObj("url", "固件下载地址"),
                new CetcRadarCodec_v2.ParamObj("token", "固件下载请求鉴权")
        };

        METHODS[0] = new CetcRadarCodec_v2.MethodObj("GET_INFO", null, "获取设备基本信息");
        METHODS[1] = new CetcRadarCodec_v2.MethodObj("SET_TIME", null, "设备校时");
        METHODS[2] = new CetcRadarCodec_v2.MethodObj("SET_PLATFORM", SET_PLATFORM_PARAMS, "云平台配置");
        METHODS[3] = new CetcRadarCodec_v2.MethodObj("UPGRADE", UPGRADE_PARAMS, "设备固件升级");
    }

    private static class ChannelObj {
        public ChannelObj(String name, String type, String desc) {
            this.name = name;
            this.type = type;
            this.desc = desc;
        }

        public String name;
        public String type;
        public String desc;
    }

    private static class MethodObj {
        public MethodObj(String method, CetcRadarCodec_v2.ParamObj[] params, String desc) {
            this.method = method;
            if (params != null) {
                for (CetcRadarCodec_v2.ParamObj paramObj : params) {
                    this.params.add(paramObj);
                }
            }
            this.desc = desc;
        }

        public String method;

        public List<CetcRadarCodec_v2.ParamObj> params = new ArrayList<>();

        public String desc;
    }

    private static class ParamObj {
        public ParamObj(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }
}

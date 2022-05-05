import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cetc.codec.radar.CetcRadarCodec_v2;
import com.cetc.codec.radar.ICpixCodec;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CodecTest {
    /**
     * 设备上行数据：用户数据上发
     * @throws Exception
     */
    @Test
    public void test01() throws Exception {
        JSONObject jsonData = new JSONObject();
        jsonData.put("version", "1.2.3");
        jsonData.put("addr", "10.88.204.95");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        jsonData.put("timestamp",timestamp);
        jsonData.put("ser", 12);
        jsonData.put("dir", 0);
        JSONObject data = new JSONObject();
        jsonData.put("data", data);

        data.put("func", 0x0A);
        JSONArray tlv = new JSONArray();
        data.put("tlv", tlv);
        JSONObject tlv1 = new JSONObject();
        JSONObject tlv2 = new JSONObject();
        JSONObject tlv3 = new JSONObject();
        JSONObject tlv4 = new JSONObject();
        tlv.add(tlv1);
        tlv.add(tlv2);
        tlv.add(tlv3);
        tlv.add(tlv4);

        tlv1.put("tag", 0x01);
        tlv1.put("len", 1);
        tlv1.put("val", 78);

        tlv2.put("tag", 0x05);
        tlv2.put("len", 1);
        tlv2.put("val", 99);

        tlv3.put("tag", 0x06);
        tlv3.put("len", 1);
        tlv3.put("val", 88949);

        tlv4.put("tag", 0x04);
        tlv4.put("len",4);
        JSONArray val = new JSONArray();
        tlv4.put("val", val);
        val.add(111);
        val.add(222);
        val.add(333);
        val.add(444);


        ICpixCodec codec = new CetcRadarCodec_v2();

        JSON res = codec.decode(jsonData);
        System.out.println(res);
    }

    /**
     * 设备上行数据：设备响应
     * @throws Exception
     */
    @Test
    public void test02() throws Exception {
        JSONObject jsonData = new JSONObject();
        jsonData.put("version", "1.2.3");
        jsonData.put("addr", "10.88.204.95");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        jsonData.put("timestamp",timestamp);
        jsonData.put("ser", 12);
        jsonData.put("dir", 0);
        JSONObject data = new JSONObject();
        jsonData.put("data", data);

//        data.put("func", 0x90);
//        data.put("resultCode", 0);
//        JSONObject interData = new JSONObject();
//        data.put("Data", interData);
//
//        interData.put("result", 0);
//        interData.put("pml", "设备型号");
//        interData.put("sw1", "系统软件版本");
//        interData.put("sw2", "DSP软件版本");
//        interData.put("hw", "硬件版本号");
//        interData.put("ip", "设备Ip地址");
//        interData.put("mac", "网卡地址");

        data.put("func", 0x8F);
        data.put("resultCode", 0);
        JSONObject interData = new JSONObject();
        data.put("Data", interData);

        interData.put("result", 0);
        interData.put("status", 0);


        ICpixCodec codec = new CetcRadarCodec_v2();

        JSON res = codec.decode(jsonData);
        System.out.println(res);
    }


    /**
     * 平台下行数据：平台指令下发与固件升级
     * @throws Exception
     */
    @Test
    public void test03() throws Exception {
        JSONObject jsonData = new JSONObject();
        jsonData.put("identify", "13");
        jsonData.put("type", "platReq");
        jsonData.put("sn", "10.88.204.95");
        jsonData.put("method","GET_INFO");
        JSONObject params = new JSONObject();
        jsonData.put("params", null);


        ICpixCodec codec = new CetcRadarCodec_v2();

        JSON res = codec.encode(jsonData);
        System.out.println(res);
    }
}

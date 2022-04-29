package codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


public interface ICpixCodec {

    /**
     * 初始化插件
     * @throws Exception
     */
    public void init() throws Exception;

    /**
     * 对设备上发数据的预处理，提取设备号
     * @param data
     * @return
     * @throws Exception
     */
    public JSON preDecode(JSONObject data) throws Exception;

    /**
     * 对设备上发数据，设备响应数据的协议解码接口
     * @param data 设备原始数据
     * {
     * 		"source": [0x00, 0x01, 0x02],	设备原始数据
     * 		"sectty": 0,					0-不加密，1-加密
     * 		"encryty": 1,					1:AES128   2:
     * 		"secret": "12345678" 			加密秘钥
     *      "ext": {}						其他扩展参数
     * }
     * @return
     * @throws Exception
     */
    public JSON decode(JSONObject data) throws Exception;

    /**
     * 给分包解码后的数据进行编排整合
     * @param data
     * {
     *     "source": {}
     *     "ext": {}
     * }
     * @return
     * @throws Exception
     */
    public JSON merge(JSONObject data) throws Exception;

    /**
     * 对平台指令请求，平台响应设备的内容编码为设备协议二进制数据
     * @param obj
     * {
     *     "type": "platAck"        platAck-平台应答，platReq-平台请求
     *     "method": "方法名",
     *     "params": {
     *         "key1": "val1",
     *         "key2": "val2",
     *     },
     *     "sn": "设备号",
     *     "sectty": 0,             0-不加密，1-加密
     *     "encryty": 1,            1:AES128 2:
     *     "secret": "12345678",    加密秘钥
     *     "ext": {}                其他扩展参数
     * }
     * @return
     * @throws Exception
     */
    public JSON encode(JSONObject obj) throws Exception;

    /**
     * 获取编解码插件厂商ID
     * @return
     * @throws Exception
     */
    String getFactoryID() throws Exception;

    /**
     * 获取编解码插件型号
     * @return
     * @throws Exception
     */
    String getModel() throws Exception;

    /**
     * 获取编解码插件其他信息，如版本信息，作者，联系电话，插件描述，创建日期等
     * @return
     * @throws Exception
     */
    JSONObject getDescription() throws Exception;

    /**
     * 获取编解码插件支持的设备指令
     * @return
     * 返回json格式如下：
     * [
     * 	{
     *   "method": "指令名称1",
     *	 "params": {
     *   	"param1": "参数描述",
     *      "param2": "参数描述"
     *   },
     *   "desc": "指令描述"
     *  },
     *  {
     *   "method": "指令名称2",
     *	 "params": {
     *   	"param1": "参数描述",
     *      "param2": "参数描述"
     *   },
     *   "desc": "指令描述"
     *  }
     * ]
     * @throws Exception
     */
    JSONArray getMethodList() throws Exception;

    /**
     * 获取编解码插件支持的通道说明
     * @return
     * [
     * 	{
     *   "name": "通道名称1",
     *   "type": "设备信息",	// 数据类型：设备信息，设备数据，设备事件
     *   "desc": "通道描述"
     *  },
     *  {
     *   "name": "通道名称2",
     *   "type": "设备数据",
     *   "desc": "通道描述"
     *  },
     *  {
     *   "name": "通道名称3",
     *   "type": "设备事件",
     *   "desc": "通道描述"
     *  }
     * ]
     * @throws Exception
     */
    JSONArray getChannelList() throws Exception;
}

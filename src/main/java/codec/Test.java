package codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class Test {
    public static void main(String[] args) throws IOException {
        InputStream is = new FileInputStream(new File("E:\\json\\3e0f3edf-ce4c-4429-8dc8-aaa51ca8cd7d.txt"));

        byte[] buf = new byte[is.available()];
        is.read(buf, 0, buf.length);

        JSONObject jsonObject = (JSONObject) JSON.parse(new String(buf));
        System.out.println(jsonObject.get("name"));
        System.out.println(jsonObject.get("age"));
        System.out.println(jsonObject.get("phone"));
        System.out.println(jsonObject.get("address"));

    }
}

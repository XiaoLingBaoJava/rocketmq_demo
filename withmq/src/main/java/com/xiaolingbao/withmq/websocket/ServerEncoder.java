package com.xiaolingbao.withmq.websocket;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.util.HashMap;

public class ServerEncoder implements Encoder.Text<HashMap> {

    private static final Logger log = LoggerFactory.getLogger(ServerEncoder.class);

    /*
     * 这里是重点，只需要返回Object序列化后的json字符串就行
     * 你也可以使用gosn，fastJson来序列化。
     * 这里我使用fastjson
     */
    @Override
    public String encode(HashMap hashMap) throws EncodeException {
        try {
            return JSONObject.toJSONString(hashMap);
        }catch (Exception e){
            log.error("",e);
        }
        return null;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}

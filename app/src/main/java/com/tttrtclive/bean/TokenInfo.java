package com.tttrtclive.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Administrator on 2017-10-18.
 */

public class TokenInfo {
    /**
     * code : 0
     * msg : momolive_1508314510_888_123
     * data : lTxtzvZI6HfnnTo+MdUZRcvm0ryGAZXQkFMLkqxrfFE=
     */
    private int code;
    private String msg;
    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getData() {
        String encodeResult = "";
        try {
            encodeResult = URLEncoder.encode(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeResult;
    }

    public void setData(String data) {
        this.data = data;
    }

}

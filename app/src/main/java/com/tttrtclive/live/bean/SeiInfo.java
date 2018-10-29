package com.tttrtclive.live.bean;

import java.util.List;

public class SeiInfo {

    /**
     * mid : 848025
     * pos : [{"id":"168144","z":1,"x":0,"y":0.4859375,"w":0.3333333333333333,"h":0.25},{"id":"848025","z":0,"x":0,"y":0,"w":1,"h":1}]
     */

    private String mid;
    private List<PosBean> pos;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public List<PosBean> getPos() {
        return pos;
    }

    public void setPos(List<PosBean> pos) {
        this.pos = pos;
    }

    public static class PosBean {
        /**
         * id : 168144
         */

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }
}

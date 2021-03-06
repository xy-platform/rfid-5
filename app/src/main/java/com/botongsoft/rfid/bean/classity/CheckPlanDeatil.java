package com.botongsoft.rfid.bean.classity;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Id;
import com.lidroid.xutils.db.annotation.Table;
import com.lidroid.xutils.db.annotation.Transient;

/**盘点错误表
 * Created by pc on 2017/6/14.
 */
@Table
public class CheckPlanDeatil implements java.io.Serializable {

    @Id
    private int lid;//安卓端主键
    @Column
    private int id;
    @Column
    private int pdid;
    @Column
    private int mjgid;
    @Column
    private String bm;
    @Column
    private int type;
    @Column
    private int status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column

    private long anchor;
    @Transient
    private String title;//页面拼接的扫描标签

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getAnchor() {
        return anchor;
    }

    public void setAnchor(long anchor) {
        this.anchor = anchor;
    }

    public int getLid() {
        return lid;
    }

    public void setLid(int lid) {
        this.lid = lid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPdid() {
        return pdid;
    }

    public void setPdid(int pdid) {
        this.pdid = pdid;
    }

    public int getMjgid() {
        return mjgid;
    }

    public void setMjgid(int mjgid) {
        this.mjgid = mjgid;
    }

    public String getBm() {
        return bm;
    }

    public void setBm(String bm) {
        this.bm = bm;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getKfid() {
        return kfid;
    }

    public void setKfid(int kfid) {
        this.kfid = kfid;
    }

    public int getMjjid() {
        return mjjid;
    }

    public void setMjjid(int mjjid) {
        this.mjjid = mjjid;
    }

    public int getZy() {
        return zy;
    }

    public void setZy(int zy) {
        this.zy = zy;
    }

    @Column
    private int kfid;
    @Column
    private int mjjid;
    @Column
    private int zy;
    @Column
    private int jlid;

    public int getJlid() {
        return jlid;
    }

    public void setJlid(int jlid) {
        this.jlid = jlid;
    }
}

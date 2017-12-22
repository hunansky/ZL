package com.wdtx.littlebell.model;

/**
 * Created by wending on 2017/6/29.
 * ...
 */
public class Relation {

    private int id;
    private int babyId;
    private int parentId;
    private RelationState state;
    private long time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBabyId() {
        return babyId;
    }

    public void setBabyId(int babyId) {
        this.babyId = babyId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public RelationState getState() {
        return state;
    }

    public void setState(RelationState state) {
        this.state = state;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Relation{" +
                "id=" + id +
                ", babyId=" + babyId +
                ", parentId=" + parentId +
                ", state=" + state +
                ", time=" + time +
                '}';
    }
}

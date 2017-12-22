package com.wdtx.littlebell.model;

/**
 * Created by wending on 2017/6/29.
 * ...
 */
public class RelationQueryResult{

    private User user;
    private RelationState state;

    public RelationState getState() {
        return state;
    }

    public void setState(RelationState state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

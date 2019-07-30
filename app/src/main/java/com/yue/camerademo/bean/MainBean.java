package com.yue.camerademo.bean;

/**
 * @auther shimingyue
 * @create 2018/8/21
 * @description
 */
public class MainBean {

    private Class activity;
    private String title;
    private String desc;

    public MainBean() {
    }



    public MainBean(Class activity, String title, String desc) {
        this.activity = activity;
        this.title = title;
        this.desc = desc;
    }

    public Class getActivity() {
        return activity;
    }

    public void setActivity(Class activity) {
        this.activity = activity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

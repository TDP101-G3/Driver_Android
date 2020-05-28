package com.lu.driver;

import java.io.Serializable;

public class Opinion implements Serializable {
    private int driver_opinion_id;
    private int driver_id;
    private String driver_opinion_question;
    private String driver_opinion_answer;

    public Opinion(int driver_opinion_id, int driver_id, String driver_opinion_question, String driver_opinion_answer) {
        this.driver_opinion_id = driver_opinion_id;
        this.driver_id = driver_id;
        this.driver_opinion_question = driver_opinion_question;
        this.driver_opinion_answer = driver_opinion_answer;
    }

    public Opinion(int driver_id, String driver_opinion_question) {
        this.driver_id = driver_id;
        this.driver_opinion_question = driver_opinion_question;
    }

    public int getDriver_opinion_id() {
        return driver_opinion_id;
    }

    public void setDriver_opinion_id(int driver_opinion_id) {
        this.driver_opinion_id = driver_opinion_id;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_opinion_question() {
        return driver_opinion_question;
    }

    public void setDriver_opinion_question(String driver_opinion_question) {
        this.driver_opinion_question = driver_opinion_question;
    }

    public String getDriver_opinion_answer() {
        return driver_opinion_answer;
    }

    public void setDriver_opinion_answer(String driver_opinion_answer) {
        this.driver_opinion_answer = driver_opinion_answer;
    }

}

package com.shortstack.hackertracker.Model;

/**
 * Created by Whitney Champion on 4/21/14.
 */
public class SpeakerList extends ApiBase {

    private Speaker[] schedule;

    public Speaker[] getSpeakers() {
        return schedule;
    }

    public void setSpeakers(Speaker[] schedule) {
        this.schedule = schedule;
    }

}
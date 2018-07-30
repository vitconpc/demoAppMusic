package vn.com.example.demoappmusicservice.service;

import java.util.List;

import vn.com.example.demoappmusicservice.model.Song;

public interface TaskListener {
    void onCommitLoad(List<Song> songs);

    void postName(String name);

    void postTime(int total);

    void onCurrentTime(int currentPosition);

    void pauseMusicButton();

    void startMusicButton();
}

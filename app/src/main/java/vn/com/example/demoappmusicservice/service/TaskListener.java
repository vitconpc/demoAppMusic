package vn.com.example.demoappmusicservice.service;

import java.util.List;

import vn.com.example.demoappmusicservice.model.Music;

public interface TaskListener {
    void onCommitLoad(List<Music> musics);

    void postName(String name);

    void postTime(int total);

    void onCurrentTime(int currentPosition);

    void pauseMusicButton();

    void startMusicButton();
}

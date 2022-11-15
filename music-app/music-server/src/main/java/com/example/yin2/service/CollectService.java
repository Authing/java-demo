package com.example.yin2.service;

import com.example.yin2.domain.Collect;

import java.util.List;

public interface CollectService {

    boolean addCollection(Collect collect);

    boolean existSongId(String authingUserId, Integer songId);

    boolean deleteCollect(String authingUserId, Integer songId);

    List<Collect> collectionOfUser(String authingUserId);
}

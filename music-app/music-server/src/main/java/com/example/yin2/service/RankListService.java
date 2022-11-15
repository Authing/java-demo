package com.example.yin2.service;

import com.example.yin2.domain.RankList;

public interface RankListService {

    boolean addRank(RankList rankList);

    int rankOfSongListId(Long songListId);

    int getUserRank(String authingUserId, Long songListId);

}

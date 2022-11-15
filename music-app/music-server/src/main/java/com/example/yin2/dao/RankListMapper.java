package com.example.yin2.dao;

import com.example.yin2.domain.RankList;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RankListMapper {

    int insert(RankList record);

    int insertSelective(RankList record);

    /**
     * 查总分
     * @param songListId
     * @return
     */
    int selectScoreSum(Long songListId);

    /**
     * 查总评分人数
     * @param songListId
     * @return
     */
    int selectRankNum(Long songListId);

    /**
     * 查制定用户评分
     * @param authingUserId
     * @param songListId
     * @return
     */
    int selectUserRank(@Param("authingUserId") String authingUserId, @Param("songListId")  Long songListId);
}

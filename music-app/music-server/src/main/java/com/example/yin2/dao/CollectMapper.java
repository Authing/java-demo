package com.example.yin2.dao;

import com.example.yin2.domain.Collect;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Collect record);

    int insertSelective(Collect record);

    Collect selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Collect record);

    int updateByPrimaryKey(Collect record);

    int existSongId(@Param("authingUserId") String authingUserId, @Param("songId") Integer songId);

    int deleteCollect(@Param("authingUserId") String authingUserId, @Param("songId") Integer songId);

    List<Collect> collectionOfUser(String authingUserId);
}

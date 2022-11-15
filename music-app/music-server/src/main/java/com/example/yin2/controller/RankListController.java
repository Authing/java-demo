package com.example.yin2.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import cn.hutool.core.util.StrUtil;
import com.example.yin2.common.ErrorMessage;
import com.example.yin2.common.SuccessMessage;
import com.example.yin2.domain.RankList;
import com.example.yin2.service.impl.RankListServiceImpl;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class RankListController {

    @Autowired
    private RankListServiceImpl rankListService;

    @Autowired
    private AuthenticationClient authenticationClient;

    // 提交评分
    @ResponseBody
    @RequestMapping(value = "/rankList/add", method = RequestMethod.POST)
    public Object addRank(HttpServletRequest req) {
        String songListId = req.getParameter("songListId").trim();
        String consumerId = req.getParameter("consumerId").trim();
        String score = req.getParameter("score").trim();

        RankList rank_list = new RankList();
        rank_list.setSongListId(Long.parseLong(songListId));
        rank_list.setConsumerId(Long.parseLong(consumerId));
        rank_list.setScore(Integer.parseInt(score));

        boolean res = rankListService.addRank(rank_list);
        if (res) {
            return new SuccessMessage<Null>("评价成功").getMessage();
        } else {
            return new ErrorMessage("评价失败").getMessage();
        }
    }

    // 获取指定歌单的评分
    @RequestMapping(value = "/rankList", method = RequestMethod.GET)
    public Object rankOfSongListId(HttpServletRequest req) {
        String songListId = req.getParameter("songListId");
        
        return new SuccessMessage<Number>(null, rankListService.rankOfSongListId(Long.parseLong(songListId))).getMessage();
    }
    
    // 获取指定用户的歌单评分
    @RequestMapping(value = "/rankList/user", method = RequestMethod.GET)
    public Object getUserRank(HttpServletRequest req, @CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
        //        String consumerId = req.getParameter("consumerId");
        String songListId = req.getParameter("songListId");

        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();

        return new SuccessMessage<Number>(null, rankListService.getUserRank(authingUserId, Long.parseLong(songListId))).getMessage();
    }
}

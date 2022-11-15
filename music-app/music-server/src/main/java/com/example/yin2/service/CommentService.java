package com.example.yin2.service;

import com.example.yin2.domain.Comment;

import java.util.List;

public interface CommentService {

    boolean addComment(Comment comment);

    boolean updateCommentMsg(Comment comment);

    boolean deleteComment(String id);

    List<Comment> commentOfSongId(Integer songId);

    List<Comment> commentOfSongListId(Integer songListId);

}

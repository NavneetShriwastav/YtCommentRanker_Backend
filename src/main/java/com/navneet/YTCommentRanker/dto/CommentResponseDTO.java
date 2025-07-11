package com.navneet.YTCommentRanker.dto;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private String author;
    private String comment;
    private int likes;
    private int replies;
    private int score;
}
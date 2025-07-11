package com.navneet.YTCommentRanker.model;

import lombok.Data;

@Data
public class YouTubeComment {
    private String author;
    private String text;
    private int likeCount;
    private int replyCount;
}

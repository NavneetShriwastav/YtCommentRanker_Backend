package com.navneet.YTCommentRanker.service;

import com.navneet.YTCommentRanker.dto.CommentResponseDTO;
import java.util.List;

public interface CommentService {
    List<CommentResponseDTO> getTopComments(String videoId);
}

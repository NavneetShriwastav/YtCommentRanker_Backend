package com.navneet.YTCommentRanker.controller;

import com.navneet.YTCommentRanker.dto.CommentResponseDTO;
import com.navneet.YTCommentRanker.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{videoId}")
    public List<CommentResponseDTO> getTopComments(@PathVariable String videoId) {
        return commentService.getTopComments(videoId.trim()); // .trim() is optional but useful
    }

}
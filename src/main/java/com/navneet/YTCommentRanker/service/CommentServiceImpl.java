package com.navneet.YTCommentRanker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navneet.YTCommentRanker.dto.CommentResponseDTO;
import com.navneet.YTCommentRanker.exception.VideoNotFoundException;
import com.navneet.YTCommentRanker.model.YouTubeComment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.base-url}")
    private String baseUrl;

    @Value("${app.top.comments.return}")
    private int topCommentsLimit;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommentServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CommentResponseDTO> getTopComments(String videoId) {
        List<YouTubeComment> commentList = new ArrayList<>();
        String pageToken = null;

        try {
            do {
                String url = String.format(
                        "%s/commentThreads?key=%s&textFormat=plainText&part=snippet&videoId=%s&maxResults=50%s",
                        baseUrl, apiKey, videoId.trim(),
                        pageToken != null ? "&pageToken=" + pageToken : ""
                );

                String response = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(response);
                JsonNode items = root.get("items");

                if (items == null || !items.isArray()) {
                    break;
                }

                for (JsonNode item : items) {
                    try {
                        JsonNode snippetNode = item.path("snippet");
                        JsonNode topLevel = snippetNode.path("topLevelComment").path("snippet");
                        if (topLevel.isMissingNode() || topLevel.isNull()) continue;

                        YouTubeComment comment = new YouTubeComment();
                        comment.setAuthor(topLevel.path("authorDisplayName").asText(""));
                        comment.setText(topLevel.path("textDisplay").asText(""));
                        comment.setLikeCount(topLevel.path("likeCount").asInt(0));
                        comment.setReplyCount(snippetNode.path("totalReplyCount").asInt(0));
                        commentList.add(comment);

                        if (commentList.size() >= topCommentsLimit) {
                            break; // stop if limit reached
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (commentList.size() >= topCommentsLimit) {
                    break;
                }

                pageToken = root.path("nextPageToken").asText(null);

            } while (pageToken != null && commentList.size() < topCommentsLimit);

            if (commentList.isEmpty()) {
                throw new VideoNotFoundException("No comments found or invalid video ID");
            }

            return commentList.stream()
                    .map(c -> {
                        int likes = c.getLikeCount();
                        int replies = c.getReplyCount();
                        int length = c.getText().length();
                        double score = likes + replies * 2 + (double) length / 50;
                        CommentResponseDTO dto = new CommentResponseDTO();
                        dto.setAuthor(c.getAuthor());
                        dto.setComment(c.getText());
                        dto.setLikes(likes);
                        dto.setReplies(replies);
                        dto.setScore((int) Math.round(score));
                        return dto;
                    })
                    .sorted(Comparator.comparingInt(CommentResponseDTO::getScore).reversed())
                    .limit(topCommentsLimit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw new VideoNotFoundException("Failed to fetch comments. Check video ID or API key.");
        }
    }
}

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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommentServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<CommentResponseDTO> getTopComments(String videoId) {
        String url = String.format(
                "%s/commentThreads?key=%s&textFormat=plainText&part=snippet&videoId=%s&maxResults=100",
                baseUrl, apiKey, videoId.trim()
        );

        try {
            // Fetch raw response
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.get("items");

            if (items == null || !items.isArray()) {
                throw new VideoNotFoundException("No comments found or invalid video ID");
            }

            // Parse comments
            List<YouTubeComment> commentList = new ArrayList<>();
            for (JsonNode item : items) {
                try {
                    JsonNode snippetNode = item.path("snippet");
                    JsonNode topLevel = snippetNode.path("topLevelComment").path("snippet");
                    if (topLevel.isMissingNode() || topLevel.isNull()) continue;

                    YouTubeComment comment = new YouTubeComment();
                    comment.setAuthor(topLevel.path("authorDisplayName").asText(""));
                    String text = topLevel.path("textDisplay").asText("");
                    comment.setText(text);
                    comment.setLikeCount(topLevel.path("likeCount").asInt(0));
                    comment.setReplyCount(snippetNode.path("totalReplyCount").asInt(0));
                    commentList.add(comment);
                } catch (Exception e) {
                    // continue on faulty comment
                    e.printStackTrace();
                }
            }

            // Compute scores with weighted factors
            List<CommentResponseDTO> scored = commentList.stream()
                    .map(c -> {
                        int likes = c.getLikeCount();
                        int replies = c.getReplyCount();
                        int length = c.getText().length();
                        // Weights: reply*2, length factor = length/50
                        double score = likes
                                + replies * 2
                                + (double) length / 50;
                        CommentResponseDTO dto = new CommentResponseDTO();
                        dto.setAuthor(c.getAuthor());
                        dto.setComment(c.getText());
                        dto.setLikes(likes);
                        dto.setReplies(replies);
                        dto.setScore((int) Math.round(score));
                        return dto;
                    })
                    .sorted(Comparator.comparingInt(CommentResponseDTO::getScore).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            return scored;
        } catch (Exception e) {
            e.printStackTrace();
            throw new VideoNotFoundException("Failed to fetch comments. Check video ID or API key.");
        }
    }
}

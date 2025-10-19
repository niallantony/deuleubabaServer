package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.feed.FeedDTO;
import com.niallantony.deulaubaba.dto.feed.FeedPostDTO;
import com.niallantony.deulaubaba.exceptions.InvalidCommentPostException;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.FeedService;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(path = "/feed",produces = "application/json")
public class FeedController {

    private final FeedService feedService;
    private final JsonUtils jsonUtils;

    public FeedController(FeedService feedService, JsonUtils jsonUtils) {
        this.feedService = feedService;
        this.jsonUtils = jsonUtils;
    }

    @GetMapping(path = "/{student_id}")
    public ResponseEntity<FeedDTO> getFeed(
            @CurrentUser String userId,
            @PathVariable("student_id") String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(feedService.getFeed(userId, studentId, page, size));
    }

    @PostMapping(path = "/{student_id}")
    public ResponseEntity<?> postComment(
            @CurrentUser String userId,
            @PathVariable("student_id") String studentId,
            @RequestBody String data
    ) {
        FeedPostDTO request = jsonUtils.parse(
                data,
                FeedPostDTO.class,
                () -> new InvalidCommentPostException("Invalid comment post")
        );
        feedService.postComment(userId, studentId, request);
        return ResponseEntity.noContent()
                             .location(URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/feed").toUriString() + "/" + studentId) )
                             .build();
    }

}

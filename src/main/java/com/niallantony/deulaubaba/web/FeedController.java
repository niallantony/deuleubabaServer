package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.feed.FeedDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.FeedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/feed",produces = "application/json")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
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

}

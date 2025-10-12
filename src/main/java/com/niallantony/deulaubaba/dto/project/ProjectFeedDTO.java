package com.niallantony.deulaubaba.dto.project;

import com.niallantony.deulaubaba.domain.ProjectFeedItem;
import lombok.Data;

import java.util.List;

@Data
public class ProjectFeedDTO {
    List<ProjectFeedItemDTO> feed;
}

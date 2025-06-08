package befly.community.dto;

import java.util.List;
import lombok.Builder;

@Builder
public class FreePostSearchResult {
    private List<FreePostSearchResponse> posts;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private boolean hasPrevious;
}
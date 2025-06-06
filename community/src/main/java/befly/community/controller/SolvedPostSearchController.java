    package befly.community.controller;

    import befly.common.apiPayload.ApiResponse;
    import befly.community.dto.FreePostSearchResponse;
    import befly.community.dto.SolvedPostSearchResponse;
    import befly.community.service.FreePostSearchService;
    import befly.community.service.SolvedPostSearchService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @RestController
    @RequestMapping("/community/search")
    @RequiredArgsConstructor
    public class SolvedPostSearchController {
        private final SolvedPostSearchService solvedPostSearchService;
        private final FreePostSearchService freePostSearchService;

        /**
         * 8개씩, 카테고리/키워드별 해결함 게시글 검색
         * 예: /api/search/solved?category=불안&page=0&keyword=좋아
         */
        @GetMapping("/solved")
        public ApiResponse<List<SolvedPostSearchResponse>> searchSolvedPosts(
                @RequestParam(required = false, defaultValue = "전체") String category,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(required = false, defaultValue = "") String keyword) {
            List<SolvedPostSearchResponse> results = solvedPostSearchService.searchSolvedPosts(category, keyword, page);
            return ApiResponse.onSuccess(results);
        }

        @GetMapping("/free")
        public ApiResponse<List<FreePostSearchResponse>> searchFreePosts(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(required = false, defaultValue = "") String keyword) {
            List<FreePostSearchResponse> results = freePostSearchService.searchFreePosts(keyword, page);
            return ApiResponse.onSuccess(results);
        }
    }

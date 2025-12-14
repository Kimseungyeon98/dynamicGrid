package com.example.dynamicgrid.main;

import com.example.dynamicgrid.dto.UserConfigReq;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DynamicGridController {

    private final DynamicGridService service;

    @GetMapping("/dynamicGrid")
    public String dynamicGrid(Model model) {
        // DB에 저장된 그리드 코드 (예: FACILITY_ASSET_GRID)
        String gridCode = "FACILITY_ASSET_GRID";

        // getGridContext 내부에서 tableData 조회 로직은 제거하거나, 빈 리스트를 반환하도록 수정했다고 가정
        // 혹은, 첫 페이지(0페이지)만 포함해서 보낼 수도 있음.
        // 여기서는 깔끔하게 V4.0 방식인 "껍데기만 가고 데이터는 JS가 부른다"로 갑니다.

        Map<String, Object> context = service.getGridContext(gridCode); // 여기선 컬럼정보, 유저설정만 가져옴
        context.put("tableData", Collections.emptyList()); // 초기 데이터는 비워둠

        model.addAttribute("gridContext", context);
        return "dynamicGrid";
    }

    // 2. [New] 데이터 조회용 API
    @GetMapping("/api/grid/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGridData(
            @RequestParam String gridCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword, // [New] 검색어
            @RequestParam(required = false, defaultValue = "id") String sortField, // [New] 정렬 필드
            @RequestParam(required = false, defaultValue = "DESC") String sortDir     // [New] 정렬 방향
    ) {
        // 정렬 객체 생성
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
                Sort.by(sortField).ascending() :
                Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Map<String, Object> result = service.getGridData(gridCode, keyword, pageable);
        return ResponseEntity.ok(result);
    }

    // [New] 사용자 설정 저장 API
    @PostMapping("/api/user/config/save")
    @ResponseBody
    public ResponseEntity<String> saveUserConfig(@RequestBody UserConfigReq req) {
        // 현재 로그인한 사용자명 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
            service.saveUserConfig(username, req);
            return ResponseEntity.ok("Saved");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
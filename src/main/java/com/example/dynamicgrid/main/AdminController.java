package com.example.dynamicgrid.main;

import com.example.dynamicgrid.dto.RoleConfigReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DynamicGridService service;

    // 1. 관리자 페이지 화면 이동
    @GetMapping("/permission")
    public String permissionPage(Model model) {
        // 그리드 코드 (단일 예제라 고정)
        String gridCode = "FACILITY_ASSET_GRID";

        // 화면 초기화에 필요한 데이터들
        model.addAttribute("roles", service.getAllRoles()); // 드롭다운용 Role 목록
        model.addAttribute("gridContext", service.getGridContext(gridCode)); // 전체 컬럼 정보 얻기 위해 재사용

        return "admin/permission"; // admin 폴더 생성 필요
    }

    // 2. [API] 특정 Role의 설정(보이는 컬럼) 가져오기
    @GetMapping("/api/config/{roleName}")
    @ResponseBody
    public ResponseEntity<List<String>> getRoleConfig(@PathVariable String roleName) {
        // 예: FACILITY_ASSET_GRID 고정
        List<String> visibleColumns = service.getVisibleColumnsForRole("FACILITY_ASSET_GRID", roleName);
        return ResponseEntity.ok(visibleColumns);
    }

    // 3. [API] 설정 저장하기
    @PostMapping("/api/save")
    @ResponseBody
    public ResponseEntity<String> saveRoleConfig(@RequestBody RoleConfigReq req) {
        try {
            service.updateRoleConfig(req);
            return ResponseEntity.ok("저장되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
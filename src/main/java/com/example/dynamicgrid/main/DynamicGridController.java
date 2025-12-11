package com.example.dynamicgrid.main;

import com.example.dynamicgrid.dto.UserConfigReq;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DynamicGridController {

    private final DynamicGridService service;

    @GetMapping("/dynamicGrid")
    public String dynamicGrid(Model model) {
        // DB에 저장된 그리드 코드 (예: FACILITY_ASSET_GRID)
        String gridCode = "FACILITY_ASSET_GRID";

        // Service에서 데이터+설정 모두 가져오기
        Map<String, Object> gridContext = service.getGridContext(gridCode);

        // Thymeleaf로 전달
        model.addAttribute("gridContext", gridContext);

        return "dynamicGrid";
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
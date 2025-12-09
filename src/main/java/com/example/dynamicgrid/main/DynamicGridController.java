package com.example.dynamicgrid.main;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
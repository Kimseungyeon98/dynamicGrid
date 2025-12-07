package com.example.dynamicgrid.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import tools.jackson.databind.ObjectMapper;

@Controller
public class DynamicGridController {

    @Autowired
    private DynamicGridService service;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/")
    public String index(Model model){
        // 1. 데이터 (권한에 따라 민감 데이터는 Map에 존재하지 않음)
        String tableDataJson = objectMapper.writeValueAsString(service.getFilteredAssets());

        // 2. 권한 설정 (렌더링 가능한 컬럼 목록)
        String allowedColumnsJson = objectMapper.writeValueAsString(service.getAllowedColumns());

        // 3. 사용자 설정 (hidden 처리할 컬럼 목록)
        String hiddenColumnsJson = objectMapper.writeValueAsString(service.getUserHiddenColumns());

        model.addAttribute("tableDataJson", tableDataJson);
        model.addAttribute("allowedColumnsJson", allowedColumnsJson);
        model.addAttribute("hiddenColumnsJson", hiddenColumnsJson);

        return "dynamicGrid";
    }
}

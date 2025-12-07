-- 테이블 생성
CREATE TABLE facility_asset (
                                id SERIAL PRIMARY KEY,
                                asset_code VARCHAR(50) NOT NULL UNIQUE,  -- 자산 번호 (예: HVAC-001)
                                asset_name VARCHAR(100) NOT NULL,        -- 자산 명칭
                                category VARCHAR(50),                    -- 분류 (기계, 전기, 소방 등)
                                location VARCHAR(100),                   -- 설치 위치
                                model_name VARCHAR(100),                 -- 모델명
                                manufacturer VARCHAR(100),               -- 제조사
                                install_date DATE,                       -- 설치일
                                status VARCHAR(20),                      -- 상태 (정상, 고장, 점검중)
                                purchase_cost NUMERIC(15, 2),            -- 구매 비용 (민감 정보 - 권한 필요)
                                contract_details TEXT                    -- 계약 내용 (민감 정보 - 권한 필요)
);

INSERT INTO facility_asset (asset_code, asset_name, category, location, model_name, manufacturer, install_date, status, purchase_cost, contract_details) VALUES
('HVAC-001', '메인 칠러 1호기', '기계설비', 'B2 기계실', 'RT-1000', 'LG전자', '2020-05-15', '정상', 150000000, '유지보수 계약: A사 (월 50만원)'),
('ELEC-102', '수배전반 2호', '전기설비', 'B1 전기실', 'PN-2023', 'LS일렉트릭', '2019-11-20', '점검중', 45000000, '하자보수 기간 만료됨'),
('FIRE-005', 'R형 수신기', '소방설비', '1F 방재실', 'FP-500', '동방전자', '2021-03-10', '정상', 12000000, '연 2회 정밀점검 필수'),
('LIFT-001', '승객용 엘리베이터 1호', '승강기', '로비', 'WB-Speed', '현대엘리베이터', '2018-08-01', '정상', 85000000, 'FMS 유지보수 계약 체결'),
('SEC-CAM-22', '지하주차장 CCTV', '보안설비', 'B3 주차장', 'Vision-X', '한화테크윈', '2022-01-15', '고장', 500000, '렌탈 계약 상품'),
('PUMP-003', '급수 펌프 3호', '기계설비', 'B2 펌프실', 'Grundfos-X', '그런포스', '2020-06-20', '정상', 3500000, '소모품 교체 주기: 1년');